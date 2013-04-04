package com.example.vtlproto;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BeaconService {

	public static final String MULTICASTADDRESS = "224.2.2.3";

	public static final String TAG = BeaconService.class.getSimpleName();

	String msg;
	private boolean runFlagListener = false;
	private boolean runFlagBeacon = false;

	private final Handler myUpdateHandler;
	private VTLApplication application;
	ListenerMulticastThread listenerMulticastThread;
	ListenerBroadCastThread listenerBroadCastThread;
	SenderThread senderThread;

	/*
	 * Handler myUpdateHandler = new Handler() { public void
	 * handleMessage(Message msg) { switch (msg.what) { case MSG_ID: TextView tv
	 * = (TextView) findViewById(R.id.helloText); Toast.makeText(context,
	 * "i got"+msg, Toast.LENGTH_SHORT).show(); tv.setText(msg);
	 * 
	 * break; default: break; } super.handleMessage(msg); } };
	 */

	public BeaconService(Context context, Handler handler) {
		Log.i(TAG, "Service: OnCreate");
		senderThread = new SenderThread();
		listenerMulticastThread = new ListenerMulticastThread();

		listenerBroadCastThread = new ListenerBroadCastThread();

		application = (VTLApplication) context.getApplicationContext();

		myUpdateHandler = handler;
	}

	private class ListenerBroadCastThread extends Thread {

		DatagramSocket socket = null;
		DatagramPacket inPacket = null;
		byte[] inBuf = new byte[256];

		public ListenerBroadCastThread() {

		}

		public void run() {
			Log.i(TAG, "Begin Listener Thread");
			// Message m = new Message();
			// m.what = VTLActivity.MSG_ID;
			try {
				// Prepare to join multicast group
				socket = new DatagramSocket(VTLApplication.PORT);

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

					Thread.sleep(VTLApplication.SLEEPTIME_RECEIVE);

				}

				/*
				 * inPacket = null; socket = null; inBuf = new byte[256];
				 */
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage());
				Thread.currentThread().interrupt();
				runFlagListener = false;
				//socket.close();
			} catch (InterruptedException ie) {
				// Log.i(VTLActivity.TAG, ie.getMessage());
				Thread.currentThread().interrupt();
				runFlagListener = false;
				socket.close();
				Log.e(TAG,
						"Interrupted ListenerMulticastThread and also closes its socket");

			}
		}
	}

	private void processRXPacket(String stringMsg, String rxIPAdress) {
		stringMsg = (new StringBuilder(stringMsg).append(",")
				.append(rxIPAdress)).toString();

		
		//Log.i(TAG, "Listener Thread got:  "+ stringMsg);
		Message msg = myUpdateHandler
				.obtainMessage(VTLApplication.BEACONSERVICE_HANDLER_RX_TEXT);
		Bundle bundle = new Bundle();
		bundle.putString("Message", stringMsg);
		msg.setData(bundle);
		myUpdateHandler.sendMessage(msg);

	}

	private class ListenerMulticastThread extends Thread {

		MulticastSocket socket = null;
		DatagramPacket inPacket = null;
		byte[] inBuf = new byte[256];

		public ListenerMulticastThread() {

		}

		public void run() {
			Log.i(TAG, "Begin Listener Thread");
			// Message m = new Message();
			// m.what = VTLActivity.MSG_ID;
			try {
				// Prepare to join multicast group
				socket = new MulticastSocket(VTLApplication.PORT);
				InetAddress address = InetAddress.getByName(MULTICASTADDRESS);
				socket.joinGroup(address);

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

					Thread.sleep(VTLApplication.SLEEPTIME_RECEIVE);

					/*
					 * inPacket = null; socket = null; inBuf = new byte[256];
					 */
				}
			} catch (IOException ioe) {
				System.out.println(ioe);
				Log.e(TAG, ioe.getMessage());
				runFlagListener = false;
				socket.close();
				Thread.currentThread().interrupt();
			} catch (InterruptedException ie) {
				// Log.i(VTLActivity.TAG, ie.getMessage());
				Thread.currentThread().interrupt();
				runFlagListener = false;
				socket.close();
				Log.e(TAG,
						"Interrupted ListenerMulticastThread and also closes its socket");
			}
		}
	}

	private class SenderThread extends Thread {

		DatagramSocket socket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;

		public SenderThread() {

		}

		public void run() {
			Log.i(TAG, "Begin Beacon Thread");

			// Keep listening to the InputStream while connected

			try {
				socket = new DatagramSocket();
				String msg;

				while (runFlagBeacon) {

					msg = (new StringBuilder(String.valueOf(VTLApplication.MSG_TYPE_BEACON))
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.time.format("%k:%M:%S")
									.toString())
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.getCurrentPositionX())
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.getCurrentPositionY()))
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.directionAngle)
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.laneId)
							.append(VTLApplication.MSG_SEPARATOR)
							.append(application.amIleader?1:0)

							.toString();

					outBuf = msg.getBytes();

					// Send to multicast IP address and port
					InetAddress address = InetAddress
							.getByName(application.isBroadCastTX ? VTLApplication.BROADCASTADDRESS
									: MULTICASTADDRESS);
					outPacket = new DatagramPacket(outBuf, outBuf.length,
							address, VTLApplication.PORT);
					socket.setBroadcast(application.isBroadCastTX);
					socket.send(outPacket);
					Thread.sleep(VTLApplication.SLEEPTIME_SEND);

					// System.out.println("Server sends : " + msg);

				}

				/*
				 * socket = null; outPacket = null; outBuf = null;
				 */
			} catch (IOException ioe) {
				Log.e(TAG, ioe.getMessage());
				runFlagBeacon = false;
				socket.close();
				Thread.currentThread().interrupt();
			} catch (InterruptedException ie) {
				// Log.i(TAG, ie.getMessage());
				Log.e(TAG,
						"Interrupted SenderThread and also closes its socket");
				runFlagBeacon = false;
				socket.close();
				Thread.currentThread().interrupt();

			}
		}
		/*
		 * public void cancel() { try { socket.close(); } catch (IOException e)
		 * { Log.e(MainActivity.TAG, "close() of connect socket failed", e); } }
		 */

	}

	public void start() {
		if (!application.beaconServiceStatus) {
			application.beaconServiceStatus=true;
			Log.d(TAG, "trying to start  BeaconServiceThreads");
			runFlagListener = true;
			runFlagBeacon = true;

			if (application.isBroadCastTX) {
				listenerBroadCastThread = new ListenerBroadCastThread();
				listenerBroadCastThread.start();
			} else {
				listenerMulticastThread = new ListenerMulticastThread();
				listenerMulticastThread.start();
			}

			senderThread = new SenderThread();
			senderThread.start();
			
		} else {
			Log.e(TAG,
					"trying to start  BeaconServiceThreads, which has already been started");

		}
	}

	public void stop() {
		if (application.beaconServiceStatus) {
			Log.d(TAG, "trying to stop  BeaconServiceThreads");
			application.beaconServiceStatus = false;
			if (application.isBroadCastTX) {
				listenerBroadCastThread.interrupt();
				listenerBroadCastThread = null;

			} else {
				listenerMulticastThread.interrupt();
				listenerMulticastThread = null;

			}

			senderThread.interrupt();
			senderThread = null;
		} else {
			Log.e(TAG,
					"trying to stop BeaconServiceThreads ,but it has already been stopped.");
		}
	}

}
