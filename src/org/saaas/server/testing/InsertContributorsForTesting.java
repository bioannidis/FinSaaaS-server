/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.testing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.saaas.server.Datastore;
import org.saaas.server.selectionalgorithm.GeoLocation;
import org.saaas.server.selectionalgorithm.DBCalls;
import org.saaas.server.selectionalgorithm.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author palinka
 */
public class InsertContributorsForTesting {
 
    public InsertContributorsForTesting(){
  
    }
    
    
    public static  void main(String[] args) {
        InsertContributorsForTesting test=new InsertContributorsForTesting();
       test.insertContributors(100);
    }
    public static void  insertContributors(int numberOfContributors) {
    Random rand=new Random();
    int limit=numberOfContributors;
    while(numberOfContributors>0){
        long id=rand.nextLong();
        String dummy="ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss";
        GeoLocation my=readXml("C:\\Users\\palinka\\Downloads\\route.gpx",rand.nextInt(limit));
        if(my==null){
            my=readXml("C:\\Users\\palinka\\Downloads\\route.gpx",numberOfContributors-rand.nextInt(numberOfContributors));
            System.out.println("null value");
            continue;
        }
        testRegister(""+id+dummy,"TestIp",99,0.1,my.getLongitudeInDegrees(),my.getLatitudeInDegrees(), "TestUser");
        System.out.println(numberOfContributors);
        numberOfContributors--;
    }
    
    }
    public static void deactivateContributor(String regId ){
        Datastore.map.put("regID", new Timestamp(0));
    }
      static boolean testRegister( final String regId, final String ipAddress,float batterypct,double cpu_usage,double longitude,double latitude,String userName) {
    	
    	
        String serverUrl = "http://localhost:8080/saaas-server/" + "/register";
        Map<String, String> params = new HashMap<String, String>();
        Date date= new Date();
        Timestamp time = new Timestamp(date.getTime()+3000000);
	    
        params.put("regId", regId);
        params.put("type", String.valueOf(1)); // if Contributor
        //params.put("type", String.valueOf(2)); // if Develloper
        params.put("ip", ipAddress);
        params.put("user",userName);
        params.put("timestamp", String.valueOf(time));
        float local_cost=1;
        if(batterypct==0)
        	local_cost=100000000;
        else 
        	local_cost=local_cost*1/((float)batterypct/100);
        local_cost=(float) (local_cost*1/(1-cpu_usage));
        params.put("local_cost", Float.toString(local_cost));
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        params.put("latitude", Double.toString(latitude));
        params.put("longitude", Double.toString(longitude));
        
        try {
            post(serverUrl, params);
        } catch (IOException ex) {
            Logger.getLogger(InsertContributorsForTesting.class.getName()).log(Level.SEVERE, null, ex);
        }
       
            
        return true;
       
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
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
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
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
      }
    private static GeoLocation readXml(String location,int position) {
       GeoLocation my=null;
        try {
            File file = new File(location);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            
            NodeList nodeLst = doc.getElementsByTagName("rtept");
            
            

                Node fstNode = nodeLst.item(position);

                if ((fstNode!=null)&&(fstNode.getNodeType() == Node.ELEMENT_NODE) ) {

                    Element fstElmnt = (Element) fstNode;
                    my = GeoLocation.fromDegrees(Double.parseDouble(fstElmnt.getAttribute("lat")), Double.parseDouble(fstElmnt.getAttribute("lon")));
                    
                }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return(my);

    }
}
