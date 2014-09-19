package org.mbenchmark.stresstests;


import org.mbenchmark.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class LocalVideoActivity extends Activity {
	
	private final String TAG = "LocalVideoActivity";
	private Intent intent;
	private VideoView videoView;
	private MediaController mediaController;
	private String message;
	private String path;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localvideo);
		
		IntentFilter filter = new IntentFilter();
	    filter.addAction("LOCAL_VIDEO_STOP_ACTIVITY");
	    registerReceiver(receiver, filter);
	    
		videoView = (VideoView) findViewById(R.id.videoView1);
		intent = getIntent();
		message = intent.getStringExtra("VIDEO_TYPE");
		if (message.equals("blackwhite")){
			path = "/MBenchmark/videos/bn_fast_1080p.mp4";
		}
		else if (message.equals("rgb1")){
			path = "/MBenchmark/videos/rgb_fast_1080p.mp4";
		}
		else{
			path = "/MBenchmark/videos/rgbv3_fast_1080p.mp4";
		}
		
		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		videoView.setVideoPath(Environment.getExternalStorageDirectory().getPath()+path);
		Log.d(TAG, "Path: "+Environment.getExternalStorageDirectory().getPath()+path);
		videoView.setMediaController(mediaController);
		videoView.start();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			 finish();
		}
	};
}
