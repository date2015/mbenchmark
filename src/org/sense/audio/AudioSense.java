package org.sense.audio;

import android.content.Context;
import android.media.AudioManager;

public class AudioSense implements AudioSenseInterface {

	AudioManager am;

	public AudioSense(Context context){
		this.am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}
	
	public AudioSense(AudioManager am){	
		this.am = am; 
	}
	
	@Override
	public boolean isMusicActive() throws AudioSenseException{
		try{
			return am.isMusicActive();
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting isMusicActive");
		}
	}
	
	@Override
	public int getMusicVolume() throws AudioSenseException{
		try{
			return am.getStreamVolume(AudioManager.STREAM_MUSIC);
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting MusicVolume");
		}
	}
	
	@Override
	public int getVoiceVolume() throws AudioSenseException{
		try{
			return am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting VoiceVolume");
		}
	}
	
	@Override
	public int getRingVolume() throws AudioSenseException{
		try{
			return am.getStreamVolume(AudioManager.STREAM_RING);
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting RingVolume");
		}
	}

	@Override
	public boolean isSpeakerOn() throws AudioSenseException{
		try{
			return am.isSpeakerphoneOn();	
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting SpeakerOn");
		}
	}
	
	@Override
	public int getMode() throws AudioSenseException{
		try{
			return am.getMode();
		}catch (Exception e) {
			throw new AudioSenseException("Error while getting Mode");
		}
	}
}
