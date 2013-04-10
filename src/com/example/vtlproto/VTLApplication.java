package com.example.vtlproto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import com.example.vtlproto.model.BeaconPacket;
import com.example.vtlproto.model.CloseCar;
import com.example.vtlproto.model.Point;
import com.example.vtlproto.model.map.Edge;
import com.example.vtlproto.model.map.Junction;
import com.example.vtlproto.model.map.Lane;
import com.example.vtlproto.model.map.Map;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;

public class VTLApplication extends Application {

	public final static boolean[][] ROAD_MATRIX_3 = {
			{ false, false, false, true, true, false, false, false },
			{ false, false, false, true, true, false, false, false },
			{ false, false, false, true, true, false, false, false },
			{ true, true, true, true, true, true, true, true },
			{ true, true, true, true, true, true, true, true },
			{ false, false, false, true, true, false, false, false },
			{ false, false, false, true, true, false, false, false },
			{ false, false, false, true, true, false, false, false } };

	public final static boolean[][] ROAD_MATRIX_2 = {
			{ false, false, true, true, false, false, true, true },
			{ false, false, true, true, false, false, true, true },
			{ true, true, true, true, true, true, true, true },
			{ true, true, true, true, true, true, true, true },
			{ false, false, true, true, false, false, true, true },
			{ false, false, true, true, false, false, true, true },
			{ true, true, true, true, true, true, true, true },
			{ true, true, true, true, true, true, true, true } };

