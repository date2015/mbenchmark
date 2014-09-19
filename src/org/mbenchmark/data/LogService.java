package org.mbenchmark.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sense.audio.AudioSense;
import org.sense.audio.AudioSenseException;
import org.sense.battery.BatterySense;
import org.sense.battery.BatterySenseException;
import org.sense.bluetooth.BluetoothSenseException;
import org.sense.bluetooth.BluetoothSenseReceiver;
import org.sense.cpu.CpuSenseException;
import org.sense.cpu.CpuSensePolling;
import org.sense.location.LocationSense;
import org.sense.location.LocationSenseException;
import org.sense.memory.MemorySense;
import org.sense.memory.MemorySenseException;
import org.sense.mobile.MobileSense;
import org.sense.mobile.MobileSenseException;
import org.sense.screen.ScreenSenseException;
import org.sense.screen.ScreenSenseReceiver;
import org.sense.wifi.WifiSenseException;
import org.sense.wifi.WifiSenseReceiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

/**
 * Main MBenchmark service.
 * It's in charge of sampling and logging the device status every SLEEP_SECONDS.
 */

// TODO allow the Activity to stop this service immediately

public class LogService extends Service{

	private final String TAG = "LogService";
	private final String LOG_ERRORS_TAG = "LogServiceErrors";
	
	
	// TODO: check if it's possible to set these in a properties file
	private static final int LOG_FREQUENCY_SECONDS = 10;
	private static final long CHECK_FILE_FREQUENCY = AlarmManager.INTERVAL_HOUR;
	
	// Sensing objects
	private AudioSense audioSense; 
	private BroadcastReceiver batterySense;
	private BroadcastReceiver bluetoothSense;
	private CpuSensePolling cpuSensePolling;
	private LocationSense locationSense;
	private MobileSense mobileSense;
	private MemorySense memorySense;
	private BroadcastReceiver wifiSense;
	private ScreenSenseReceiver screenSense;

	private FileHelper fileHelper;
	private String stressTestName;
	
	// Files checking intent and alarm
	private static Intent globalIntent;
	private static AlarmManager checkFileAlarm;
	
	// Logging Loop variables
	private Handler loggingLoopHandler = new Handler();
	private PowerManager powerManager;
	private WakeLock wakelock = null; 

	// Log variables
    private long lastSampleTimestamp;
	private int charge_on;
	private int charge_percentage;
	private int battery_temperature;
	private int battery_voltage;
	private int battery_health;
	private String battery_technology;
	private int airplane_mode;
	private int wifi_active;
	private int wifi_signal_strenght;
	private int wifi_connected;
	private int screen_on;
	private int screen_brightness;
	private int mobile_network_type;		// GPRS=1, EDGE=2, UMTS=3, HSDPA=8
	private int mobile_signal_strenght;
	private int mobile_state;				// disconnected=0, connecting=1, connected=2, suspended=3
	private int mobile_activity;			// no_traffic=0, data_receiving=1, data_sending=2, data_rec/send=3, phisical_link_down=4
	private int call_state;					// idle=0, ringing=1, offhook=2
	private long mobile_tx_bytes, mobile_rx_bytes;
	private int bluetooth_on;				
	private int gps_on;
	private int numberOfCPUs;
	private int[] maxCpuFreq;
	private int[] minCpuFreq ;
	private int[] currentCpuFreq; 
	private String[] cpuGovernor;
	private int[] cpuMaxScaling;
	private int[] cpuMinScaling;
	private float[] cpuUsage;
	private long usedMemory;
	private int bluetooth_state;
	private float screen_w;
	private float screen_h;
	private float screen_refresh_rate;
	private float screen_orientation;
	private int screen_brightness_mode;
	private int wifi_link_speed;
	private long wifi_tx_bytes,	wifi_rx_bytes;
	private int gps_status;
	private int musicActive; 
	private int speakerOn; 
	private int musicVolume; 
	private int ringVolume; 
	private int voiceVolume;
	private int audioMode;
    
	@Override
	public void onCreate(){
		
		Log.d(TAG, "LogService start");
		
		audioSense = new AudioSense(getApplicationContext());
		
		batterySense = new BatterySense(getApplicationContext());
		IntentFilter intentFilterBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batterySense, intentFilterBattery);
		
