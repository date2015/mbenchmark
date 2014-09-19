package org.mbenchmark;

import org.mbenchmark.data.LogService;
import org.mbenchmark.stresstests.BluetoothDownloadStressTest;
import org.mbenchmark.stresstests.BluetoothUploadStressTest;
import org.mbenchmark.stresstests.CPUEdgeDetectionStressTest;
import org.mbenchmark.stresstests.CPUIntegerStressTest;
import org.mbenchmark.stresstests.LocalVideoStressTest;
import org.mbenchmark.stresstests.MobileDataDownloadStressTest;
import org.mbenchmark.stresstests.MobileDataUploadStressTest;
import org.mbenchmark.stresstests.StressTestInterface;
import org.mbenchmark.stresstests.WiFiDownloadStressTest;
import org.mbenchmark.stresstests.WiFiUploadStressTest;
import org.mbenchmark.stresstests.YouTubeVideoStressTest;
import org.mbenchmark.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Spinner stressTestSpinner;
	private Spinner durationSpinner;
	private ArrayAdapter<CharSequence> stressTestSpinnerAdapter;
	private ArrayAdapter<CharSequence> durationSpinnerAdapter;
	private ProgressBar progressBar;
	private Button startButton;
	private Button stopButton;
	private String selectedStressTest;
	private String selectedDuration;
	private long selectedDurationMilliseconds;
	private StressTestInterface currentStressTest;
	private final String TAG = "MainActivity";
	private Handler durationHandler = new Handler();

	private Runnable stopStressTestTask = new Runnable(){
		@Override
		public void run() {
			stopBenchmark(stopButton);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
				
		stressTestSpinner = (Spinner) findViewById(R.id.benchmarkSpinner);
		stressTestSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.stress_test_array, android.R.layout.simple_spinner_item);
		stressTestSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stressTestSpinner.setAdapter(stressTestSpinnerAdapter);
		stressTestSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	selectedStressTest = (String) parentView.getSelectedItem();
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    	selectedStressTest = "WiFi Download";
		    }

		});
		
		
		durationSpinner = (Spinner) findViewById(R.id.durationSpinner);
		durationSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.duration_array, android.R.layout.simple_spinner_item);
		durationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(durationSpinnerAdapter);
		durationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	selectedDuration = (String) parentView.getSelectedItem();
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    	selectedDuration = "8 min";
		    }

		});
		
		//set the default to 8 min
		durationSpinner.setSelection(2);	
	}

	@Override
	protected void onPause(){
		super.onPause();
	}

	@Override
	protected void onResume(){
		super.onResume();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void startBenchmark (View view){
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setVisibility(View.INVISIBLE);
		
		progressBar = (ProgressBar) findViewById(R.id.benchmarkProgressBar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);
		
		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setVisibility(View.VISIBLE);
		StartBenchmarkTask startTask = new StartBenchmarkTask();
		startTask.execute();
	}
	
	public void stopBenchmark (View view){
		
		Log.d(TAG, "Stopping current stress test:"+currentStressTest.getName());
		
		try {
			currentStressTest.stopStressTest();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
			return;
		}
		
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setVisibility(View.VISIBLE);
		
		progressBar = (ProgressBar) findViewById(R.id.benchmarkProgressBar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.INVISIBLE);
		
		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setVisibility(View.INVISIBLE);
		
		/* Stop the LogService in a separate thread to keep the UI responsive */
		Thread stopServiceThread = new Thread(){
			@Override
			public void run(){
				stopService(new Intent(getApplicationContext(), LogService.class));
			}
		};
		stopServiceThread.start();
		
		Log.d(TAG, "LogService stopped");
		
		durationHandler.removeCallbacks(stopStressTestTask);
	
	}
	
	public class StartBenchmarkTask extends AsyncTask<Void, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			if (selectedStressTest.equals("WiFi Download")){
				currentStressTest = new WiFiDownloadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("WiFi Upload")){
				currentStressTest = new WiFiUploadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("Mobile Data Download")){
				currentStressTest = new MobileDataDownloadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("Mobile Data Upload")){
				currentStressTest = new MobileDataUploadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("Bluetooth Download")){
				currentStressTest = new BluetoothDownloadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("Bluetooth Upload")){
				currentStressTest = new BluetoothUploadStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("CPU (Edge Detection)")){
				currentStressTest = new CPUEdgeDetectionStressTest(getApplicationContext());
			}
			else if (selectedStressTest.equals("CPU (Integer)")){
				currentStressTest = new CPUIntegerStressTest();
			}
			else if (selectedStressTest.equals("Video (Local File - BlackWhite)")){
				currentStressTest = new LocalVideoStressTest(getApplicationContext(),selectedStressTest);
			}
			else if (selectedStressTest.equals("Video (Local File - RGB1)")){
				currentStressTest = new LocalVideoStressTest(getApplicationContext(),selectedStressTest);
			}
			else if (selectedStressTest.equals("Video (Local File - RGB2)")){
				currentStressTest = new LocalVideoStressTest(getApplicationContext(),selectedStressTest);
			}
			else if (selectedStressTest.equals("Video (YouTube)")){
				currentStressTest = new YouTubeVideoStressTest(getApplicationContext());
			}
			else{
				return false;
			}
			
			try {
				currentStressTest.startStressTest();
			} catch (Exception e) {
				Log.e(TAG, "Exception: "+e);
				return false;
			}
			
			if (selectedDuration.equals("1 min")){
				selectedDurationMilliseconds = 60*1000;
			}
			else if (selectedDuration.equals("5 min")){
				selectedDurationMilliseconds = 5*60*1000;
			}
			else if (selectedDuration.equals("8 min")){
				selectedDurationMilliseconds = 8*60*1000;
			}
			else if (selectedDuration.equals("10 min")){
				selectedDurationMilliseconds = 10*60*1000;
			}
			else if (selectedDuration.equals("15 min")){
				selectedDurationMilliseconds = 15*60*1000;
			}
			durationHandler.postDelayed(stopStressTestTask, selectedDurationMilliseconds);
			
			Intent startIntent = new Intent(getApplicationContext(), LogService.class);
			startIntent.putExtra("stressTestName", currentStressTest.getName());
			Log.d(TAG, "Current stress test:"+currentStressTest.getName());
			startService(startIntent);
			Log.d(TAG, "LogService started");
			return true;
		}
		
		@Override
		protected void onPostExecute (Boolean result){
			if (result==false){
				Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.StressTest_exception) , Toast.LENGTH_SHORT).show();
				startButton = (Button) findViewById(R.id.startButton);
				startButton.setVisibility(View.VISIBLE);
				
				progressBar = (ProgressBar) findViewById(R.id.benchmarkProgressBar);
				progressBar.setIndeterminate(true);
				progressBar.setVisibility(View.INVISIBLE);
				
				stopButton = (Button) findViewById(R.id.stopButton);
				stopButton.setVisibility(View.INVISIBLE);
			}
		}	
	}
}
