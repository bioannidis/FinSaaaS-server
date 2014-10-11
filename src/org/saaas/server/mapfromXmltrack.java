/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author palinka
 */
public class mapfromXmltrack {
    HashMap <GeoLocation, Integer> my_map;
    public int max_sensing;
    public mapfromXmltrack(mapfromXmltrack map){
           this.my_map=(HashMap<GeoLocation, Integer>) map.my_map.clone();
           this.max_sensing=map.max_sensing;
       }
    //two main artiries as Sensing Points
    public mapfromXmltrack(int max_sense){
        max_sensing=max_sense;
        my_map=new HashMap<GeoLocation,Integer>();
        readXml("C:\\Users\\palinka\\Downloads\\marousi-peireas.gpx");
        readXml("C:\\Users\\palinka\\Downloads\\xaidari-alimos.gpx");
    }
    private void readXml(String location){
        try {
  File file = new File(location);
  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  DocumentBuilder db = dbf.newDocumentBuilder();
  Document doc = db.parse(file);
  doc.getDocumentElement().normalize();
  System.out.println("Root element " + doc.getDocumentElement().getNodeName());
  NodeList nodeLst = doc.getElementsByTagName("trkpt");
  System.out.println("gpx file");

  for (int s = 0; s < nodeLst.getLength(); s++) {

    Node fstNode = nodeLst.item(s);
   
    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
  
      Element fstElmnt = (Element) fstNode;
      GeoLocation my=GeoLocation.fromDegrees(Double.parseDouble(fstElmnt.getAttribute("lat")),Double.parseDouble(fstElmnt.getAttribute("lon")));
      my_map.put(my,0);
    }
  }
  } catch (Exception e) {
    e.printStackTrace();
  }
        
    }
}
