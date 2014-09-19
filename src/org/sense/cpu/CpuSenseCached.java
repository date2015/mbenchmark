package org.sense.cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.util.Log;


/**
 * This implementation is meant to cache the static information retrieved, 
 * throwing exceptions if cpu files are not found.
 * 
 * NOTE: on some devices (e.g.: LG Nexus 4), this behavior is wrong, since
 * cpu files are created/removed as soon as cores are turned on/off.
 * This is the reason why a new "polling" implementation has been developed.
 *
 */
public class CpuSenseCached implements CpuSenseInterface {
		
	final String TAG = "org.morphone.sense";
	
	private Integer cpu_number = 0;					// Number of CPUs on the device
	
	private ArrayList<Integer> cpu_freq_max = new ArrayList<Integer>();
	private ArrayList<Integer> cpu_freq_min = new ArrayList<Integer>();
	
	private ArrayList<Integer> cpu_freq_scaling_max = new ArrayList<Integer>();
	private ArrayList<Integer> cpu_freq_scaling_min = new ArrayList<Integer>();
	
	private ArrayList<String> cpu_governor = new ArrayList<String>();
	
	
	private static final String CPU_FREQ_CURRENT = "/cpufreq/scaling_cur_freq";
	private static final String CPU_FREQ_MAX = "/cpufreq/cpuinfo_max_freq";
	private static final String CPU_FREQ_MIN = "/cpufreq/cpuinfo_min_freq";
	
	private static final String CPU_FREQ_SCALING_MAX = "/cpufreq/scaling_max_freq";
	private static final String CPU_FREQ_SCALING_MIN = "/cpufreq/scaling_min_freq";
	private static final String CPU_FREQ_SCALING_GOVERNOR = "/cpufreq/scaling_governor";
	
	private static final String CPU_DIRECTORY = "/sys/devices/system/cpu/";
	private static final String CPU_NAME_PATTERN = "cpu[0-9]";
	
	private static final String CPU_USAGE_STATS = "/proc/stat";
	
    private class CpuNameFilter implements FileFilter {		// Filter for CPU devices
        @Override
        public boolean accept(File pathname) {
            // Filename is "cpu", followed by a single digit number
            return Pattern.matches(CPU_NAME_PATTERN, pathname.getName());
        }      
    }
    
    public CpuSenseCached(){
    	
    	try {
    		File dir = new File(CPU_DIRECTORY);					// Get dir
 	        File[] files = dir.listFiles(new CpuNameFilter());	// list cpus
 	        
 	        // TODO: compare this number with Runtime.getRuntime().availableProcessors()==2
 	       cpu_number = files.length;
 	    } catch(Exception e){
 	    	cpu_number = 1;
 	    } 
    	
    	// Cache information in local variables
    	for(int i = 0; i < cpu_number; i++){	
    		try{
    			int current_cpu_freq_max = getValue(CPU_DIRECTORY + "cpu" + i + CPU_FREQ_MAX);
        		cpu_freq_max.add(current_cpu_freq_max);
    		}catch(Exception e){
    			cpu_freq_max.add(-1);
    		}
    		
    		try{
    			int current_cpu_freq_min = getValue(CPU_DIRECTORY + "cpu" + i + CPU_FREQ_MIN);
        		cpu_freq_min.add(current_cpu_freq_min);
    		}catch(Exception e){
    			cpu_freq_min.add(-1);
    		}
    		
    		try{
    			int current_cpu_freq_scaling_max = getValue(CPU_DIRECTORY + "cpu" + i + CPU_FREQ_SCALING_MAX);
        		cpu_freq_scaling_max.add(current_cpu_freq_scaling_max);
    		}catch(Exception e){
    			cpu_freq_scaling_max.add(-1);
    		}
    		
    		try{
    			int current_cpu_freq_scaling_min = getValue(CPU_DIRECTORY + "cpu" + i + CPU_FREQ_SCALING_MIN);
        		cpu_freq_scaling_min.add(current_cpu_freq_scaling_min);
    		}catch(Exception e){
    			cpu_freq_scaling_min.add(-1);
    		}
    		
    		try{
    			String current_cpu_governor = getValueString(CPU_DIRECTORY + "cpu" + i + CPU_FREQ_SCALING_GOVERNOR);
        		cpu_governor.add(current_cpu_governor);	
    		}catch(Exception e){
        		cpu_governor.add("not found");	
    		}
    	}
    }
	
	@Override
	public int getNumberOfCPUs() throws CpuSenseException{
		if(cpu_number > 0)
			return cpu_number;
		else
			throw new CpuSenseException("Error while getting cpu number");
	}

	@Override
	public int getCpuCurrentFrequency(int cpu_index) throws CpuSenseException{
		return getValue(CPU_DIRECTORY + "cpu" + cpu_index + CPU_FREQ_CURRENT);
	}
	
	@Override
	public int getCpuMaxScaling(int cpu_index) throws CpuSenseException{
		if(cpu_index < cpu_freq_scaling_max.size()){
			int value = cpu_freq_scaling_max.get(cpu_index);
			if(value != -1)
				return value;
			else
				throw new CpuSenseException("Error while getting cpu_freq_scaling_max.get(" + cpu_index + "): value = -1");
		}else
			throw new CpuSenseException("Error while getting cpu_freq_scaling_max.get(" + cpu_index + "): cpu_index out of range");
	}

