package org.mbenchmark.stresstests;

import android.content.Context;
import android.content.Intent;

public class YouTubeVideoStressTest implements StressTestInterface {

	private Context context;
	private String name = "Video (YouTube)";
	
	@Override
	public void startStressTest() throws Exception {
		Intent startIntent = new Intent(context, YouTubeVideoActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(startIntent);
	}

	@Override
	public void stopStressTest() throws Exception {
		Intent stopIntent = new Intent();
		stopIntent.setAction("YOUTUBE_VIDEO_STOP_ACTIVITY");
		context.sendBroadcast(stopIntent);
	}

	@Override
	public String getName() {
		return name;
	}
	
	public YouTubeVideoStressTest(Context context){
		this.context = context;	
	}

}
