package org.sense.location;

public interface LocationSenseInterface {

	boolean isGPSOn() throws LocationSenseException;

	int getCurrentProviderStatus() throws LocationSenseException;

}
