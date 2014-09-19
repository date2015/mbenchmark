package org.sense.hw_sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class AccelerometerSense extends GenericSensorSense{
	
	public AccelerometerSense(Context cont) {
		super(cont, Sensor.TYPE_ACCELEROMETER);
	   
	}
	
	public double getXAcceleration(){
		 double accX = - (super.sensorEvent.values[0])/SensorManager.GRAVITY_EARTH;
		 double accY = -( super.sensorEvent.values[1])/SensorManager.GRAVITY_EARTH;
		 double accZ = ( super.sensorEvent.values[2])/SensorManager.GRAVITY_EARTH;
		 double totAcc = Math.sqrt((accX*accX)+(accY*accY)+(accZ*accZ));
		 double linX = Math.asin(accX/totAcc);
		 return linX;	
	}
	
	public double getYAcceleration(){
		 double accX = - (super.sensorEvent.values[0])/SensorManager.GRAVITY_EARTH;
		 double accY = -( super.sensorEvent.values[1])/SensorManager.GRAVITY_EARTH;
		 double accZ = ( super.sensorEvent.values[2])/SensorManager.GRAVITY_EARTH;
		 double totAcc = Math.sqrt((accX*accX)+(accY*accY)+(accZ*accZ));
		 double linY = Math.asin(accY/totAcc);
		 return linY;		
	}
	
	public double getZAcceleration(){
		 double accX = - (super.sensorEvent.values[0])/SensorManager.GRAVITY_EARTH;
		 double accY = -( super.sensorEvent.values[1])/SensorManager.GRAVITY_EARTH;
		 double accZ = ( super.sensorEvent.values[2])/SensorManager.GRAVITY_EARTH;
		 double totAcc = Math.sqrt((accX*accX)+(accY*accY)+(accZ*accZ));
		 double linZ = Math.asin(accZ/totAcc);
		 return linZ;	
	}

}
