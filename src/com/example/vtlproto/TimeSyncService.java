package com.example.vtlproto;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class TimeSyncService {

	public static final String MULTICASTADDRESS = "224.2.2.3";

	public static final String TAG = TimeSyncService.class.getSimpleName();

	private boolean runFlagBeacon = false;

	private VTLApplication application;

	SyncThread syncThread;

	/*
	 * Handler myUpdateHandler = new Handler() { public void
	 * handleMessage(Message msg) { switch (msg.what) { case MSG_ID: TextView tv
	 * = (TextView) findViewById(R.id.helloText); Toast.makeText(context,
	 * "i got"+msg, Toast.LENGTH_SHORT).show(); tv.setText(msg);
	 * 
	 * break; default: break; } super.handleMessage(msg); } };
	 */

	public TimeSyncService(Context context) {
		Log.i(TAG, "Service: OnCreate");
		application = (VTLApplication) context.getApplicationContext();

		syncThread = new SyncThread();
		runFlagBeacon = true;
		syncThread.start();
		
	}





	private class SyncThread extends Thread {


		public SyncThread() {

		}

		public void run() {

			// Keep listening to the InputStream while connected
			 SntpClient client = new SntpClient();
		
			try {
		

				while (runFlagBeacon) {
					  if (client.requestTime("time.windows.com",1000*10)) {
						  application.timeDifference = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference() - System.currentTimeMillis();
						  Log.i(TAG, "got time difference: "+application.timeDifference);

					  }
					  else
						  Log.e(TAG, "coudltn get");
					
						Thread.sleep(VTLApplication.SLEEPTIME_TIMESYNC);
				}

				/*
				 * socket = null; outPacket = null; outBuf = null;
				 */
			}  catch (InterruptedException ie) {
				// Log.i(TAG, ie.getMessage());
				Log.e(TAG,
						"Interrupted SenderThread and also closes its socket");
				runFlagBeacon = false;
				Thread.currentThread().interrupt();

			}
		}
		/*
		 * public void cancel() { try { socket.close(); } catch (IOException e)
		 * { Log.e(MainActivity.TAG, "close() of connect socket failed", e); } }
		 */

	}

	

}
