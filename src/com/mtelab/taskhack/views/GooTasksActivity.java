package com.mtelab.taskhack.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.TaskApplication;
import com.mtelab.taskhack.adapters.GooTasksCursorAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.auth.OAuthReceiver;
import com.mtelab.taskhack.base.ActivityHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooTaskListsOpenHelper;
import com.mtelab.taskhack.database.GooTasksOpenHelper;
import com.mtelab.taskhack.database.TCTagMapOpenHelper;
import com.mtelab.taskhack.dialogs.TaskActionsDialog;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooSyncBase;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.services.TasksAppService;

public class GooTasksActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener, OnLongClickListener {
	
	private static final String TAG = GooTasksActivity.class.getName();
	
	private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
	private final GooTaskListsOpenHelper dbTLCHelper = new GooTaskListsOpenHelper(this);
	private final TCTagMapOpenHelper dbTagMapHelper = new TCTagMapOpenHelper(this);

	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";
	
	private ListView listView;
	private GooTasksCursorAdapter mAdapter;
	
	private long mActiveTaskListId = GooBase.INVALID_ID;
	private TaskActionsDialog mTaskActionsDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getOAuthHelper().onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
    		finish();
		}
    	
		mActiveTaskListId = extras.getLong(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);
		    	
    	GooTaskList list = dbTLCHelper.read(mActiveTaskListId);
    	if(list == null)
    	{
    		Log.e(TAG, "onCreate - list is null");
    		finish();
    	}
    	
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View headerContainer = inflater.inflate(R.layout.task_list_item_header, null);
		TextView header = (TextView)headerContainer.findViewById(R.id.taskList_header);
		headerContainer.setEnabled(false);
		header.setText(list.title);
		
		setContentView(R.layout.task_list);
        
		getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_LIST);

		listView = (ListView) findViewById(R.id.task_list);
		
		listView.addHeaderView(headerContainer);
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		getOAuthHelper().onResume();
		
		listView.setAdapter(null);	 
		Cursor c = dbTLHelper.queryCursor(mActiveTaskListId, GooSyncBase.SYNC_DELETE);
		mAdapter = new GooTasksCursorAdapter(this, c, true);		
		listView.setAdapter(mAdapter);	  
		
		mAdapter.requery();	
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(OAuthHelper.INTENT_ON_AUTH);
		registerReceiver(mOAuthReceiver, filter);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dbTLHelper != null) dbTLHelper.close(); 
		if (dbTLCHelper != null) dbTLCHelper.close(); 
		if (dbTagMapHelper != null) dbTagMapHelper.close(); 
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mOAuthReceiver);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sync_menu_items, menu);
		getMenuInflater().inflate(R.menu.edit_mode_menu_item, menu);
		getMenuInflater().inflate(R.menu.compose_task_menu_items, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_sync: {
				sync();
				return true;
			}
			case R.id.menu_compose_task: {
				//goAccounts();
				return true;
			}
			case R.id.menu_edit_mode: {
				//composeTaskList();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case TaskActionsDialog.DIALOG_ID:
	    	mTaskActionsDialog = new TaskActionsDialog(this);
	        return mTaskActionsDialog;
	    default:
	        dialog = null;
	    }
	    return dialog;
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
		if(withRefresh) mAdapter.requery();
		TasksAppService.syncTasks(this, mActiveTaskListId, mSyncReceiver);
	}
	
	public void showTaskActionsDialog(long taskId)
	{		
		showDialog(TaskActionsDialog.DIALOG_ID);		
		mTaskActionsDialog.setTaskId(taskId);		
	}
	
	public void showTags(long taskId)
	{		
	}
	
	// List Actions

	@Override
	public void onClick(View v) {
		long taskId = (Long)v.getTag();
		if(taskId != GooBase.INVALID_ID)
		{
			GooTaskViewActivity.go(this, false, taskId);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		long taskId = (Long)v.getTag();
		if(taskId != GooBase.INVALID_ID)
		{
			showTaskActionsDialog(taskId);
		}	
		return false;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		long taskId = (Long)v.getTag();
		if(taskId != GooBase.INVALID_ID)
		{
			GooTask task = dbTLHelper.read(taskId);
			
			switch(v.getId())
			{
			case R.id.taskItem_statusCheckBox:
				GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
				task.setStatus(status);
				break;
			case R.id.taskItem_starCheckBox:		
				if(isChecked) dbTagMapHelper.replace("blue-star", taskId);
				else dbTagMapHelper.delete("blue-star", taskId);					
				break;			
			}
			task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
			dbTLHelper.update(task);	
			mAdapter.requery();			
		}		   
	}
	
	public void readAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			GooTaskViewActivity.go(this, false, mTaskActionsDialog.getTaskId());
		}		
	}	
	
	public void editAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			GooTaskComposeActivity.go(this, false, mTaskActionsDialog.getTaskId());
		}		
	}
	
	public void changeTagsAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			TCTagListActivity.go(this, mTaskActionsDialog.getTaskId());
		}		
	}
	
	public void deleteAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
     	    new DeleteTask().execute(mTaskActionsDialog.getTaskId());
			mTaskActionsDialog.dismiss();
		}			
	}
	
	public void helpAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
		}			
	}

	private class DeleteTask extends AsyncTask<Long, Void, Boolean> {
	     protected Boolean doInBackground(Long... ids) {
	    	 Boolean ret = true;
	    	 try
	    	 {
		    	 for (Long id : ids)
		    	 {
		    		 GooTask task = dbTLHelper.read(id);
		    		 task.setSyncState(GooSyncBase.SYNC_DELETE);
		    		 dbTLHelper.update(task);
		    	 }   		 
	    	 }
	    	 catch(Exception ex)
	    	 {
	    		 ret = false;
	    	 }
			return ret;	    	 
	     }
	
	     protected void onPostExecute(Boolean result) {
	         mAdapter.requery();
	     }
	}
	// Receivers
	
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
		        		mAdapter.requery();
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
