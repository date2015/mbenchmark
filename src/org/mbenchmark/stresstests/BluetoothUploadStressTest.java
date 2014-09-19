package org.mbenchmark.stresstests;

import java.io.IOException;
import java.io.InputStream;
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

public class BluetoothUploadStressTest implements StressTestInterface {

	private String name = "Bluetooth Upload";
	private final String TAG = "Bluetooth Upload Benchmark";
	
	private BluetoothDevice pairedDevice;
	private String pairedDeviceName = "U8150";
	private boolean isExecutionStopped = false;
	private Context context;
	private BluetoothAdapter btadapter;
	private SendingThread sendingThread;
	
	private static final UUID MY_UUID = UUID.fromString("8ce255d0-200a-11e0-ac64-0800200c9a66");
	private static final String clientSendingMessage = "CLIENT_SENDING";
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
		if (sendingThread != null){
			sendingThread.cancel();
		}
		btadapter.disable();
	}

	@Override
	public String getName() {
		return name;
	}
	
	public BluetoothUploadStressTest(Context context){
		this.context = context;
	}
	
	public void startSendingThread(BluetoothSocket socket){
		if(!isExecutionStopped){
			sendingThread = new SendingThread(socket);
	        sendingThread.start();
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
            startSendingThread(mmSocket);
            sendingThread.initializeSending();
        }
    }
	
	private class SendingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private InputStream fileInputStream;
        private PrintWriter writer;

        public SendingThread(BluetoothSocket socket) {
            Log.d(TAG, "create SendingThread");
            mmSocket = socket;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpOut = socket.getOutputStream();
                fileInputStream = context.getResources().openRawResource(R.drawable.bitmap);
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmOutStream = tmpOut;
            writer = new PrintWriter(mmOutStream, true);
        }

        public void run() {
            Log.i(TAG, "BEGIN SendingThread");
            int maxBufferSize = 1*1024*1024;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            
            try {
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0 && !isExecutionStopped){
                	writer.println(buffer);
                	bytesAvailable = fileInputStream.available();
                	bufferSize = Math.min(bytesAvailable, maxBufferSize);
                	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                fileInputStream.close();
            } 
            catch (IOException e) {
                    Log.e(TAG, "IOException in mConnectedThread", e);
            }
            
            startSendingThread(mmSocket);
        }

        public void initializeSending(){
            writer.println(clientSendingMessage);
        }
        
        public void cancel() {
            try {
            	writer.println(clientEndMessage);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
	

}
