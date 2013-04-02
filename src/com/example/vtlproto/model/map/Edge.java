package com.example.vtlproto.model.map;

import java.util.ArrayList;

public class Edge {

	ArrayList<Lane> lanes;
	String id;

	public ArrayList<Lane> getLanes() {
		return lanes;
	}

	public void setLanes(ArrayList<Lane> lanes) {
		this.lanes = lanes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
