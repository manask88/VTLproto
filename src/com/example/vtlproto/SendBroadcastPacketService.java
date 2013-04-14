// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.vtlproto;

import android.app.IntentService;

import android.content.Intent;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


public class SendBroadcastPacketService extends IntentService {

	private static final String TAG = SendBroadcastPacketService.class.getSimpleName();
	public static final String EXTRAS_RAW_PACKET = "raw_packet";
	public static final String EXTRAS_DST_IP = "dst_ip";
	private VTLApplication application;
	private String rawPacket;

	public SendBroadcastPacketService(String name) {
		super(name);
	}

	public SendBroadcastPacketService() {
		super("SendUnicastService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		String IPAdddress;
		Log.d(TAG, "onHandleItent");
		application = (VTLApplication) getApplication();
		IPAdddress = intent.getExtras().getString(EXTRAS_DST_IP);
		DatagramSocket socket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;

		
		try {
			socket = new DatagramSocket();

			
			rawPacket = new StringBuilder(
					String.valueOf(VTLApplication.MSG_TYPE_LEADER_ACK))
					.append(VTLApplication.MSG_SEPARATOR)
					.append(application.getTimeAndDate()).toString();

			
			outBuf = rawPacket.getBytes();

			InetAddress address;
			address = InetAddress.getByName(IPAdddress);
			
			outPacket = new DatagramPacket(outBuf, outBuf.length, address,
					VTLApplication.PORT);

			socket.send(outPacket);

			Log.i(TAG,"SENDING : " + rawPacket+ "to "+IPAdddress);
			
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	
		

	}
}
