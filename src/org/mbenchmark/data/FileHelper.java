package org.mbenchmark.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.sense.device.DeviceSense;
import org.sense.device.DeviceSenseException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHelper {
	
	private final String TAG = "FileHelper";
	private final int MAX_SD_MOUNTING_ATTEMPTS = 2;
	private final int MOUNTING_ATTEMPTS_TIMEOUT = 5;	// Seconds
	private final String MBENCHMARK_DIR = "MBenchmark";
	
	private String deviceId = null;
	private String stressTestName;
	private FileOutputStream out;
	
	public FileHelper(Context context, String stressTestName){
		
		this.stressTestName = stressTestName.replace(" ", "_");
		
		// Init the deviceId, it is used to create the name of the log file
		DeviceSense deviceSense = new DeviceSense(context);
		try {
			deviceId = deviceSense.getDeviceId();
		} catch (DeviceSenseException e) {
			Log.w(TAG, "Device id not found (neither IMEI nor MAC)");
			deviceId = "DEVICEIDERROR";
		}
		
		// Try to mount the device's filesystem
		int mountingAttempts = 0;
		
		while (!(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) && mountingAttempts < MAX_SD_MOUNTING_ATTEMPTS) {
			try {
				Thread.sleep(MOUNTING_ATTEMPTS_TIMEOUT * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mountingAttempts++;
		}
					
		if (mountingAttempts == MAX_SD_MOUNTING_ATTEMPTS) {
			// Unable to mount the filesystem
			Log.w(TAG, "Unable to mount the filesystem");
			out = null;
			return;
		}
		else{
			// Filesystem mounted
			Log.d(TAG, "Filesystem mounted");
						
			File root = new File(Environment.getExternalStorageDirectory() + "/" + MBENCHMARK_DIR);	
			if(!root.exists()){
				//Create the directory if it doesn't exists
				root.mkdir();
				Log.d(TAG, "MBenchmark dir created");
			}
		}
		
		// Determine current file name
		String currentFileName = buildFileName();
		Log.d(TAG, "Current log file name:" + currentFileName);
		
		// New file creation
		File logFile = new File(Environment.getExternalStorageDirectory() + "/" + MBENCHMARK_DIR, currentFileName);
		try {
			// Append if exists
			out = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public FileOutputStream getFileOutputStream() {
		return out;
	}
	
	private String buildFileName(){
		
		StringBuffer monitoring_file_name = new StringBuffer(stressTestName + "_" + deviceId + "_" + new SimpleDateFormat("yyyyMMddHHmm", Locale.US).format(new Date()));
		monitoring_file_name.append(".log");
		return monitoring_file_name.toString();
	}

}
