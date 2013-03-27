package com.example.vtlproto.model;

import com.example.vtlproto.VTLApplication;
import com.example.vtlproto.VTLApplication.Direction;


public class BeaconPacket {

	private int x,y;
	private String time;
	private String IPAdress;
	private Direction direction;
	private String type;
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
		x=Integer.parseInt(fields[2]);
		y=Integer.parseInt(fields[3]);
		direction=Direction.valueOf(fields[4]);
		IPAdress=fields[5];
	}

	public BeaconPacket()
	{
		
	}
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getIPAdress() {
		return IPAdress;
	}

	public void setIPAdress(String iPAdress) {
		IPAdress = iPAdress;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
