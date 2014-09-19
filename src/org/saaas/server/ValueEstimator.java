/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author basilhs
 */
public class ValueEstimator {
    public static int MaxNrofSensing=10,point_at_each_dimension=40,nrPointsGen;
    public static double latGen,lonGen,centralLat=51.0,centralLon=43.0,distanceOfPointsInMeters=8,sensing_radius=0.004;
    public static float maxDistGen;
    /**
     * @param args the command line arguments
     */
    //takes a central point a max distance and a number of point and returns a random list o points
    public static List<GeoLocation> pointGenerator(double lat, double lon, float my_distance,int numberOfPoints) {
    Random random = new Random();

    List<GeoLocation> geos = new ArrayList<GeoLocation>();

    double dn,de,dLat,dLon,latO,lonO;
    double  R=6378137;

              for (int j=0;j<numberOfPoints;j++){
                dn=my_distance*random.nextDouble();;
                de=my_distance*random.nextDouble();;
                dLat = dn/R;
                dLon = de/(R*Math.cos(Math.PI*lat/180));

                //OffsetPosition, decimal degrees
                latO = lat + dLat * 180/Math.PI;
                lonO = lon + dLon * 180/Math.PI ;
                GeoLocation geo =GeoLocation.fromDegrees(latO , lonO);
                geos.add(geo);
                }

    return(geos);
 }
    public static myMap Mapcreator(){
    myMap map=new myMap(distanceOfPointsInMeters,point_at_each_dimension,MaxNrofSensing,centralLat,centralLon);
    return(map);
    }
    public static void addtoMap(CostProfile cos,myMap map){
        GeoLocation geo=GeoConverter(cos);
         for (int i=0;i<point_at_each_dimension;i++)
            for (int j=0;j<point_at_each_dimension;j++){
            //System.out.println("error"+geos.distanceTo(map.my_map[i][j].myLoc));
            //System.out.println("start : "+geos.distanceTo(start));
            if ((geo.distanceTo(map.my_map[i][j].myLoc)<sensing_radius) && (map.my_map[i][j].timesSensed<map.max_sensing))
            map.my_map[i][j].timesSensed++;
            }
    }
    public static float Value_est(CostProfile costprofus, List <CostProfile> To,myMap map){
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
    }
    private static List<GeoLocation> GeoListConverter (List <CostProfile> To){
       List<GeoLocation> geos = new ArrayList<GeoLocation>(); 
       Iterator<CostProfile> itr1 = To.iterator();
       CostProfile cos;
       while(itr1.hasNext()){
       cos=itr1.next();   
       GeoLocation geo=GeoLocation.fromDegrees(cos.lat, cos.lon);
       geos.add(geo);
       }
       return(geos);
       
    }
    private static GeoLocation GeoConverter(CostProfile costprofus){
           return(GeoLocation.fromDegrees(costprofus.lat, costprofus.lon));
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