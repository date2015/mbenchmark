package org.sense.bluetooth;

import java.util.Set;

import android.bluetooth.BluetoothDevice;

public interface BluetoothSenseInterface {

	Set<BluetoothDevice> getBondedDevices() throws BluetoothSenseException;

	int getState() throws BluetoothSenseException;

	boolean isActive() throws BluetoothSenseException;

}
