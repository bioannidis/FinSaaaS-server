/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.selectionalgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.saaas.server.GeoLocation;

/**
 *
 * @author basilhs
 */
public class ValueEstimator {

    public static int MaxNrofSensing = 10, point_at_each_dimension = 40, nrPointsGen;
    public static double latGen, lonGen, centralLat = 51.0, centralLon = 43.0, distanceOfPointsInMeters = 8, sensing_radius = 0.8;//for testing tweek
    public static float maxDistGen;

    /**
     * @param args the command line arguments
     */
    //takes a central point a max distance and a number of point and returns a random list o points
    public static List<GeoLocation> pointGenerator(double lat, double lon, float my_distance, int numberOfPoints) {
        Random random = new Random();

        List<GeoLocation> geos = new ArrayList<GeoLocation>();

        double dn, de, dLat, dLon, latO, lonO;
        double R = 6378137;

        for (int j = 0; j < numberOfPoints; j++) {
            dn = my_distance * random.nextDouble();;
            de = my_distance * random.nextDouble();;
            dLat = dn / R;
            dLon = de / (R * Math.cos(Math.PI * lat / 180));

            //OffsetPosition, decimal degrees
            latO = lat + dLat * 180 / Math.PI;
            lonO = lon + dLon * 180 / Math.PI;
            GeoLocation geo = GeoLocation.fromDegrees(latO, lonO);
            geos.add(geo);
        }

        return (geos);
    }
    /*
     public static myMap Mapcreator(){
     myMap map=new myMap(distanceOfPointsInMeters,point_at_each_dimension,MaxNrofSensing,centralLat,centralLon);
     return(map);
     }*/

    public static void addtoMap(CostProfile cos, mapfromXmltrack map, double near_area) {
        GeoLocation geo = GeoConverter(cos);
        Point loc;
        SortedMap.Entry pair, minPair = null;

        double distance = geo.distanceTo(map.pivot);
        //return the points within the sensing radious
        double minDist = 10000000, currDist;
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - near_area, distance + near_area);
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            // System.out.println("distance  "+geo.distanceTo(loc.myLoc));
            currDist = (geo.distanceTo(loc.myLoc));
            if ((currDist < minDist) && ((loc.timesSensed) < map.max_sensing)) {
                minDist = currDist;
                minPair = pair;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
        if ((minDist <= near_area) && (!(minPair == null))) {
            ((Point) minPair.getValue()).timesSensed++;
        }

    }

    public static void addtoMap(CostProfile cos, mapfromXmltrack map) {
        GeoLocation geo = GeoConverter(cos);
        Point loc;
        SortedMap.Entry pair;

        double distance = geo.distanceTo(map.pivot);
        //return the points within the sensing radious
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - sensing_radius, distance + sensing_radius);
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            //System.out.println(geo.distanceTo(loc));
            if ((geo.distanceTo(loc.myLoc) < sensing_radius) && ((loc.timesSensed) < map.max_sensing)) {
                loc.timesSensed++;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
    }

    public static void extractFromMap(CostProfile cos, mapfromXmltrack map, double near_area) {
        GeoLocation geo = GeoConverter(cos);
        Point loc;
        SortedMap.Entry pair, minPair = null;

        double distance = geo.distanceTo(map.pivot);
        //return the points within the sensing radious
        double minDist = 10000000, currDist;
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - near_area, distance + near_area);
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            // System.out.println("distance  "+geo.distanceTo(loc.myLoc));
            currDist = (geo.distanceTo(loc.myLoc));
            if ((currDist < minDist) && ((loc.timesSensed) <= map.max_sensing)) {
                minDist = currDist;
                minPair = pair;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
        if ((minDist <= near_area) && (!(minPair == null))) {
            ((Point) minPair.getValue()).timesSensed--;
        }

    }

    public static void extractFromMap(CostProfile cos, mapfromXmltrack map) {
        GeoLocation geo = GeoConverter(cos);
        Point loc;
        SortedMap.Entry pair;

        double distance = geo.distanceTo(map.pivot);
        //return the points within the sensing radious
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - sensing_radius, distance + sensing_radius);
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            //System.out.println(geo.distanceTo(loc));
            if ((geo.distanceTo(loc.myLoc) < sensing_radius) && ((loc.timesSensed) <= map.max_sensing)) {
                loc.timesSensed--;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
    }

    //TODO overload values est add parameter for point or area cover.
    //near area should be the semi diffrence of the near point sof interest
    public static float Value_est(CostProfile costprofus, mapfromXmltrack map, double near_area) {
        float value = 0;
        GeoLocation geo = GeoConverter(costprofus);
        Point loc;
        SortedMap.Entry pair;
        //Iterator ite=map.nav_map.entrySet().iterator();
        //System.out.println("distanceFromPivot---------------------------");
        //System.out.println("geo "+geo.distanceTo(map.pivot));
        //while(ite.hasNext()){
        //   System.out.println(((SortedMap.Entry )ite.next()).getKey());
        //}
        double distance = geo.distanceTo(map.pivot);
       // System.out.println(map.pivot);
        //System.out.println(geo);
        //System.out.println(distance);
        //return the points within the sensing radious
        double minDist = 10000000, currDist;
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - near_area, distance + near_area);
        //System.out.println("size of map"+sub_map.size());
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
          //  System.out.println("distance  "+geo.distanceTo(loc.myLoc));
            currDist = (geo.distanceTo(loc.myLoc));
            if ((currDist < minDist) && ((loc.timesSensed) < map.max_sensing)) {
                minDist = currDist;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
        if (minDist <= near_area) {
            value = 1;
        } else {
            value = 0;
        }
        return (value);
    }

