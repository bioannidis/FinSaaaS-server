package org.saaas.server;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContributorsThread implements Runnable{

	private final Logger logger = Logger.getLogger(getClass().getName());
	private final Sender sender;
	private static final int MULTICAST_SIZE = 1000;
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	Thread t;
        private Datastore datastore;
	
	public ContributorsThread(Sender sender) {
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
////	        	 try {
		        	logger.info("Thread Contributors Running");
		        	List<Contributor> devices = datastore.getContributors();
		        	logger.log(Level.INFO, "Thread nb Contributors :{0}", String.valueOf(devices.size()));
		    	    String status;
		    	    if (devices.isEmpty()) {
		    	      status = "Message ignored as there is no device registered!";
		    	    } else {
		    	      // NOTE: check below is for demonstration purposes; a real application
		    	      // could always send a multicast, even for just one recipient
		    	      if (devices.size() == 1) {
		    	        // send a single message using plain post
////		    	        String registrationId = devices.get(0).getRegId();
////		    	        Message message = new Message.Builder().timeToLive(60).build();
////		    	        Result result;
////						result = sender.send(message, registrationId, 5);
////						if(result.getErrorCodeName() != null)
////						{
////				        	  logger.log(Level.INFO, "2=====> {0}", result.getErrorCodeName());
////				        	  if (result.getErrorCodeName().equals(Constants.ERROR_NOT_REGISTERED)) {
////					              // application has been removed from device - unregister it
////					              logger.log(Level.INFO, "Unregistered device: {0}", registrationId);
////					              datastore.unregister(registrationId);
////				        	  }
////						}
////		    	        status = "Sent message to one device: " + result;
////                                if(message != null)
////                                {
////                                    if(message.getTimeToLive()<10)
////                                    {
////                                        Contributor unreachable = datastore.getContributor(registrationId);
////                                        unreachable.setReachability(false);
////                                        unreachable.unsetTimer();                                                                        
////                                    }
////                                }
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
////		    	            asyncSend(partialDevices);
		    	            partialDevices.clear();
		    	            tasks++;
		    	          }
		    	        }
		    	        status = "Asynchronously sending " + tasks + " multicast messages to " +
		    	            total + " devices";
		    	      }
		    	    }
////	    	    } catch (IOException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
	        	
	            Thread.sleep(120000);
	         }
	      } catch (InterruptedException e) {
	    	  logger.info("Thread Contributors Stop");
	      }
	}
		
	private void asyncSend(List<String> partialDevices) {
	    // make a copy
	    final List<String> devices = new ArrayList<String>(partialDevices);
	    threadPool.execute(new Runnable() {

              @Override
	      public void run() {
	        Message message = new Message.Builder().timeToLive(60).build();
	        MulticastResult multicastResult;
	        try {
	          multicastResult = sender.send(message, devices, 5);
	        } catch (IOException e) {
	          logger.log(Level.SEVERE, "Error posting messages", e);
	          return;
	        }
	        List<Result> results = multicastResult.getResults();
	        // analyze the results
                final Map<String,Message> registered = new HashMap<String, Message>();
	        for (int i = 0; i < devices.size(); i++) {
	          String regId = devices.get(i);
	          Result result = results.get(i);
	          if(result.getErrorCodeName() != null)
	        	  logger.log(Level.INFO, "2=====> {0}", result.getErrorCodeName());
	          String messageId = result.getMessageId();
	          if (messageId != null) {
	            logger.log(Level.FINE, "Succesfully sent message to device: {0}; messageId = {1}", new Object[]{regId, messageId});
	            String canonicalRegId = result.getCanonicalRegistrationId();
                    registered.put(regId, message);
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
                Iterator<Map.Entry<String,Message>> reg_it = registered.entrySet().iterator();
                while(reg_it.hasNext())
                {
                    final Map.Entry<String,Message> current = reg_it.next();
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(current.getValue() != null)
                            {
                                if(current.getValue().getTimeToLive()<10)
                                {
                                    Contributor unreachable = datastore.getContributor(current.getKey());
                                    unreachable.setReachability(false);
                                    unreachable.unsetTimer();
                                }
                            }
                      }
                  });
                }
            }});
	  }
	
	public void interrupt()
	{
		t.interrupt();
	}
	
	

}
