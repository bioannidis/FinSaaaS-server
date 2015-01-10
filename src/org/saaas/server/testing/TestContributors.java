/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.testing;

import java.io.File;
import java.sql.Timestamp;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.saaas.server.Datastore;
import org.saaas.server.GeoLocation;
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
public class TestContributors {
    Datastore datastore;
    DBCalls dbCalls;
    public TestContributors(){
      datastore =Datastore.getInstance();
     dbCalls =DBCalls.getInstance();   
    }
    
    
    public static  void main(String[] args) {
        TestContributors test=new TestContributors();
       test.insertContributors(10);
    }
    public static void  insertContributors(int numberOfContributors) {
    Random rand=new Random();
    int limit=numberOfContributors;
    while(numberOfContributors>0){
        long id=rand.nextLong();
        Datastore.register(""+id, "1", "TestIp",new Timestamp(System.currentTimeMillis()+10000000), "TestUser");
        GeoLocation my=readXml("C:\\Users\\palinka\\Downloads\\route.gpx",rand.nextInt(limit));
        if(my==null){
            my=readXml("C:\\Users\\palinka\\Downloads\\route.gpx",numberOfContributors-rand.nextInt(numberOfContributors));
            System.out.println("null value");
            continue;
        }
        DBCalls.new_user(""+id, rand.nextFloat()*3,my.getLatitudeInDegrees(), my.getLongitudeInDegrees());
        System.out.println(numberOfContributors);
        numberOfContributors--;
    }
    
    }
    public static void deactivateContributor(String regId ){
        Datastore.map.put("regID", new Timestamp(0));
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
