package com.mtelab.taskhack.views;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.TaskApplication;
import com.mtelab.taskhack.adapters.GooTaskListAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.auth.OAuthReceiver;
import com.mtelab.taskhack.base.ActivityHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooTaskListCollectionOpenHelper;
import com.mtelab.taskhack.database.GooTaskListOpenHelper;
import com.mtelab.taskhack.database.TCTagMapOpenHelper;
import com.mtelab.taskhack.database.TCTagsOpenHelper;
import com.mtelab.taskhack.dialogs.TaskActionsDialog;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.models.TCTag;
import com.mtelab.taskhack.services.TasksAppService;

public class GooTaskListActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener, OnLongClickListener {
	
	private static final String TAG = GooTaskListActivity.class.getName();
	private final GooTaskListOpenHelper dbTLHelper = new GooTaskListOpenHelper(this);
	private final GooTaskListCollectionOpenHelper dbTLCHelper = new GooTaskListCollectionOpenHelper(this);
	private final TCTagMapOpenHelper dbTagMapHelper = new TCTagMapOpenHelper(this);

	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";
	
	private ListView listView;
	private GooTaskListAdapter mAdapter;
	
	private long mActiveTaskListId = GooBase.INVALID_ID;
	private TaskActionsDialog mTaskActionsDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
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
		// get the task application to store the adapter which will act as the
		// task storage
		// for this demo.
		TaskApplication app = (TaskApplication) getApplication();
		mAdapter = app.getTaskListAdapter(this);
		listView.setAdapter(mAdapter);	    
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		getOAuthHelper().onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(OAuthHelper.INTENT_ON_AUTH);
		registerReceiver(mOAuthReceiver, filter);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbTLHelper != null) {
			dbTLHelper.close();
	    }
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
				syncTaskList();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TCTagListActivity.REQUEST_TAGS:
			if (resultCode == Activity.RESULT_OK) {
				
			}
			break;
		}
	}
	
	public void commitAddTaskList(View view)
	{
		
	}
	
	public void cancelAddTaskList(View view)
	{
		
	}
	
	public void goAccounts() {
////		final Intent intent = new Intent(this, AccountsActivity.class);
////		startActivity(intent);
////		overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
//		final Intent intent = new Intent(this, ManageAccountsActivity.class);
//		startActivity(intent);TaskActionsDialog
//		overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
	}

	public void syncTaskList() {	    
    	if(mActiveTaskListId == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "refreshTaskList - invalid task list id.");
    		return;
    	}
    	
		mAdapter.set(dbTLHelper.query(mActiveTaskListId));
		Intent intent = new Intent(this, TasksAppService.class);
		intent.setFlags(TasksAppService.REQUEST_SYNC_TASKS);
		intent.putExtra(TasksAppService.EXTRA_TASK_LIST_ID, mActiveTaskListId);		
		intent.putExtra(TasksAppService.REQUEST_RECEIVER_EXTRA, mLoadTasksReceiver);
		startService(intent);
	}

//	private void updateRemoteTask(long taskId)
//	{
//		Intent intent = new Intent(this, TasksAppService.class);
//		intent.setFlags(TasksAppService.UPDATE_TASK);
//		intent.putExtra(TasksAppService.EXTRA_TASK_ID, taskId);		
//		intent.putExtra(TasksAppService.REQUEST_RECEIVER_EXTRA, mLoadTasksReceiver);
//		startService(intent);		
//	}
	
	public void composeTaskList() {
		
	}

	public void readTask(long taskId)
	{
		Intent intent = new Intent(this, GooTaskViewActivity.class);
		intent.putExtra(GooTaskViewActivity.EXTRA_ACTIVE_TASK_ID, taskId);
		startActivity(intent);	
	}
	
	public void editTask(long taskId)
	{
		Intent intent = new Intent(this, GooTaskComposeActivity.class);
		intent.putExtra(GooTaskComposeActivity.EXTRA_ACTIVE_TASK_ID, taskId);
		startActivity(intent);    
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
	
	public void showTaskActionsDialog(long taskId)
	{		
		showDialog(TaskActionsDialog.DIALOG_ID);		
		mTaskActionsDialog.setTaskId(taskId);		
	}
	
	public void showTags(long taskId)
	{		
		Intent intent = new Intent(this, TCTagListActivity.class);
		intent.putExtra(TCTagListActivity.EXTRA_TASK_ID, taskId);
		startActivityForResult(intent, TCTagListActivity.REQUEST_TAGS);    
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
	
	// List Actions

	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooTask task = mAdapter.getItem(position);
			readTask(task.getId());
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooTask task = mAdapter.getItem(position);
			switch(v.getId())
			{
			case R.id.taskItem_statusCheckBox:
				processCheckChangeForStatus(task, position, isChecked);				
				break;
			case R.id.taskItem_starCheckBox:
				processCheckChangeForStar(task, position, isChecked);
				break;			
			}
		}		   
	}

	private void processCheckChangeForStatus(GooTask task, int position, boolean isChecked)
	{
		GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
		task.setStatus(status);
		dbTLHelper.update(task.getId(), status);		
		//TasksAppService.updateTask(this, task.getId(), mLoadTasksReceiver);
		mAdapter.notifyDataSetChanged();
	}
	
	private void processCheckChangeForStar(GooTask task, int position, boolean isChecked)
	{		
		//task add/remove Star Tag
		if(isChecked)
			dbTagMapHelper.replace("blue-star", task.getId());
		else
			dbTagMapHelper.delete("blue-star", task.getId());			
		
		List<TCTag> tags = dbTagMapHelper.query(task.getId());
		task.setTags(tags);
		mAdapter.notifyDataSetChanged();		
	}
	
	@Override
	public boolean onLongClick(View v) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooTask task = mAdapter.getItem(position);
			showTaskActionsDialog(task.getId());
		}	
		return false;
	}
	
	// Task Actions
	
	public void readAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			readTask(mTaskActionsDialog.getTaskId());
		}		
	}	
	
	public void editAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			editTask(mTaskActionsDialog.getTaskId());
		}		
	}
	
	public void changeTagsAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
			mTaskActionsDialog.dismiss();
			showTags(mTaskActionsDialog.getTaskId());
		}		
	}
	
	public void deleteAction(View v)
	{
		if(mTaskActionsDialog != null && mTaskActionsDialog.isShowing())
		{
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

	// Receivers
	
	private OAuthReceiver mOAuthReceiver = new OAuthReceiver() {	
	    
	    @Override
	    public void onAuthToken(Context context, String authToken) {	

		    TaskApplication app = (TaskApplication)getApplication();
	    	app.setAccessToken(authToken);  
	    	
	    	syncTaskList();
	    }
	};
	
	private ResultReceiver mLoadTasksReceiver = new ResultReceiver(null) {
	    @Override
	    protected void onReceiveResult(final int resultCode, final Bundle resultData) {			    	
				runOnUiThread(new Runnable() {
					public void run() {					
						if (resultCode == TasksAppService.RESULT_SYNC_TASKS_SUCCESS) {
							getOAuthHelper().resetAuthAttempts();							
							mAdapter.set(dbTLHelper.query(mActiveTaskListId));
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
