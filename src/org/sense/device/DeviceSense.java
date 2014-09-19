package org.sense.device;

import org.sense.mobile.MobileSense;
import org.sense.mobile.MobileSenseException;
import org.sense.wifi.WifiSenseException;
import org.sense.wifi.WifiSenseReceiver;

import android.content.Context;
import android.os.Build;

public class DeviceSense implements DeviceSenseInterface{

	private String deviceId = null;
	
	public DeviceSense(Context context){
		try {
			// Device with GSM module
			MobileSense mobileSense = new MobileSense(context);
			deviceId = mobileSense.getIMEI();
		} catch (MobileSenseException e) {
			// Device without GSM module
			try {
				
				WifiSenseReceiver wifiSense = new WifiSenseReceiver(context);
				deviceId = "MAC" + wifiSense.getMACAddress();
			} catch (WifiSenseException e1) {
				// Abnormal device hardware
				deviceId = null;
			}
		}
	}
	
	@Override
	public int getSDKVersion() throws DeviceSenseException{
		try{
			return Build.VERSION.SDK_INT;
		}catch (Exception e) {
			throw new DeviceSenseException("Error while getting SDKVersion (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public String getBrand() throws DeviceSenseException{	
		try{
			return Build.BRAND;
		}catch (Exception e) {
			throw new DeviceSenseException("Error while getting brand (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public String getModel() throws DeviceSenseException{	
		try{
			return Build.MODEL;
		}catch (Exception e) {
			throw new DeviceSenseException("Error while getting model (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public String getKernelVersion() throws DeviceSenseException{
		return getSystemProperty("os.version");
	}
	
	@Override
	public String getArch() throws DeviceSenseException{	
		return getSystemProperty("os.arch");
	}
	
	@Override
	public String getDeviceId() throws DeviceSenseException{
		if(deviceId != null)
			return deviceId;
		else
			throw new DeviceSenseException("Error while getting deviceId");
	}
	
	private String getSystemProperty(String propertyName) throws DeviceSenseException{
		String property = System.getProperty(propertyName);
		if(property != null)
			return property;
		else
			throw new DeviceSenseException("Error while getting " + propertyName);
	}
}
