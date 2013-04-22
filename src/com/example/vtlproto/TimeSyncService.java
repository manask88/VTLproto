package com.example.vtlproto;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class TimeSyncService {


	public static final String TAG = TimeSyncService.class.getSimpleName();

	private boolean runFlagBeacon = false;

	private VTLApplication application;

	SyncThread syncThread;

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

			 SntpClient client = new SntpClient();
		
			try {
		
				while (runFlagBeacon) {
					  if (client.requestTime(VTLApplication.NTP_SERVER,VTLApplication.NTP_TIME_OUT)) {
						  application.timeDifference = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference() - System.currentTimeMillis();
						  Log.i(TAG, "got time difference: "+application.timeDifference);

					  }
					  else
						  Log.e(TAG, "coudltn get");
					
						Thread.sleep(VTLApplication.SLEEPTIME_TIMESYNC);
				}

		
			}  catch (InterruptedException ie) {
				 Log.e(TAG, ie.getMessage());
				Log.e(TAG,
						"Interrupted TimeSync Thread ");
				runFlagBeacon = false;
				Thread.currentThread().interrupt();

			}
		}
	

	}

	

}