	@Override
	public int getCpuMinScaling(int cpu_index) throws CpuSenseException{
		if(cpu_index < cpu_freq_scaling_min.size()){
			int value = cpu_freq_scaling_min.get(cpu_index);
			if(value != -1)
				return value;
			else
				throw new CpuSenseException("Error while getting cpu_freq_scaling_min.get(" + cpu_index + "): value = -1");
		}else
			throw new CpuSenseException("Error while getting cpu_freq_scaling_min.get(" + cpu_index + "): cpu_index out of range");
	}
	
	@Override
	public int getCpuMaxFrequency(int cpu_index) throws CpuSenseException{
		if(cpu_index < cpu_freq_max.size()){
			int value = cpu_freq_max.get(cpu_index);
			if(value != -1)
				return value;
			else
				throw new CpuSenseException("Error while getting cpu_freq_max.get(" + cpu_index + "): value = -1");
		}else
			throw new CpuSenseException("Error while getting cpu_freq_max.get(" + cpu_index + "): cpu_index out of range");
	}
	
	@Override
	public int getCpuMinFrequency(int cpu_index) throws CpuSenseException{
		if(cpu_index < cpu_freq_min.size()){
			int value = cpu_freq_min.get(cpu_index);
			if(value != -1)
				return value;
			else
				throw new CpuSenseException("Error while getting cpu_freq_min.get(" + cpu_index + "): value = -1");
		}else
			throw new CpuSenseException("Error while getting cpu_freq_min.get(" + cpu_index + "): cpu_index out of range");
	}
	
	@Override
	public String getCpuGovernor(int cpu_index) throws CpuSenseException{
		if(cpu_index < cpu_governor.size()){
			String value = cpu_governor.get(cpu_index);
			if(!value.equals("not found"))
				return value;
			else
				throw new CpuSenseException("Error while getting cpu_governor.get(" + cpu_index + "): value = not found");
		}else
			throw new CpuSenseException("Error while getting cpu_governor.get(" + cpu_index + "): cpu_index out of range");
	}
	
	@Override
	public float getCpuUsage(int cpu_index) throws CpuSenseException {
	    try {
	        String load = "";

	    	/**
	    	 * Example of /proc/stat: 
	    	 	cpu  2255 34 2290 22625563 6290 127 456
	    		cpu0 1132 34 1441 11311718 3675 127 438
	    		cpu1 1123 0 849 11313845 2614 0 18
	    		...
	    	 * Implemented method:
	    		[1] user: normal processes executing in user mode
				[2] nice: niced processes executing in user mode
				[3] system: processes executing in kernel mode
				[4] idle: twiddling thumbs
				find previous_idle ([4]) and previous_cpu (sum of [2][3][1])
		    	Sleep for 360 millis
		    	find current_idle ([4]) and current_cpu (sum of [2][3][1])
		    	proc stat return (float)(current_cpu - previous_cpu) / ((current_cpu + current_idle) - (previous_cpu + previous_idle))*100;
	    	*/
	        RandomAccessFile reader = new RandomAccessFile(CPU_USAGE_STATS, "r");
	   
	        // Skip lines until the right CPU
	        for(int i = 0; i <= cpu_index; i++)
	        	load = reader.readLine();

	        load = reader.readLine();
	        String[] toks = load.split(" ");			// Parse on white spaces
	        long previous_idle = Long.parseLong(toks[4]);
	        long previous_cpu = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[1]);
	        
	        try {
	            Thread.sleep(360);						// Sleep for an epsilon time  
	        } catch (Exception e) {}

	        reader.seek(0);								// Go back in the same file
	        
	        // Skip lines until the right CPU
	        for(int i = 0; i <= cpu_index; i++)
	        	load = reader.readLine();

	        load = reader.readLine();
	        reader.close();
	        
	        toks = load.split(" ");						// Parse on white spaces
	        long current_idle = Long.parseLong(toks[4]);
	        long current_cpu = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[1]);

	        return (float)(current_cpu - previous_cpu) / ((current_cpu + current_idle) - (previous_cpu + previous_idle))*100;

	    } catch (Exception e) {
	    	throw new CpuSenseException("Error while getting cpu usage (" + e.getMessage() + ")");
	    }
	} 
	

	private int getValue(String path) throws CpuSenseException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(path), 8*24);
			int value = Integer.parseInt(in.readLine());
			in.close();
			return value;
		} catch (Exception e) {
			throw new CpuSenseException("Error while accessing int in file: " + path + "  (" + e.getMessage() + ")");
		}
	}
	
	private String getValueString(String path) throws CpuSenseException{
		try {
			BufferedReader in = new BufferedReader(new FileReader(path), 8*24);
			String value = in.readLine();
			in.close();
			return value;
		} catch (Exception e) {
			throw new CpuSenseException("Error while accessing string in file: " + path + "  (" + e.getMessage() + ")");
		}
	}

}
