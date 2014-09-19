package org.sense.bluetooth;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothSenseReceiver extends BroadcastReceiver 
										implements BluetoothSenseInterface {
	
	private BluetoothAdapter bluetoothAdapter = null;
	private int currentState = -1;
	
	@SuppressWarnings("unused")
	private int previousState = -1;
	
	public BluetoothSenseReceiver(){
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		currentState = bluetoothAdapter.getState();
	}
	
	@Override
	public Set<BluetoothDevice> getBondedDevices() throws BluetoothSenseException {
		try{
			return bluetoothAdapter.getBondedDevices();
		}catch (Exception e) {
			throw new BluetoothSenseException("Error while getting BondedDevices (" + e.getMessage() + ")");
		}
	}

	@Override
	public int getState() throws BluetoothSenseException{
		try{
			return currentState;
		}catch (Exception e) {
			throw new BluetoothSenseException("Error while getting State (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public boolean isActive() throws BluetoothSenseException{
		try{
			if (currentState != BluetoothAdapter.STATE_OFF && 
				currentState != BluetoothAdapter.STATE_TURNING_OFF)
				return true;
			else 
				return false;
		}catch (Exception e) {
			throw new BluetoothSenseException("Error while getting isActive (" + e.getMessage() + ")");
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
		previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
	}
}
