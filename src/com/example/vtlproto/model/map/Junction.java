package com.example.vtlproto.model.map;

import java.util.ArrayList;
import com.example.vtlproto.model.Point;

public class Junction {

	String id;
	ArrayList<String> intLanes;
	ArrayList<Point> shape;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String> getIntLanes() {
		return intLanes;
	}

	public void setIntLanes(ArrayList<String> intLanes) {
		this.intLanes = intLanes;
	}

	public ArrayList<Point> getShape() {
		return shape;
	}

	public void setShape(ArrayList<Point> shape) {
		this.shape = shape;
	}

}
