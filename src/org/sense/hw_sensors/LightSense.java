package org.sense.hw_sensors;

import android.content.Context;
import android.hardware.Sensor;

public class LightSense extends GenericSensorSense {

	public LightSense(Context cont) {
		super(cont, Sensor.TYPE_LIGHT);
	}
	
	public double getAmbientLight(){
		try{
		return super.sensorEvent.values[0];
		}catch(NullPointerException e){
			return -1;
			}
		}
		
	
}
