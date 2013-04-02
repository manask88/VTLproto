package com.example.vtlproto.model.map;

import java.util.ArrayList;
import com.example.vtlproto.model.Point;

public class Junction {

	String id;
	ArrayList<String> inLanes,outLanes;
	ArrayList<Point> shape;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	public ArrayList<String> getInLanes() {
		return inLanes;
	}

	public void setInLanes(ArrayList<String> inLanes) {
		this.inLanes = inLanes;
	}

	public ArrayList<String> getOutLanes() {
		return outLanes;
	}

	public void setOutLanes(ArrayList<String> outLanes) {
		this.outLanes = outLanes;
	}

	public ArrayList<Point> getShape() {
		return shape;
	}

	public void setShape(ArrayList<Point> shape) {
		this.shape = shape;
	}

}
