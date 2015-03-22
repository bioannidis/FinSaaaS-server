package org.saaas.server.dataobjects;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.*;
import org.saaas.server.CountdownTask;

public class Registrant {
    private String regId;
    private String ipAddress;
    private boolean reachability;
    private boolean availability;
    private int ttl;
    private static final long delay = 1000L;
    private Timer crono;
    private TimerTask countdown;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private String userName;
    private final Runnable setReachabilityToFalseTask = new Runnable() {
            @Override
            public void run() {
                logger.log(Level.INFO, "timeout");
                setReachability(false);
            }
        };
	
    public String getRegId() {
            return regId;
    }
    
    public void setRegId(String regId) {
            this.regId = regId;
    }
    
    public String getIpAddress() {
            return ipAddress;
    }
    
    public String getUserName(){
        return userName;
    }
    
    public void setUserName(String userName){
        this.userName=userName;
    }
    
    public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
    }

    public boolean getReachability() {
            return reachability;
    }

    public void setReachability(boolean reachable) {
            this.reachability = reachable;
    }

    public boolean getAvailability() {
            return availability;
    }

    public void setAvailability(boolean available) {
            this.availability = available;
    }

    public int getTTL() {
            return ttl;
    }
    
    public void setTTL(int timetolive) {
            this.ttl = timetolive;
    }

    public void setTimer(int timer) {
        setTTL(timer);
        this.crono = new Timer("reachability timeout counter for " + this.regId, false);
        this.countdown = new CountdownTask(this.ttl, this.setReachabilityToFalseTask);
        crono.scheduleAtFixedRate(countdown, delay, delay);
        logger.log(Level.INFO, "countdown started");
    }

    public void resetTimer() {
        unsetTimer();
        setTimer(getTTL());
        logger.log(Level.INFO, "countdown REstarted");
    }
    
    public void unsetTimer() {
        this.crono.cancel();
        this.countdown.cancel();
        logger.log(Level.INFO, "countdown UNset");
    }
}
