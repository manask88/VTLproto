package com.example.vtlproto.model;

public class CloseCar extends BeaconPacket {

	float distance;

	public CloseCar(BeaconPacket beaconPacket) {

		super.setColor(beaconPacket.getColor());
		super.setDirection(beaconPacket.getDirection());
		super.setIPAdress(beaconPacket.getIPAdress());
		super.setTime(beaconPacket.getTime());
		super.setType(beaconPacket.getType());
		super.setX(beaconPacket.getX());
		super.setY(beaconPacket.getY());
		distance = 0;
	}

	public CloseCar() {
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

}
