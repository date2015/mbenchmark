package org.sense.device;

public interface DeviceSenseInterface {

	int getSDKVersion() throws DeviceSenseException;

	String getKernelVersion() throws DeviceSenseException;

	String getArch() throws DeviceSenseException;

	String getBrand() throws DeviceSenseException;

	String getModel() throws DeviceSenseException;

	String getDeviceId() throws DeviceSenseException;

}
