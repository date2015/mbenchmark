package org.mbenchmark.stresstests;

import org.mbenchmark.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CPUEdgeDetectionStressTest implements StressTestInterface{
	
	private Bitmap bitmap;
	private CPUEdgeDetectionBenchmarkThread cbt;
	private String name = "CPU (Edge Detection)";
	private CannyEdgeDetector detector;
	private boolean isExecutionStopped = false;

	public CPUEdgeDetectionStressTest(Context context){
		this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bitmap);
	}
	
	public void startStressTest() {	
		detector = new CannyEdgeDetector();
		cbt = new CPUEdgeDetectionBenchmarkThread();
		cbt.start();
	}

	public void stopStressTest() {
		isExecutionStopped = true;
		cbt.stopExecution();
		detector = null;
		bitmap = null;
		cbt = null;
	}
	
	public String getName(){
		return name;
	}

	public class CPUEdgeDetectionBenchmarkThread extends Thread {
	
		public void run() {
			boolean result = edgeDetection();
			if (result && !isExecutionStopped){
				startStressTest();
			}
		}
	
		public boolean edgeDetection(){
			
			detector.setLowThreshold(0.5f);
			detector.setHighThreshold(1.0f);
			detector.setSourceImage(bitmap);
			return detector.process();
		}
		
		public void stopExecution(){
			detector.setExecutionStopped(true);
		}
	
		
	}
}
