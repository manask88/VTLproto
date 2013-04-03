package com.example.vtlproto;

import java.util.HashMap;

import com.example.vtlproto.model.BeaconPacket;
import com.example.vtlproto.model.NameValue;
import com.example.vtlproto.model.TrafficLightPacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class VTLActivity extends Activity {

	public final static String TAG = VTLActivity.class.getSimpleName();
	public final static int SQUARESIZE = 50;
	public final static int SQUAREMARGIN = 13;
	private Button buttonLeft, buttonRight, buttonUp, buttonDown, buttonStart,
			trafficLight;
	boolean shouldDraw = false;
	private Context context = this;
	private TextView tvCurrentX, tvCurrentY, tvTime, tvIPAddress;
	private ImageView imageView;
	private int numNeighbors;
	private Canvas canvas;
	private Bitmap bitmap;
	private VTLApplication application;;
	BeaconService beaconService;
	Boolean servicesStatus;
	VTLLogicService VTLLogicService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (VTLApplication) this.getApplication();

		setContentView(R.layout.activity_main);
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
		imageView = (ImageView) this.findViewById(R.id.imageView);
		/*
		 * Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
		 * .getDefaultDisplay().getWidth(), (int) getWindowManager()
		 * .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
		 */
		bitmap = Bitmap.createBitmap((int) 600, 600, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		imageView.setImageBitmap(bitmap);
		for (int i = 0; i < VTLApplication.SIZEY; i++)
			for (int j = 0; j < VTLApplication.SIZEX; j++) {

				resetSquare(j, VTLApplication.SIZEY - 1 - i);

			}
		drawSquare((int)application.getCurrentPositionX(),
				(int)application.getCurrentPositionY(), Color.BLUE);
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonDown = (Button) findViewById(R.id.buttonDown);
		buttonUp = (Button) findViewById(R.id.buttonUp);
		buttonLeft = (Button) findViewById(R.id.buttonLeft);
		buttonRight = (Button) findViewById(R.id.buttonRight);
		/*
		 * buttonTest = (Button) findViewById(R.id.buttonTest);
		 * 
		 * buttonTest.setOnClickListener(new View.OnClickListener() { public
		 * void onClick(View v) { Intent serviceIntent = new Intent(context,
		 * SendUnicastService.class);
		 * serviceIntent.putExtra(SendUnicastService.EXTRAS_DST_IP,
		 * "10.20.1.144"); // serviceIntent.setAction("something"); Log.i(TAG,
		 * "trying to start FileTransferService");
		 * context.startService(serviceIntent); } });
		 */

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

		buttonDown.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				shouldDraw = true;

				// Log.i(VTLActivity.TAG, "Go down");
				resetSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY());
				tvCurrentY.setText(String.valueOf(application.decCurrentY()));
				drawSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY(), Color.BLUE);

			}
		});

		buttonUp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Log.i(VTLActivity.TAG, "Go up");
				resetSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY());
				tvCurrentY.setText(String.valueOf(application.incCurrentY()));
				drawSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY(), Color.BLUE);
			}
		});
		buttonLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Log.i(VTLActivity.TAG, "Go left");
				resetSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY());
				tvCurrentX.setText(String.valueOf(application.decCurrentX()));
				drawSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY(), Color.BLUE);

			}
		});
		buttonRight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Log.i(VTLActivity.TAG, "Go right");
				resetSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY());
				tvCurrentX.setText(String.valueOf(application.incCurrentX()));
				drawSquare((int)application.getCurrentPositionX(),
						(int)application.getCurrentPositionY(), Color.BLUE);

			}
		});
	}

	public void drawSquare(int x, int y, int color) {

		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(10);

		int productx = (x) * SQUARESIZE;
		int producty = (VTLApplication.SIZEY - 1 - y) * SQUARESIZE;
		canvas.drawRect(productx + SQUAREMARGIN, producty + SQUAREMARGIN
				+ VTLApplication.OFFSETY, productx + SQUARESIZE, producty
				+ SQUARESIZE + VTLApplication.OFFSETY, paint);
		imageView.setImageBitmap(bitmap);
	}

	public void resetSquare(int x, int y) {
		/*
		 * Log.i(VTLActivity.TAG, "j: " + x + " i:" + (VTLApplication.SIZEY - 1
		 * - y));
		 */
		if (VTLApplication.ROAD_MATRIX[VTLApplication.SIZEY - 1 - y][x])
			drawSquare(x, y, Color.GRAY);
		else
			drawSquare(x, y, Color.GREEN);

	}

	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
		getParent().finish();
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
				case VTLApplication.MSG_TYPE_BEACON:

					BeaconPacket beaconPacket = new BeaconPacket(text);
					String IPAddress = beaconPacket.getIPAdress();

					if (application.hashMapNeighbors.get(IPAddress) == null) {

						Log.i(TAG, "got:" + IPAddress);
						beaconPacket
								.setColor(VTLApplication.COLORS[numNeighbors]);
						application.hashMapNeighbors.put(IPAddress,
								beaconPacket);
						numNeighbors++;
					} else {
						/* to reset last position */
						resetSquare((int)application.hashMapNeighbors.get(IPAddress)
								.getX(),
								(int)application.hashMapNeighbors.get(IPAddress)
										.getY());
						beaconPacket.setColor(application.hashMapNeighbors.get(
								IPAddress).getColor());
						application.hashMapNeighbors.put(IPAddress,
								beaconPacket);
					}
					drawSquare((int)beaconPacket.getX(),(int) beaconPacket.getY(),
							beaconPacket.getColor());

					break;

				case VTLApplication.MSG_TYPE_LIGHT_STATUS:
					if (true)
				//	if (application.conflictDetected && application.waitingForLeaderMessage)
