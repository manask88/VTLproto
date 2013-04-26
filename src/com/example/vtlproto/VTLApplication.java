package com.example.vtlproto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class VTLApplication extends Application {

	
	public static boolean[][] ROAD_MATRIX = new boolean[12][12];


	private static final String TAG = VTLApplication.class.getSimpleName();
	private double currentPositionX, currentPositionY;
	public final static int PORT = 8888;
	
	public final static  int SLEEPTIME_SEND = 100;
	public final static  int SLEEPTIME_RECEIVE = 25;
	public final static int SLEEPTIME_VTLSTATUS = 1000;
	public final static  int SLEEPTIME_CONFLICTDETECTION = 500;
	public final static int SLEEPTIME_TIME = 100;
	public final static int SLEEPTIME_MOCKLOCATION = 1000;
	public final static  int SLEEPTIME_TIMESYNC = 1000 * 60 * 1;
	public final static  int NTP_TIME_OUT = 1000 * 10;
	public final static String NTP_SERVER="time.windows.com";
	public final static  String MULTICASTADDRESS = "224.2.2.3";
	public static String BROADCASTADDRESS;
	
	
	
	
	public final static  int DISTANCE_CLOSEX = 10;
	public final static  int DISTANCE_SAME_INTERSECTION = 2;
	public final static int MAX_NEIGHBORS = 4;
	public final static int OFFSETY = 0;
	
	public final static int ORANGE = Color.rgb(0xff, 0xa5, 0);
	public final static int[] COLORS = { Color.BLACK, Color.RED, Color.WHITE,
			Color.YELLOW };
	public final static int[] CARS = { R.drawable.car_icon_blue, R.drawable.car_icon_green, R.drawable.car_icon_red,
		R.drawable.car_icon_yellow};
	public final static int BEACONSERVICE_HANDLER_RX_TEXT = 1;
	public final static int VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED = 2;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS = 3;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_DISTANCE = 4;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_CLUSTER_LEADER = 5;
	public final static int VTLLOGICSERVICE_HANDLER_NEW_VTL_LEADER = 6;

	public final static char MSG_SEPARATOR = ',';
	public final static char MSG_TYPE_BEACON = 'B';
	public final static char MSG_TYPE_LIGHT_STATUS = 'S';
	public final static char MSG_TYPE_LEADER_REQ = 'R';
	public final static char MSG_TYPE_LEADER_ACK = 'A';
	public final static char MSG_LIGHT_STATUS_GREEN = 'G';
	public final static char MSG_LIGHT_STATUS_RED = 'R';

	public boolean isBroadCastTX;
	public long timeDifference;
	public String IPAddress;
	public int trafficLightColor;
	public Point junctionPoint;
	public HashMap<String, BeaconPacket> hashMapNeighbors;
	public TimeSyncService beaconService;
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
	public GoogleMap googleMap;
	private OutputStreamWriter outWriter;
	private FileOutputStream fileOut;
	public Marker mylocation, marker1, marker2, marker3, marker4, marker5,
			marker6;

	GroundOverlay groundOverlay;

	public double getCurrentPositionX() {
		return currentPositionX;
	}

	public double getCurrentPositionY() {
		return currentPositionY;
	}



	public void setCurrentPosition(Location location) {

		Point oldPoint = new Point(currentPositionY,currentPositionX);
		Point newPoint = new Point(location.getLatitude(),location.getLongitude());
		currentPositionX = location.getLongitude();
		currentPositionY = location.getLatitude();
		refreshParams(oldPoint, newPoint);
	}



	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "onCreate");

		AssetManager assetManager = getAssets();
		InputStream inputStream = null;
		try {
			inputStream = assetManager.open("map.xml");
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}

		String s = readTextFile(inputStream);
		map = new Map(s);
		// setBooleanMap(map);
		isBroadCastTX = true;
		currentPositionX = 0;
		currentPositionY = 0;
		trafficLightColor = Color.WHITE;
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

		/*
		 * if (IPAddress == null || IPAddress.equals("0.0.0.0")) IPAddress =
		 * "192.168.49.1";
		 */

		if (getWiFIDirectIPAddress(true) != null) {

			IPAddress = getWiFIDirectIPAddress(true);
			if (IPAddress == null || IPAddress.equals("0.0.0.0"))
				IPAddress = "192.168.49.1";
			BROADCASTADDRESS = "192.168.49.255";
			Log.i(TAG, IPAddress);
			// BROADCASTADDRESS = "255.255.255.255";
			/* gets ip in case its using wifi direct */
			// BROADCASTADDRESS = "192.168.49.255";

		}
	/*	else
		{
			BROADCASTADDRESS = "255.255.255.255";
			
		}*/
	}



	public static String getWiFIDirectIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				// intf.getName().equals("wlan0")
				if (intf.getName().equals("p2p-wlan0-0")
						|| intf.getName().equals("p2p0"))

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



	public void refreshParams(Point oldPoint, Point newPoint) {
		directionAngle = HelperFunctions.bearing(oldPoint, newPoint);
		laneId = HelperFunctions.findLane(map, newPoint.getX(), newPoint.getY(), directionAngle);
		setJunctionAndPointByLaneId(laneId);
		Log.i(TAG, "Direction Angle:" + directionAngle);

	}

	public String getTimeDisplay() {

		String stringTime = new SimpleDateFormat("H:mm:ss.S").format(System
				.currentTimeMillis() + timeDifference);

		return stringTime.substring(0, stringTime.length() - 2);



	}

	public String getTimeAndDate() {
		return new SimpleDateFormat("yyyy.MM.dd H:mm:ss.S").format(System
				.currentTimeMillis() + timeDifference);

	}

	public long getTimeAndDateInLong() {
		return System.currentTimeMillis()+ timeDifference;

	}

	/*this is used to write logs */
	void createAndOpenFile() {
		try {
			File logFile = new File("/sdcard/logVTL.txt");

			logFile.createNewFile();

			fileOut = new FileOutputStream(logFile);
			outWriter = new OutputStreamWriter(fileOut);

		} catch (IOException e) {
			Log.e(TAG, e.getMessage());

		}
	}
	
	/*this is used to write logs */
	void closeFile() {
		try {
			outWriter.close();
			fileOut.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());

		}

	}

	/*this is used to write logs */
	void writeToFile(String text) {

		try {
			outWriter.append(text);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/*this is used to read map information*/
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
	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "onTerminated");
	}
}
