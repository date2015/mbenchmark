package org.sense.wifi;

import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiSense implements WifiSenseInterface {
	
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private SupplicantState supplicantState; 
	private String macAddress;


	public WifiSense(WifiManager wifiManager, ConnectivityManager connectivityManager){
		this.wifiManager = wifiManager;

		wifiInfo = wifiManager.getConnectionInfo();
		
		if(wifiManager.isWifiEnabled()) {
		    // WIFI ALREADY ENABLED. GRAB THE MAC ADDRESS HERE
		    WifiInfo info = wifiManager.getConnectionInfo();
		    macAddress = info.getMacAddress();

		    // NOW DISABLE IT AGAIN
		    wifiManager.setWifiEnabled(false);
		} else {
		    // ENABLE THE WIFI FIRST
		    wifiManager.setWifiEnabled(true);

		    // WIFI IS NOW ENABLED. GRAB THE MAC ADDRESS HERE
		    WifiInfo info = wifiManager.getConnectionInfo();
		    macAddress = info.getMacAddress();

		    // NOW DISABLE IT AGAIN
		    wifiManager.setWifiEnabled(false);
		}
	}
	
	@Override
	public boolean isConnected() throws WifiSenseException{
		try{
			wifiInfo = wifiManager.getConnectionInfo();
			supplicantState = wifiInfo.getSupplicantState();
			if(supplicantState.toString().compareTo("COMPLETED")==0 || 
					supplicantState.toString().compareTo("ASSOCIATED")==0 ||
					supplicantState.toString().compareTo("INACTIVE")==0)
				return true;
			else
				return false;
		}catch(Exception e){
			throw new WifiSenseException("Error while getting isConnected (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public boolean isActive() throws WifiSenseException{
		try{
			return wifiManager.isWifiEnabled();
		}catch(Exception e){
			throw new WifiSenseException("Error while getting signalStrenght (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public int getNetworkId() throws WifiSenseException{
		int networkId = -1;
		
		try{
			networkId = wifiInfo.getNetworkId();
		}catch(Exception e){
			throw new WifiSenseException("Error while getting networkId (" + e.getMessage() + ")");
		}
		
		if(networkId < 0)
			throw new WifiSenseException("No currently connected network");
		else
			return networkId;
	}
	
	@Override
	public int getSignalStrenght() throws WifiSenseException{
		try{
			return wifiInfo.getRssi();
		}catch(Exception e){
			throw new WifiSenseException("Error while getting signalStrenght (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public int getLinkSpeed() throws WifiSenseException{
		try{
			return wifiInfo.getLinkSpeed();
		}catch(Exception e){
			throw new WifiSenseException("Error while getting linkSpeed (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public int getIPAddress() throws WifiSenseException{
		try{
			return wifiInfo.getIpAddress();
		}catch(Exception e){
			throw new WifiSenseException("Error while getting IPAddress (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public long getTxBytes() throws WifiSenseException{

		long totalTxBytes = TrafficStats.UNSUPPORTED;
		long mobileTxBytes = TrafficStats.UNSUPPORTED;
		
		try{
			totalTxBytes = TrafficStats.getTotalTxBytes();
			mobileTxBytes = TrafficStats.getMobileTxBytes();
		}catch (Exception e) {
			throw new WifiSenseException("Error while getting txBytes (" + e.getMessage() + ")");
		}
		
		if(totalTxBytes == TrafficStats.UNSUPPORTED ||
			mobileTxBytes == TrafficStats.UNSUPPORTED)
			throw new WifiSenseException("The device does not support the statistic: txBytes");
		else
			return (totalTxBytes - mobileTxBytes);
	}
	
	@Override
	public long getRxBytes() throws WifiSenseException{
		
		long totalRxBytes = TrafficStats.UNSUPPORTED;
		long mobileRxBytes = TrafficStats.UNSUPPORTED;
		
		try{
			totalRxBytes = TrafficStats.getTotalRxBytes();
			mobileRxBytes = TrafficStats.getMobileRxBytes();
		}catch (Exception e) {
			throw new WifiSenseException("Error while getting rxBytes (" + e.getMessage() + ")");
		}
		
		if(totalRxBytes == TrafficStats.UNSUPPORTED ||
			mobileRxBytes == TrafficStats.UNSUPPORTED)
			throw new WifiSenseException("The device does not support the statistic: rxBytes");
		else
			return (totalRxBytes - mobileRxBytes);
	}
	
	@Override
	public String getMACAddress() throws WifiSenseException{
		if(macAddress != null)
			return macAddress;
		else
			throw new WifiSenseException("Error while getting MAC Address");
	}
	
	
}
