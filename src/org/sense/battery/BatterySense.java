package org.sense.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatterySense extends BroadcastReceiver 
							implements BatterySenseInterface{

	private int charge_on = -1;
	private int health = -1;
	private int temperature = -1;
	private int voltage = -1;
	private int percentage = -1;
	private String technology = "";
	private int charge_on_prev = -1;
	private int status_prev = -1;

	public BatterySense(Context context){
		
		IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent currentBatteryCharge = context.registerReceiver(null, batteryIntentFilter);
		
		// current battery level, from 0 to EXTRA_SCALE.
		int rawlevel = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        
        charge_on = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        health = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        technology = currentBatteryCharge.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        
        if (rawlevel>=0 && scale>0) {
        	percentage = (rawlevel*100)/scale;
        }
        
        temperature = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        voltage = currentBatteryCharge.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
	}
	
	@Override
	public boolean isCharging() throws BatterySenseException{
		if(charge_on < 0)
			throw new BatterySenseException("Error while getting charge_on");
		else{
			if(charge_on==0)
				return false;
			else
				return true;
		}
	}
	
	@Override
	public int getPercentage() throws BatterySenseException{
		if(percentage < 0)
			throw new BatterySenseException("Error while getting percentage");
		else
			return percentage;
	}
	
	@Override
	public int getTemperature() throws BatterySenseException{
		if(temperature < 0)
			throw new BatterySenseException("Error while getting temperature");
		else
			return temperature;
	}
	
	@Override
	public int getVoltage() throws BatterySenseException{
		if(voltage < 0)
			throw new BatterySenseException("Error while getting voltage");
		else
			return voltage;
	}

	@Override
	public String getTechnology() throws BatterySenseException {
		if(technology.equals(""))
			throw new BatterySenseException("Error while getting technology");
		else
			return technology;
	}

	@Override
	public int getHealth() throws BatterySenseException {
		if(health < 0)
			throw new BatterySenseException("Error while getting health");
		else
			return health;
	}

	
	@Override
	public void onReceive(Context context, Intent intent) {
		charge_on_prev = charge_on;
		
		// current battery level, from 0 to EXTRA_SCALE.
		int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        
        charge_on = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        
        int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                 
        if (rawlevel>=0 && scale>0) {
        	percentage = (rawlevel*100)/scale;
        }    
         
        if(status!=status_prev && status_prev!=-1)
        {
        	if(status_prev==BatteryManager.BATTERY_STATUS_FULL && 
        			(status==BatteryManager.BATTERY_STATUS_DISCHARGING || 
        			status==BatteryManager.BATTERY_STATUS_NOT_CHARGING) 
        			&& percentage==100){
        		Intent i = new Intent("mpower_event");  
    			i.putExtra("type", "msg_battery_full");
    			context.sendBroadcast(i);
        	 }
         }
        status_prev=status;

        if(charge_on==BatteryManager.BATTERY_PLUGGED_AC || charge_on== BatteryManager.BATTERY_PLUGGED_USB 
        		&& charge_on_prev!=BatteryManager.BATTERY_PLUGGED_AC && charge_on_prev!=BatteryManager.BATTERY_PLUGGED_USB)
        {
         
        	Intent i = new Intent("mpower_event");  
			i.putExtra("type", "msg_plugged_on");
			context.sendBroadcast(i);
        }
         
        temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
	}
	
}
