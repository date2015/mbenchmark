package org.mbenchmark.stresstests;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sense.cpu.CpuSenseException;
import org.sense.cpu.CpuSensePolling;

import android.util.Log;

public class CPUIntegerStressTest implements StressTestInterface{
	
	private CPUIntegerBenchmarkThread thread;
	private String name = "CPU (Integer)";
	private boolean isExecutionStopped = false;
	private CpuSensePolling cpuSensePolling;
	private int numberOfThreads;
	private final String TAG = "CPUIntegerBenchmark";
	private ThreadPoolExecutor executor;
	private static final long KEEP_ALIVE_TIME = Long.MAX_VALUE;
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.NANOSECONDS;
	private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

	public CPUIntegerStressTest(){
		super();
		cpuSensePolling = new CpuSensePolling();
		try {
			numberOfThreads = cpuSensePolling.getNumberOfCPUs()*2;
		} catch (CpuSenseException e) {
			numberOfThreads = 2;
			Log.e(TAG, "Error while detecting the number of CPUs: " + e.getMessage());
		}
		executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, workQueue, new ThreadPoolExecutor.DiscardPolicy());
	}
	
	public void startStressTest() {
		for(int i=0; i<numberOfThreads; i++){
			thread = new CPUIntegerBenchmarkThread();
			executor.execute(thread);
		}
	}

	public void stopStressTest() {
		isExecutionStopped = true;
		executor.shutdownNow();
	}
	
	public String getName(){
		return name;
	}
	
	public void addNewThread(){
		thread = new CPUIntegerBenchmarkThread();
		executor.execute(thread);
	}

	public class CPUIntegerBenchmarkThread implements Runnable {
		
		
		private int a = 34;
		private int b = 78;
		private int c;
		
		public void run() {
			for(int i=0; i<1000 && !isExecutionStopped; i++){
				c= (a*a)-(b*2);
				c = c + c;
			}
			if (!isExecutionStopped){
				addNewThread();
			}
		}
		
	}
}
