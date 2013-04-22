package com.example.vtlproto;

import java.util.ArrayList;

import com.example.vtlproto.model.CoordinatePair;
import com.example.vtlproto.model.Point;
import com.example.vtlproto.model.map.Edge;
import com.example.vtlproto.model.map.Junction;
import com.example.vtlproto.model.map.Lane;
import com.example.vtlproto.model.map.Map;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.location.Location;
import android.util.Log;

public class HelperFunctions {

	public final static String TAG = HelperFunctions.class.getSimpleName();

	public static float bearing(Point p1, Point p2) {
		int MILLION = 1000000;
		double lat1 = p1.getLatitude();
		double lon1 = p1.getLongitude();
		double lat2 = p2.getLatitude();
		double lon2 = p2.getLongitude();

		return bearing(lat1, lon1, lat2, lon2);
	}

	public static float bearing(double lat1, double lon1, double lat2,
			double lon2) {
		double longitude1 = lon1;
		double longitude2 = lon2;
		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);
		double longDiff = Math.toRadians(longitude2 - longitude1);
		double y = Math.sin(longDiff) * Math.cos(latitude2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(longDiff);

		return (float) (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
	}

	/*
	 * public static float getAngle(Point oldPoint, Point newPoint) { float
	 * angle = (float) Math.toDegrees(Math.atan2(newPoint.getX() -
	 * oldPoint.getX(), newPoint.getY() - oldPoint.getY()));
	 * 
	 * 
	 * float angle = (float) Math.toDegrees(Math.atan2(oldPoint.getX() -
	 * newPoint.getX(), oldPoint.getY() - newPoint.getY()));
	 * 
	 * 
	 * if (angle < 0) { angle += 360; }
	 * 
	 * angle = 360 - angle + 90;
	 * 
	 * if (angle >= 360) angle = angle - 360; return angle; }
	 */
	public static double getDistance(double x1, double y1, double x2, double y2) {

		Location l1 = new Location("loc1");
		l1.setLatitude(y1);
		l1.setLongitude(x1);
		Location l2 = new Location("loc2");
		l2.setLatitude(y2);
		l2.setLongitude(x2);

		return l1.distanceTo(l2);
	}

	/*
	 * static float getDistance(float x1, float y1, float x2, float y2) { float
	 * sqrX = (float) Math.pow(x1 - x2, 2); float sqrY = (float) Math.pow(y1 -
	 * y2, 2); return (float) Math.sqrt(sqrX + sqrY); }
	 */

	public static ArrayList<Point> obtainRectangleCoordinates(Point point1,
			Point point2, double thickness)

	{

		ArrayList<Point> coordinates = new ArrayList<Point>();

		double width, x2, x1, y1, y2, height, length, xS, yS;
		y1 = point2.getLatitude();
		x1 = point2.getLongitude();
		y2 = point1.getLatitude();
		x2 = point1.getLongitude();

		width = x2 - x1;

		height = y2 - y1;
		// To find the length of the line, use Pythagoras's theorem:

		length = Math.sqrt(width * width + height * height);

		// Now the x shift (let's call it xS):

		xS = (thickness * height / length) / 2;

		yS = (thickness * width / length) / 2;

		// Now you have the x shift and y shift.

		Point coord1, coord2, coord3, coord4;

		coord1 = new Point();
		coord1.setLatitude(y1 + yS);
		coord1.setLongitude(x1 - xS);
		coord2 = new Point();
		coord2.setLatitude(y1 - yS);
		coord2.setLongitude(x1 + xS);
		coord3 = new Point();
		coord3.setLatitude(y2 - yS);
		coord3.setLongitude(x2 + xS);
		coord4 = new Point();
		coord4.setLatitude(y2 + yS);
		coord4.setLongitude(x2 - xS);
		coordinates.add(coord1);
		coordinates.add(coord2);
		coordinates.add(coord3);
		coordinates.add(coord4);
		// Log.i(TAG,
		// "Lat1:"+coord1.getLatitude()+"Lat2:"+coord1.getLatitude()+"Lon1:"+
		// coord2.getLongitude() +"Long2:"+coord2.getLongitude());

		/*
		 * First coordinate is: (x1 - xS, y1 + yS)
		 * 
		 * Second: (x1 + xS, y1 - yS)
		 * 
		 * Third: (x2 + xS, y2 - yS)
		 * 
		 * Fourth: (x2 - xS, y2 + yS)
		 */

		return coordinates;

	}

	public static ArrayList<Point> getNorthEastAndSouthWest(
			ArrayList<Point> points) {
		ArrayList<CoordinatePair> coordinates = new ArrayList<CoordinatePair>();

		for (Point point1 : points) {

			for (Point point2 : points)

			{

				if (point1.getLatitude() > point2.getLatitude()
						&& point1.getLongitude() > point2.getLongitude()) {
					CoordinatePair coordinatePair = new CoordinatePair();
					coordinatePair.setPoint1(point1);
					coordinatePair.setPoint2(point2);

					Log.i(TAG,
							"Lat1:" + point1.getLatitude() + "Lat2:"
									+ point2.getLatitude() + "Lon1:"
									+ point1.getLongitude() + "Long2:"
									+ point2.getLongitude());

					coordinates.add(coordinatePair);

				}
				/* to north lat is more positive */
				/*
				 * to east long is less negative, which is the same as more
				 * positive
				 */

			}

		}

		if (coordinates.size() > 0) {
			double distance = 0;
			int coor = -1;

			for (int i = 0; i < coordinates.size(); i++) {

				// getDistance(double x1, double y1, double x2, double y2)
				double tempdistance = HelperFunctions.getDistance(coordinates
						.get(i).getPoint1().getLongitude(), coordinates.get(i)
						.getPoint1().getLatitude(), coordinates.get(i)
						.getPoint2().getLongitude(), coordinates.get(i)
						.getPoint2().getLatitude());
				if (tempdistance > distance) {
					distance = tempdistance;
					coor = i;
				}

			}

			if (coor >= 0) {
				ArrayList<Point> pointsNorthEastAndSouthWest = new ArrayList<Point>();
				pointsNorthEastAndSouthWest.add(coordinates.get(coor)
						.getPoint1());
				pointsNorthEastAndSouthWest.add(coordinates.get(coor)
						.getPoint2());
				return pointsNorthEastAndSouthWest;

			} else
				return null;
		} else

			return null;

	}

	
	
	
	
	
	
	
	
	
	public static String findJunctionByLaneId(Map map, String laneId) {

		for (Junction junction : map.getJunctions()) {
			for (String laneIdToCompare : junction.getInLanes()) {

				if (laneIdToCompare.equals(laneId))
					return junction.getId();
			}

		}

		return null;

	}
	
	
	
	
	
	public static String findLane(Map map, double x, double y,
			float directionAngle) {
		LatLng newPointLocation = new LatLng(y, x);
		// Log.i(TAG, "lat:" + y + "log" + x);

		for (Edge edge : map.getEdges()) {

			if (edge.getAngle() - 45 < directionAngle
					&& directionAngle < edge.getAngle() + 45)

			{
				for (Lane lane : edge.getLanes()) {

					Point origin = lane.getShape().get(0);
					Point end = lane.getShape().get(1);

					ArrayList<Point> points = HelperFunctions
							.obtainRectangleCoordinates(origin, end, 0.0001);

					if (points.size() > 3) {

						LatLng newlocation;
						/*
						 * LatLng newlocation = new LatLng(points.get(0)
						 * .getLatitude(), points.get(0) .getLongitude());
						 */

						/*
						 * marker1 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc")); newlocation
						 * = new LatLng(points.get(1) .getLatitude(),
						 * points.get(1) .getLongitude());
						 * 
						 * 
						 * 
						 * marker2 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc")); newlocation
						 * = new LatLng(points.get(2) .getLatitude(),
						 * points.get(2) .getLongitude());
						 * 
						 * 
						 * marker3 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc")); newlocation
						 * = new LatLng(points.get(3) .getLatitude(),
						 * points.get(3) .getLongitude());
						 * 
						 * marker4 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc"));
						 */
						/*
						 * newlocation = new LatLng(end.getLatitude(),
						 * end.getLongitude());
						 * 
						 * 
						 * marker5 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc"));
						 * 
						 * newlocation = new LatLng(origin.getLatitude(),
						 * origin.getLongitude());
						 * 
						 * 
						 * 
						 * marker6 = googleMap .addMarker(new MarkerOptions()
						 * .position(newlocation).title( "MyLoc"));
						 */

						ArrayList<Point> pointsNorthEastAndSouthWest = HelperFunctions
								.getNorthEastAndSouthWest(points);

						if (pointsNorthEastAndSouthWest == null) {

							Log.e(TAG, "pointsNorthEastAndSouthWest is null");

						} else {
							Log.i(TAG, "laneid:" + lane.getId());
							Log.e(TAG, "pointsNorthEastAndSouthWest is ok");

							newlocation = new LatLng(
									pointsNorthEastAndSouthWest.get(0)
											.getLatitude(),
									pointsNorthEastAndSouthWest.get(0)
											.getLongitude());

							/*
							 * marker1 = googleMap .addMarker(new
							 * MarkerOptions() .position(newlocation).title(
							 * "MyLoc")); newlocation = new
							 * LatLng(pointsNorthEastAndSouthWest.get(1)
							 * .getLatitude(),
							 * pointsNorthEastAndSouthWest.get(1)
							 * .getLongitude());
							 * 
							 * 
							 * 
							 * marker2 = googleMap .addMarker(new
							 * MarkerOptions() .position(newlocation).title(
							 * "MyLoc"));
							 */

							LatLngBounds rectangle = new LatLngBounds(
									new LatLng(pointsNorthEastAndSouthWest.get(
											1).getLatitude(),
											pointsNorthEastAndSouthWest.get(1)
													.getLongitude()),
									new LatLng(pointsNorthEastAndSouthWest.get(
											0).getLatitude(),
											pointsNorthEastAndSouthWest.get(0)
													.getLongitude()));
							if (rectangle.contains(newPointLocation))

							{

								/*
								 * LatLng newlocation = new LatLng(points.get(0)
								 * .getLatitude(), points.get(0)
								 * .getLongitude());
								 * 
								 * if (marker1 != null) marker1.remove();
								 * 
								 * marker1 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc")); newlocation = new
								 * LatLng(points.get(1) .getLatitude(),
								 * points.get(1) .getLongitude());
								 * 
								 * if (marker2 != null) marker2.remove();
								 * 
								 * marker2 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc")); newlocation = new
								 * LatLng(points.get(2) .getLatitude(),
								 * points.get(2) .getLongitude()); if (marker3
								 * != null) marker3.remove();
								 * 
								 * marker3 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc")); newlocation = new
								 * LatLng(points.get(3) .getLatitude(),
								 * points.get(3) .getLongitude()); if (marker4
								 * != null) marker4.remove();
								 * 
								 * marker4 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc")); newlocation = new
								 * LatLng(end.getLatitude(),
								 * end.getLongitude()); if (marker5 != null)
								 * marker5.remove();
								 * 
								 * marker5 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc"));
								 * 
								 * newlocation = new
								 * LatLng(origin.getLatitude(),
								 * origin.getLongitude());
								 * 
								 * if (marker6 != null) marker6.remove();
								 * 
								 * marker6 = googleMap .addMarker(new
								 * MarkerOptions() .position(newlocation).title(
								 * "MyLoc"));
								 */

								return lane.getId();
							}

						}

					}

				}

			}
		}

		/* not found */
		return null;

	}

}
