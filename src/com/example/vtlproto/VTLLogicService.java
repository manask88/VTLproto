package com.example.vtlproto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.example.vtlproto.VTLApplication.Direction;
import com.example.vtlproto.model.BeaconPacket;
import com.example.vtlproto.model.CloseCar;
import com.example.vtlproto.model.Point;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VTLLogicService {

	private boolean runFlagConflictDetection = false;
	private CloseCar closestCarToIntersection;
	private final Handler myUpdateHandler;
	private VTLApplication application;
	Message msg;
	boolean runFlagListener;
	private final static String TAG = VTLLogicService.class.getSimpleName();
	ConflictDetectionThread conflictDetectionThread;
	private Context context;
	Bundle bundle;

	/*
	 * Handler myUpdateHandler = new Handler() { public void
	 * handleMessage(Message msg) { switch (msg.what) { case MSG_ID: TextView tv
	 * = (TextView) findViewById(R.id.helloText); Toast.makeText(context,
	 * "i got"+msg, Toast.LENGTH_SHORT).show(); tv.setText(msg);
	 * 
	 * break; default: break; } super.handleMessage(msg); } };
	 */

	public VTLLogicService(Context context, Handler handler) {
		Log.i(TAG, "Service: OnCreate");
		closestCarToIntersection = new CloseCar();
		this.context = context;
		application = (VTLApplication) context.getApplicationContext();

		myUpdateHandler = handler;
	}

	private class ConflictDetectionThread extends Thread {

		public ConflictDetectionThread() {

		}

		public void run() {
			Log.i(VTLActivity.TAG, "Begin ConflictDetectionThread");
			try {
				while (runFlagConflictDetection) {

					application.intersection = getIntersection(
							application.getCurrentPositionX(),
							application.getCurrentPositionY(),
							application.direction);
					if (application.intersection != null) {

						ArrayList<CloseCar> closeCars = closeCars();
						if (application.intersection != null
								&& isConflictingIntersection(closeCars)) {
							
							/* conflict code begins here */
							application.conflictDetected = true;
							application.trafficLightColor = VTLApplication.ORANGE;

							closestCarToIntersection.setDistance(getDistance(
									application.getCurrentPositionX(),
									application.intersection.getX(),
									application.getCurrentPositionY(),
									application.intersection.getY()));

							closestCarToIntersection
									.setIPAdress(application.IPAddress);

							closestCarToIntersection(closeCars);

							Message msg = myUpdateHandler
									.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED);

							myUpdateHandler.sendMessage(msg);

							/*
							 * Log.i(TAG, "The furthestCarFromIntersection is: "
							 * + furthestCarFromIntersection.getIPAdress());
							 */

							/* code for the leader */
							String rawPacket;
							if (closestCarToIntersection.getIPAdress().equals(
									application.IPAddress)) {

								Log.i(TAG,
										"I am the leader, so i send a unicast packet");
								for (CloseCar closeCar : closeCars) {

									Log.i(TAG, "ill send messages to this car "
											+ closeCar.getIPAdress());
									rawPacket = new StringBuilder(
											String.valueOf(VTLApplication.MSG_TYPE_LIGHT_STATUS))
											.append(VTLApplication.MSG_SEPARATOR)
											.append(application.time.format(
													"%k:%M:%S").toString())
											.append(VTLApplication.MSG_SEPARATOR)
											.append(isConflictingDirection(
													closeCar.getDirection(),
													application.direction) ? VTLApplication.MSG_LIGHT_STATUS_GREEN
													: VTLApplication.MSG_LIGHT_STATUS_RED)
											.toString();

									Intent serviceIntent = new Intent(context,
											SendUnicastService.class);
									serviceIntent.putExtra(
											SendUnicastService.EXTRAS_DST_IP,
											closeCar.getIPAdress());
									serviceIntent
											.putExtra(
													SendUnicastService.EXTRAS_RAW_PACKET,
													rawPacket);

									context.startService(serviceIntent);
								}

								application.trafficLightColor = Color.RED;
								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								Bundle bundle = new Bundle();
								bundle.putString("closestCarToIntersection",
										closestCarToIntersection.getIPAdress());
								msg.setData(bundle);
								myUpdateHandler.sendMessage(msg);

								Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								Log.i(TAG,
										"I am the leader, so i send a unicast packet");
								for (CloseCar closeCar : closeCars) {

									;
									rawPacket = new StringBuilder(
											String.valueOf(VTLApplication.MSG_TYPE_LIGHT_STATUS))
											.append(VTLApplication.MSG_SEPARATOR)
											.append(application.time.format(
													"%k:%M:%S").toString())
											.append(VTLApplication.MSG_SEPARATOR)
											.append(isConflictingDirection(
													closeCar.getDirection(),
													application.direction) ? VTLApplication.MSG_LIGHT_STATUS_RED
													: VTLApplication.MSG_LIGHT_STATUS_GREEN)
											.toString();

									Intent serviceIntent = new Intent(context,
											SendUnicastService.class);
									serviceIntent.putExtra(
											SendUnicastService.EXTRAS_DST_IP,
											closeCar.getIPAdress());
									serviceIntent
											.putExtra(
													SendUnicastService.EXTRAS_RAW_PACKET,
													rawPacket);

									context.startService(serviceIntent);
								}

								application.trafficLightColor = Color.GREEN;
								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								bundle = new Bundle();
								bundle.putString("closestCarToIntersection",
										null);
								msg.setData(bundle);
								myUpdateHandler.sendMessage(msg);

								Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								/* code for leader ends here */

								/* code for others */
							} else {

								Log.i(TAG,
										"I am not the leader, so i am waiting for the leader");

								application.trafficLightColor = VTLApplication.ORANGE;

								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								bundle = new Bundle();
								bundle.putString("closestCarToIntersection",
										closestCarToIntersection.getIPAdress());
								msg.setData(bundle);

								myUpdateHandler.sendMessage(msg);

								application.waitingForLeaderMessage = true;

								// int counter=3; /*wait for 3 seconds or
								// timeout*/
								while (application.waitingForLeaderMessage) {
									Log.d(TAG, "looping");

									Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								}

								/*
								 * if (counter==0) { Log.d(TAG,
								 * "it was a timeout");
								 * application.trafficLightColor =
								 * VTLApplication.ORANGE; //I really dont need
								 * to, but it will be useful for when i get red
								 * msg = myUpdateHandler
								 * .obtainMessage(VTLApplication.
								 * HANDLER_NEW_LIGHT_STATUS); bundle = new
								 * Bundle();
								 * bundle.putString("furthestCarFromIntersection"
								 * , furthestCarFromIntersection.getIPAdress());
								 * msg.setData(bundle); } else Log.d(TAG,
								 * "must be on green already");
								 */
								/*
								 * means I received a traffic light packet from
								 * leader if reach here
								 */

								if (application.trafficLightColor == Color.RED) {
									// wait until i get a green
									application.waitingForLeaderMessage = true;
									while (application.waitingForLeaderMessage) {
									}

									Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								}

								/*
								 * did i pass internsection,, if yes, i should
								 * get out of here, if not, wait for next
								 * message
								 */

							} /* code for others ends here */

							application.conflictDetected = false;

						} /* conflict code ends here */

					} else {
						Message msg = myUpdateHandler
								.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED);
						myUpdateHandler.sendMessage(msg);
						application.trafficLightColor = Color.WHITE;
					}

					Thread.sleep(VTLApplication.SLEEPTIME_CONFLICTDETECTION);

				}
			} catch (InterruptedException ie) {
				Log.i(TAG, ie.getMessage());
				runFlagConflictDetection = false;

				Thread.currentThread().interrupt();
			}

		}

	}

	private ArrayList<CloseCar> closeCars() {

		ArrayList<CloseCar> closeCars = new ArrayList<CloseCar>();

		for (Entry<String, BeaconPacket> entry : application.hashMapNeighbors
				.entrySet()) {
			// String key = entry.getKey();
			BeaconPacket value = entry.getValue();

			/*
			 * int sqrX = (int) Math.pow( value.getX() -
			 * application.getCurrentPositionX(), 2); int sqrY = (int) Math.pow(
			 * value.getY() - application.getCurrentPositionY(), 2); float
			 * result = (float) Math.sqrt(sqrX + sqrY);
			 */
			Float result = getDistance(value.getX(), value.getY(),
					application.getCurrentPositionX(),
					application.getCurrentPositionY());

			/*
			 * Log.i(TAG, "Car with IP " + value.getIPAdress() +
			 * " has a distance to me of " + result);
			 */

			if (result < VTLApplication.DISTANCE_CLOSE) {
				CloseCar closecar = new CloseCar(value);
				closecar.setDistance(result);
				closeCars.add(closecar);

			}

		}

		if (closeCars.size() > 0)
			return closeCars;
		else
			return null;

	}

	static float getDistance(float x1, float y1, float x2, float y2) {
		float sqrX = (float) Math.pow(x1 - x2, 2);
		float sqrY =  (float) Math.pow(y1 - y2, 2);
		return (float) Math.sqrt(sqrX + sqrY);
	}

	void closestCarToIntersection(ArrayList<CloseCar> closeNeighbors) {

		for (CloseCar closeCar : closeNeighbors) {

			Point neighborIntersection;
			float distanceFromOwnIntersecion;

			neighborIntersection = getIntersection(closeCar.getX(),
					closeCar.getY(), closeCar.getDirection());

			{
				distanceFromOwnIntersecion = getDistance(
						neighborIntersection.getX(),
						neighborIntersection.getY(), closeCar.getX(),
						closeCar.getY());

				msg = myUpdateHandler
						.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_DISTANCE);
				Bundle bundle = new Bundle();
				bundle.putFloat("otherDistanceToIntersection",
						distanceFromOwnIntersecion);
				msg.setData(bundle);

				if (distanceFromOwnIntersecion < closestCarToIntersection
						.getDistance()) {
					closestCarToIntersection
							.setIPAdress(closeCar.getIPAdress());
					closestCarToIntersection
							.setDistance(distanceFromOwnIntersecion);
				}
				Log.i(TAG, "Car with IP " + closeCar.getIPAdress()
						+ " has a distance to his intersectionof value: "
						+ distanceFromOwnIntersecion);
				Log.i(TAG,
						"My IP "
								+ application.IPAddress
								+ " has a distance to my intersectionof value: "
								+ getDistance(
										application.getCurrentPositionX(),
										application.getCurrentPositionY(),
										application.intersection.getX(),
										application.intersection.getY()));
			}
		}

	}

	boolean isConflictingIntersection(ArrayList<CloseCar> closeNeighbors) {

		Point neighborIntersection;
		float distance;

		if (closeNeighbors == null)
			return false;

		for (BeaconPacket beaconPacket : closeNeighbors) {

			neighborIntersection = getIntersection(beaconPacket.getX(),
					beaconPacket.getY(), beaconPacket.getDirection());

			if (neighborIntersection != null
					&& isConflictingDirection(beaconPacket.getDirection(),
							application.direction)) {
				distance = getDistance(neighborIntersection.getX(),
						neighborIntersection.getY(),
						application.intersection.getX(),
						application.intersection.getY());

				if (distance < VTLApplication.DISTANCE_SAME_INTERSECTION)
					return true;
			}
		}

		return false;

	}

	public Point getIntersection(int x, int y, Direction direction) {
		int i = (int) VTLApplication.getIfromY(y);
		int j = x;
		boolean found = false;

		switch (direction) {
		case S:
			/*
			 * if (j - 1 < 0) break;
			 */
			while (i < VTLApplication.SIZEY) {

				// verifies intersection to right
				if (j - 1 >= 0 && VTLApplication.ROAD_MATRIX[i][j - 1]) {
					found = true;
					break;
				}

				// verifies intersection to left
				if ((i - 1 >= 0 && j + 2 < VTLApplication.SIZEX && VTLApplication.ROAD_MATRIX[i - 1][j + 2])
						&& (j + 2 < VTLApplication.SIZEX && VTLApplication.ROAD_MATRIX[i][j + 2])) {
					found = true;
					break;
				}

				i++;
			}

			// Log.i(TAG, "i: " + i + " j:" + j);
			break;
		case N:
			/*
			 * if (j + 1 > VTLApplication.SIZEX - 1) break;
			 */
			while (i >= 0) {

				// verifies intersection to right
				if (j + 1 < VTLApplication.SIZEX
						&& VTLApplication.ROAD_MATRIX[i][j + 1])

				{
					found = true;
					break;
				}

				// verifies intersection to left
				if ((i + 1 < VTLApplication.SIZEY && j - 2 >= 0 && VTLApplication.ROAD_MATRIX[i + 1][j - 2])
						&& (j - 2 >= 0 && VTLApplication.ROAD_MATRIX[i][j - 2])) {

					found = true;

					break;
				}
				i--;
			}
			break;
		case W:

			/*
			 * if (i - 1 < 0) break;
			 */
			while (j >= 0) {

				// verifies intersection to right
				if (i - 1 >= 0 && VTLApplication.ROAD_MATRIX[i - 1][j]) {
					found = true;
					break;
				}

				// verifies intersection to left
				if ((i + 2 < VTLApplication.SIZEY
						&& j + 1 < VTLApplication.SIZEX && VTLApplication.ROAD_MATRIX[i + 2][j + 1])
						&& (i + 2 < VTLApplication.SIZEY && VTLApplication.ROAD_MATRIX[i + 2][j])) {
					found = true;
					break;
				}

				j--;
			}
			break;
		case E:

			/*
			 * if (i + 1 > VTLApplication.SIZEY + 1) break;
			 */
			while (j < VTLApplication.SIZEX) {

				// verifies intersection to right

				if (i + 1 < VTLApplication.SIZEY
						&& VTLApplication.ROAD_MATRIX[i + 1][j]) {
					found = true;
					break;
				}

				// verifies intersection to left

				if ((i - 2 >= 0 && j - 1 >= 0 && VTLApplication.ROAD_MATRIX[i - 2][j - 1])
						&& (i - 2 >= 0 && VTLApplication.ROAD_MATRIX[i - 2][j])) {
					found = true;
					break;
				}

				j++;

			}
			break;
		}// switch ends

		if (found)
			return new Point(j, VTLApplication.getYfromI(i));
		else
			return null;

	}

	public void start() {
		runFlagConflictDetection = true;
		conflictDetectionThread = new ConflictDetectionThread();

		conflictDetectionThread.start();

	}

	public void stop() {
		conflictDetectionThread = null;

		runFlagConflictDetection = false;

	}

	public boolean isConflictingDirection(Direction direction1,
			Direction direction2) {

		if (direction1.compareTo(direction2) == 0)
			return false;
		if (direction1.compareTo(Direction.N) == 0
				&& direction2.compareTo(Direction.S) == 0)
			return false;

		if (direction1.compareTo(Direction.S) == 0
				&& direction2.compareTo(Direction.N) == 0)
			return false;

		if (direction1.compareTo(Direction.W) == 0
				&& direction2.compareTo(Direction.E) == 0)
			return false;

		if (direction1.compareTo(Direction.E) == 0
				&& direction2.compareTo(Direction.W) == 0)
			return false;

		return true;

	}

	private void processRXPacket(String stringMsg, String rxIPAdress) {
		stringMsg = (new StringBuilder(stringMsg).append(",")
				.append(rxIPAdress)).toString();

		// Log.i(VTLActivity.TAG, "got Mms"+stringMsg);
		Message msg = myUpdateHandler
				.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);

		myUpdateHandler.sendMessage(msg);

	}

	private class ListenerBroadCastThread extends Thread {

		DatagramSocket socket = null;
		DatagramPacket inPacket = null;
		byte[] inBuf = new byte[256];

		public ListenerBroadCastThread() {

		}

		public void run() {
			Log.i(VTLActivity.TAG, "Begin Listener Thread");
			// Message m = new Message();
			// m.what = VTLActivity.MSG_ID;
			try {

				socket = new DatagramSocket(VTLApplication.PORT_2);

				while (runFlagListener) {
					inPacket = new DatagramPacket(inBuf, inBuf.length);
					socket.receive(inPacket);
					String rxIPAdress = inPacket.getAddress().toString()
							.substring(1);
					if (application.IPAddress.equals(rxIPAdress))
						continue;

					String stringMsg = new String(inBuf, 0,
							inPacket.getLength());
					processRXPacket(stringMsg, rxIPAdress);

					try {
						Thread.sleep(VTLApplication.SLEEPTIME_RECEIVE);
					} catch (InterruptedException ie) {
						Log.i(VTLActivity.TAG, ie.getMessage());
						// Thread.currentThread().interrupt();

					}

				}

				inPacket = null;
				socket = null;
				inBuf = new byte[256];
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage());
				runFlagListener = false;
				// Thread.currentThread().interrupt();
			}

		}
	}
}
