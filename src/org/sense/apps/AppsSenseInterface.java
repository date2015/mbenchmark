package org.sense.apps;

import java.util.List;

public interface AppsSenseInterface {
	
	List<String> getForegroundApps() throws AppsSenseException;
	
	List<String> getForegroundApps(int maxNumber) throws AppsSenseException;

	String getCurrentApp() throws AppsSenseException;
}
