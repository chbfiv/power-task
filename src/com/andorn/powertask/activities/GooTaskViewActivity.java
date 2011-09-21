package com.andorn.powertask.activities;

import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;
import com.andorn.powertask.fragments.BaseFragment;
import com.andorn.powertask.fragments.GooTaskViewFragment;
import com.andorn.powertask.interfaces.IGooTaskFrag;
import com.andorn.powertask.interfaces.IGooTaskHost;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class GooTaskViewActivity extends BaseActivity 
	implements IGooTaskHost {

	private static final String TAG = GooTaskViewActivity.class.getName();	

	public static final String EXTRA_ACTIVE_TASK_ID = "active_task_id";
	private long mActiveTaskId = GooBase.INVALID_ID;

	private final GooTasksOpenHelper dbhTasks = new GooTasksOpenHelper(this);
	
	private GooTaskViewFragment mTaskViewFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
			finish();
		}
    	
		mActiveTaskId = extras.getLong(EXTRA_ACTIVE_TASK_ID, GooBase.INVALID_ID);
		
        setContentView(R.layout.activity_task_view);
        
        mTaskViewFragment = (GooTaskViewFragment) getSupportFragmentManager().findFragmentById(R.id.task_view_fragment);

        getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_VIEW);  
    }   
    
	@Override
	protected void onDestroy() {
		super.onDestroy();		
		if (dbhTasks != null) dbhTasks.close(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public long getActiveTaskId() {
		return mActiveTaskId;
	}

	public void setActiveTaskId(long taskId) {
		mActiveTaskId = taskId;
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTasks;
	}

	public void refresh()
	{
		if(mTaskViewFragment != null) BaseFragment.<IGooTaskFrag>frag(mTaskViewFragment).refresh();
	}
	
    public static void go(Activity activity, long taskId) 
    {
    	go(activity, true, taskId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskViewActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		if(finishActivity) activity.finish();
	}
}
