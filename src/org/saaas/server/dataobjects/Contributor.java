package org.saaas.server.dataobjects;

import java.sql.Timestamp;

public class Contributor extends Registrant {
	private boolean lastReceiver;
	private Timestamp timestamp;
        private String bookingId;
        public Contributor(Registrant device) {
                this.setRegId(device.getRegId());
                this.setIpAddress(device.getIpAddress());
                this.setReachability(device.getReachability());
                this.setAvailability(device.getAvailability());
                //bill
                this.setUserName(device.getUserName());
                this.setBookingId(null);
        }
        
	public boolean isLastReceiver() {
		return lastReceiver;
	}
	
        public void setLastReceiver(boolean lastReceiver) {
		this.lastReceiver = lastReceiver;
	}
	
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
        
        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }
        
        public String getBookingId() {
            if(bookingId != null) {
                return bookingId;
            } else {
                return "none";
            }
        }
	
}
