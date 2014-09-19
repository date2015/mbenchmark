package org.mbenchmark.stresstests;

import android.content.Context;
import android.content.Intent;

public class LocalVideoStressTest implements StressTestInterface {

	private Context context;
	private String name;
	
	@Override
	public void startStressTest() throws Exception {
		
		Intent intent = new Intent(context, LocalVideoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (name.equals("Video (Local File - BlackWhite)")){
			intent.putExtra("VIDEO_TYPE", "blackwhite");
		}
		else if (name.equals("Video (Local File - RGB1)")){
			intent.putExtra("VIDEO_TYPE", "rgb1");
		}
		else{
			intent.putExtra("VIDEO_TYPE", "rgb2");
		}
		context.startActivity(intent);

	}

	@Override
	public void stopStressTest() throws Exception {
		Intent stopIntent = new Intent();
		stopIntent.setAction("LOCAL_VIDEO_STOP_ACTIVITY");
		context.sendBroadcast(stopIntent);	
	}

	@Override
	public String getName() {
		return name;
	}
	
	public LocalVideoStressTest(Context context, String name){
		this.context = context;
		this.name = name;
	}

}
