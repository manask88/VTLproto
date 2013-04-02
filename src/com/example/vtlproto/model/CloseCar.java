package com.example.vtlproto.model;

public class CloseCar extends BeaconPacket {

	float distance;

	public CloseCar(BeaconPacket beaconPacket) {

		super.setType(beaconPacket.getType());
		super.setTime(beaconPacket.getTime());
		super.setX(beaconPacket.getX());
		super.setY(beaconPacket.getY());
		super.setDirectionAngle(beaconPacket.getDirectionAngle());
		super.setLaneId(beaconPacket.getLaneId());
		super.setVTLLeader(beaconPacket.isVTLLeader);
		super.setIPAdress(beaconPacket.getIPAdress());
		super.setColor(beaconPacket.getColor());

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
