package com.andorn.powertask.activities;

import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;
import com.andorn.powertask.fragments.BaseFragment;
import com.andorn.powertask.fragments.GooTaskEditFragment;
import com.andorn.powertask.interfaces.IGooTaskEditFrag;
import com.andorn.powertask.interfaces.IGooTaskEditHost;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class GooTaskEditActivity extends BaseActivity 
	implements IGooTaskEditHost {

	private static final String TAG = GooTaskEditActivity.class.getName();	
    
	public static final String EXTRA_ACTIVE_TASK_ID = "active_task_id";
	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";	
	
	private long mActiveTaskId = GooBase.INVALID_ID;
	private long mActiveTaskListId = GooBase.INVALID_ID;

	private final GooTasksOpenHelper dbhTasks = new GooTasksOpenHelper(this);

	private GooTaskEditFragment mTaskEditFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
			finish();
		}
    	
		mActiveTaskId = extras.getLong(EXTRA_ACTIVE_TASK_ID, GooBase.INVALID_ID);
		mActiveTaskListId = extras.getLong(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);
		
        setContentView(R.layout.activity_task_edit);         

        mTaskEditFragment = (GooTaskEditFragment) getSupportFragmentManager().findFragmentById(R.id.task_edit_fragment);
        
        if(mActiveTaskId == GooBase.INVALID_ID)        
        	getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_COMPOSE);
        else
        	getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_EDIT);   
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
		
		getTrackerHelper().trackPageView("/" + TAG);
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

	public long getActiveTaskListId() {
		return mActiveTaskListId;
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTasks;
	}

	public void refresh()
	{
		if(mTaskEditFragment != null) BaseFragment.<IGooTaskEditFrag>frag(mTaskEditFragment).refresh();		
	}
	
    public static void go(Activity activity, long taskListId, long taskId) 
    {
    	go(activity, true, taskListId, taskId);
    }
    
    public static void go(Activity activity, long taskId) 
    {
    	go(activity, true, taskId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskEditActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade, R.anim.hold);
		if(finishActivity) activity.finish();
	}
	
	public static void go(Activity activity, boolean finishActivity, long taskListId, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskEditActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_LIST_ID, taskListId);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade, R.anim.hold);
		if(finishActivity) activity.finish();
	}
}