		bluetoothSense = new BluetoothSenseReceiver();
		IntentFilter intentFilterBluetooth = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(bluetoothSense, intentFilterBluetooth);
		
		cpuSensePolling = new CpuSensePolling();

		locationSense = new LocationSense(getApplicationContext());
		
		memorySense = new MemorySense(getApplicationContext());
		
		mobileSense = new MobileSense(getApplicationContext());
		
		wifiSense = new WifiSenseReceiver(getApplicationContext());
		IntentFilter intentFilterWiFi = new IntentFilter();
		intentFilterWiFi.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilterWiFi.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilterWiFi.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(wifiSense, intentFilterWiFi);
		
		screenSense = new ScreenSenseReceiver(getApplicationContext());
		IntentFilter intentFilterScreen = new IntentFilter();
		intentFilterScreen.addAction(Intent.ACTION_SCREEN_ON);
		intentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenSense, intentFilterScreen);
		
		Log.d(TAG, "Sense objects initialized correctly");
		
		// Used for wake locks
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// Init CPUs variables
		try {
			numberOfCPUs = cpuSensePolling.getNumberOfCPUs();
			Log.d(TAG, "CPUs detected: " + numberOfCPUs);
		} catch (CpuSenseException e) {
			numberOfCPUs = 1;
			Log.e(TAG, "Error while detecting the number of CPUs: " + e.getMessage());
		}
		maxCpuFreq = new int[numberOfCPUs];
		minCpuFreq = new int[numberOfCPUs];
		currentCpuFreq = new int[numberOfCPUs];
		cpuGovernor = new String[numberOfCPUs];
		cpuMaxScaling = new int[numberOfCPUs];
		cpuMinScaling = new int[numberOfCPUs];
		cpuUsage = new float[numberOfCPUs];
		
		// Init the looping logic
		loggingLoopHandler.removeCallbacks(loggingTask);
        loggingLoopHandler.postDelayed(loggingTask, 0); 
		Log.d(TAG, "Looping tasks setup correctly");
		
		// Ask the EventHandler to check the file 
		globalIntent = new Intent();
		globalIntent.setAction("message");
		globalIntent.putExtra("type", "msg_check_file");
		sendBroadcast(globalIntent);
		
		// Set an alarm to check the file every CHECK_FILE_INTERVAL
		PendingIntent checkFile;
		checkFile = PendingIntent.getBroadcast(this, 999, globalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		checkFileAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		checkFileAlarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), CHECK_FILE_FREQUENCY, checkFile);
		Log.d(TAG, "LogService created");
	}
	
	
	// Logging Task: sample and log information on the file every LOG_FREQUENCY_SECONDS
	private Runnable loggingTask = new Runnable() {
		public void run() {
			try {
				// Acquire wakelock
				wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "loggingTask wakelock");
				wakelock.acquire();
				
				// Sampling
				sampleDeviceState();
				
				// Write log
				writeLog();	
				
				// Setup timeout for the next iteration
				loggingLoopHandler.postDelayed(this, LOG_FREQUENCY_SECONDS*1000);
			} catch (Exception e) {
				Log.e(TAG, "Exception in loggingTask: " + e.getMessage());
				e.printStackTrace();
			}finally{
				wakelock.release();
			}
		}
	};	
	
	// Sample the device state
	@SuppressLint({"NewApi"})
	@SuppressWarnings("deprecation")
	private void sampleDeviceState(){
		
		// TODO TeoF: handle exceptions (report&tracking)
		String logErrors = "";
		
		lastSampleTimestamp = System.currentTimeMillis();
		while(lastSampleTimestamp == 0)
			lastSampleTimestamp = System.currentTimeMillis();

		// airplane mode
		try {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
				airplane_mode = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON);
			}
			else{
				airplane_mode = android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON);
			}
		} catch (SettingNotFoundException e) {
			Log.e("SENSING_AIRPLANE", "Errore while reading setting");
			e.printStackTrace();
		}
		
		// For every CPU which information is available
		for(int i = 0; i < numberOfCPUs; i++){
					
			try{
				maxCpuFreq[i] = cpuSensePolling.getCpuMaxFrequency(i);
			}catch (CpuSenseException e) {
				maxCpuFreq[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
			
			try{
				currentCpuFreq[i] = cpuSensePolling.getCpuCurrentFrequency(i);
			}catch (CpuSenseException e) {
				currentCpuFreq[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
			
			try{
				cpuGovernor[i] = cpuSensePolling.getCpuGovernor(i);
			}catch (CpuSenseException e) {
				cpuGovernor[i] = null;
				logErrors += e.getMessage() + "\n";
			}
				
			try{
				cpuMaxScaling[i] = cpuSensePolling.getCpuMaxScaling(i);
			}catch (CpuSenseException e) {
				cpuMaxScaling[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
			
			try{
				minCpuFreq[i]  = cpuSensePolling.getCpuMinFrequency(i);
			}catch (CpuSenseException e) {
				minCpuFreq[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
			
			try{
				cpuMinScaling[i] = cpuSensePolling.getCpuMinScaling(i);
			}catch (CpuSenseException e) {
				cpuMinScaling[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
			
			try{
				cpuUsage[i] = cpuSensePolling.getCpuUsage(i);
			}catch (CpuSenseException e) {
				cpuUsage[i] = -1;
				logErrors += e.getMessage() + "\n";
			}
		}

		
		/* MEMORY */
		try{
			usedMemory = memorySense.getUsedMemory();
		}catch (MemorySenseException e) {
			usedMemory = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		
		/* MOBILE CONNECTION */
		try {
			mobile_state = mobileSense.getDataState();
		} catch (MobileSenseException e) {
			mobile_state = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			mobile_activity = mobileSense.getDataActivity();
		} catch (MobileSenseException e) {
			mobile_activity = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			mobile_network_type = mobileSense.getNetworkType();
		} catch (MobileSenseException e) {
			mobile_network_type = -1;
			logErrors += e.getMessage() + "\n";
		}	
		try {
			call_state = mobileSense.getCallState();
		} catch (MobileSenseException e) {
			call_state = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			mobile_tx_bytes = mobileSense.getTxBytes();
		} catch (MobileSenseException e) {
			mobile_tx_bytes = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			mobile_rx_bytes = mobileSense.getRxBytes();
		} catch (MobileSenseException e) {
			mobile_rx_bytes = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			mobile_signal_strenght = mobileSense.getSignalStrenght();
		} catch (MobileSenseException e) {
			mobile_signal_strenght = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		
		/* WIFI CONNECTION */
		try {
			wifi_active = ((WifiSenseReceiver) wifiSense).isActive() ? 1 : 0;
		} catch (WifiSenseException e) {
			wifi_active = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			wifi_signal_strenght = ((WifiSenseReceiver) wifiSense).getSignalStrenght();
		} catch (WifiSenseException e) {
			wifi_signal_strenght = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			wifi_connected = ((WifiSenseReceiver) wifiSense).isConnected() ? 1 : 0;
		} catch (WifiSenseException e) {
			wifi_connected = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			wifi_link_speed = ((WifiSenseReceiver) wifiSense).getLinkSpeed();
		} catch (WifiSenseException e) {
			wifi_link_speed = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			wifi_tx_bytes = ((WifiSenseReceiver) wifiSense).getTxBytes();
		} catch (WifiSenseException e) {
			wifi_tx_bytes = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			wifi_rx_bytes = ((WifiSenseReceiver) wifiSense).getRxBytes();
		} catch (WifiSenseException e) {
			wifi_rx_bytes = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		
		/* GPS */
		try {
			gps_on = locationSense.isGPSOn() ? 1 : 0;
		} catch (LocationSenseException e) {
			gps_on = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			gps_status = locationSense.getCurrentProviderStatus();
		} catch (LocationSenseException e) {
			gps_status = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		
		/* BLUETOOTH */
		try {
			bluetooth_on = ((BluetoothSenseReceiver) bluetoothSense).isActive() ? 1 : 0;
		} catch (BluetoothSenseException e) {
			bluetooth_on = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			bluetooth_state = ((BluetoothSenseReceiver) bluetoothSense).getState();
		} catch (BluetoothSenseException e) {
			bluetooth_state = -1;
			logErrors += e.getMessage() + "\n";
		}

		
		/* SCREEN */
		try {
			screen_brightness_mode = screenSense.getScreenBrightnessMode();
		} catch (ScreenSenseException e) {
			screen_brightness_mode = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			screen_brightness = screenSense.getScreenBrightness();
		} catch (ScreenSenseException e) {
			screen_brightness = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			screen_on = screenSense.isScreenOn() ? 1 : 0;
		} catch (ScreenSenseException e) {
			screen_on = -1;
			logErrors += e.getMessage() + "\n";
		}

		try {
			screen_w = screenSense.getDisplayWidth();
		} catch (ScreenSenseException e) {
			screen_w = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			screen_h = screenSense.getDisplayHeight();
		} catch (ScreenSenseException e) {
			screen_h = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			screen_refresh_rate = screenSense.getDisplayRefreshRate();
		} catch (ScreenSenseException e) {
			screen_refresh_rate = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			screen_orientation = screenSense.getOrientation();
		} catch (ScreenSenseException e) {
			screen_orientation = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		
		/* BATTERY STATE */
		try {
			charge_on = ((BatterySense) batterySense).isCharging() ? 1 : 0;
		} catch (BatterySenseException e) {
			charge_on = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		try {
			charge_percentage = ((BatterySense) batterySense).getPercentage();
		} catch (BatterySenseException e) {
			charge_percentage = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			battery_temperature = ((BatterySense) batterySense).getTemperature();
		} catch (BatterySenseException e) {
			battery_temperature = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			battery_voltage = ((BatterySense) batterySense).getVoltage();
		} catch (BatterySenseException e) {
			battery_voltage = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			battery_health = ((BatterySense) batterySense).getHealth();
		} catch (BatterySenseException e) {
			battery_health = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			battery_technology = ((BatterySense) batterySense).getTechnology();
		} catch (BatterySenseException e) {
			battery_technology = "not specified";
			logErrors += e.getMessage() + "\n";
		}
		
		/* AUDIO */
		try {
			musicActive = audioSense.isMusicActive() ? 1 : 0;
		} catch (AudioSenseException e) {
			musicActive = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			speakerOn = audioSense.isSpeakerOn() ? 1 : 0;
		} catch (AudioSenseException e) {
			speakerOn = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			musicVolume = audioSense.getMusicVolume();
		} catch (AudioSenseException e) {
			musicVolume = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			ringVolume = audioSense.getRingVolume();
		} catch (AudioSenseException e) {
			ringVolume = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			voiceVolume = audioSense.getVoiceVolume();
		} catch (AudioSenseException e) {
			voiceVolume = -1;
			logErrors += e.getMessage() + "\n";
		}
		try {
			audioMode = audioSense.getMode();
		} catch (AudioSenseException e) {
			audioMode = -1;
			logErrors += e.getMessage() + "\n";
		}
		
		// Show log string if any error
		if(!logErrors.equals(""))
			Log.e(LOG_ERRORS_TAG, logErrors);
	}

	private void writeLog() throws Exception {
		String log = jsonLog() + "\n";
		
		Log.d(TAG, "Trying to write the log on external storage");
		try {			
			fileHelper.getFileOutputStream().write(log.toString().getBytes());
        } catch (Exception e) {
            Log.e(TAG,"Error writing on external storage: " + e.getMessage());
        }
	}
	
	// Format the log string as JSON
	private String jsonLog() throws JSONException{
		
		/* Complete log */
		JSONObject log = new JSONObject();
		log.put("timestamp", lastSampleTimestamp);		
		
		
		/* CPU */
		JSONArray cpusLog = new JSONArray();
		for(int i = 0; i < numberOfCPUs; i++){
			JSONObject cpuLog = new JSONObject();
			cpuLog.put("cpu_id", i);
			cpuLog.put("max_freq", maxCpuFreq[i]);
			cpuLog.put("min_freq", minCpuFreq[i]);
			cpuLog.put("current_freq", currentCpuFreq[i]);
			cpuLog.put("max_freq_scaling", cpuMaxScaling[i]);
			cpuLog.put("min_freq_scaling", cpuMinScaling[i]);
			cpuLog.put("governor", cpuGovernor[i]);
			
			// TODO TeoF: this is just for debugging purpose
			try{
				cpuLog.put("usage", cpuUsage[i]);
			} catch (Exception e){
				cpuLog.put("usage", -10);
			}
			
			cpusLog.put(cpuLog);
		}
		
		log.putOpt("cpu", cpusLog);
		
		
		/* Battery */
		JSONObject batteryLog = new JSONObject();
		batteryLog.put("on_charge", charge_on);
		batteryLog.put("temperature", (float)(battery_temperature/10));
		batteryLog.put("voltage", battery_voltage);
		batteryLog.put("percentage", charge_percentage);
		batteryLog.put("technology", battery_technology);
		batteryLog.put("health", battery_health);
		
		log.putOpt("battery", batteryLog);
		
		
		/* Bluetooth */
		JSONObject bluetoothLog = new JSONObject();
		bluetoothLog.put("is_on", bluetooth_on);
		bluetoothLog.put("state", bluetooth_state);
		
		log.putOpt("bluetooth", bluetoothLog);
		
		
		/* GPS */
		JSONObject gpsLog = new JSONObject();
		gpsLog.put("is_on", gps_on);
		gpsLog.put("status", gps_status);
		
		log.putOpt("gps", gpsLog);
		
		
		/* Mobile */
		JSONObject mobileLog = new JSONObject();
		mobileLog.put("call_state", call_state);
		mobileLog.put("airplane_mode", airplane_mode);
		mobileLog.put("state", mobile_state);
		mobileLog.put("data_activity", mobile_activity);
		mobileLog.put("net_type", mobile_network_type);
		mobileLog.put("signal_strenght", mobile_signal_strenght);
		mobileLog.put("tx_bytes", mobile_tx_bytes);
		mobileLog.put("rx_bytes", mobile_rx_bytes);
		
		log.putOpt("mobile", mobileLog);
		
		
		/* Screen */
		JSONObject screenLog = new JSONObject();
		screenLog.put("is_on", screen_on);
		screenLog.put("brightness_mode", screen_brightness_mode);
		screenLog.put("brightness_value", screen_brightness);
		screenLog.put("width", screen_w);
		screenLog.put("height", screen_h);
		screenLog.put("refresh_rate", screen_refresh_rate);
		screenLog.put("orientation", screen_orientation);
		// screenLog.put("max_brightness", screen_max_brightness);
		
		log.putOpt("screen", screenLog);
		
		
		/* WiFi */
		JSONObject wifiLog = new JSONObject();
		wifiLog.put("is_on", wifi_active);
		wifiLog.put("is_connected", wifi_connected);
		wifiLog.put("signal_strenght", wifi_signal_strenght);
		wifiLog.put("link_speed", wifi_link_speed);
		wifiLog.put("tx_bytes", wifi_tx_bytes);
		wifiLog.put("rx_bytes", wifi_rx_bytes);
		
		log.putOpt("wifi", wifiLog);
		
		
		/* Audio */
		JSONObject audioLog = new JSONObject();
		audioLog.put("music_active", musicActive);
		audioLog.put("speaker_on", speakerOn);
		audioLog.put("music_volume", musicVolume);
		audioLog.put("ring_volume", ringVolume);
		audioLog.put("voice_volume", voiceVolume);
		audioLog.put("audio_mode", audioMode);
		audioLog.put("used_memory", usedMemory);
		
		log.putOpt("audio", audioLog);
		return log.toString();
	}	
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		stressTestName = intent.getStringExtra("stressTestName");
		Log.d(TAG, "Creating FileHelper with stress test name: "+stressTestName);
		fileHelper = new FileHelper(getApplicationContext(), stressTestName);
	    return START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		// Unregister receivers
		unregisterReceiver(batterySense);
		unregisterReceiver(bluetoothSense);
		unregisterReceiver(wifiSense);
		unregisterReceiver(screenSense);
		
		loggingLoopHandler.removeCallbacks(loggingTask);
		
		Log.d(TAG, "LogService destroyed");
	}		
}


