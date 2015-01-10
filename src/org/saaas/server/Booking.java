/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author gmerlino
 */
public class Booking {
    private String bookingId;
    private List<Contributor> booked;
    private int requestSize;
    private int actualSize;
    private String appName;
    private static final String PATH = "/api.key";
    private Sender sender;
    private String pushEndpoint;
    private static final long delay = 10000L;
    private Timer crono;
    private TimerTask churnm;
    private final Logger logger = Logger.getLogger(getClass().getName());
    final private Runnable replaceUnreachableTask = new Runnable() {
            @Override
            public void run() {
                updateResources();
            }
        };

  /**
   * Gets the access key.
     * @return 
   */
  protected String getKey() {
    InputStream stream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(PATH);
    if (stream == null) {
      throw new IllegalStateException("Could not find file " + PATH +
          " on web resources)");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    try {
      String key = reader.readLine();
      return key;
    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + PATH, e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        logger.log(Level.WARNING, "Exception closing " + PATH, e);
      }
    }
  }
    
    public String getBookingId() {
            return this.bookingId;
    }
    public void setBookingId(String bId) {
            this.bookingId = bId;
    }
    public List<Contributor> getResources() {
            return this.booked;
    }
    public void setResources(List<Contributor> brlist) {
            this.booked = brlist;
            this.requestSize = brlist.size();
            this.actualSize = this.requestSize;
    }
    public void freeResource(Contributor addition) {
        try {
            this.booked.remove(addition);
            this.actualSize--;
            addition.setBookingId(null);
            addition.setAvailability(true);
        } catch (Exception e) {}
    }
    public void addResource(Contributor addition) {
        try {
            addition.setAvailability(false);
            addition.setBookingId(this.bookingId);
            this.booked.add(addition);
            this.actualSize++;
        } catch (Exception e) {
            addition.setBookingId(null);
            addition.setAvailability(true);
        }
    } 
    public int getSize() {
            return this.requestSize;
    }
    public String getAppName() {
            return appName;
    }
    public void setAppName(String aName) {
            appName = aName;
    }
    public String getPushEndpoint() {
        return this.pushEndpoint;
    }
    public void setPushEndpoint(String pEP) {
        this.pushEndpoint = pEP;
    }
    public String sendPush(JSONObject upd, String EP) {
        JSONObject built_json = upd;
        String result = sendJsonPOST(built_json.toString(), EP);
        return result;
    }

