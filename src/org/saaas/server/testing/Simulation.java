/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saaas.server.testing;

/**
 *
 * @author palinka
 */
public class Simulation {
    
    public static  void main(String[] args) {
   TestAlgorithm testAlgo=new TestAlgorithm();
    //testAlgo.Call_algorithm("20", "12000", "360", "online");
    testAlgo.Call_algorithm("20", "6000", "360", "offline");
    }
}
