package com.example.vtlproto.model.map;

import java.util.ArrayList;

import com.example.vtlproto.model.Point;

public class Lane {

	String id;
	ArrayList<Point> shape;
	float length;
	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Point> getShape() {
		return shape;
	}

	public void setShape(ArrayList<Point> shape) {
		this.shape = shape;
	}
}
