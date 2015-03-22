/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server.selectionalgorithm;

/**
 *
 * @author basilhs
 */
public class Point {
    GeoLocation myLoc;
    int timesSensed;
    public Point (int times,GeoLocation geo){
        timesSensed=times;
        myLoc=geo;
    }
}
