package org.sense.screen;

public interface ScreenSenseInterface {

	boolean isScreenOn() throws ScreenSenseException;

	float getDisplayHeight() throws ScreenSenseException;

	float getDisplayWidth() throws ScreenSenseException;

	float getDisplayRefreshRate() throws ScreenSenseException;

	float getOrientation() throws ScreenSenseException;
	
	int getScreenBrightnessMode() throws ScreenSenseException;

	int getScreenBrightness() throws ScreenSenseException;

}
