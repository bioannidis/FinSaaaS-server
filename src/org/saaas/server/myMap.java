/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server;

/**
 *
 * @author basilhs
 */
    //2-d grid of coordinates map where each pointofinterest covers an area of a^2 meters^2

public class myMap extends Object{
       Point[][] my_map;
       double my_distance;
       int max_sensing;
       public myMap(myMap map){
           this.my_map=map.my_map.clone();
           this.my_distance=map.my_distance;
           this.max_sensing=map.max_sensing;
       }
       public myMap(double distanceOfPointsInMeters,int NumberOfPointAtEachDimension, int max_sense,double centralLat,double centralLon){
           max_sensing=max_sense;
           my_map =new Point [NumberOfPointAtEachDimension][NumberOfPointAtEachDimension];
           my_distance=distanceOfPointsInMeters;
           double  R=6378137;
           double lat = centralLat;
           double lon =centralLon;
           double dn,de;
           double dLat,dLon,latO,lonO;
           for (int i=0;i<NumberOfPointAtEachDimension;i++)
              for (int j=0;j<NumberOfPointAtEachDimension;j++){
                dn=my_distance*i;
                de=my_distance*j;
                dLat = dn/R;
                dLon = de/(R*Math.cos(Math.PI*lat/180));

                //OffsetPosition, decimal degrees
                latO = lat + dLat * 180/Math.PI;
                lonO = lon + dLon * 180/Math.PI ;
                my_map[i][j]=new Point(0,GeoLocation.fromDegrees(latO, lonO));
                }
       }
    
}
