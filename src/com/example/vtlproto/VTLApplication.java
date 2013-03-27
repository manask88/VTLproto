package com.example.vtlproto;

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
import com.example.vtlproto.model.Point;

import android.app.Application;
import android.content.Context;
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

	public enum Direction {
		N, S, W, E
	}

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
	
	public final static boolean[][] ROAD_MATRIX = {
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },

		{ true, true, true, true, true, true, true, true, true, true ,true,true},
		{ true, true, true, true, true, true, true, true, true, true ,true,true},
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false },
		{ false, false, false, false, false, true, true, false, false, false,false,	false }
 };
	
	private static final String TAG = VTLApplication.class.getSimpleName();
	private int currentPositionX, currentPositionY;
	public final static int PORT = 8888;
	public final static int PORT_2 = 8889;
	public boolean isBroadCastTX;
	public static final int SLEEPTIME_SEND = 1000;
	public static final int SLEEPTIME_RECEIVE = 1000;
	public static final int SLEEPTIME_TRAFFIC_LIGHT = 10000;

	public static final int SLEEPTIME_CONFLICTDETECTION = 1000;
	public static  String BROADCASTADDRESS;
	public static final int DISTANCE_CLOSE = 10;
	public static final int DISTANCE_SAME_INTERSECTION = 2;

	public static final int SIZEX = ROAD_MATRIX.length;
	public final static int SIZEY = ROAD_MATRIX.length;
	public final static int MAX_NEIGHBORS = 4;
	public final static int OFFSETY = 0;
	public final static int ORANGE = Color.rgb(0xff, 0xa5, 0);
	public final static int[] COLORS = { Color.BLACK, Color.RED, Color.WHITE,
			Color.YELLOW };

	public final static int SLEEPTIME_TIME = 1000;
	public final static int HANDLER_RX_CONFLICT_DETECTED = 2;
	public final static int HANDLER_RX_TEXT = 1;
	public final static int HANDLER_NEW_LIGHT_STATUS = 3;
	public final static int HANDLER_NEW_DISTANCE = 4;

	public final static char MSG_SEPARATOR = ',';
	public final static char MSG_TYPE_BEACON = 'B';
	public final static char MSG_TYPE_LIGHT_STATUS = 'S';
	public final static char MSG_LIGHT_STATUS_GREEN = 'G';
	public final static char MSG_LIGHT_STATUS_RED = 'R';


	public Time time;
	public String IPAddress;
	public int trafficLightColor;
	public Point intersection;
	public Direction direction;
	public HashMap<String, BeaconPacket> hashMapNeighbors;
	public BeaconService beaconService;
	public  boolean beaconServiceStatus=false;
	public  boolean waitingForLeaderMessage=false;

	public int getCurrentPositionX() {
		return currentPositionX;
	}

	public int getCurrentPositionY() {
		return currentPositionY;
	}

	public int incCurrentX() {
		setCurrentPositionX(currentPositionX + 1);
		return currentPositionX;
	}

	public int incCurrentY() {
		setCurrentPositionY(currentPositionY + 1);
		return currentPositionY;
	}

	public int decCurrentX() {
		setCurrentPositionX(currentPositionX - 1);
		return currentPositionX;
	}

	public int decCurrentY() {
		setCurrentPositionY(currentPositionY - 1);
		return currentPositionY;
	}

	public void setCurrentPositionY(int newPositionY) {
		if ((newPositionY < SIZEY) && (newPositionY >= 0)
				&& (ROAD_MATRIX[SIZEY - 1 - newPositionY][currentPositionX])) {
			Log.d(TAG, "j : " + currentPositionX + " i: "
					+ (SIZEY - 1 - newPositionY));
			if (newPositionY > currentPositionY)
				direction = Direction.N;
			else
				direction = Direction.S;
			currentPositionY = newPositionY;

		}
	}

	public void setCurrentPositionX(int newPositionX) {
		if ((newPositionX < SIZEX) && (newPositionX >= 0)
				&& (ROAD_MATRIX[SIZEY - 1 - currentPositionY][newPositionX])) {
			Log.d(TAG, "j : " + newPositionX + " i: "
					+ (SIZEY - 1 - currentPositionY));

			if (newPositionX > currentPositionX)
				direction = Direction.E;
			else
				direction = Direction.W;

			currentPositionX = newPositionX;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		direction = Direction.N;
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
			int broadcastAddress = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			int ipAddress = wifiInfo.getIpAddress();
			IPAddress = android.text.format.Formatter
					.formatIpAddress(ipAddress);
			//MulticastLock ml = wifiManager.createMulticastLock("just some tag text"); //ADDED THIS
			//ml.acquire();  //ADDED THIS
			
			//Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER); //ADDED THIS
			/*BROADCASTADDRESS = android.text.format.Formatter
					.formatIpAddress(broadcastAddress);*/
					BROADCASTADDRESS="255.255.255.255";
			//BROADCASTADDRESS = "10.20.1.255";
			} else {
			Log.e(TAG, "You are NOT connected to WIFI");
			IPAddress = getIPAddress(true);
			/* gets ip in case its using wifi direct */
			BROADCASTADDRESS = "192.168.49.255";
		}


	}
	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {

				if (intf.getName().equals("wlan0") || intf.getName().equals("p2p-wlan0-0"))

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
		return "";
	}

	public static int getIfromY(int y) {
		return (SIZEY - 1 - y);

	}

	public static int getYfromI(int i) {
		return (SIZEY - 1 - i);

	}
}
