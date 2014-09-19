package org.mbenchmark.stresstests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

public class MobileDataDownloadStressTest implements StressTestInterface {

	private Context context;
	private String name = "Mobile Data Download";
	private final String mobileDataBenchmarkDir = "/MBenchmark/WiFiBenchmark";
	private final String TAG = "MBenchmark";
	private DownloadThread thread;
	private boolean isExecutionStopped = false;
	private boolean previouslyEnabledWiFi;
	private String urlString;
	private String filename;
	private WifiManager wifiManager;
	private ConnectivityManager connMan;
	private NetworkInfo netInfo;
	
	@Override
	public void startStressTest() throws Exception {
		this.isExecutionStopped = false;
		previouslyEnabledWiFi = wifiManager.isWifiEnabled();
		connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInfo = connMan.getActiveNetworkInfo();
		if(previouslyEnabledWiFi){
			wifiManager.setWifiEnabled(false);
		}
		urlString = "http://http.us.debian.org/debian/pool/main/f/file-roller/file-roller_2.30.2-2_i386.deb";
		filename = "file-roller_2.30.2-2_i386.deb";
		Log.d(TAG, "Starting thread");
		startNewThread();
	}

	@Override
	public void stopStressTest() throws Exception {
		this.isExecutionStopped = true;
		if(previouslyEnabledWiFi){
			wifiManager.setWifiEnabled(true);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public MobileDataDownloadStressTest(Context context){
		this.context = context;
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void startNewThread(){
		thread = new DownloadThread(urlString,filename);
		thread.start();
	}
	
	public class DownloadThread extends Thread {
		
		private String urlString;
		private String filename;
		
		public DownloadThread(String urlString, String filename){
			this.urlString = urlString;
			this.filename = filename;
		}
		
		public void run(){
			
			if(netInfo==null || netInfo.getType()!=ConnectivityManager.TYPE_MOBILE){
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					Log.e(TAG, "Error: " + e.getMessage(), e);
				}
			}
			
			netInfo = connMan.getActiveNetworkInfo();
			
			while(netInfo==null){
				try {
					sleep(200);
					Log.d(TAG, "Sleeping because netInfo is null");
					netInfo = connMan.getActiveNetworkInfo();
				} catch (InterruptedException e) {
					Log.e(TAG, "Error: " + e.getMessage(), e);
				}
			}
			while(!netInfo.isConnected()){
				try {
					sleep(100);
					Log.d(TAG, "Sleeping because network is not connected");
				} catch (InterruptedException e) {
					Log.e(TAG, "Error: " + e.getMessage(), e);
				}
			}
			
			Log.d(TAG, "Current network :"+netInfo.getTypeName());
			Log.d(TAG, "Network is connected :"+netInfo.isConnected());
			
			try {
				URL url = new URL(urlString);
		        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		        urlConnection.setRequestMethod("GET");
		        urlConnection.setDoOutput(true);
		        urlConnection.connect();
		        File directory = new File (Environment.getExternalStorageDirectory()+mobileDataBenchmarkDir);
		        directory.mkdirs();
		        File file = new File(directory, filename);
		        if (file.exists()){
		        	file.delete();
		        }
		        FileOutputStream fileOutput = new FileOutputStream(file);
		        InputStream inputStream = urlConnection.getInputStream();
		        byte[] buffer = new byte[1024];
		        int bufferLength = 0;
		        while ( (bufferLength = inputStream.read(buffer)) > 0 && !isExecutionStopped) {                
		                fileOutput.write(buffer, 0, bufferLength);
		        }
		        fileOutput.close();
		        fileOutput = null;
		        directory = null;
		        inputStream = null;
		        if (isExecutionStopped){
		        	file.delete();
		        	file = null;
		        	return;
		        }
		        file = null;
		        if(!isExecutionStopped){
		        	startNewThread();
		        }
			} 
			catch (MalformedURLException e) {
				Log.e(TAG, "Error: " + e.getMessage(), e);
			} 
			catch (IOException e) {
				Log.e(TAG, "Error: " + e.getMessage(), e);
			}
		}
	}
}
