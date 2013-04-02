package com.example.vtlproto.model;

import com.example.vtlproto.VTLApplication;
import com.example.vtlproto.VTLApplication.Direction;


public class BeaconPacket {

	private String type;
	private String time;
	private float x,y;
	private float directionAngle;
	private String laneId;
	boolean isVTLLeader;
	private String IPAdress;
	private int color;
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public BeaconPacket(String rawPacket)
	{
		String[] fields=rawPacket.split(String.valueOf(VTLApplication.MSG_SEPARATOR));
		type=fields[0];
		time=fields[1];
		x=Float.valueOf(fields[2]);
		y=Float.valueOf(fields[3]);
		directionAngle=Float.valueOf(fields[4]);
		laneId=fields[5];
		isVTLLeader=fields[5].equals("0")?false:true;
		IPAdress=fields[6];
	}

	public BeaconPacket()
	{
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getDirectionAngle() {
		return directionAngle;
	}

	public void setDirectionAngle(float directionAngle) {
		this.directionAngle = directionAngle;
	}

	public String getLaneId() {
		return laneId;
	}

	public void setLaneId(String laneId) {
		this.laneId = laneId;
	}

	public boolean isVTLLeader() {
		return isVTLLeader;
	}

	public void setVTLLeader(boolean isVTLLeader) {
		this.isVTLLeader = isVTLLeader;
	}

	public String getIPAdress() {
		return IPAdress;
	}

	public void setIPAdress(String iPAdress) {
		IPAdress = iPAdress;
	}
	
	
}
