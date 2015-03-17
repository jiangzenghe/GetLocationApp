package com.myapp.getlocation.entity;

import java.io.Serializable;

public class Points implements Serializable{
	private Double absoluteLongitude;
	private Double absoluteLatitude;
	private Double absoluteAltitude;
	
	public Points(Double longitude, Double latitude, Double altitude) {
		this.absoluteAltitude = altitude;
		this.absoluteLatitude = latitude;
		this.absoluteLongitude = longitude;
	}
	
	public Double getAbsoluteLongitude() {
		return absoluteLongitude;
	}
	public void setAbsoluteLongitude(Double absoluteLongitude) {
		this.absoluteLongitude = absoluteLongitude;
	}
	public Double getAbsoluteLatitude() {
		return absoluteLatitude;
	}
	public void setAbsoluteLatitude(Double absoluteLatitude) {
		this.absoluteLatitude = absoluteLatitude;
	}
	public Double getAbsoluteAltitude() {
		return absoluteAltitude;
	}
	public void setAbsoluteAltitude(Double absoluteAltitude) {
		this.absoluteAltitude = absoluteAltitude;
	}
    
    
}
