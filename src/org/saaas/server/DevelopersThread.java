package org.saaas.server;

import org.saaas.server.dataobjects.Contributor;
import org.saaas.server.dataobjects.Registrant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class DevelopersThread implements Runnable{

	private final Logger logger = Logger.getLogger(getClass().getName());
	private Sender sender;
	private static final int MULTICAST_SIZE = 1000;
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	Thread t;
        private Datastore datastore;
        
	
	public DevelopersThread(Sender sender) {
		super();
		this.sender = sender;
		t = new Thread(this);
		t.start();
                datastore=Datastore.getInstance();
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
	         while (true) {
	        	 try {
		        	//logger.info("Thread Developers Running");
		        	List<Registrant> devices = datastore.getDevelopers();
		        	//logger.info("Thread nb Developers :" + String.valueOf(devices.size()));
		    	    String status;
		    	    if (devices.isEmpty()) {
		    	      status = "Message ignored as there is no device registered!";
		    	    } else {
		    	      // NOTE: check below is for demonstration purposes; a real application
		    	      // could always send a multicast, even for just one recipient
		    	      if (devices.size() == 1) {
		    	        // send a single message using plain post
		    	        String registrationId = devices.get(0).getRegId();
		    	        List<Contributor> contributors = datastore.getContributors();
		    	        List<Contributor> subscriptors = datastore.getSubscriptors();
		    	       // logger.info("nb Contributors :" + String.valueOf(contributors.size()));
		    	        Message message = new Message.Builder().addData("nbContributors", String.valueOf(contributors.size()))
		    	        		.addData("nbSubscriptors", String.valueOf(subscriptors.size())).build();
		    	        
		    	        Result result;
						result = sender.send(message, registrationId, 5);
						if(result.getErrorCodeName() != null)
						{
				        	  logger.info("1=====> "+ result.getErrorCodeName());
				        	  if (result.getErrorCodeName().equals(Constants.ERROR_NOT_REGISTERED)) {
					              // application has been removed from device - unregister it
					              logger.info("Unregistered device: " + registrationId);
					              datastore.unregister(registrationId);
				        	  }
						}
		    	        status = "Sent message to one device: " + result;
		    	      } else {
		    	        // send a multicast message using JSON
		    	        // must split in chunks of 1000 devices (GCM limit)
		    	        int total = devices.size();
		    	        List<String> partialDevices = new ArrayList<String>(total);
		    	        int counter = 0;
		    	        int tasks = 0;
		    	        for (Registrant device : devices) {
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
	    	    } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	            Thread.sleep(5000);
	         }
	      } catch (InterruptedException e) {
	    	  logger.info("Thread Developers Stop");
	      }
	}
		
	private void asyncSend(List<String> partialDevices) {
	    // make a copy
	    final List<String> devices = new ArrayList<String>(partialDevices);
	    threadPool.execute(new Runnable() {

	      public void run() {
	    	List<Contributor> contributors = datastore.getContributors();
  	        List<Contributor> subscriptors = datastore.getSubscriptors();
  	        logger.info("nb Contributors :" + String.valueOf(contributors.size()));
  	        Message message = new Message.Builder().addData("nbContributors", String.valueOf(contributors.size()))
  	        		.addData("nbSubscriptors", String.valueOf(subscriptors.size())).build();
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
	          if(result.getErrorCodeName() != null)
	        	  logger.info("2=====> "+ result.getErrorCodeName());
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
	
	public void interrupt()
	{
		t.interrupt();
	}

}
