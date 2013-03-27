package com.example.vtlproto;

import java.io.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


public class UnicastPacketAsyncTask extends AsyncTask<Void, Void, String> {
	public final static String TAG= UnicastPacketAsyncTask.class.getSimpleName();
  /*  private Context context;
    private Activity	activity;
    private TextView textViewInfo;
    private String out;
    

    public UnicastPacketAsyncTask(Context context) {
    	this.context = context;
    	activity=(Activity) context;

        this.textViewInfo = (TextView) activity.findViewById(R.id.textViewInfo);      //  TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);

    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(MainActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(MainActivity.TAG, "Server: connection done");
         

            Log.d(MainActivity.TAG, "server: getting string " );
            InputStream inputstream = client.getInputStream();
           
            out=readFully(inputstream);
            
            
  
            
            
            
            serverSocket.close();
            Log.i(TAG, "done working on brackground");
            return out;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
*/
    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
    	Log.e(TAG, "onPostExecute");
    


    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        //statusText.setText("Opening a server socket");
    	Log.i(TAG, "onPreExecute");
    }

	@Override
	protected String doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}



}