	public final static boolean[][] ROAD_MATRIX_4 = {
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ true, true, true, true, true, true, true, true, true, true },
			{ true, true, true, true, true, true, true, true, true, true },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false },
			{ false, false, false, false, true, true, false, false, false,
					false } };

	/*
	 * public final static boolean[][] ROAD_MATRIX = { { false, false, false,
	 * false, false, true, true, false, false, false, false, false }, { false,
	 * false, false, false, false, true, true, false, false, false, false, false
	 * }, { false, false, false, false, false, true, true, false, false, false,
	 * false, false }, { false, false, false, false, false, true, true, false,
	 * false, false, false, false }, { false, false, false, false, false, true,
	 * true, false, false, false, false, false },
	 * 
	 * { true, true, true, true, true, true, true, true, true, true, true, true
	 * }, { true, true, true, true, true, true, true, true, true, true, true,
	 * true }, { false, false, false, false, false, true, true, false, false,
	 * false, false, false }, { false, false, false, false, false, true, true,
	 * false, false, false, false, false }, { false, false, false, false, false,
	 * true, true, false, false, false, false, false }, { false, false, false,
	 * false, false, true, true, false, false, false, false, false }, { false,
	 * false, false, false, false, true, true, false, false, false, false, false
	 * } };
	 */

	public static boolean[][] ROAD_MATRIX = new boolean[12][12];

	public static final int SIZEX = ROAD_MATRIX.length;
	public final static int SIZEY = ROAD_MATRIX.length;
	private static final String TAG = VTLApplication.class.getSimpleName();
	private float currentPositionX, currentPositionY;
	public final static int PORT = 8888;
	public final static int PORT_2 = 8889;
	public boolean isBroadCastTX;
	public static final int SLEEPTIME_SEND = 1000;
	public static final int SLEEPTIME_RECEIVE = 100;
	public final static int SLEEPTIME_VTLSTATUS = 1000;

	public static final int SLEEPTIME_CONFLICTDETECTION = 1000;
	public static String BROADCASTADDRESS;
	public static final int DISTANCE_CLOSE = 10;
	public static final int DISTANCE_SAME_INTERSECTION = 2;

	public final static int MAX_NEIGHBORS = 4;
	public final static int OFFSETY = 0;
	public final static int ORANGE = Color.rgb(0xff, 0xa5, 0);
	public final static int[] COLORS = { Color.BLACK, Color.RED, Color.WHITE,
			Color.YELLOW };

	public final static int SLEEPTIME_TIME = 1000;
	public final static int BEACONSERVICE_HANDLER_RX_TEXT = 1;

	public final static int VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED = 2;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS = 3;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_DISTANCE = 4;
	public static final int VTLLOGICSERVICE_HANDLER_NEW_CLUSTER_LEADER = 5;

	public final static char MSG_SEPARATOR = ',';
	public final static char MSG_TYPE_BEACON = 'B';
	public final static char MSG_TYPE_LIGHT_STATUS = 'S';
	public final static char MSG_TYPE_LEADER_REQ = 'R';
	public final static char MSG_TYPE_LEADER_ACK = 'A';

	public final static char MSG_LIGHT_STATUS_GREEN = 'G';
	public final static char MSG_LIGHT_STATUS_RED = 'R';

	public Time time;
	public String IPAddress;
	public int trafficLightColor;
	public Point junctionPoint;
	public HashMap<String, BeaconPacket> hashMapNeighbors;
	public BeaconService beaconService;
	public CloseCar clusterLeader = null;
	public boolean beaconServiceStatus = false;
	public boolean conflictDetected = false;
	public boolean amIleader = false;
	public boolean didIgetLeaderPacket = false;
	public int timeLeftForCurrentStatus = 0;

	public boolean waitingForLeaderMessage = false;
	public String junctionId, laneId;
	public float directionAngle;
	public Map map;

	public float getCurrentPositionX() {
		return currentPositionX;
	}

	public float getCurrentPositionY() {
		return currentPositionY;
	}

	public float incCurrentX() {
		setCurrentPosition(new Point(currentPositionX + 1, currentPositionY));
		return currentPositionX;
	}

	public float incCurrentY() {
		setCurrentPosition(new Point(currentPositionX, currentPositionY + 1));
		return currentPositionY;
	}

	public float decCurrentX() {
		setCurrentPosition(new Point(currentPositionX - 1, currentPositionY));
		return currentPositionX;
	}

	public float decCurrentY() {
		setCurrentPosition(new Point(currentPositionX, currentPositionY - 1));
		return currentPositionY;
	}

	public void setCurrentPosition(Point newPoint) {

		Point oldPoint = new Point(currentPositionX, currentPositionY);
		setCurrentPositionY(newPoint.getY());
		setCurrentPositionX(newPoint.getX());

		if (oldPoint.getX() != newPoint.getX()
				|| oldPoint.getY() != newPoint.getY())
			refreshParams(oldPoint, newPoint);

	}

	public void setCurrentPositionY(float newPositionY) {
		if ((newPositionY < SIZEY)
				&& (newPositionY >= 0)
				&& (ROAD_MATRIX[(int) (SIZEY - 1 - newPositionY)][(int) currentPositionX])) {
			Log.d(TAG, "j : " + currentPositionX + " i: "
					+ (SIZEY - 1 - newPositionY));

			currentPositionY = newPositionY;

		}
	}

	public void setCurrentPositionX(float newPositionX) {
		if ((newPositionX < SIZEX)
				&& (newPositionX >= 0)
				&& (ROAD_MATRIX[(int) (SIZEY - 1 - currentPositionY)][(int) newPositionX])) {
			Log.d(TAG, "j : " + newPositionX + " i: "
					+ (SIZEY - 1 - currentPositionY));

			currentPositionX = newPositionX;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		AssetManager assetManager = getAssets();
		InputStream inputStream = null;
		try {
			inputStream = assetManager.open("map.xml");
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		String s = readTextFile(inputStream);
		map = new Map(s);
		setBooleanMap(map);
		isBroadCastTX = true;
		currentPositionX = SIZEX / 2;
		currentPositionY = 0;
		trafficLightColor = Color.WHITE;
		time = new Time(Time.getCurrentTimezone());
		Log.i(TAG, "onCreated");

		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled() == true) {

			// Log.d(TAG, "You are connected to WIFI "+
			// wifiManager.getConnectionInfo());
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			DhcpInfo dhcp = wifiManager.getDhcpInfo();
			int broadcastAddress = (dhcp.ipAddress & dhcp.netmask)
					| ~dhcp.netmask;
			int ipAddress = wifiInfo.getIpAddress();
			IPAddress = android.text.format.Formatter
					.formatIpAddress(ipAddress);
			// MulticastLock ml =
			// wifiManager.createMulticastLock("just some tag text"); //ADDED
			// THIS
			// ml.acquire(); //ADDED THIS

			// Settings.System.putInt(getContentResolver(),
			// Settings.System.WIFI_SLEEP_POLICY,
			// Settings.System.WIFI_SLEEP_POLICY_NEVER); //ADDED THIS
			/*
			 * BROADCASTADDRESS = android.text.format.Formatter
			 * .formatIpAddress(broadcastAddress);
			 */
			BROADCASTADDRESS = "255.255.255.255";
			// BROADCASTADDRESS = "10.20.1.255";
		} else {
			Log.e(TAG, "You are NOT connected to WIFI");

		}

		if (getWiFIDirectIPAddress(true) != null) {

			IPAddress =getWiFIDirectIPAddress(true);
			/* gets ip in case its using wifi direct */
			BROADCASTADDRESS = "192.168.49.255";

		}

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	public static String getWiFIDirectIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {

				if (intf.getName().equals("wlan0")
						|| intf.getName().equals("p2p-wlan0-0"))

				{

					List<InetAddress> addrs = Collections.list(intf
							.getInetAddresses());
					for (InetAddress addr : addrs) {
						if (!addr.isLoopbackAddress()) {
							String sAddr = addr.getHostAddress().toUpperCase();
							boolean isIPv4 = InetAddressUtils
									.isIPv4Address(sAddr);
							if (useIPv4) {
								if (isIPv4)
									return sAddr;
							} else {
								if (!isIPv4) {
									int delim = sAddr.indexOf('%'); // drop ip6
																	// port
																	// // suffix
									return delim < 0 ? sAddr : sAddr.substring(
											0, delim);
								}
							}
						}
					}

				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions return "";
		return null;
	}

	public static float getIfromY(float y) {
		return (SIZEY - 1 - y);

	}

	public static float getYfromI(float i) {
		return (SIZEY - 1 - i);

	}

	private String readTextFile(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {

		}
		return outputStream.toString();
	}

	private void setBooleanMap(Map map) {

		for (Edge edge : map.getEdges()) {
			for (Lane lane : edge.getLanes()) {

				Point origin = lane.getShape().get(0);
				Point end = lane.getShape().get(1);

				// Log.i(TAG, "origin:x:" + origin.getX() + ",y:" +
				// origin.getY());
				// Log.i(TAG, "end:x:" + end.getX() + ",y:" + end.getY());

				float distanceX = end.getX() - origin.getX();
				float distanceY = end.getY() - origin.getY();

				float length = lane.getLength();

				for (float i = 0; i <= length; i++) {
					float x, y;
					if (distanceX >= 0)
						x = origin.getX() + i * distanceX / length;
					else
						x = end.getX() + i * -distanceX / length;

					if (distanceY >= 0)
						y = origin.getY() + i * distanceY / length;
					else
						y = end.getY() + i * -distanceY / length;
					// Log.i(TAG, "trying to access ceel x:" + x + ",y" + y);

					ROAD_MATRIX[(int) (x)][(int) getIfromY(y)] = true;

				}

			}

		}

		for (Junction junction : map.getJunctions()) {
			for (Point point : junction.getShape()) {

				ROAD_MATRIX[(int) (point.getX())][(int) getIfromY(point.getY())] = true;

			}

		}

	}

	private String findLane(Map map, float x, float y) {

		for (Edge edge : map.getEdges()) {
			for (Lane lane : edge.getLanes()) {

				Point origin = lane.getShape().get(0);
				Point end = lane.getShape().get(1);

				if (origin.getX() <= x && x <= end.getX() && origin.getY() <= y
						&& y <= end.getY())
					return lane.getId();

				if (end.getX() <= x && x <= origin.getX() && end.getY() <= y
						&& y <= origin.getY())
					return lane.getId();

			}

		}

		/* not found */
		return null;

	}

	public String findJunctionByLaneId(String laneId) {

		for (Junction junction : map.getJunctions()) {
			for (String laneIdToCompare : junction.getInLanes()) {

				if (laneIdToCompare.equals(laneId))
					return junction.getId();
			}

		}

		return null;

	}

	public void setJunctionAndPointByLaneId(String laneId) {

		junctionId = null;
		junctionPoint = new Point(0, 0);

		for (Junction junction : map.getJunctions()) {
			for (String laneIdToCompare : junction.getInLanes()) {

				if (laneIdToCompare.equals(laneId)) {

					junctionId = junction.getId();
					for (Point junctionPointToAdd : junction.getShape()) {

						junctionPoint.setX(junctionPointToAdd.getX()
								+ junctionPoint.getX());
						junctionPoint.setY(junctionPointToAdd.getY()
								+ junctionPoint.getY());

					}

					if (junction.getShape().size() > 0) {
						junctionPoint.setX(junctionPoint.getX()
								/ junction.getShape().size());
						junctionPoint.setY(junctionPoint.getY()
								/ junction.getShape().size());
					}
					return;
				}
			}

		}

	}

	public float getAngle(Point oldPoint, Point newPoint) {
		float angle = (float) Math.toDegrees(Math.atan2(newPoint.getX()
				- oldPoint.getX(), newPoint.getY() - oldPoint.getY()));

		/*
		 * float angle = (float) Math.toDegrees(Math.atan2(oldPoint.getX() -
		 * newPoint.getX(), oldPoint.getY() - newPoint.getY()));
		 */

		if (angle < 0) {
			angle += 360;
		}

		angle = 360 - angle + 90;

		if (angle >= 360)
			angle = angle - 360;
		return angle;
	}

	public void refreshParams(Point oldPoint, Point newPoint) {
		directionAngle = getAngle(oldPoint, newPoint);
		laneId = findLane(map, newPoint.getX(), newPoint.getY());
		setJunctionAndPointByLaneId(laneId);
		Log.i(TAG, "Direction Angle:" + directionAngle);

	}

}
