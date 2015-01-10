/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.selectionalgorithm;

import java.io.File;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.saaas.server.GeoLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author palinka
 */
public class mapfromXmltrack {

    public int max_sensing;
    GeoLocation pivot;
    NavigableMap<Double, Point> nav_map;

    public mapfromXmltrack(mapfromXmltrack map) {
        this.max_sensing = map.max_sensing;
        this.nav_map = new TreeMap<Double, Point>();
    }

    //two main artiries as Sensing Points
    public mapfromXmltrack(int max_sense) {
        max_sensing = max_sense;

        this.nav_map = new TreeMap<Double, Point>();
       // readXml("C:\\Users\\palinka\\Downloads\\marousi-peireas.gpx");
       // readXml("C:\\Users\\palinka\\Downloads\\xaidari-alimos.gpx");
        readXml("C:\\Users\\palinka\\Downloads\\route.gpx");
    }

    private void readXml(String location) {
        try {
            File file = new File(location);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("rtept");
            System.out.println("gpx file");
            int s=0;
            while( s < nodeLst.getLength()) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstNode;
                    GeoLocation my = GeoLocation.fromDegrees(Double.parseDouble(fstElmnt.getAttribute("lat")), Double.parseDouble(fstElmnt.getAttribute("lon")));
                    if (s == 0) {
                        pivot = my;
                    }
                    Point point = new Point(0, my);

                    nav_map.put(pivot.distanceTo(point.myLoc), point);
                }
                s=s+2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
