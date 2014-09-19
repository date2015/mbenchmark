package org.sense.bluetooth;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class BluetoothSense implements BluetoothSenseInterface {

	private BluetoothAdapter bluetoothAdapter = null;
	
	public BluetoothSense(){
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	
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
			return bluetoothAdapter.getState();
		}catch (Exception e) {
			throw new BluetoothSenseException("Error while getting State (" + e.getMessage() + ")");
		}
	}
	
	@Override
	public boolean isActive() throws BluetoothSenseException{
		try{
			if (this.getState()!= BluetoothAdapter.STATE_OFF && 
				this.getState()!= BluetoothAdapter.STATE_TURNING_OFF)
				return true;
			else 
				return false;
		}catch (Exception e) {
			throw new BluetoothSenseException("Error while getting isActive (" + e.getMessage() + ")");
		}
	}

}
