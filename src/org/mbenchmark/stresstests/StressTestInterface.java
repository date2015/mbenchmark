package org.mbenchmark.stresstests;

public interface StressTestInterface {
	
	public void startStressTest() throws Exception;
	
	public void stopStressTest() throws Exception;
	
	public String getName();

}
