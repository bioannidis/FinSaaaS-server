/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saaas.server.selectionalgorithm;

/**
 *
 * @author Administrator
 */
public class CostProfile {
    protected String regId;
    protected float local_cost;
    protected int particepated;
    protected float pay;
    protected double lat,lon;
    

    public CostProfile(String regId,float local_cost,int particepated, float pay,double lat,double lon){
        this.regId=regId;
        this.local_cost=local_cost;
        this.pay=pay;
        this.particepated=particepated;
        this.lat=lat;
        this.lon=lon;
    }
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
    if (getClass() != o.getClass()) return false;

    CostProfile other = (CostProfile) o;
    if(!this.regId.equals(other.regId)) return false;
    return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.regId != null ? this.regId.hashCode() : 0);
        hash = 53 * hash + Float.floatToIntBits(this.local_cost);
        hash = 53 * hash + this.particepated;
        hash = 53 * hash + Float.floatToIntBits(this.pay);
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.lat) ^ (Double.doubleToLongBits(this.lat) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.lon) ^ (Double.doubleToLongBits(this.lon) >>> 32));
        return hash;
    }


    }

