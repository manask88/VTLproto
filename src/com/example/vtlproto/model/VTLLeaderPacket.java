package com.example.vtlproto.model;

import com.example.vtlproto.VTLApplication;

public class VTLLeaderPacket {

	private String type;
	private String time;

	private String IPAdress;

	public VTLLeaderPacket(String rawPacket) {
		String[] fields = rawPacket.split(String
				.valueOf(VTLApplication.MSG_SEPARATOR));
		type = fields[0];
		time = fields[1];
		IPAdress = fields[2];
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

	public String getIPAdress() {
		return IPAdress;
	}

	public void setIPAdress(String iPAdress) {
		IPAdress = iPAdress;
	}
}
