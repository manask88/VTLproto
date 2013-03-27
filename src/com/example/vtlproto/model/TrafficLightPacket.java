package com.example.vtlproto.model;

import com.example.vtlproto.VTLApplication;

public class TrafficLightPacket {

	private String time;
	

	private String IPAdress;
	private String type;
	private char status;

	public TrafficLightPacket(String rawPacket) {
		String[] fields = rawPacket.split(String.valueOf(VTLApplication.MSG_SEPARATOR));
		type = fields[0];
		time = fields[1];
		status = fields[2].charAt(0);
		IPAdress = fields[3];

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public char getStatus() {
		return status;
	}


	public void setStatus(char status) {
		this.status = status;
	}


}