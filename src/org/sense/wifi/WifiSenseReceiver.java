package org.sense.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiSenseReceiver extends BroadcastReceiver  
									implements WifiSenseInterface {
	
	private static final String TAG = "WifiSenseReceiver.java";
	
	private WifiManager wifiManager;
	WifiInfo wifiInfo;
	
	private int isEnabled = -1;
	private int isConnected = -1;
	private long rxBytes = -1;
	private long txBytes = -1;
	
	private String macAddress = null;
	
	
	public WifiSenseReceiver(Context context){
		
		try{
			// Init boolean variables 
			this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if(wifiManager.isWifiEnabled()){
				isEnabled = 1;
				
				wifiInfo = wifiManager.getConnectionInfo();
				SupplicantState supplicantState = wifiInfo.getSupplicantState();
				if(supplicantState.toString().compareTo("COMPLETED")==0 || 
						supplicantState.toString().compareTo("ASSOCIATED")==0 ||
						supplicantState.toString().compareTo("INACTIVE")==0)
					isConnected = 1;
				else
					isConnected = 0;
			}else{
				isEnabled = 0;
				isConnected = 0;
			}
			
			// Look for MAC address stored in SharedPreferences
			SharedPreferences pref = context.getSharedPreferences("senseLibraries", 0);
			macAddress = pref.getString("macAddress", null);
			
			// MAC address never sensed: try to get it
			if(macAddress != null){
				Log.d(TAG, "MAC address found in SharedPreferences: '" + macAddress + "'");
			}else{
				Log.d(TAG, "MAC address NOT found in SharedPreferences");
				
				if(isEnabled == 1){
					 // WiFi already enabled: get MAC address
				    macAddress = wifiInfo.getMacAddress().replace(":", "");
				}else{					
					// NOTE: It's not possible to get the WiFi MAC Address if the WiFi module is off.
					// Enable it and grab MAC address
				    wifiManager.setWifiEnabled(true);
				    wifiInfo = wifiManager.getConnectionInfo();
				    macAddress = wifiInfo.getMacAddress().replace(":", "");

				    // Disable WiFI again
				    wifiManager.setWifiEnabled(false);
				}
				
				// Store the MAC address in SharedPreferences
		    	SharedPreferences.Editor editor = pref.edit();
		        editor.putString("macAddress", macAddress);
		        editor.commit();
		        
				Log.d(TAG, "MAC address now stored in SharedPreferences: '" + macAddress + "'");
			}
		}catch(Exception e){
			isEnabled = -1;
			isConnected = -1;
			macAddress = null;
		}		
		
		rxBytes = calculateRxBytes();
		txBytes = calculateTxBytes();
	}
	
	@Override
	public int getNetworkId() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			int networkId = -1;
			networkId = wifiInfo.getNetworkId();
			
			if(networkId < 0)
				throw new WifiSenseException("No currently connected network");
			else
				return networkId;
		}else
			return -1;
	}

	@Override
	public int getSignalStrenght() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			try{
				return wifiInfo.getRssi();
			}catch(Exception e){
				throw new WifiSenseException("Error while getting signalStrenght (" + e.getMessage() + ")");
			}
		}else
			return -1;
	}

	@Override
	public int getLinkSpeed() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			try{
				return wifiInfo.getLinkSpeed();
			}catch(Exception e){
				throw new WifiSenseException("Error while getting linkSpeed (" + e.getMessage() + ")");
			}
		}else
			return -1;
	}

	@Override
	public int getIPAddress() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			try{
				return wifiInfo.getIpAddress();
			}catch(Exception e){
				throw new WifiSenseException("Error while getting IPAddress (" + e.getMessage() + ")");
			}
		}else
			return -1;
	}

	@Override
	public long getTxBytes() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			txBytes = calculateTxBytes();
			if(txBytes < 0)
				throw new WifiSenseException("The device does not support the statistic: txBytes");
			else
				return txBytes;
		}else
			return txBytes;
	}
	
	private long calculateTxBytes(){
		long totalTxBytes = TrafficStats.UNSUPPORTED;
		long mobileTxBytes = TrafficStats.UNSUPPORTED;
		
			totalTxBytes = TrafficStats.getTotalTxBytes();
			mobileTxBytes = TrafficStats.getMobileTxBytes();
		
		if(totalTxBytes == TrafficStats.UNSUPPORTED ||
			mobileTxBytes == TrafficStats.UNSUPPORTED)
			return -1;
		else{
			return (totalTxBytes - mobileTxBytes);
		}
	}

	@Override
	public long getRxBytes() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1){
			rxBytes = calculateRxBytes();
			if(rxBytes < 0)
				throw new WifiSenseException("The device does not support the statistic: rxBytes");
			else
				return rxBytes;
		}else
			return rxBytes;
	}
	
	private long calculateRxBytes(){
		long totalRxBytes = TrafficStats.UNSUPPORTED;
		long mobileRxBytes = TrafficStats.UNSUPPORTED;
		
		totalRxBytes = TrafficStats.getTotalRxBytes();
		mobileRxBytes = TrafficStats.getMobileRxBytes();
		
		if(totalRxBytes == TrafficStats.UNSUPPORTED ||
			mobileRxBytes == TrafficStats.UNSUPPORTED)
			return -1;
		else{
			return (totalRxBytes - mobileRxBytes);
		}
	}

	@Override
	public boolean isConnected() throws WifiSenseException {
		if(isEnabled == 1 && isConnected == 1)
			return true;
		else if(isConnected < 0)
			throw new WifiSenseException("Error while getting isConnected (isConnected == -1)");
		else
			return false;
	}

	@Override
	public boolean isActive() throws WifiSenseException {
		if(isEnabled == 1)
			return true;
		else if(isEnabled == 0)
			return false;
		else
			throw new WifiSenseException("Error while getting isActive (isEnabled == -1)");
	}
	
	@Override
	public String getMACAddress() throws WifiSenseException{
		if(macAddress != null)
			return macAddress;
		else
			throw new WifiSenseException("Error while getting MAC Address");
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action  = intent.getAction();
		if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			// Nothing to do here
		}else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			// WiFi state change: modify isEnabled
			int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			switch(extraWifiState){
				case WifiManager.WIFI_STATE_DISABLED:
					isEnabled = 0; 
					isConnected = 0; 
					wifiInfo = wifiManager.getConnectionInfo();
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					isEnabled = 0; 
					isConnected = 0; 
					wifiInfo = wifiManager.getConnectionInfo();
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					isEnabled = 1; 
					wifiInfo = wifiManager.getConnectionInfo();
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					isEnabled = 1; 
					wifiInfo = wifiManager.getConnectionInfo();
					break;
				case WifiManager.WIFI_STATE_UNKNOWN:
					isEnabled = -1; 
					wifiInfo = wifiManager.getConnectionInfo();
					break;
			}
//		}else if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
//			// This cast throws an exception
//			DetailedState state = WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED));
//			updateIsConnected(state);
//			wifiInfo = wifiManager.getConnectionInfo();
		}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			DetailedState state = ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState();
			updateIsConnected(state);
			wifiInfo = wifiManager.getConnectionInfo();
		}  
	}
	

	private void updateIsConnected(DetailedState aState){
		if (aState == DetailedState.SCANNING){
			// SCANNING
			isConnected = 0;
		}else if (aState == DetailedState.CONNECTING){
			// CONNECTING
			isConnected = 0;
		}else if(aState == DetailedState.OBTAINING_IPADDR){
			// OBTAINING IP ADDR
			isConnected = 0;
		}else if (aState == DetailedState.CONNECTED){
			// CONNECTED
			isConnected = 1;
		}else if (aState == DetailedState.DISCONNECTING){
			// DISCONNECTING
			isConnected = 0;
		}else if (aState == DetailedState.DISCONNECTED){
			// DISCONNECTED
			isConnected = 0;
		}else if (aState == DetailedState.FAILED){
			// FAILED
			isConnected = 0;
		}
	}

}
