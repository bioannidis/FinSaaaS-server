package org.saaas.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class CostProfTesting {
        protected String regId;
    protected float local_cost;
    protected int particepated;
    protected float pay;
    protected double lat,lon;
    protected double time_left;

    public CostProfTesting(String regId,float local_cost,int particepated, float pay,double lat,double lon,double time_left){
        this.regId=regId;
        this.local_cost=local_cost;
        this.pay=pay;
        this.particepated=particepated;
        this.lat=lat;
        this.lon=lon;
        this.time_left=time_left;
    }
    
}
