package org.sense.wifi;

public interface WifiSenseInterface {

	int getNetworkId() throws WifiSenseException;

	int getSignalStrenght() throws WifiSenseException;

	int getLinkSpeed() throws WifiSenseException;

	int getIPAddress() throws WifiSenseException;

	long getTxBytes() throws WifiSenseException;

	long getRxBytes() throws WifiSenseException;

	boolean isConnected() throws WifiSenseException;

	boolean isActive() throws WifiSenseException;
	
	String getMACAddress() throws WifiSenseException;

}
