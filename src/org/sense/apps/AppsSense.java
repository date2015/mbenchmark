package org.sense.apps;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class AppsSense implements AppsSenseInterface {

	private final String LOG_ERRORS_TAG = "AppsSenseErrors";
	private ActivityManager activityManager = null;
	
	public AppsSense(Context context){
		activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
	}
	
	@Override
	public String getCurrentApp() throws AppsSenseException {
		return getForegroundApps(1).get(0);
	}
	
	@Override
	public List<String> getForegroundApps() throws AppsSenseException {
		return getForegroundApps(Integer.MAX_VALUE);
	}
	
	@Override
	public List<String> getForegroundApps(int maxNumber) throws AppsSenseException {
		if(activityManager == null)
			throw new AppsSenseException("Library not initialized");
		try{
			/* Information you can retrieve about a particular task that is currently "running" in the system. 
			 * Note that a running task does not mean the given task actually has a process it is actively running in; 
			 * it simply means that the user has gone to it and never closed it, 
			 * but currently the system may have killed its process and is only holding on to its last state 
			 * in order to restart it when the user returns.*/
			List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(maxNumber);
			List<String> runningTasksNames = new LinkedList<String>();
			for (int i = 0; i < runningTasks.size(); i++){
				String appName = runningTasks.get(i).baseActivity.toShortString().split("/")[1];	// Keep only the activity path
				appName = appName.substring(0, appName.length()-1);									// drop the last "}"
				runningTasksNames.add(appName);
			}
			return runningTasksNames;
		}catch(Exception e){
			throw new AppsSenseException("Error while retrieving running tasks: " + e.getMessage());
		}
	}
	
	
	
	
	
	// Method used to check which information are available
	public void printAvailableInformation(){
		try{
			// Information you can retrieve about tasks that the user has most recently started or visited.
			List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(Integer.MAX_VALUE, 1);
			Log.d(LOG_ERRORS_TAG, "START RECENT TASK ------------------------------------------------------");
			for (int i = 0; i < recentTasks.size(); i++)
				if(recentTasks.get(i).origActivity != null)
					Log.d(LOG_ERRORS_TAG, recentTasks.get(i).origActivity.toShortString());         
			Log.d(LOG_ERRORS_TAG, "END RECENT TASK ------------------------------------------------------");
		}catch(Exception e){
			Log.e(LOG_ERRORS_TAG, "Exception while getting RecentTaskInfos");
			e.printStackTrace();
		}
		
		try{
			// Information you can retrieve about a running process.
			List<ActivityManager.RunningAppProcessInfo>	runningAppProcesses = activityManager.getRunningAppProcesses();
			
			Log.d(LOG_ERRORS_TAG, "START RUNNING APP ------------------------------------------------------");
			for (int i = 0; i < runningAppProcesses.size(); i++)
		        Log.d(LOG_ERRORS_TAG, runningAppProcesses.get(i).processName + ", importance: " + runningAppProcesses.get(i).importance);         
			Log.d(LOG_ERRORS_TAG, "END RUNNING APP ------------------------------------------------------");	
		}catch(Exception e){
			Log.e(LOG_ERRORS_TAG, "Exception while getting runningAppProcesses");
			e.printStackTrace();
		}
		
		try{
			// Information you can retrieve about a particular Service that is currently running in the system.
			List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
									
			Log.d(LOG_ERRORS_TAG, "START RUNNING SERVICE ------------------------------------------------------");
			for (int i = 0; i < runningServices.size(); i++)
		        Log.d(LOG_ERRORS_TAG, runningServices.get(i).process + ", service: " + runningServices.get(i).service.toShortString());         
			Log.d(LOG_ERRORS_TAG, "END RUNNING SERVICE ------------------------------------------------------");	
		}catch(Exception e){
			Log.e(LOG_ERRORS_TAG, "Exception while getting runningAppProcesses");
			e.printStackTrace();
		}
		
		try{
			/* Information you can retrieve about a particular task that is currently "running" in the system. 
			 * Note that a running task does not mean the given task actually has a process it is actively running in; 
			 * it simply means that the user has gone to it and never closed it, 
			 * but currently the system may have killed its process and is only holding on to its last state 
			 * in order to restart it when the user returns.*/
			List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
			
			Log.d(LOG_ERRORS_TAG, "START RUNNING TASK ------------------------------------------------------");
		    for (int i = 0; i < runningTasks.size(); i++)
		        Log.d(LOG_ERRORS_TAG, runningTasks.get(i).baseActivity.toShortString());         
			Log.d(LOG_ERRORS_TAG, "END RUNNING TASK ------------------------------------------------------");
		}catch(Exception e){
			Log.e(LOG_ERRORS_TAG, "Exception while getting runningAppProcesses");
			e.printStackTrace();
		}
	}
}