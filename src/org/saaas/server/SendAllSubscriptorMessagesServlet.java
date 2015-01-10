package org.saaas.server;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SendAllSubscriptorMessagesServlet extends BaseServlet{

	private static final int MULTICAST_SIZE = 1000;

	  private Sender sender;

	  private static final Executor threadPool = Executors.newFixedThreadPool(5);
          Datastore datastore;
	  @Override
	  public void init(ServletConfig config) throws ServletException {
	    super.init(config);
            datastore=Datastore.getInstance();
	    sender = newSender(config);
	  }

	  /**
	   * Creates the {@link Sender} based on the servlet settings.
	   */
	  protected Sender newSender(ServletConfig config) {
	    String key = (String) config.getServletContext()
	        .getAttribute(ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
	    return new Sender(key);
	  }

	  /**
	   * Processes the request to add a new message.
	   */
	  @Override
	  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	      throws IOException, ServletException {
              try {
            if(datastore.getConnection().isClosed())
                datastore.setConnection();
            
        } catch (SQLException ex) {
            Logger.getLogger(RegisterServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
	    List<Contributor> devices = datastore.getSubscriptors();
	    String status;
	    if (devices.isEmpty()) {
	      status = "Message ignored as there is no device registered!";
	    } else {
	      // NOTE: check below is for demonstration purposes; a real application
	      // could always send a multicast, even for just one recipient
	      if (devices.size() == 1) {
	        // send a single message using plain post
	        String registrationId = devices.get(0).getRegId();
	        Message message = new Message.Builder().build();
	        Result result = sender.send(message, registrationId, 5);
	        status = "Sent message to one device: " + result;
	      } else {
	        // send a multicast message using JSON
	        // must split in chunks of 1000 devices (GCM limit)
	        int total = devices.size();
	        List<String> partialDevices = new ArrayList<String>(total);
	        int counter = 0;
	        int tasks = 0;
	        for (Contributor device : devices) {
	          counter++;
	          partialDevices.add(device.getRegId());
	          int partialSize = partialDevices.size();
	          if (partialSize == MULTICAST_SIZE || counter == total) {
	            asyncSend(partialDevices);
	            partialDevices.clear();
	            tasks++;
	          }
	        }
	        status = "Asynchronously sending " + tasks + " multicast messages to " +
	            total + " devices";
	      }
	    }
	    req.setAttribute(HomeServlet.ATTRIBUTE_STATUS, status.toString());
	    getServletContext().getRequestDispatcher("/home").forward(req, resp);
	  }

	  private void asyncSend(List<String> partialDevices) {
	    // make a copy
	    final List<String> devices = new ArrayList<String>(partialDevices);
	    threadPool.execute(new Runnable() {

	      public void run() {
	        Message message = new Message.Builder().build();
	        MulticastResult multicastResult;
	        try {
	          multicastResult = sender.send(message, devices, 5);
	        } catch (IOException e) {
	          logger.log(Level.SEVERE, "Error posting messages", e);
	          return;
	        }
	        List<Result> results = multicastResult.getResults();
	        // analyze the results
	        for (int i = 0; i < devices.size(); i++) {
	          String regId = devices.get(i);
	          Result result = results.get(i);
	          String messageId = result.getMessageId();
	          if (messageId != null) {
	            logger.fine("Succesfully sent message to device: " + regId +
	                "; messageId = " + messageId);
	            String canonicalRegId = result.getCanonicalRegistrationId();
	            if (canonicalRegId != null) {
	              // same device has more than on registration id: update it
	              logger.info("canonicalRegId " + canonicalRegId);
	              datastore.updateRegistration(regId, canonicalRegId);
	            }
	          } else {
	            String error = result.getErrorCodeName();
	            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
	              // application has been removed from device - unregister it
	              logger.info("Unregistered device: " + regId);
	              datastore.unregister(regId);
	            } else {
	              logger.severe("Error sending message to " + regId + ": " + error);
	            }
	          }
	        }
	      }});
	  }
}
