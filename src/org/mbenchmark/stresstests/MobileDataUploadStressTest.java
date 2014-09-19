package org.mbenchmark.stresstests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.mbenchmark.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MobileDataUploadStressTest implements StressTestInterface {

	private Context context;
	private String name = "Mobile Data Upload";
	private final String TAG = "MBenchmark";
	private UploadThread thread;
	private boolean isExecutionStopped = false;
	private boolean previouslyEnabledWiFi;
	private String urlString;
	private String filename;
	private InputStream fileInputStream;
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
		urlString = "http://morphone.elet.polimi.it/upload_file.php";
		filename = "bitmap.jpg";
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
	
	public MobileDataUploadStressTest(Context context){
		this.context = context;
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void startNewThread(){
		fileInputStream = context.getResources().openRawResource(R.drawable.bitmap);
		thread = new UploadThread(urlString,fileInputStream);
		thread.start();
	}
	
	public class UploadThread extends Thread {
		
		private String urlString;
		private InputStream fileInputStream;
		
		public UploadThread(String urlString, InputStream fileInputStream){
			this.urlString = urlString;
			this.fileInputStream = fileInputStream;
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
			
			Log.d(TAG, "Current network :"+netInfo.getTypeName());
			Log.d(TAG, "Network is connected :"+netInfo.isConnected());
			
			HttpURLConnection urlConnection = null;
            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1*1024*1024;
            
            try{
            	
            	URL url = new URL(urlString);
            	urlConnection = (HttpURLConnection) url.openConnection();
            	urlConnection.setDoInput(true);
            	urlConnection.setDoOutput(true);
            	urlConnection.setUseCaches(false);
            	
            	urlConnection.setRequestMethod("POST");
            	urlConnection.setRequestProperty("Connection", "Keep-Alive");
            	urlConnection.setRequestProperty("Charset", "UTF-8");
            	urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
            	
            	outputStream = new DataOutputStream(urlConnection.getOutputStream());
            	outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            	outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + filename + "\"" + lineEnd);
            	outputStream.writeBytes(lineEnd);

            	bytesAvailable = fileInputStream.available();
            	bufferSize = Math.min(bytesAvailable, maxBufferSize);
            	buffer = new byte[bufferSize];

            	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            	while (bytesRead > 0 && !isExecutionStopped){
            		outputStream.write(buffer, 0, bufferSize);
            		bytesAvailable = fileInputStream.available();
            		bufferSize = Math.min(bytesAvailable, maxBufferSize);
            		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            	}
            	// send multipart form data necessary after file data...
            	outputStream.writeBytes(lineEnd);
            	outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            	outputStream.writeBytes("Content-Disposition: form-data; name=\"submit\"");
            	outputStream.writeBytes(lineEnd);
            	outputStream.writeBytes("Submit");
            	outputStream.writeBytes(lineEnd);
            	outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            	
            	// close streams
            	
            	fileInputStream.close();
            	outputStream.flush();
            	outputStream.close();       	
            }
            catch (MalformedURLException ex){
            	Log.e(TAG, "error: " + ex.getMessage(), ex);
            }
            catch (IOException ioe){
            	Log.e(TAG, "error: " + ioe.getMessage(), ioe);
            }
            
            try {
            	inputStream = new DataInputStream ( urlConnection.getInputStream() );
            	String str;

            	while (( str = inputStream.readLine()) != null){
            		//Log.d(TAG,"Server Response "+str);
                }
            	urlConnection.disconnect();
            	inputStream.close();
            	if(!isExecutionStopped){
            		startNewThread();
            	}
            }
            catch (IOException ioex){
                 Log.e(TAG, "Error: " + ioex.getMessage(), ioex);
            }
		}
	}
}
