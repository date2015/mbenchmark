package org.sense.screen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.sense.screen.brightness.BrightnessFilePattern;
import org.sense.screen.brightness.CyanogenFilePattern;
import org.sense.screen.brightness.SamsungAndLGFilePattern;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class ScreenSenseReceiver extends BroadcastReceiver 
									implements ScreenSenseInterface {

	private final String TAG = "ScreenSenseReceiver";
	
	private PowerManager powerManager;
	private ContentResolver contentResolver;
	private Display display;
	
	// These won't change
	private float displayHeight = -1;
	private float displayWidth = -1;
	
	// Changed by the onReceive method
	private int screenOn = -1;
	
	private String brightness_path = null;
	private boolean brightness_file_found = false;

	// TODO: Add here file patterns for future smartphone models
	private final BrightnessFilePattern[] known_brightness_paths = {
			new SamsungAndLGFilePattern(),
			new CyanogenFilePattern()
		};
	

	@SuppressWarnings("deprecation")
	public ScreenSenseReceiver(Context context){
		
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		contentResolver = context.getContentResolver();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		
		// Init static values
		try{
			displayHeight = display.getHeight();
		}catch (Exception e) {
			displayHeight = -1;
		}
		try{
			displayWidth = display.getWidth();
		}catch (Exception e) {
			displayWidth = -1;
		}
		
		// screenOn: first init
		try{
			if(powerManager.isScreenOn())
				screenOn = 1;
			else
				screenOn = 0;
		}catch (Exception e) {
			screenOn = -1;
		}
		
		// Find the brightness path
		findBrightnessFile();
	}

	private void findBrightnessFile() {
		for(int i = 0; 
				i < known_brightness_paths.length 
				&& !brightness_file_found; 
				i++){
			
			brightness_path = known_brightness_paths[i].findFileWithPattern();
			try {
				getValue(brightness_path);
				brightness_file_found = true;
				Log.d(TAG, "Brightness file found at: " + brightness_path);
			} catch (Exception e) {
				brightness_path = null;
				brightness_file_found = false;
			}
		}
	}

	@Override
	public float getDisplayHeight() throws ScreenSenseException{
		if(displayHeight < 0)
			throw new ScreenSenseException("Error while getting displayHeight (displayHeight < 0)");
		else
			return displayHeight;
	}
	
	@Override
	public float getDisplayWidth() throws ScreenSenseException{
		if(displayWidth < 0)
			throw new ScreenSenseException("Error while getting displayWidth (displayWidth < 0)");
		else
			return displayWidth;
	}
	
	@Override
	public float getDisplayRefreshRate() throws ScreenSenseException{
		try{
			return display.getRefreshRate();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting DisplayRefreshRate (" + e.getMessage() + ")");
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getOrientation() throws ScreenSenseException{
		try{
			return display.getOrientation();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting Orientation (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public int getScreenBrightnessMode() throws ScreenSenseException{
		try {
			return android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException e) {
			throw new ScreenSenseException("Error while getting brightness mode (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public boolean isScreenOn() throws ScreenSenseException {
		if(screenOn == 1)
			return true;
		else if(screenOn == 0)
			return false;
		else
			throw new ScreenSenseException("Error while getting screenOn (screenOn == " + screenOn + ")");
	}

	@Override
	public int getScreenBrightness() throws ScreenSenseException {
		// Return 0 if the screen if off
		if(screenOn < 1)
			return 0;
		else{
			int currentBrightnessMode = getScreenBrightnessMode();
			if(currentBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL){
				// Manual screen brightness: no problem
				try {
					return android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
				} catch (Exception e) {
					throw new ScreenSenseException("Error while getting screen brightness ( " + e.getMessage() + ")");
				}
			}else{
				// auto brightness mode: available if the path is kwnown
				if(brightness_path != null){
					try {
						return getValue(brightness_path);
					} catch (Exception e) {
						throw new ScreenSenseException("Error while getting screen brightness ( " + e.getMessage() + ")");
					}
				}else
					throw new ScreenSenseException("Error while init screen brightness path (Current path: " + brightness_path + ")");
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action  = intent.getAction();

		if(action.equals(Intent.ACTION_SCREEN_OFF)){
			screenOn = 0;
		}else if(action.equals(Intent.ACTION_SCREEN_ON)){
			screenOn = 1;
		}else{
			try{
				if(powerManager.isScreenOn())
					screenOn = 1;
				else
					screenOn = 0;
			}catch (Exception e) {
				screenOn = -1;
			}
		}
	
	}
	
	private int getValue(String path) throws NumberFormatException, IOException {
		BufferedReader buffIn = new BufferedReader(new FileReader(path), 8*24);
		String content = buffIn.readLine();
		buffIn.close();
		return Integer.parseInt(content);
	}
}
