package com.example.vtlproto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.vtlproto.R;
import com.example.vtlproto.model.BeaconPacket;
import com.example.vtlproto.model.CoordinatePair;
import com.example.vtlproto.model.NameValue;
import com.example.vtlproto.model.Point;
import com.example.vtlproto.model.TrafficLightPacket;
import com.example.vtlproto.model.VTLLeaderPacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class VTLActivity extends Activity implements LocationListener {

	public final static String TAG = VTLActivity.class.getSimpleName();
	public final static int SQUARESIZE = 50;
	public final static int SQUAREMARGIN = 13;
	private Button buttonStartV, buttonStartH, buttonStart,
			trafficLight;
	boolean shouldDraw = false;
	private Context context = this;
	private TextView tvCurrentX, tvCurrentY, tvTime, tvIPAddress, tvLat,
			tvLong, tvAngle;
	private ImageView imageView;
	private int numNeighbors;
	private Canvas canvas;
	private Bitmap bitmap;
	private VTLApplication application;
	long lDateTime;

	BeaconService beaconService;
	Boolean servicesStatus;
	VTLLogicService VTLLogicService;
	public LocationManager locationManager;
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	static final LatLng intersection = new LatLng(40.440444, -79.942161);
	public Marker mylocation, marker1, marker2, marker3, marker4, marker5;

	public float gradeMinSecTograde(float grade, float min, float seconds) {

		float secondsToHours = seconds / 3600;
		float minToHours = min / 60;

		return grade + minToHours + secondsToHours;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		application = (VTLApplication) this.getApplication();

		application.googleMap = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

	
		application.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				intersection, 25));
		UiSettings uiSettings = application.googleMap.getUiSettings();
		// uiSettings.setAllGesturesEnabled(false);
		// uiSettings.setZoomControlsEnabled(false);

		// Zoom in, animating the camera.
		// map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		/*
		 * if (!enabled) { Intent intent = new
		 * Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		 * startActivity(intent); }
		 */
		/*mock coordinates*/
		  locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
		            false, false, true, true, true, 0, 5);
		    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
		    
		    
		 
		
		  
		  buttonStartV = (Button) findViewById(R.id.buttonMochV);
			buttonStartH = (Button) findViewById(R.id.buttonMochH);
			buttonStartH.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

				    new MockLocationProvider(locationManager, LocationManager.GPS_PROVIDER, "horizontal.txt",context).start();

				}
			});
		  
			
			
			
			buttonStartV.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

				    new MockLocationProvider(locationManager, LocationManager.GPS_PROVIDER, "vertical.txt",context).start();

				}
			});
		  
		  
		  
		  
		  
		  /*end mock coordinates*/
		  application.hashMapNeighbors = new HashMap<String, BeaconPacket>(
				VTLApplication.MAX_NEIGHBORS);
		numNeighbors = 0;
		runTimeThread();
		trafficLight = (Button) findViewById(R.id.trafficLight);
		trafficLight.setText("        ");
		trafficLight.setBackgroundColor(application.trafficLightColor);
		tvCurrentX = (TextView) this.findViewById(R.id.tvPositionX);
		tvCurrentX.setText(application.getCurrentPositionX() + "");
		tvCurrentY = (TextView) this.findViewById(R.id.tvPositionY);
		tvCurrentY.setText(application.getCurrentPositionY() + "");
		tvTime = (TextView) this.findViewById(R.id.tvTime);
		tvIPAddress = (TextView) this.findViewById(R.id.tvIPAddress);
		tvIPAddress.setText(application.IPAddress);
		tvLat = (TextView) this.findViewById(R.id.tvLat);
		tvLong = (TextView) this.findViewById(R.id.tvLong);
		tvAngle = (TextView) this.findViewById(R.id.tvAngle);

		
		buttonStart = (Button) findViewById(R.id.buttonStart);
		
		
		

		
		
		
		TimeSyncService timeSync = new TimeSyncService(context);

		beaconService = new BeaconService(context, beconServiceHandler);
		VTLLogicService = new VTLLogicService(context, VTLLogicServiceHandler);

		buttonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (!application.beaconServiceStatus) {
					beaconService.start();
					VTLLogicService.start();
					buttonStart.setText("Stop");
				} else {
					beaconService.stop();
					VTLLogicService.stop();
					buttonStart.setText("Start");
				}

			}
		});

		
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null)
			onLocationChanged(lastLocation);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				100, 1, this);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		application.createAndOpenFile();

	}



	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		/*
		 * use to have this before implementaiton location, but that was gor
		 * logging time
		 */
		// application.closeFile();
		// beaconService.stop();
		// VTLLogicService.stop();
		// application.beaconServiceStatus=false;
		/*
		 * use to have this before implementaiton location, but that was gor
		 * logging time
		 */

		// this.finish();
		// android.os.Process.killProcess(android.os.Process.myPid());
		// System.exit(0);
		// getParent().finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			getParent().finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	Handler beconServiceHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VTLApplication.BEACONSERVICE_HANDLER_RX_TEXT:
				TextView tvReceived = (TextView) findViewById(R.id.tvReceived);
				String text = msg.getData().getString("Message");
				tvReceived.setText(text);

				char type = text.charAt(0);

				switch (type) {
				case VTLApplication.MSG_TYPE_BEACON: {
					BeaconPacket beaconPacket = new BeaconPacket(text);
					String IPAddress = beaconPacket.getIPAdress();
					LatLng latLng = new LatLng(beaconPacket.getY(),
							beaconPacket.getX());

					if (application.hashMapNeighbors.get(IPAddress) == null) {

						Log.i(TAG, "got:" + IPAddress);
						beaconPacket
								.setColor(VTLApplication.COLORS[numNeighbors]);
						Marker marker = application.googleMap
								.addMarker(new MarkerOptions().position(latLng)
										.title(beaconPacket.getIPAdress()));
						beaconPacket.setMarker(marker);
						application.hashMapNeighbors.put(IPAddress,
								beaconPacket);
						numNeighbors++;
					} else {
						

						Marker marker = application.hashMapNeighbors.get(
								IPAddress).getMarker();
						marker.remove();
						marker = application.googleMap
								.addMarker(new MarkerOptions().position(latLng)
										.title(beaconPacket.getIPAdress()));
						beaconPacket.setMarker(marker);
						application.hashMapNeighbors.put(IPAddress,
								beaconPacket);

					}
			

					break;
				}

				case VTLApplication.MSG_TYPE_LEADER_REQ:

				{
					VTLLeaderPacket VTLLeaderPacket = new VTLLeaderPacket(text);
					String IPAddress = VTLLeaderPacket.getIPAdress();

					if (application.clusterLeader != null
							&& application.clusterLeader.getIPAdress().equals(
									application.IPAddress))

					{
						Intent serviceIntent = new Intent(context,
								SendUnicastAckPacketService.class);
						serviceIntent.putExtra(
								SendUnicastAckPacketService.EXTRAS_DST_IP,
								IPAddress);
						context.startService(serviceIntent);
					}
					break;
				}

				case VTLApplication.MSG_TYPE_LIGHT_STATUS: {
	
					{
						TrafficLightPacket trafficLightPacket = new TrafficLightPacket(
								text);

					
						application.timeLeftForCurrentStatus = Integer
								.valueOf(trafficLightPacket.getTimer());
						for (NameValue statusLaneId : trafficLightPacket
								.getStatusLaneIds()) {
							if (statusLaneId.getName().equals(
									application.laneId)) {

								char status = statusLaneId.getCharValue();
								switch (status) {
								case VTLApplication.MSG_LIGHT_STATUS_GREEN: {
									application.trafficLightColor = Color.GREEN;
									trafficLight
											.setBackgroundColor(application.trafficLightColor);
									
									break;
								}
								case VTLApplication.MSG_LIGHT_STATUS_RED: {

									application.trafficLightColor = Color.RED;
									trafficLight
											.setBackgroundColor(application.trafficLightColor);

								
									break;
								}
								}

								break;
							}
						}

					}

				}

				
					break;
				}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	Handler VTLLogicServiceHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED: {
				trafficLight.setBackgroundColor(application.trafficLightColor);
				TextView tvIntersection = (TextView) findViewById(R.id.tvIntersection);
				if (application.junctionId != null)
					tvIntersection.setText("x:"
							+ application.junctionPoint.getX() + " y:"
							+ application.junctionPoint.getY());
				else
					tvIntersection.setText("no");

				break;
			}
			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS: {
		

				trafficLight.setBackgroundColor(application.trafficLightColor);

				break;
			}
			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_DISTANCE: {
				TextView tvOtherDistanceToIntersection = (TextView) findViewById(R.id.tvOtherDistanceToIntersection);
				float otherDistanceToIntersection = msg.getData().getFloat(
						"otherDistanceToIntersection");
				tvOtherDistanceToIntersection.setText(String
						.valueOf(otherDistanceToIntersection));

				break;
			}

			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_CLUSTER_LEADER: {
				TextView tvClusterLeaader = (TextView) findViewById(R.id.tvClusterLeaader);

				tvClusterLeaader.setText((String) msg.obj);

				break;
			}
			
			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_VTL_LEADER: {
				TextView tvClosestCarToIntersection = (TextView) findViewById(R.id.tvClosestCarToIntersection);
				String textClosestCarToIntersection = msg.getData().getString(
						"closestCarToIntersection");
				tvClosestCarToIntersection
						.setText(textClosestCarToIntersection);

				break;
			}

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void runTimeThread() {

		new Thread() {
			public void run() {
				while (true) {
					try {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {

								

								tvTime.setText(application.getTimeDisplay());


								/* for debug */
								double distanceToIntersection = 0;
								if (application.junctionId != null)
									distanceToIntersection = HelperFunctions.getDistance(
											application.getCurrentPositionX(),
											application.getCurrentPositionY(),
											application.junctionPoint.getX(),
											application.junctionPoint.getY());

								TextView tvMyDistanceToIntersection = (TextView) findViewById(R.id.tvMyDistanceToIntersection);
								tvMyDistanceToIntersection.setText(String
										.valueOf(distanceToIntersection));

							}
						});
						Thread.sleep(VTLApplication.SLEEPTIME_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public void onLocationChanged(Location location) {

		tvLat.setText("Lat:" + location.getLatitude());
		tvLong.setText("Long:" + location.getLongitude());
	

	
		application.setCurrentPosition(location);
		tvCurrentX.setText(location.getLongitude() + "");
		tvCurrentY.setText(location.getLatitude() + "");
		tvAngle.setText(application.directionAngle + "");

		LatLng newLoc = new LatLng(location.getLatitude(),
				location.getLongitude());
		if (mylocation != null)
			mylocation.remove();

		mylocation = application.googleMap.addMarker(new MarkerOptions()
				.position(newLoc).title("MyLoc"));

		

	}

	

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}

}
