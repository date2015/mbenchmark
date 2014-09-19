package org.mbenchmark.stresstests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

import org.mbenchmark.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class BluetoothDownloadStressTest implements StressTestInterface {

	private String name = "Bluetooth Download";
	private final String TAG = "Bluetooth Donwload Benchmark";
	
	private BluetoothDevice pairedDevice;
	private String pairedDeviceName = "U8150";
	private boolean isExecutionStopped = false;
	private Context context;
	private BluetoothAdapter btadapter;
	private ReceivingThread receivingThread;
	
	private static final UUID MY_UUID = UUID.fromString("8ce255d0-200a-11e0-ac64-0800200c9a66");
	private static final String clientReceivingMessage = "CLIENT_RECEIVING";
	private static final String clientEndMessage = "CLIENT_END";
	
	@Override
	public void startStressTest() throws Exception {
		
		btadapter = BluetoothAdapter.getDefaultAdapter();
		if (btadapter == null){
			Log.d(TAG, "Bluetooth is not supported on this device");
			throw new Exception(context.getResources().getString(R.string.BT_not_supported_exception));
		}
		if (!btadapter.isEnabled()){
			btadapter.enable();
		}
		Set<BluetoothDevice> pairedDevices = btadapter.getBondedDevices();
		for(int attempts=0; attempts <20 && pairedDevices.size()==0; attempts++){
			pairedDevices = btadapter.getBondedDevices();
			Thread.sleep(500);
		}
		if (pairedDevices.size()==0){
			Log.d(TAG, "No Paired Device Found");
			throw new Exception(context.getResources().getString(R.string.Pdev_not_found_exception));
		}
		for(BluetoothDevice device : pairedDevices){
			Log.d(TAG, "Paired Device: "+device.getName());
		    if(device.getName().equals(pairedDeviceName)){
		    	pairedDevice = device;
		        Log.d(TAG, "Paired Device Found");
		        break;
		    }
		}
		ConnectThread mConnectThread = new ConnectThread(pairedDevice);
		mConnectThread.start();
	}

	@Override
	public void stopStressTest() throws Exception {
		isExecutionStopped = true;
		if (receivingThread != null){
			receivingThread.cancel();
		}
		btadapter.disable();
	}

	@Override
	public String getName() {
		return name;
	}
	
	public BluetoothDownloadStressTest(Context context){
		this.context = context;
	}
	
	public void startReceivingThread(BluetoothSocket socket){
		if(!isExecutionStopped){
			receivingThread = new ReceivingThread(socket);
	        receivingThread.start();
		}
	}
	
	private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            btadapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }
            // Start the sending thread
            startReceivingThread(mmSocket);
            receivingThread.initializeReceiving();
        }
    }
	
	private class ReceivingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BufferedReader reader;
        private PrintWriter writer;

        public ReceivingThread(BluetoothSocket socket) {
            Log.d(TAG, "create ReceivingThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            reader = new BufferedReader(new InputStreamReader(mmInStream));
            writer = new PrintWriter(mmOutStream, true);
        }

        public void run() {
            Log.i(TAG, "BEGIN ReceivingThread");

            while(!isExecutionStopped){
            	try {
            		while ( (reader.readLine()) !=  null) {
            		}
            	} catch (IOException e) {
            		Log.e(TAG, "Error reading line", e);
            	}
            }
        }
        
        public void initializeReceiving(){
            writer.println(clientReceivingMessage);
        }
        
        public void cancel(){
        	try {
            	writer.println(clientEndMessage);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
        
    }

}