    /**
     *
     * @param urlParameters
     * @param targetURL
     * @return
     */
    public static String sendJsonPOST(String urlParameters, String targetURL)
      {
        URL url;
        HttpURLConnection connection = null;  
        try {
          //Create connection
          url = new URL(targetURL);
          connection = (HttpURLConnection)url.openConnection();
          connection.setRequestMethod("POST");
          connection.setRequestProperty("Content-Type", 
               "application/json");

          connection.setRequestProperty("Content-Length", "" + 
                   Integer.toString(urlParameters.getBytes().length));
          connection.setRequestProperty("Content-Language", "en-US");  

          connection.setUseCaches (false);
          connection.setDoInput(true);
          connection.setDoOutput(true);

          //Send request
          DataOutputStream wr = new DataOutputStream (
                      connection.getOutputStream ());
          wr.writeBytes(urlParameters);
          wr.flush ();
          wr.close ();

          //Get Response	
          InputStream is = connection.getInputStream();
          BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          String line;
          StringBuilder response = new StringBuilder(); 
          while((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
          }
          rd.close();
          return response.toString();

        } catch (IOException e) {
          return null;

        } finally {

          if(connection != null) {
            connection.disconnect(); 
          }
        }
  }
    public JSONObject buildPush(List<Contributor> upd_contrib, String upd_type) {
        JSONObject json_obj = new JSONObject();
        Iterator<Contributor> upd_it = upd_contrib.iterator();
        int count = 0;
        while(upd_it.hasNext()){
            Contributor upd_curr = upd_it.next();
            json_obj.put(count, upd_curr.getRegId());
            count++;
        }
        json_obj.put("booking ID", this.bookingId.toString());
        return json_obj;
    }           
    public void pruneResources() {
        List<Contributor> reach = new ArrayList<Contributor>(this.booked);
        List<Contributor> prune_tmp = new ArrayList<Contributor>();
        Iterator<Contributor> prune_it = reach.iterator();
        while(prune_it.hasNext())
        {
            Contributor current_pruning = prune_it.next();
            if(!current_pruning.getReachability()){
                prune_tmp.add(current_pruning);
                logger.log(Level.INFO, "pruned {0} from booking ID {1}", new Object[]{current_pruning.getRegId().toString(), this.bookingId});
            }
        }
        this.booked.removeAll(prune_tmp);
        this.actualSize = this.booked.size();
        Iterator<Contributor> free_it = prune_tmp.iterator();
        while(free_it.hasNext())
        {
            Contributor current_freeing = free_it.next();
            current_freeing.setBookingId(null);
            current_freeing.setAvailability(true);
        }
        if(!prune_tmp.isEmpty()){
            logger.log(Level.INFO, "number of pruned contributors: {0}", new Integer(this.requestSize - this.actualSize).toString());
            sendPush(buildPush(prune_tmp, "pruning"), this.pushEndpoint);
        }
    }
    public void refillResources() {
        int toBeCollected = this.requestSize - this.actualSize;
        if(toBeCollected > 0){
            logger.log(Level.INFO, "booking ID: {0}, number of contributors to be collected: {1}", new Object[]{this.bookingId, toBeCollected});
            int count = 0;
            Datastore datastore=Datastore.getInstance();
            List<Contributor> avail = datastore.getAvailableContributors();
            List<Contributor> refill_tmp = new ArrayList<Contributor>();
            Iterator<Contributor> refill_it = avail.iterator();
            while(count < toBeCollected){
                if(refill_it.hasNext()){
                    Contributor current_refilling = refill_it.next();
                    if(current_refilling.getReachability()){
                        addResource(current_refilling);
                        refill_tmp.add(current_refilling);
                        logger.log(Level.INFO, "refilled booking ID {0} with {1}", new Object[]{this.bookingId, current_refilling.getRegId().toString()});
                        count++;
                    }
                } else {
                    logger.log(Level.INFO, "should still refill booking ID {0}, no more avail unfortunately", this.bookingId);
                    break;
                }
            }
            if(count != 0){
                logger.log(Level.INFO, "number of refilled contributors: {0}", new Integer(count).toString());
                List<String> selected_strings = new ArrayList<String>();
                Iterator<Contributor> select_it = refill_tmp.iterator();
                while (select_it.hasNext()) {
                    Contributor curr = select_it.next();
                    selected_strings.add(curr.getRegId());
                    logger.log(Level.INFO, "CURRENT REFILL CANDIDATE {0}", curr.getRegId());
                }
                logger.log(Level.INFO, "APPNAME {0}", this.appName);
                logger.log(Level.INFO, "CURRENT REFILL CANDIDATES {0}", selected_strings);
                sendAPK(selected_strings);
                logger.log(Level.INFO, "AFTER SENDAPK");
                sendPush(buildPush(refill_tmp, "refilling"), this.pushEndpoint);
            }
        }
    }
    private void sendAPK(List<String> selectedId) {
        Message message;
        message = new Message.Builder().addData("title", appName).build();
        try {
            String key = getKey();
            logger.log(Level.INFO, "KEY: {0}", key);
            sender = new Sender(key);
            MulticastResult multiResult = sender.send(message, selectedId, 10);
            List<Result> results = multiResult.getResults();
            for (int j = 0; j < results.size(); j++) {
                Result result = results.get(j);

                if (result.getMessageId() != null) {
                    // Success
                    String canonicalRegId = result.getCanonicalRegistrationId();
                    if (canonicalRegId != null) {
                        // same device has more than one registration id.Update it
                    }
                } else {
                    // Error occurred
                    String error = result.getErrorCodeName();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.info("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void updateResources() {
        this.pruneResources();
        this.refillResources(); 
    }
    public void setTimer() {
        this.crono = new Timer("churn management timer for booking ID: " + this.bookingId, false);
        this.churnm = new ChurnMgmtTask(this.replaceUnreachableTask);
        crono.scheduleAtFixedRate(churnm, delay, delay);
        logger.log(Level.INFO, "churn timer set for booking ID: {0}", this.bookingId);
    }
}
