package com.example.vtlproto.model;

public class Point {


	private double longitude,latitude;
	
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Point(double latitude, double longitude)
	{
	
		this.longitude=longitude;
		this.latitude=latitude;
		
	}
	public Point()
	{
		
	}
	
	public double getX() {
		return longitude;
	}
	public void setX(double x) {
		this.longitude=x;
	
	}
	public double getY() {
		return latitude;
	}
	public void setY(double y) {
		this.latitude=y;
	
	}
	
}
