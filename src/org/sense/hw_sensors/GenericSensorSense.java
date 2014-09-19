package org.sense.hw_sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GenericSensorSense {

	SensorManager sensorManager;
	Context context;
	Sensor currentSensor;
	int currentAccuracy;
	SensorEvent sensorEvent;
	
	public GenericSensorSense(Context cont, int sensor_type) {
		this.context = cont;
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(new MyListener(),
				sensorManager.getDefaultSensor(sensor_type),
				SensorManager.SENSOR_DELAY_NORMAL);
		}
	
	
	
	private class MyListener implements SensorEventListener{

		// TODO TeoF: check if the Override annotation has to be here
		// @Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			currentSensor = sensor;
			currentAccuracy = accuracy;
		}

		// TODO TeoF: check if the Override annotation has to be here
		// @Override
		public void onSensorChanged(SensorEvent event) {
			sensorEvent = event;
		}
		
		
	}
}
