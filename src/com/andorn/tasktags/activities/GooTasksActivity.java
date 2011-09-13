package com.andorn.tasktags.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.andorn.tasktags.TaskApplication;
import com.andorn.tasktags.adapters.GooTasksPagerAdapter;
import com.andorn.tasktags.auth.OAuthHelper;
import com.andorn.tasktags.auth.OAuthReceiver;
import com.andorn.tasktags.base.ActivityHelper;
import com.andorn.tasktags.database.GooTaskListsOpenHelper;
import com.andorn.tasktags.database.GooTasksOpenHelper;
import com.andorn.tasktags.fragments.BaseFragment;
import com.andorn.tasktags.fragments.GooTaskViewFragment;
import com.andorn.tasktags.fragments.GooTasksFragment;
import com.andorn.tasktags.interfaces.IGooTaskFrag;
import com.andorn.tasktags.interfaces.IGooTaskHost;
import com.andorn.tasktags.interfaces.IGooTasksFrag;
import com.andorn.tasktags.interfaces.IGooTasksHost;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.services.TasksAppService;
import com.andorn.tasktags.R;

public class GooTasksActivity extends BaseActivity 
	implements IGooTasksHost, IGooTaskHost { 
	
	private static final String TAG = GooTasksActivity.class.getName();

	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";		
	private long mActiveTaskListId = GooBase.INVALID_ID;	
	private long mActiveTaskId = GooBase.INVALID_ID;

	public GooTasksOpenHelper dbhTasks = new GooTasksOpenHelper(this);
	public GooTaskListsOpenHelper dbhTaskLists = new GooTaskListsOpenHelper(this);

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
		mAdapter = new GooTasksPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		
		getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_LIST);
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(OAuthHelper.INTENT_ON_AUTH);
		registerReceiver(mOAuthReceiver, filter);		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mOAuthReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbhTasks != null) dbhTasks.close(); 
		if (dbhTaskLists != null) dbhTaskLists.close(); 		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.general_settings_menu_item, menu);
		getMenuInflater().inflate(R.menu.sync_menu_items, menu);
		getMenuInflater().inflate(R.menu.compose_task_menu_items, menu);
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
				sync();
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
		if(mTaskViewFragment != null) 
			BaseFragment.<IGooTaskFrag>frag(mTaskViewFragment).reload();
		else
			GooTaskViewActivity.go(this, false, taskId);
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTasks;
	}

	public GooTaskListsOpenHelper getDbhTaskLists() {
		return dbhTaskLists;
	}

	public void onDbChange()
	{
    	GooTasksFragment frag = mAdapter.getCurrentFragment();
		if(frag != null)
		{
			if(frag.getTasksAdapter() != null) frag.getTasksAdapter().requery();
		}		
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
	
	public void sync(boolean withRefresh) {
		
    	GooTasksFragment frag = mAdapter.getCurrentFragment();
		if(frag != null)
		{
			if(withRefresh && frag.getTasksAdapter() != null) frag.getTasksAdapter().requery();
			TasksAppService.syncTasks(this, getActiveTaskListId(), mSyncReceiver);
		}
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
					if (resultCode == TasksAppService.RESULT_SYNC_TASKS_SUCCESS) {
						getOAuthHelper().resetAuthAttempts();	
				    	GooTasksFragment frag = mAdapter.getCurrentFragment();
						if(frag != null)
						{
							if(frag.getTasksAdapter() != null) frag.getTasksAdapter().requery();
						}
			        }
					else if (resultCode == TasksAppService.RESULT_FAILED_UNAUTHORIZED) {
						getOAuthHelper().updateTokenExpiration(true);
					}
					else if (resultCode == TasksAppService.RESULT_LOADING)
					{
						getActivityHelper().setSyncing(true);
					}
					else if (resultCode == TasksAppService.RESULT_LOADING_COMPLETE)
					{
						getActivityHelper().setSyncing(false);
					}
				}							
			});
	    }
	};	
}
