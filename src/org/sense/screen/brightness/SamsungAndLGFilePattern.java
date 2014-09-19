package org.sense.screen.brightness;

import java.io.File;

import android.util.Log;

public class SamsungAndLGFilePattern extends BrightnessFilePattern {

	@Override
	public String findFileWithPattern() {
		
		// Example (Samsung Galaxy Nexus): /sys/class/backlight/s6e8aa0/actual_brightness
		String mainDirPath = "/sys/class/backlight/";
		String deviceSpecificDirPath = "";
		String fileNamePath = "actual_brightness";
		String finalBrightnessPath = null;
		
		try{
			File mainDir = new File(mainDirPath);
			if(mainDir != null && mainDir.isDirectory()){
				// In the main directory						
				
				File[] objectsContained = mainDir.listFiles();
				if(	objectsContained != null &&
					objectsContained.length == 1 && 
					objectsContained[0].isDirectory()){
					// Device specific directory found (should be only one)
				
					deviceSpecificDirPath = objectsContained[0].getName();
					finalBrightnessPath = mainDirPath + deviceSpecificDirPath + "/" + fileNamePath;
					File brightnessFile = new File(finalBrightnessPath);
					if(brightnessFile.exists())
						return finalBrightnessPath;				// Brightness file found
				}
			}			
		}catch(Exception e){
			Log.e(TAG, "Error while finding a Samsung or a LG file pattern: " + e.getMessage());
			return null;
		}
		return finalBrightnessPath;
	}
}
