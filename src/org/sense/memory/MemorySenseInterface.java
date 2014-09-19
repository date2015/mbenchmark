package org.sense.memory;

public interface MemorySenseInterface {

	long getUsedMemory() throws MemorySenseException;

	long getTotalMemory() throws MemorySenseException;

}
