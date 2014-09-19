package org.sense.audio;

public interface AudioSenseInterface {

	boolean isMusicActive() throws AudioSenseException;

	int getMusicVolume() throws AudioSenseException;

	int getVoiceVolume() throws AudioSenseException;

	int getRingVolume() throws AudioSenseException;

	boolean isSpeakerOn() throws AudioSenseException;

	int getMode() throws AudioSenseException;

}
