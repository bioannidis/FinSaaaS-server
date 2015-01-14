package org.saaas.server.testing;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class TestAlgorithm {

    /**
     * @param args the command line arguments
     */
   public void Call_algorithm(String nbContributors,String budget,String evaluation_time,String algoType){
            // TODO code application logic here
            //DBCalls.zero_particepated();
            //AlgoChoise.select_winner_to_deploy_online(2,10,130);
            //mapfromXmltrack map=new mapfromXmltrack(10);
            //System.out.println(map.my_map.size());
            // call servlet
       try{
            byte buf[] = new byte[100];
            String b64str = new String(Base64.encodeBase64(buf, false, true));
            String serverUrl = "http://localhost:8080/saaas-server/" + "/getcontributors";
            Map<String, String> par = new HashMap<String, String>();
            par.put("nb_contributors", nbContributors);
            par.put("budget", budget);
            par.put("evaluation_time", evaluation_time);
            par.put("algorithm", algoType);
            
            par.put("application_content", b64str);
            par.put("application_name", "test_application1");
            post(serverUrl, par);
            //ValueEstimator.addtoMap(new CostProfile("",0,0,0,38.057509,23.808519),map);
        } catch (IOException ex) {
            Logger.getLogger(TestAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
}

    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
//        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            String s = conn.getResponseMessage();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status + s);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
