package com.example.vtlproto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class MockLocationProvider extends Thread{

	public final static String TAG=MockLocationProvider.class.getSimpleName();
	private String mocLocationProvider;
	private List<String> data;
	private LocationManager locationManager;
	private VTLApplication application;
	
	public MockLocationProvider(LocationManager locationManager,
	        String mocLocationProvider, String filename, Context context) {

	    this.locationManager = locationManager;
	    this.mocLocationProvider = mocLocationProvider;
		application = (VTLApplication) context.getApplicationContext();

	    
	    
	    
	    try {

	         data = new ArrayList<String>();
	        InputStream is = context.getAssets().open(filename);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        String line = null;
	        while ((line = reader.readLine()) != null) {

	            data.add(line);
	        }
	        Log.i(TAG, data.size() + " lines");


	    } catch (IOException e) {
	    	  Log.e(TAG,e.getMessage());
	    }
	    
	    
	}

	@Override
	public void run() {

	    for (String str : data) {

	    	try {

	            Thread.sleep(1000);

	        } catch (InterruptedException e) {

	            e.printStackTrace();
	        }
	    	while (application.trafficLightColor==Color.RED){
	    	try {

	            Thread.sleep(2000);

	        } catch (InterruptedException e) {

	            e.printStackTrace();
	        }
	    	}
	        // Set one position
	        String[] parts = str.split(",");
	        Double latitude = Double.valueOf(parts[0]);
	        Double longitude = Double.valueOf(parts[1]);
	      //  Double altitude = Double.valueOf(parts[2]);
	        Location location = new Location(mocLocationProvider);
	        location.setLatitude(latitude);
	        location.setLongitude(longitude);
	       // location.setAltitude(altitude);

	        Log.e(TAG, location.toString());

	        // set the time in the location. If the time on this location
	        // matches the time on the one in the previous set call, it will be
	        // ignored
	        location.setTime(System.currentTimeMillis());

	        locationManager.setTestProviderLocation(mocLocationProvider,
	                location);
	    }
	}
	
}
