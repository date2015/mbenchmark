package org.mbenchmark.stresstests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.mbenchmark.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class WiFiDownloadStressTest implements StressTestInterface {

	private Context context;
	private String name = "WiFi Download";
	private final String wifiBenchmarkDir = "/MBenchmark/WiFiBenchmark";
	private final String TAG = "MBenchmark";
	private DownloadThread thread;
	private boolean isExecutionStopped = false;
	private String urlString;
	private String filename;
	
	@Override
	public void startStressTest() throws Exception {
		this.isExecutionStopped = false;
		ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(!netInfo.isConnected()){
			throw new Exception(context.getResources().getString(R.string.WiFi_not_connected_exception));
		}
		connMan.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
		urlString = "http://http.us.debian.org/debian/pool/main/f/file-roller/file-roller_2.30.2-2_i386.deb";
		filename = "file-roller_2.30.2-2_i386.deb";
		startNewThread();
	}
	
	@Override
	public void stopStressTest() throws Exception {
		this.isExecutionStopped = true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public WiFiDownloadStressTest(Context context){
		this.context = context;
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
			try {
				URL url = new URL(urlString);
		        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		        urlConnection.setRequestMethod("GET");
		        urlConnection.setDoOutput(true);
		        urlConnection.connect();
		        File directory = new File (Environment.getExternalStorageDirectory()+wifiBenchmarkDir);
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
