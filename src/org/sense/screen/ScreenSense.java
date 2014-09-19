package org.sense.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.os.PowerManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;

public class ScreenSense implements ScreenSenseInterface {

	private PowerManager pm = null;
	private Display display = null;
	private ContentResolver contentResolver = null;
	private String brightness_path = null;		// Final path found
	private FilePattern foundPattern = null;	// Final pattern found

	
	
	/**
	 * Class to support patterns informations handling
	 * Patterns like:
	 * 		backlightPath/.../backlightDir/.../files
	 */
	public class FilePattern {

		String backlightPath;
		String backlightDir;
		GenericFileFilter brightnessFile;
		GenericFileFilter maxBrightnessFile;
		
		public FilePattern(String backlightPath, String backlightDir,
				GenericFileFilter brightnessFile,
				GenericFileFilter maxBrightnessFile) {
			super();
			this.backlightPath = backlightPath;
			this.backlightDir = backlightDir;
			this.brightnessFile = brightnessFile;
			this.maxBrightnessFile = maxBrightnessFile;
		}
	}
	
	/**
	 * Class to support file filtering in a directory
	 */
	private class GenericFileFilter implements FileFilter {
		
		private String fileName = null;
		
		public GenericFileFilter(String pattern){
			super();
			this.fileName = pattern;
		}
		
    	@Override
        public boolean accept(File pathname) {
            return Pattern.matches(this.fileName, pathname.getName());
        }      
    }



	// Constructor that finds the brightness path independently
	public ScreenSense(PowerManager pm, Display display, ContentResolver contentResolver) {
		this.display = display;
		this.pm = pm;
		this.contentResolver = contentResolver;
		
		try {
			
			ArrayList<FilePattern> knownPatterns = new ArrayList<FilePattern>(); 
			
			// Standard path
			knownPatterns.add(new FilePattern(
									"/sys/class/", 
									"backlight", 
									new GenericFileFilter("brightness"), 
									new GenericFileFilter("max_brightness")));
			
			// Cyanogen path
			knownPatterns.add(new FilePattern(
									"/sys/devices/platform/", 
									"lcd-backlight", 
									new GenericFileFilter("brightness"), 
									new GenericFileFilter("max_brightness")));
			
			// Add here new patterns as soon as they're found
			
			
			// Find the brightness path
			Log.d("TEST", "Looking for brightness path");
			findBrightnessPath(knownPatterns);
			Log.d("TEST", "brightness_path found: " + brightness_path);
		} catch (ScreenSenseException e) {
			Log.d("TEST", "brightness_path not found (" + e.getMessage() + ")");
		}
	}


	@Override
	public boolean isScreenOn() throws ScreenSenseException{
		try{
			return pm.isScreenOn();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting isScreenOn (" + e.getMessage() + ")");
		}
	}

	@Override
	public float getDisplayHeight() throws ScreenSenseException{
		try{
			return display.getHeight();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting DisplayHeight (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public float getDisplayWidth() throws ScreenSenseException{
		try{
			return display.getWidth();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting DisplayWidth (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public float getDisplayRefreshRate() throws ScreenSenseException{
		try{
			return display.getRefreshRate();
		}catch (Exception e) {
			throw new ScreenSenseException("Error while getting DisplayRefreshRate (" + e.getMessage() + ")");
		}
	}
	
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
	public int getScreenBrightness() throws ScreenSenseException{
		return getValue(brightness_path + foundPattern.brightnessFile.fileName);
	}
	
	private int getValue(String path) throws ScreenSenseException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(path), 8*24);
			return Integer.parseInt(in.readLine());
		} catch (Exception e) {
			throw new ScreenSenseException("Error while accessing file: " + path + "  (" + e.getMessage() + ")");
		}
	}
	

	private String findBrightnessPath(ArrayList<FilePattern> knownPatterns) throws ScreenSenseException {
		
		// TODO: just a temporary workaround for Samsung Galaxy Nexus, need to be fixed
		brightness_path = "/sys/class/backlight/s6e8aa0/";
		foundPattern = new FilePattern("/sys/class/", "backlight", 
										new GenericFileFilter("brightness"), 
										new GenericFileFilter("max_brightness"));
		return brightness_path;
		
		// OLD CODE - Working on Android < 4.3
//		for(FilePattern currentPattern : knownPatterns){
//			String backlightPath = findBacklightPathOn(currentPattern);
//			if(backlightPath != null){
//				brightness_path = backlightPath + "/";
//				foundPattern = currentPattern;
//				return brightness_path;
//			}
//		}
//		
//		throw new ScreenSenseException("Brightness files not found");
	}
	
	private String findBacklightPathOn(FilePattern currentPattern) {
		
		Log.d("TEST", "Currently in: " + currentPattern.backlightPath + "/.../" + currentPattern.backlightDir + "/.../");
		
		try {
			// Find all the absolute paths down the given path (no symlinks)
			Process shellProcess = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ls -R " + currentPattern.backlightPath});
			
			String currentLine = null;
			boolean found = false;
			BufferedReader in = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
			while((currentLine = in.readLine()) != null && !found) {
				
				// Read every line
				if(currentLine.matches(".+" + currentPattern.backlightDir + ":")){
					
					// Pattern found: backlightPath/.../backlightDir:
					currentLine = currentLine.replace(":", "/");
					File currentDir = new File(currentLine);
					
					// Find files down this path
					return findFilesRecursive(currentDir, currentPattern);
				}
			}
			in.close();
			
			return null;
			
		} catch (IOException e) {
			// throw new ScreenSenseException("Error while searching file (" + e.getMessage() + ")");
			return null;
		}
	}

	private String findFilesRecursive(File currentDir, FilePattern currentPattern){
		
		if(currentDir.isDirectory()){
			
			// Find files inside this directory
			File[] brightnessMatches = currentDir.listFiles(currentPattern.brightnessFile);
			File[] maxBrightnessMatches = currentDir.listFiles(currentPattern.maxBrightnessFile);
			
			if(	brightnessMatches != null && 
				brightnessMatches.length == 1 &&
				maxBrightnessMatches != null && 
				maxBrightnessMatches.length == 1)
				return currentDir.getAbsolutePath();	// Files found: return the absolute path
			else{
				// Files not found: keep on going down the path
				File[] allFiles = currentDir.listFiles();
				for(File currentFile : allFiles){
					// Recursive call on the current file
					String foundPath = findFilesRecursive(currentFile, currentPattern);
					if(foundPath != null)
						return foundPath;	// Stop recursion if valid path found
				}
			}
		}
		
		return null;
	}
	
}
