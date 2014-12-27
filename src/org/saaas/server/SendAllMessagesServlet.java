/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.saaas.server;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that adds a new message to all registered devices.
 * <p>
 * This servlet is used just by the browser (i.e., not device).
 */
@SuppressWarnings("serial")
public class SendAllMessagesServlet extends BaseServlet {

  private static final int MULTICAST_SIZE = 1000;

  private Sender sender;

  private static final Executor threadPool = Executors.newFixedThreadPool(5);
  Datastore datastore;
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    datastore=new Datastore();
    sender = newSender(config);
  }

  /**
   * Creates the {@link Sender} based on the servlet settings.
     * @param config
     * @return 
   */
  protected Sender newSender(ServletConfig config) {
    String key = (String) config.getServletContext()
        .getAttribute(ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
    return new Sender(key);
  }

  /**
   * Processes the request to add a new message.
     * @param req
     * @param resp
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    List<Contributor> devices = datastore.getContributors();
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
    		List<Contributor> partialDevices = new ArrayList<Contributor>(total);
    		int counter = 0;
    		int tasks = 0;
    		for (Contributor device : devices) {
    			counter++;
    			partialDevices.add(device);
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

  private void asyncSend(List<Contributor> partialDevices) {
      
        final List<String> devices = new ArrayList<String>();
        Iterator<Contributor> pdev_it = partialDevices.iterator();
        while(pdev_it.hasNext())
        {
            Contributor current = pdev_it.next();
            devices.add(current.getRegId());
        }
    // final List<String> devices = new ArrayList<String>(partialDevices);
    threadPool.execute(new Runnable() {

      @Override
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
            logger.log(Level.FINE, "Succesfully sent message to device: {0}; messageId = {1}", new Object[]{regId, messageId});
            String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {
              // same device has more than on registration id: update it
              logger.log(Level.INFO, "canonicalRegId {0}", canonicalRegId);
              datastore.updateRegistration(regId, canonicalRegId);
            }
          } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
              // application has been removed from device - unregister it
              logger.log(Level.INFO, "Unregistered device: {0}", regId);
              datastore.unregister(regId);
            } else {
              logger.log(Level.SEVERE, "Error sending message to {0}: {1}", new Object[]{regId, error});
            }
          }
        }
      }});
  }

}
