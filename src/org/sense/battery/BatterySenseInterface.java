package org.sense.battery;

public interface BatterySenseInterface {

	boolean isCharging() throws BatterySenseException;

	int getPercentage() throws BatterySenseException;

	int getTemperature() throws BatterySenseException;

	int getVoltage() throws BatterySenseException;

	String getTechnology() throws BatterySenseException;

	int getHealth() throws BatterySenseException;

}
