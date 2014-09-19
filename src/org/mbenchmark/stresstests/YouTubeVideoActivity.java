package org.mbenchmark.stresstests;

import org.mbenchmark.R;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

public class YouTubeVideoActivity extends YouTubeBaseActivity implements OnInitializedListener {

	private YouTubePlayer player;
	private final String DEVELOPER_KEY = "AIzaSyDNDzGWM9DNjcVxitt10gQuj3VRlWHKtwU";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youtube);

		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		youTubeView.initialize(DEVELOPER_KEY, this);
		
		IntentFilter filter = new IntentFilter();
	    filter.addAction("YOUTUBE_VIDEO_STOP_ACTIVITY");
	    registerReceiver(receiver, filter);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (player!=null){
			player.setFullscreen(true);
			player.play();
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
		this.player = player;
		
		if (!wasRestored) {
			player.loadVideo("rr7ymJwx4-Q");
			player.setFullscreen(true);
			player.play();
		}
	}
  
	@Override
	public void onInitializationFailure(Provider arg0, YouTubeInitializationResult arg1) {
		Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.init_failure) , Toast.LENGTH_SHORT).show();
	}

	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView) findViewById(R.id.youtube_view);
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			 finish();
		}
	};
}
