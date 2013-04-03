package com.example.vtlproto.model;

import java.util.ArrayList;

import com.example.vtlproto.VTLApplication;

public class TrafficLightPacket {

	private String type;
	private String time;
	private String timer;
	private ArrayList<NameValue> statusLaneIds;

	private String IPAdress;

	public TrafficLightPacket(String rawPacket) {
		String[] fields = rawPacket.split(String
				.valueOf(VTLApplication.MSG_SEPARATOR));
		type = fields[0];
		time = fields[1];
		timer = fields[2];
		statusLaneIds=new ArrayList<NameValue>();
		for (int i = 3; i < fields.length - 1; i = i + 2) {
			NameValue statusLaneId = new NameValue();
			statusLaneId.setValue(fields[i].charAt(0));
			statusLaneId.setName(fields[i + 1]);

			statusLaneIds.add(statusLaneId);

		}

		IPAdress = fields[fields.length - 1];

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

	public String getTimer() {
		return timer;
	}

	public void setTimer(String timer) {
		this.timer = timer;
	}

	public ArrayList<NameValue> getStatusLaneIds() {
		return statusLaneIds;
	}

	public void setStatusLaneIds(ArrayList<NameValue> statusLaneIds) {
		this.statusLaneIds = statusLaneIds;
	}

	public String getIPAdress() {
		return IPAdress;
	}

	public void setIPAdress(String iPAdress) {
		IPAdress = iPAdress;
	}

}