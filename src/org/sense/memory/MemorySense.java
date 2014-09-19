package org.sense.memory;

import java.io.RandomAccessFile;

import android.content.Context;

public class MemorySense implements MemorySenseInterface {
	
	private long totalMemory = -1; 
	
	private static final String MEM_USAGE_INFO = "/proc/meminfo";
	
	public MemorySense(Context context){

		try {
			// Init totalMemory
			String load = "";
		    String[] toks = null;
		    
		    // Example of /proc/meminfo
		    //	MemTotal:         710776 kB
			//	...
			
		    RandomAccessFile reader = new RandomAccessFile(MEM_USAGE_INFO, "r");

		    // Read MemTotal
		    load = reader.readLine();
		    load = load.replace(" ", "");
		    toks = load.split(":");
		    totalMemory = Long.parseLong(toks[1].replace("kB", "").trim());
		    reader.close();
		} catch (Exception e) {
			totalMemory = -1;
		}
			
	    // NOT WORKING
		// ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// MemoryInfo mi = new MemoryInfo();
		// am.getMemoryInfo(mi);
		// totalMemory = mi.totalMem / 1024L;		// Total mem in KB
	}
	
	@Override
	public long getUsedMemory() throws MemorySenseException {
	    try {
	    	String load = "";
	    	String[] toks = null;
	    	
	    	// Example of /proc/meminfo
	    	//	MemTotal:         710776 kB
			//	MemFree:           30720 kB
			//	...
	    	
	        RandomAccessFile reader = new RandomAccessFile(MEM_USAGE_INFO, "r");

	        // Read MemTotal
	        load = reader.readLine();
	        load = load.replace(" ", "");
	    	toks = load.split(":");
	        long memTot = Long.parseLong(toks[1].replace("kB", "").trim());
	        
	        // Read MemFree
	        load = load.replace(" ", "");
	    	toks = load.split(":");
	        long free =  Long.parseLong(toks[1].replace("kB", "").trim());
	        reader.close();
	        
	        return memTot-free;
	    
		    // NOT WORKING
			//	MemoryInfo mi = new MemoryInfo();
			//	am.getMemoryInfo(mi);
			//	return mi.availMem / 1024L;		// Available mem in KB
	    } catch (Exception e) {
	    	throw new MemorySenseException("Error while getting mem usage (" + e.getMessage() + ")");
	    }
	}
	
	@Override
	public long getTotalMemory() throws MemorySenseException {
		if(totalMemory < 0)
			throw new MemorySenseException("Error while getting mem total (totalMemory == " + totalMemory + ")");
		else
			return totalMemory;
	}
	
}
