package org.sense.mobile;

import android.content.Context;
import android.net.TrafficStats;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class MobileSense implements MobileSenseInterface {
	
	final String TAG = "org.morphone.sense";

	private TelephonyManager telephonyManager;
	private MyPhoneStateListener myPhoneListener;
	
	private int signal_strenght = -1;
	private int network_type = -1;
	private int data_state = -1;
	private int data_activity = -1;
	private int call_state = -1;
	
	private String IMEI = null;
	
	public MobileSense(TelephonyManager tel){
		
		this.telephonyManager = tel;
		
		// Create a listener 
		myPhoneListener = new MyPhoneStateListener();
	    tel.listen(myPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	
	    tel.listen(myPhoneListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
	    tel.listen(myPhoneListener, PhoneStateListener.LISTEN_DATA_ACTIVITY);
	    tel.listen(myPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	    
		// Init local variables
		IMEI = telephonyManager.getDeviceId();
	
		// TODO: find an init value
		// Init not working on devices < API 17
		// CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
		// CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
		signal_strenght = -1;
		
		network_type = telephonyManager.getNetworkType();
		data_state = telephonyManager.getDataState();
		data_activity = telephonyManager.getDataActivity();
		call_state = telephonyManager.getCallState();
	}
	
	public MobileSense(Context context){
		
		this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		// Create a listener 
		myPhoneListener = new MyPhoneStateListener();
		telephonyManager.listen(myPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	
		telephonyManager.listen(myPhoneListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		telephonyManager.listen(myPhoneListener, PhoneStateListener.LISTEN_DATA_ACTIVITY);
		telephonyManager.listen(myPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	    
		// Init local variables
		IMEI = telephonyManager.getDeviceId();
	
		// TODO: find an init value
		// Init not working on devices < API 17
		// CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
		// CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
		signal_strenght = -1;
		
		network_type = telephonyManager.getNetworkType();
		data_state = telephonyManager.getDataState();
		data_activity = telephonyManager.getDataActivity();
		call_state = telephonyManager.getCallState();
	}
	
	@Override
	public String getIMEI() throws MobileSenseException{
		if(IMEI != null)
			return IMEI;
		else
			throw new MobileSenseException("IMEI not available for this device");
	}
	
	@Override
	public int getCallState() throws MobileSenseException{
		if(call_state < 0)
			throw new MobileSenseException("Error while getting callState");
		else
			return call_state;
	}
	
	@Override
	public long getRxBytes() throws MobileSenseException{
		long rxBytes = TrafficStats.UNSUPPORTED;
		
		try{
			rxBytes = TrafficStats.getMobileRxBytes();
		}catch(Exception e){
			throw new MobileSenseException("Error while getting rxBytes (" + e.getMessage() + ")");
		}

		if(rxBytes == TrafficStats.UNSUPPORTED)
			throw new MobileSenseException("The device does not support the statistic: rxBytes");
		else
			return rxBytes;
	}

	@Override
	public long getTxBytes() throws MobileSenseException{
		long txBytes = TrafficStats.UNSUPPORTED;
		
		try{
			txBytes = TrafficStats.getMobileTxBytes();
		}catch(Exception e){
			throw new MobileSenseException("Error while getting txBytes (" + e.getMessage() + ")");
		}

		if(txBytes == TrafficStats.UNSUPPORTED)
			throw new MobileSenseException("The device does not support the statistic: txBytes");
		else
			return txBytes;
	}
	
	// These returns values set by MyPhoneStateListener
	@Override
	public int getSignalStrenght() throws MobileSenseException{
		if(signal_strenght < 0)
			throw new MobileSenseException("Error while getting signal strenght");
		else
			return signal_strenght;
	}
	
	@Override
	public int getDataState() throws MobileSenseException{
		if(data_state < 0)
			throw new MobileSenseException("Error while getting data state");
		else
			return data_state;
	}
	
	@Override
	public int getDataActivity() throws MobileSenseException{
		if(data_activity < 0)
			throw new MobileSenseException("Error while getting data activity");
		else
			return data_activity;
	}
	
	@Override
	public int getNetworkType() throws MobileSenseException{
		if(network_type < 0)
			throw new MobileSenseException("Error while getting network type");
		else
			return network_type;
	}
	
	
	private class MyPhoneStateListener extends PhoneStateListener {
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
			super.onSignalStrengthsChanged(signalStrength);
		    signal_strenght = signalStrength.getGsmSignalStrength();
		}
		  
		@Override
		public void onDataConnectionStateChanged(int state, int networkType){
			super.onDataConnectionStateChanged(state, networkType);
			data_state = state;
			network_type = networkType;
		}
		  
		@Override
		public void onDataActivity(int direction){
			super.onDataActivity(direction);
			data_activity = direction;
		}
		
		@Override
		public void onCallStateChanged (int state, String incomingNumber){
			super.onCallStateChanged(state, incomingNumber);
			call_state = state;
		}
	};
	
}
