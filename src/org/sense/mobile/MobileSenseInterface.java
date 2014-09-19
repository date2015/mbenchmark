package org.sense.mobile;

public interface MobileSenseInterface {

	int getCallState() throws MobileSenseException;

	long getRxBytes() throws MobileSenseException;

	long getTxBytes() throws MobileSenseException;

	int getSignalStrenght() throws MobileSenseException;

	int getDataState() throws MobileSenseException;

	int getDataActivity() throws MobileSenseException;

	int getNetworkType() throws MobileSenseException;

	String getIMEI() throws MobileSenseException;

}