    public static float Value_est(CostProfile costprofus, mapfromXmltrack map) {
        float value = 0;
        GeoLocation geo = GeoConverter(costprofus);
        Point loc;
        SortedMap.Entry pair;
        //Iterator ite=map.nav_map.entrySet().iterator();
        //System.out.println("distanceFromPivot---------------------------");
        //System.out.println("geo "+geo.distanceTo(map.pivot));
        //while(ite.hasNext()){
        //   System.out.println(((SortedMap.Entry )ite.next()).getKey());
        //}
        double distance = geo.distanceTo(map.pivot);
        //return the points within the sensing radious
        SortedMap<Double, Point> sub_map = map.nav_map.subMap(distance - sensing_radius, distance + sensing_radius);
        Iterator it = sub_map.entrySet().iterator();
        while (it.hasNext()) {
            pair = (SortedMap.Entry) it.next();
            loc = (Point) pair.getValue();
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            // System.out.println("distance  "+geo.distanceTo(loc.myLoc));
            if ((geo.distanceTo(loc.myLoc) < sensing_radius) && ((loc.timesSensed) < map.max_sensing)) {
                value++;
                //   System.out.println(geo.distanceTo(loc));
            }
        }
        return (value);
    }
    /*float value=0;
     GeoLocation geo=GeoConverter(costprofus);
     GeoLocation loc;
     HashMap.Entry pair;
     Iterator it=map.my_map.entrySet().iterator();
     while (it.hasNext()){
     pair=(HashMap.Entry)it.next();
     loc=(GeoLocation) pair.getKey();
     if ((geo.distanceTo(loc)<sensing_radius) && (map.my_map.get(loc)<map.max_sensing)){
     value++;
     }
     else{
     System.out.println("Geopoints distance "+geo.distanceTo(loc));
     // System.out.println(map.my_map.get(loc));
     }
     }
     return value;
     */

    /*
     public static float Value_est(CostProfile costprofus, List <CostProfile> To,mapfromXmltrack map){
     float value=0;
     GeoLocation newgeo=GeoConverter(costprofus);
     List<GeoLocation> geo=GeoListConverter(To);
     Iterator<GeoLocation> itr1 = geo.iterator();
     GeoLocation geos;
     //myMap helper=new myMap(map);
     myMap helper=new myMap(distanceOfPointsInMeters,point_at_each_dimension,MaxNrofSensing,centralLat,centralLon);
        
     while (itr1.hasNext()){
     geos=itr1.next();
     for (int i=0;i<point_at_each_dimension;i++)
     for (int j=0;j<point_at_each_dimension;j++){
     //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
     //System.out.println("start : "+geos.distanceTo(start));
     if ((geos.distanceTo(helper.my_map[i][j].myLoc)<sensing_radius) && (helper.my_map[i][j].timesSensed<helper.max_sensing))
     helper.my_map[i][j].timesSensed++;
     }
     }
        
     for (int i=0;i<point_at_each_dimension;i++)
     for (int j=0;j<point_at_each_dimension;j++){
     //System.out.println(newgeo.distanceTo(map.my_map[i][j].myLoc));
     //System.out.println("start : "+geos.distanceTo(start));
     if ((newgeo.distanceTo(helper.my_map[i][j].myLoc)<sensing_radius) && (helper.my_map[i][j].timesSensed<helper.max_sensing)){
     helper.my_map[i][j].timesSensed++;
     value++;
     }
     }
     return(value);
     }*/
    private static List<GeoLocation> GeoListConverter(List<CostProfile> To) {
        List<GeoLocation> geos = new ArrayList<GeoLocation>();
        Iterator<CostProfile> itr1 = To.iterator();
        CostProfile cos;
        while (itr1.hasNext()) {
            cos = itr1.next();
            GeoLocation geo = GeoLocation.fromDegrees(cos.lat, cos.lon);
            geos.add(geo);
        }
        return (geos);

    }

    private static GeoLocation GeoConverter(CostProfile costprofus) {
        return (GeoLocation.fromDegrees(costprofus.lat, costprofus.lon));
    }

    //2-d grid of coordinates map where each pointofinterest covers an area of a^2 meters^2
    /*
     public static void main(String[] args) throws CloneNotSupportedException {
     List<GeoLocation> geo=pointGenerator(51.0,43.0 , 100,50);
     myMap map=new myMap(50,10,10,centralLat,centralLon);
     int sensing_radius=10;
     Iterator<GeoLocation> itr1 = geo.iterator();
     GeoLocation geos,start;
     myMap helper= new myMap(map);
     start= GeoLocation.fromDegrees(51.0, 43.0);
     map=helper;
     while (itr1.hasNext()){
     geos=itr1.next();
     for (int i=0;i<10;i++)
     for (int j=0;j<10;j++){
     System.out.println(geos.distanceTo(map.my_map[i][j].myLoc));
     //System.out.println("start : "+geos.distanceTo(start));
     if ((geos.distanceTo(map.my_map[i][j].myLoc)<sensing_radius) && (map.my_map[i][j].timesSensed<map.max_sensing))
     map.my_map[i][j].timesSensed++;
     }
        
     for (int i=1;i<10;i++)
     for (int j=0;j<10;j++){
     // System.out.println(map.my_map[i][j].myLoc+" | "+ map.my_map[i][j].timesSensed );
     //   System.out.println(map.my_map[i][j].myLoc.distanceTo(map.my_map[i-1][j].myLoc));

     }
     }   
     }
     */
}
