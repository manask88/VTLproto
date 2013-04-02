package com.example.vtlproto.model.map;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.w3c.dom.Element;

import com.example.vtlproto.model.Point;

import android.util.Log;

public class Map {

	public static final String SHAPE_SEPARATOR = " ";

	public static final String POINT_SEPARATOR = ",";
	public static final String INTLANES_SEPARATOR = ":";

	ArrayList<Edge> edges;
	ArrayList<Junction> junctions;
	public static String TAG = Map.class.getSimpleName();

	public Map(String xml) {

		edges = new ArrayList<Edge>();
		junctions = new ArrayList<Junction>();
		Document doc = XMLfunctions.XMLfromString(xml);

		NodeList edgesNodeList = doc.getElementsByTagName("edge");

		for (int i = 0; i < edgesNodeList.getLength(); i++) {

			Edge edge = new Edge();
			Element edgeNodeList = (Element) edgesNodeList.item(i);
			// Log.i(TAG, XMLfunctions.getAttribute(edgeNodeList, "id"));
			edge.setId(XMLfunctions.getAttribute(edgeNodeList, "id"));
			NodeList lanesNodeList = edgeNodeList.getElementsByTagName("lane");

			ArrayList<Lane> lanes = new ArrayList<Lane>();
			for (int j = 0; j < lanesNodeList.getLength(); j++) {

				Lane lane = new Lane();
				Element laneNodeList = (Element) lanesNodeList.item(j);

				Log.i(TAG, XMLfunctions.getAttribute(laneNodeList, "id"));
				lane.setId(XMLfunctions.getAttribute(laneNodeList, "id"));
				lane.setLength(Float.valueOf(XMLfunctions.getAttribute(
						laneNodeList, "length")));

				/* begin parse points in shape */
				String shape = XMLfunctions.getAttribute(laneNodeList, "shape");
				String[] pointsString = shape.split(String
						.valueOf(SHAPE_SEPARATOR));

				ArrayList<Point> points = new ArrayList<Point>();

				for (int k = 0; k < pointsString.length; k++) {

					pointsString[k] = pointsString[k].replace("\"", "");
					String[] pointString = pointsString[k].split(String
							.valueOf(POINT_SEPARATOR));

					Point point = new Point(Float.valueOf(pointString[0]),
							Float.valueOf(pointString[1]));

					// Point point = new Point(1, 1);

					points.add(point);

				}
				if (points.size() > 0)
					lane.setShape(points);
				/* ends parse points in shape */

				if (lane.getShape() != null)
					lanes.add(lane);
			}

			if (lanes.size() > 0)
				edge.setLanes(lanes);

			if (edge.getLanes() != null)

				edges.add(edge);
		}

		NodeList junctionsNodeList = doc.getElementsByTagName("junction");

		for (int i = 0; i < junctionsNodeList.getLength(); i++) {

			Junction junction = new Junction();
			Element junctionNodeList = (Element) junctionsNodeList.item(i);
			junction.setId(XMLfunctions.getAttribute(junctionNodeList, "id"));

			/* begin parse points in shape */
			String shape = XMLfunctions.getAttribute(junctionNodeList, "shape");
			String[] pointsString = shape
					.split(String.valueOf(SHAPE_SEPARATOR));

			ArrayList<Point> points = new ArrayList<Point>();

			for (int k = 0; k < pointsString.length; k++) {

				Log.i(TAG, "got:" + pointsString[k]);
				pointsString[k] = pointsString[k].replace("\"", "");
				String[] pointString = pointsString[k].split(String
						.valueOf(POINT_SEPARATOR));

				Point point = new Point(Float.valueOf(pointString[0]),
						Float.valueOf(pointString[1]));

				points.add(point);

			}

			if (points.size() > 0)
				junction.setShape(points);
			/* ends parse points in shape */

			/* begin parse ids in inLanes */
			String inLanesString = XMLfunctions.getAttribute(junctionNodeList,
					"inLanes");
			String[] inLanesStringArray = inLanesString.split(String
					.valueOf(INTLANES_SEPARATOR));

			ArrayList<String> inLanes = new ArrayList<String>();

			for (int k = 0; k < inLanesStringArray.length; k++) {

				String intLaneString = ":" + inLanesStringArray[k];

				inLanes.add(intLaneString);
			}

			if (inLanes.size() > 0)
				junction.setInLanes(inLanes);
			/* end parse ids in inLanes */

			/* begin parse ids in outLanes */
			String outLanesString = XMLfunctions.getAttribute(junctionNodeList,
					"outLanes");
			String[] outLanesStringArray = outLanesString.split(String
					.valueOf(INTLANES_SEPARATOR));

			ArrayList<String> outLanes = new ArrayList<String>();

			for (int k = 0; k < outLanesStringArray.length; k++) {

				String outLaneString = ":" + outLanesStringArray[k];

				outLanes.add(outLaneString);
			}

			if (outLanes.size() > 0)
				junction.setOutLanes(outLanes);
			/* end parse ids in outLanes */

			if (junction.getShape() != null
					&& (junction.getInLanes() != null || junction.getOutLanes() != null))
				junctions.add(junction);

		}
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	public ArrayList<Junction> getJunctions() {
		return junctions;
	}

	public void setJunctions(ArrayList<Junction> junctions) {
		this.junctions = junctions;
	}

}
