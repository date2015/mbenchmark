package org.sense.screen.brightness;


public class CyanogenFilePattern extends BrightnessFilePattern {
	
	@Override
	public String findFileWithPattern() {
		
		// Example (Desire HD Cyano 4.2): /sys/devices/platform/spade-backlight.0/leds/lcd-backlight/brightness
		// TODO: check if this pattern is applicable to other devices
		return "/sys/devices/platform/spade-backlight.0/leds/lcd-backlight/brightness";
	}
}
