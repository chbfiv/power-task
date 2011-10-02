package com.andorn.powertask.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.adapters.GooTasksPagerAdapter;
import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.auth.OAuthReceiver;
import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;
import com.andorn.powertask.fragments.BaseFragment;
import com.andorn.powertask.fragments.GooTaskViewFragment;
import com.andorn.powertask.fragments.GooTasksFragment;
import com.andorn.powertask.helpers.AnalyticsTrackerHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.interfaces.IGooTaskFrag;
import com.andorn.powertask.interfaces.IGooTaskHost;
import com.andorn.powertask.interfaces.IGooTasksHost;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.services.TasksAppService;
import com.andorn.powertask.R;

public class GooTasksActivity extends BaseActivity 
	implements IGooTasksHost, IGooTaskHost { 
	
	private static final String TAG = GooTasksActivity.class.getName();

	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";		
	private long mActiveTaskListId = GooBase.INVALID_ID;	
	private long mActiveTaskId = GooBase.INVALID_ID;

	private GooTaskListsOpenHelper dbhTaskLists = new GooTaskListsOpenHelper(this);

	private GooTaskViewFragment mTaskViewFragment;
	
	private ViewPager mViewPager;
	private GooTasksPagerAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
    		finish();
		} 	
	    
		mActiveTaskListId = extras.getLong(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);		    	
	
		setContentView(R.layout.activity_tasks);
		
    	mTaskViewFragment = (GooTaskViewFragment) getSupportFragmentManager().findFragmentById(R.id.task_view_fragment);
	
    	mViewPager = (ViewPager) findViewById(R.id.tasks_viewPager);
		mAdapter = new GooTasksPagerAdapter(this, getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);		
		mViewPager.setOnPageChangeListener(mAdapter);

		int initialPage = mAdapter.getCount() / 2 ;
	    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
	    initialPage = prefs.getInt(SharedPrefUtil.PREF_ACTIVE_TASKS_PAGE_POSITION, initialPage);   
	    
		mViewPager.setCurrentItem(initialPage);
		
		getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_LIST);

		getTrackerHelper().setScreenOrientationCustomVar(this);
	}	
	
	@Override
	protected void onResume() {
		super.onResume();		
		IntentFilter filter = new IntentFilter();
		filter.addAction(OAuthHelper.INTENT_ON_AUTH);
		registerReceiver(mOAuthReceiver, filter);		
		
		getTrackerHelper().trackPageView("/" + TAG);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mOAuthReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbhTaskLists != null) dbhTaskLists.close(); 		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_item_general_settings, menu);
		getMenuInflater().inflate(R.menu.menu_item_sync, menu);
		getMenuInflater().inflate(R.menu.menu_item_compose_task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_general_settings: {
				GeneralSettingsActivity.go(this, false);
				return true;
			}
			case R.id.menu_sync: {
				getOAuthHelper().resetAuthAttempts();
				sync();
				getTrackerHelper().trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_SYNC, TAG, 0);
				return true;
			}
			case R.id.menu_compose_task: {
				GooTaskEditActivity.go(this, false, getActiveTaskListId(), GooBase.INVALID_ID);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	public long getActiveTaskListId() {
		return mActiveTaskListId;
	}
	
	public void setActiveTaskListId(long taskListId) {
		mActiveTaskListId = taskListId;
	}	
	
	public long getActiveTaskId() {
		return mActiveTaskId;
	}

	public void setActiveTaskId(long taskId) {
		mActiveTaskId = taskId;
		if(mTaskViewFragment != null) BaseFragment.<IGooTaskFrag>frag(mTaskViewFragment).refresh();
		else GooTaskViewActivity.go(this, false, taskId);
	}
	
	public GooTaskListsOpenHelper getDbhTaskLists() {
		return dbhTaskLists;
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTaskLists.getDbhTasks();
	}
	
    public static void go(Activity activity, long taskListId) 
    {
    	go(activity, true, taskListId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long taskListId) {		
		final Intent intent = new Intent(activity, GooTasksActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_LIST_ID, taskListId);
		activity.startActivity(intent);
		if(finishActivity) activity.finish();
	}
    
	public void sync()
	{
		sync(true);
	}
	
	public void sync(boolean withRefresh) {		
		if(withRefresh) refresh();
		TasksAppService.syncTasks(this, getActiveTaskListId(), mSyncReceiver);

		getTrackerHelper().trackEvent(AnalyticsTrackerHelper.CATEGORY_BACKGROUND_PROCESS, 
				AnalyticsTrackerHelper.ACTION_SYNC, TAG, 0);
	}
	
	public void refresh()
	{
		for (GooTasksFragment frag : mAdapter.getFragments())
		{
			if(frag != null) frag.refresh();				
		}
		if(mTaskViewFragment != null) BaseFragment.<IGooTaskFrag>frag(mTaskViewFragment).refresh();
	}
	
	private OAuthReceiver mOAuthReceiver = new OAuthReceiver() {	
	    
	    @Override
	    public void onAuthToken(Context context, String authToken) {
		    TaskApplication app = (TaskApplication)getApplication();
	    	app.setAccessToken(authToken);  	    	
	    	sync();
	    }
	};

	private ResultReceiver mSyncReceiver = new ResultReceiver(null) {
	    @Override
	    protected void onReceiveResult(final int resultCode, final Bundle resultData) {			    	
	    	runOnUiThread(new Runnable() {
				public void run() {					
					if (resultCode == TasksAppService.RESULT_SYNC_SUCCESS_TASKS) {
						getOAuthHelper().resetAuthAttempts();	
						refresh();
			        }
					else if (resultCode == TasksAppService.RESULT_SYNC_LOADING)
					{
						getActivityHelper().setSyncing(true);
					}
					else if (resultCode == TasksAppService.RESULT_SYNC_LOADING_COMPLETE)
					{
						getActivityHelper().setSyncing(false);
					}
					else if (resultCode == TasksAppService.RESULT_SYNC_FAILED_UNAUTHORIZED)
					{
						getOAuthHelper().updateTokenExpiration(true);						
					}
				}							
			});
	    }
	};	
}
