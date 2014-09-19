package org.sense.cpu;

public interface CpuSenseInterface {

	int getNumberOfCPUs() throws CpuSenseException;

	int getCpuCurrentFrequency(int num) throws CpuSenseException;

	int getCpuMaxScaling(int cpu_index) throws CpuSenseException;

	int getCpuMinScaling(int cpu_index) throws CpuSenseException;

	int getCpuMaxFrequency(int cpu_index) throws CpuSenseException;

	int getCpuMinFrequency(int cpu_index) throws CpuSenseException;

	String getCpuGovernor(int cpu_index) throws CpuSenseException;

	float getCpuUsage(int cpu_index) throws CpuSenseException;
	
}