//Log.i(TAG,"got a S packet:"+text);
					{
						TrafficLightPacket trafficLightPacket = new TrafficLightPacket(
								text);

						/*Log.i(TAG,"wih timer"+trafficLightPacket.getTimer());
						Log.i(TAG,"laneid0:"+trafficLightPacket.getStatusLaneIds().get(0).getName());
						Log.i(TAG,"status0:"+trafficLightPacket.getStatusLaneIds().get(0).getCharValue());
*/

						for (NameValue statusLaneId:trafficLightPacket.getStatusLaneIds())
						{
							if (statusLaneId.getName().equals(application.laneId))
							{
								
								char status = statusLaneId.getCharValue(); 
										switch (status) {
										case VTLApplication.MSG_LIGHT_STATUS_GREEN:
											application.trafficLightColor = Color.GREEN;
											trafficLight
													.setBackgroundColor(application.trafficLightColor);
											//application.waitingForLeaderMessage = false;
											//Log.d(TAG, "got green light message from leader");
											break;
										case VTLApplication.MSG_LIGHT_STATUS_RED:
									
											
											application.trafficLightColor = Color.RED;
											trafficLight
													.setBackgroundColor(application.trafficLightColor);
											
											//Log.d(TAG, "got red light message from leader");

											//application.waitingForLeaderMessage = false;

											break;
										}
								
								break;
							}
						}
						
					}

					break;
				}

				// Toast.makeText(context,
				// "i got"+msg.getData().getString("Message"),
				// Toast.LENGTH_SHORT).show();

				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	Handler VTLLogicServiceHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case VTLApplication.VTLLOGICSERVICE_HANDLER_RX_CONFLICT_DETECTED:

				trafficLight.setBackgroundColor(application.trafficLightColor);
				TextView tvIntersection = (TextView) findViewById(R.id.tvIntersection);
				if (application.junctionId != null)
					tvIntersection.setText("x:"
							+ application.junctionPoint.getX() + " y:"
							+ application.junctionPoint.getY());
				else
					tvIntersection.setText("no");

				break;
			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_LIGHT_STATUS:

				TextView tvClosestCarToIntersection = (TextView) findViewById(R.id.tvClosestCarToIntersection);
				String textClosestCarToIntersection = msg.getData().getString(
						"closestCarToIntersection");
				tvClosestCarToIntersection
						.setText(textClosestCarToIntersection);

				trafficLight.setBackgroundColor(application.trafficLightColor);

				break;

			case VTLApplication.VTLLOGICSERVICE_HANDLER_NEW_DISTANCE:
				TextView tvOtherDistanceToIntersection = (TextView) findViewById(R.id.tvOtherDistanceToIntersection);
				float otherDistanceToIntersection = msg.getData().getFloat(
						"otherDistanceToIntersection");
				tvOtherDistanceToIntersection.setText(String
						.valueOf(otherDistanceToIntersection));

				break;
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

								drawSquare((int)application.getCurrentPositionX(),
										(int)application.getCurrentPositionY(),
										Color.BLUE);
								application.time.setToNow();
								tvTime.setText(application.time
										.format("%k:%M:%S"));

								/* for debug */
								float flo = 0;
								if (application.junctionId != null)
									flo = VTLLogicService.getDistance(
											application.getCurrentPositionX(),
											application.getCurrentPositionY(),
											application.junctionPoint.getX(),
											application.junctionPoint.getY());

								TextView tvMyDistanceToIntersection = (TextView) findViewById(R.id.tvMyDistanceToIntersection);
								tvMyDistanceToIntersection.setText(String
										.valueOf(flo));

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
}
