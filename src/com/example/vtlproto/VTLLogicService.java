package com.example.vtlproto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.example.vtlproto.model.BeaconPacket;
import com.example.vtlproto.model.CloseCar;
import com.example.vtlproto.model.NameValue;
import com.example.vtlproto.model.Point;
import com.example.vtlproto.model.TrafficLightPacket;
import com.example.vtlproto.model.map.Edge;
import com.example.vtlproto.model.map.Junction;
import com.example.vtlproto.model.map.Lane;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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

					if (application.junctionId != null) {

						ArrayList<CloseCar> closeCars = closeCars();

						application.clusterLeader = getClusterLeader(closeCars);

						if (application.junctionId != null
								&& isConflictingIntersection(closeCars)) {

							/* conflict code begins here */
							//application.conflictDetected = true;
							// application.trafficLightColor =
							// VTLApplication.ORANGE;

							closestCarToIntersection
									.setDistance(HelperFunctions.getDistance(
											application.getCurrentPositionX(),
											application.getCurrentPositionY(),
											application.junctionPoint.getX(),
											application.junctionPoint.getY()));
							Log.i(TAG,
									"me:"
											+ closestCarToIntersection
													.getDistance());
							Log.i(TAG,
									"my xing x:"
											+ application.junctionPoint.getX()
											+ "y:"
											+ application.junctionPoint.getY());
							Log.i(TAG,
									"my car x:"
											+ application.getCurrentPositionX()
											+ "y:"
											+ application.getCurrentPositionY());
							closestCarToIntersection
									.setIPAdress(application.IPAddress);

							/*sets closestCarToIntersection*/
							getVTLLeader(closeCars);

							Message msg = myUpdateHandler
									.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED);

							myUpdateHandler.sendMessage(msg);

							/*
							 * Log.i(TAG, "The furthestCarFromIntersection is: "
							 * + furthestCarFromIntersection.getIPAdress());
							 */

							

							if (closestCarToIntersection.getIPAdress().equals(
									application.IPAddress))
								application.amIleader = true;

							Log.i(TAG, "closest car to intersection:"
									+ closestCarToIntersection.getIPAdress());

							msg = myUpdateHandler
									.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_VTL_LEADER);
							Bundle bundle = new Bundle();
							bundle.putString("closestCarToIntersection",
									closestCarToIntersection.getIPAdress());
							msg.setData(bundle);
							myUpdateHandler.sendMessage(msg);
							
							/* code for the leader */
							if (application.amIleader) {

								Log.i(TAG,
										"I am the leader, so i send a broadcast packet");

								VTLLeaderPacketSender();

								application.trafficLightColor = Color.RED;
								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);

								myUpdateHandler.sendMessage(msg);
								VTLStatusSender(Color.RED);
								// Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								Log.i(TAG,
										"I am the leader, so i send a broadcast packet");

								application.trafficLightColor = Color.GREEN;
								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);

								myUpdateHandler.sendMessage(msg);
								VTLStatusSender(Color.GREEN);

								application.trafficLightColor = Color.WHITE;
								msg = myUpdateHandler
										.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								myUpdateHandler.sendMessage(msg);
								//application.conflictDetected = false;
								// Thread.sleep(VTLApplication.SLEEPTIME_TRAFFIC_LIGHT);

								/* code for leader ends here */
								application.amIleader = false;
								/* code for others */
							} else {

								Log.i(TAG,
										"I am not the leader, so i am waiting for the leader");

								/*
								 * application.trafficLightColor =
								 * VTLApplication.ORANGE;
								 * 
								 * msg = myUpdateHandler
								 * .obtainMessage(VTLApplication
								 * .VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								 * bundle = new Bundle(); msg.setData(bundle);
								 */

								while (application.timeLeftForCurrentStatus > 0) {

									
									
									
									Thread.sleep(VTLApplication.SLEEPTIME_VTLSTATUS);
									application.timeLeftForCurrentStatus--;
								}
								/*
								 * while (!application.didIgetLeaderPacket) {
								 * 
								 * Thread.sleep(VTLApplication.
								 * SLEEPTIME_CONFLICTDETECTION);
								 * 
								 * }
								 */

								/*
								 * application.trafficLightColor =
								 * VTLApplication.ORANGE;
								 * 
								 * msg = myUpdateHandler
								 * .obtainMessage(VTLApplication
								 * .VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
								 * bundle = new Bundle();
								 * bundle.putString("closestCarToIntersection",
								 * closestCarToIntersection.getIPAdress());
								 * msg.setData(bundle);
								 * 
								 * myUpdateHandler.sendMessage(msg);
								 * 
								 * while (application.trafficLightColor ==
								 * VTLApplication.ORANGE) {
								 * Thread.sleep(VTLApplication
								 * .SLEEPTIME_CONFLICTDETECTION); }
								 * 
								 * if (application.trafficLightColor ==
								 * Color.RED) {
								 * 
								 * while (application.trafficLightColor ==
								 * Color.RED) { Thread.sleep(VTLApplication.
								 * SLEEPTIME_CONFLICTDETECTION); }
								 * 
								 * }
								 */
								/*
								 * application.waitingForLeaderMessage = true;
								 * 
								 * 
								 * while (application.waitingForLeaderMessage) {
								 * Log.d(TAG, "looping");
								 * 
								 * Thread.sleep(VTLApplication.
								 * SLEEPTIME_TRAFFIC_LIGHT);
								 * 
								 * }
								 */

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

								/*
								 * while
								 * (application.trafficLightColor!=Color.GREEN)
								 * {}
								 * 
								 * 
								 * 
								 * 
								 * if (application.trafficLightColor ==
								 * Color.RED) { // wait until i get a green
								 * application.waitingForLeaderMessage = true;
								 * while (application.waitingForLeaderMessage) {
								 * }
								 * 
								 * Thread.sleep(VTLApplication.
								 * SLEEPTIME_TRAFFIC_LIGHT);
								 * 
								 * }
								 */

								/*
								 * did i pass internsection,, if yes, i should
								 * get out of here, if not, wait for next
								 * message
								 */

							} /* code for others ends here */

						} /* conflict code ends here */
						else {
							Message msg = myUpdateHandler
									.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED);
							myUpdateHandler.sendMessage(msg);
							application.trafficLightColor = Color.WHITE;
							msg = myUpdateHandler
									.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
							myUpdateHandler.sendMessage(msg);
						}
					} else {
						Message msg = myUpdateHandler
								.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED);
						myUpdateHandler.sendMessage(msg);
						application.trafficLightColor = Color.WHITE;
						msg = myUpdateHandler
								.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS);
						myUpdateHandler.sendMessage(msg);
					}

					Thread.sleep(VTLApplication.SLEEPTIME_CONFLICTDETECTION);

				}
			}

			catch (InterruptedException ie) {
				Log.i(TAG, ie.getMessage());
				runFlagConflictDetection = false;

				Thread.currentThread().interrupt();
			}

			catch (Exception e) {
				Log.i(TAG, e.getMessage());
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

			double result = HelperFunctions.getDistance(value.getX(),
					value.getY(), application.getCurrentPositionX(),
					application.getCurrentPositionY());

			/*
			 * Log.i(TAG, "Car with IP " + value.getIPAdress() +
			 * " has a distance to me of " + result);
			 */
			// TODO result < VTLApplication.DISTANCE_CLOSE
			if (true) {
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

	void getVTLLeader(ArrayList<CloseCar> closeNeighbors) {

		for (CloseCar closeCar : closeNeighbors) {

			double neighbordistanceFromIntersecion;

			{
				neighbordistanceFromIntersecion = HelperFunctions.getDistance(
						application.junctionPoint.getX(),
						application.junctionPoint.getY(), closeCar.getX(),
						closeCar.getY());
				Log.i(TAG, "xing x:" + application.junctionPoint.getX() + "y:"
						+ application.junctionPoint.getY());
				Log.i(TAG,
						"neigh car x:" + closeCar.getX() + "y:"
								+ closeCar.getY());
				Log.i(TAG, "neigh distance:" + neighbordistanceFromIntersecion);

				msg = myUpdateHandler
						.obtainMessage(VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_DISTANCE);
				Bundle bundle = new Bundle();
				bundle.putDouble("otherDistanceToIntersection",
						neighbordistanceFromIntersecion);
				msg.setData(bundle);

				if (neighbordistanceFromIntersecion < closestCarToIntersection
						.getDistance()) {
					closestCarToIntersection
							.setIPAdress(closeCar.getIPAdress());
					closestCarToIntersection
							.setDistance(neighbordistanceFromIntersecion);

				}

				/*
				 * in case there is a tie in distances, i choose the one with
				 * the lowest ip value
				 */
				if (neighbordistanceFromIntersecion == closestCarToIntersection
						.getDistance()) {

					long closeCarIP = Long.valueOf(closeCar.getIPAdress()
							.replace(".", ""));
					long closestCarToIntersectionIP = Long
							.valueOf(closestCarToIntersection.getIPAdress()
									.replace(".", ""));
					if (closeCarIP < closestCarToIntersectionIP)

					{
						closestCarToIntersection.setIPAdress(closeCar
								.getIPAdress());
						closestCarToIntersection
								.setDistance(neighbordistanceFromIntersecion);

					}

				}

				/*
				 * Log.i(TAG, "Car with IP " + closeCar.getIPAdress() +
				 * " has a distance to his intersectionof value: " +
				 * neighbordistanceFromIntersecion); Log.i(TAG, "My IP " +
				 * application.IPAddress +
				 * " has a distance to my intersectionof value: " +
				 * HelperFunctions.getDistance(
				 * application.getCurrentPositionX(),
				 * application.getCurrentPositionY(),
				 * application.junctionPoint.getX(),
				 * application.junctionPoint.getY()));
				 */
			}
		}

	}

	CloseCar getClusterLeader(ArrayList<CloseCar> closeNeighbors) {

		if (application.junctionPoint != null && closeNeighbors != null
				&& closeNeighbors.size() > 0) {
			CloseCar clusterLeader = new CloseCar();
			clusterLeader.setDistance(HelperFunctions.getDistance(
					application.getCurrentPositionX(),
					application.getCurrentPositionY(),
					application.junctionPoint.getX(),
					application.junctionPoint.getY()));

			clusterLeader.setIPAdress(application.IPAddress);

			for (CloseCar closeCar : closeNeighbors) {

				if (closeCar.getLaneId().equals(application.laneId)) {
					double neighbordistanceFromIntersecion;

					{
						neighbordistanceFromIntersecion = HelperFunctions
								.getDistance(application.junctionPoint.getX(),
										application.junctionPoint.getY(),
										closeCar.getX(), closeCar.getY());

						if (neighbordistanceFromIntersecion < clusterLeader
								.getDistance()) {
							clusterLeader.setIPAdress(closeCar.getIPAdress());
							clusterLeader
									.setDistance(neighbordistanceFromIntersecion);

						}

						/*
						 * in case there is a tie in distances, i choose the one
						 * with the lowest ip value
						 */
						if (neighbordistanceFromIntersecion == clusterLeader
								.getDistance()) {

							long closeCarIP = Long.valueOf(closeCar
									.getIPAdress().replace(".", ""));
							long closestCarToIntersectionIP = Long
									.valueOf(clusterLeader.getIPAdress()
											.replace(".", ""));
							if (closeCarIP < closestCarToIntersectionIP)

							{
								clusterLeader.setIPAdress(closeCar
										.getIPAdress());
								clusterLeader
										.setDistance(neighbordistanceFromIntersecion);

							}

						}

						Log.i(TAG,
								"Car with IP "
										+ closeCar.getIPAdress()
										+ " has a distance to his intersectionof value: "
										+ neighbordistanceFromIntersecion);
						Log.i(TAG,
								"My IP "
										+ application.IPAddress
										+ " has a distance to my intersectionof value: "
										+ HelperFunctions.getDistance(
												application
														.getCurrentPositionX(),
												application
														.getCurrentPositionY(),
												application.junctionPoint
														.getX(),
												application.junctionPoint
														.getY()));
					}
				}
			}

			myUpdateHandler.obtainMessage(
					VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_CLUSTER_LEADER,
					clusterLeader.getIPAdress()).sendToTarget();

			return clusterLeader;
		} else
			return null;
	}

	boolean isConflictingIntersection(ArrayList<CloseCar> closeNeighbors) {

		String neighborIntersection;

		if (closeNeighbors == null)
			return false;

		for (BeaconPacket beaconPacket : closeNeighbors) {

			neighborIntersection = HelperFunctions
					.findJunctionByLaneId(application.map,beaconPacket.getLaneId());

			if (neighborIntersection != null
					&& isConflictingDirection(beaconPacket.getDirectionAngle(),
							application.directionAngle)) {

				if (neighborIntersection.equals(application.junctionId))
					return true;
			}
		}

		return false;

	}

	public boolean isConflictingDirection(float angle1, float angle2) {
		float difference = Math.abs(angle1 - angle2);
		if (difference == 0 || difference == 180)
			return false;
		
		if (0<=difference && difference<45 || 135<  difference &&  difference<= 225 || 315<difference && difference<=360)
			return false;


		return true;

	}

	public ArrayList<NameValue> getConflictLanes(String laneId, float angle) {

		ArrayList<NameValue> conflictLanes = new ArrayList<NameValue>();

		for (Junction junction : application.map.getJunctions()) {
			if (junction.getId().equals(laneId)) {

				for (String conflictingLane : junction.getInLanes()) {

					NameValue nameValue = new NameValue();
					nameValue.setName(conflictingLane);
					conflictLanes.add(nameValue);

				}
			
			}

		}

		for (int i = 0; i < conflictLanes.size(); i++)

		{
			NameValue nameValue = conflictLanes.get(i);
			boolean found = false;
			float angleToCompare = 0;
			for (Edge edge : application.map.getEdges()) {

				for (Lane lane : edge.getLanes()) {

					if (lane.getId().equals(nameValue.getName())) {

						found = true;
						angleToCompare = edge.getAngle();
						if (isConflictingDirection(angleToCompare, angle))
							nameValue.setValue(true);
						else
							nameValue.setValue(false);
						conflictLanes.set(i, nameValue);
					}

					if (found)
						break;

				}

				if (found)
					break;

			}

		}

		return conflictLanes;

	}

	public void VTLLeaderPacketSender() {
		Log.i(TAG, "Begin VTLLeaderPacketSEnder method");
		DatagramSocket socket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;
		int cont = 3;
		// Keep listening to the InputStream while connected

		try {
			/* send this packet 3 times */
			while (cont > 0) {
				socket = new DatagramSocket();
				StringBuilder rawPacket;

				rawPacket = new StringBuilder(
						String.valueOf(VTLApplication.MSG_TYPE_LEADER_REQ))
						.append(VTLApplication.MSG_SEPARATOR).append(
								application.getTimeAndDate());

				outBuf = rawPacket.toString().getBytes();

				// Send to multicast IP address and port
				InetAddress address = InetAddress
						.getByName(application.isBroadCastTX ? VTLApplication.BROADCASTADDRESS
								: VTLApplication.MULTICASTADDRESS);
				outPacket = new DatagramPacket(outBuf, outBuf.length, address,
						VTLApplication.PORT);
				socket.setBroadcast(application.isBroadCastTX);
				socket.send(outPacket);
				cont--;
				// System.out.println("Server sends : " + msg);

				/*
				 * socket = null; outPacket = null; outBuf = null;
				 */}
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage());

		}

		catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
			socket.close();
		}
	}

	public void VTLStatusSender(int leaderColor) {
		Log.i(TAG, "Begin VTLSatusSender method");
		DatagramSocket socket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;
		// Keep listening to the InputStream while connected

		try {

			socket = new DatagramSocket();
			int timer = 10;
			StringBuilder rawPacket;

			while (timer > 0) {

				rawPacket = new StringBuilder(
						String.valueOf(VTLApplication.MSG_TYPE_LIGHT_STATUS))
						.append(VTLApplication.MSG_SEPARATOR)
						.append(application.getTimeAndDate())
						.append(VTLApplication.MSG_SEPARATOR).append(timer);
				for (NameValue isConflictingLane : getConflictLanes(
						application.junctionId, application.directionAngle)) {

					if (leaderColor == Color.RED)

					{
						rawPacket
								.append(VTLApplication.MSG_SEPARATOR)
								.append(isConflictingLane.getBooleanValue() ? VTLApplication.MSG_LIGHT_STATUS_GREEN
										: VTLApplication.MSG_LIGHT_STATUS_RED)

								.append(VTLApplication.MSG_SEPARATOR)

								.append(isConflictingLane.getName());
					}

					if (leaderColor == Color.GREEN)

					{
						rawPacket
								.append(VTLApplication.MSG_SEPARATOR)
								.append(isConflictingLane.getBooleanValue() ? VTLApplication.MSG_LIGHT_STATUS_RED
										: VTLApplication.MSG_LIGHT_STATUS_GREEN)

								.append(VTLApplication.MSG_SEPARATOR)

								.append(isConflictingLane.getName());
					}

				}

				outBuf = rawPacket.toString().getBytes();

				// Send to multicast IP address and port
				InetAddress address = InetAddress
						.getByName(application.isBroadCastTX ? VTLApplication.BROADCASTADDRESS
								: VTLApplication.MULTICASTADDRESS);
				outPacket = new DatagramPacket(outBuf, outBuf.length, address,
						VTLApplication.PORT);
				socket.setBroadcast(application.isBroadCastTX);
				socket.send(outPacket);

				timer--;
				Thread.sleep(VTLApplication.SLEEPTIME_VTLSTATUS);

				// System.out.println("Server sends : " + msg);

			}

			/*
			 * socket = null; outPacket = null; outBuf = null;
			 */
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage());

		}

		catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
			socket.close();
			Thread.currentThread().interrupt();
		} catch (InterruptedException ie) {
			// Log.i(TAG, ie.getMessage());
			Log.e(TAG,
					"Interrupted VTLStatusSenderThread and also closes its socket");
			socket.close();
			Thread.currentThread().interrupt();

		}
	}

	/*
	 * public void cancel() { try { socket.close(); } catch (IOException e) {
	 * Log.e(MainActivity.TAG, "close() of connect socket failed", e); } }
	 */

	public void start() {
		runFlagConflictDetection = true;
		conflictDetectionThread = new ConflictDetectionThread();

		conflictDetectionThread.start();

	}

	public void stop() {
		conflictDetectionThread = null;

		runFlagConflictDetection = false;

	}

}
