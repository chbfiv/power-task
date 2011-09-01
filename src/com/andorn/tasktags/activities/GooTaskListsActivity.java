package com.andorn.tasktags.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.andorn.tasktags.TaskApplication;
import com.andorn.tasktags.adapters.GooTaskListsCursorAdapter;
import com.andorn.tasktags.auth.OAuthHelper;
import com.andorn.tasktags.auth.OAuthReceiver;
import com.andorn.tasktags.base.ActivityHelper;
import com.andorn.tasktags.base.BaseActivity;
import com.andorn.tasktags.database.GooAccountsOpenHelper;
import com.andorn.tasktags.database.GooTaskListsOpenHelper;
import com.andorn.tasktags.dialogs.TaskListActionsDialog;
import com.andorn.tasktags.helpers.SharedPrefUtil;
import com.andorn.tasktags.models.GooAccount;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTaskList;
import com.andorn.tasktags.services.TasksAppService;
import com.andorn.tasktags.R;

public class GooTaskListsActivity extends BaseActivity implements
	OnClickListener, OnLongClickListener {
	
	private static final String TAG = GooTaskListsActivity.class.getName();
	
	private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);
	private final GooTaskListsOpenHelper dbTLCHelper = new GooTaskListsOpenHelper(this);
	
	private ListView listView;
	private GooTaskListsCursorAdapter mAdapter;
	
	private TaskListActionsDialog mTaskListActionsDialog;
	private long mActiveAccountId = GooBase.INVALID_ID;

	public static final String EXTRA_ACTIVE_ACCOUNT_ID = "active_account_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getOAuthHelper().onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mActiveAccountId = extras.getLong(EXTRA_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);	
		}
		else
		{
		    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
		    mActiveAccountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);		    
		}

    	if(mActiveAccountId == GooBase.INVALID_ID)
    	{
    		Log.w(TAG, "onCreate - invalid account id.");
    		if(isTaskRoot())    		
    			GooAccountsActivity.go(this, true);
    		else
    			finish();
    		return;
    	}    	

	    SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
	    editor.putLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, mActiveAccountId);
	    editor.commit();		
    	
    	GooAccount account = dbACCHelper.read(mActiveAccountId);
    	if(account == null)
    	{
    		Log.e(TAG, "onCreate - account is null");
    		if(isTaskRoot())    		
    			GooAccountsActivity.go(this, true);
    		else
    			finish();
    		return;
    	}
    	
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View headerContainer = inflater.inflate(R.layout.task_list_item_header, null);
		TextView header = (TextView)headerContainer.findViewById(R.id.taskList_header);
		headerContainer.setEnabled(false);
		header.setText(account.getName());
		
		setContentView(R.layout.task_list_collection);
		
		getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_LIST_COLLECTION);

		listView = (ListView) findViewById(R.id.task_list_collection);
		
		listView.addHeaderView(headerContainer);
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		getOAuthHelper().onResume();

		listView.setAdapter(null);	 
		Cursor c = dbTLCHelper.queryCursor(mActiveAccountId, GooSyncBase.SYNC_DELETE);
		mAdapter = new GooTaskListsCursorAdapter(this, c, true);		
		listView.setAdapter(mAdapter);	  
		
		mAdapter.requery();			
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(OAuthHelper.INTENT_ON_AUTH);
		registerReceiver(mOAuthReceiver, filter);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(mAdapter != null) mAdapter.getCursor().close();
		if (dbACCHelper != null) dbACCHelper.close(); 
		if (dbTLCHelper != null) dbTLCHelper.close(); 
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mOAuthReceiver);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.general_settings_menu_item, menu);
		getMenuInflater().inflate(R.menu.sync_menu_items, menu);
		getMenuInflater().inflate(R.menu.create_task_list_menu_item, menu);
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
			case R.id.menu_edit_mode: {
				//composeTaskList();
				return true;
			}
			case R.id.menu_create_task_list: {
				createTaskList();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case TaskListActionsDialog.DIALOG_ID:
	    	mTaskListActionsDialog = new TaskListActionsDialog(this);
	        return mTaskListActionsDialog;
	    default:
	        dialog = null;
	        break;
	    }
	    return dialog;
	}
	
    public static void go(Activity activity)
    {
    	go(activity, true);
    }
    
    public static void go(Activity activity, long accountId)
    {
    	go(activity, true, accountId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long accountId) {
		final Intent intent = new Intent(activity, GooTaskListsActivity.class);
		intent.putExtra(EXTRA_ACTIVE_ACCOUNT_ID, accountId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		if(finishActivity) activity.finish();
	}
	
	public static void go(Activity activity, boolean finishActivity) {
		final Intent intent = new Intent(activity, GooTaskListsActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		if(finishActivity) activity.finish();
	}
	
	@Override
	public void onClick(View v) {
		long taskListId = (Long)v.getTag();
		if(taskListId != GooBase.INVALID_ID)
		{
			GooTasksActivity.go(this, false, taskListId);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		long taskListId = (Long)v.getTag();
		if(taskListId != GooBase.INVALID_ID)
		{
			showTaskListActionsDialog(taskListId);
		}
		return false;
	}
	
	@Override
	public void sync(boolean withRefresh) {
		if(withRefresh) mAdapter.requery();
		TasksAppService.syncTaskLists(this, mSyncReceiver);
	}
	
	public void showTaskListActionsDialog(long taskListId)
	{		
		showDialog(TaskListActionsDialog.DIALOG_ID);		
		mTaskListActionsDialog.setTaskListId(taskListId);		
	}
	
	public void openAction(View v)
	{
		if(mTaskListActionsDialog != null && mTaskListActionsDialog.isShowing())
		{
			mTaskListActionsDialog.dismiss();
			GooTasksActivity.go(this, false, mTaskListActionsDialog.getTaskListId());
		}		
	}	
	
	public void deleteAction(View v)
	{
		if(mTaskListActionsDialog != null && mTaskListActionsDialog.isShowing())
		{
			mTaskListActionsDialog.dismiss();
			deleteTaskList(mTaskListActionsDialog.getTaskListId());
		}		
	}	
	
	public void renameAction(View v)
	{
		if(mTaskListActionsDialog != null && mTaskListActionsDialog.isShowing())
		{
			mTaskListActionsDialog.dismiss();
			renameTaskList(mTaskListActionsDialog.getTaskListId());
		}		
	}	
	
	public void helpAction(View v)
	{
		if(mTaskListActionsDialog != null && mTaskListActionsDialog.isShowing())
		{
			mTaskListActionsDialog.dismiss();
		}		
	}	
	
	public void deleteTaskList(final long taskListId)
	{    	
		if(taskListId == GooBase.INVALID_ID)
		{
			Log.e(TAG, "invalid taskListId.");
			return;
		}

		GooTaskList taskList = dbTLCHelper.read(taskListId);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Do you want to delete the list: \"" + taskList.title + "\"?\nThe list will be permanently deleted.")
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   new DeleteTaskList().execute(taskListId);
    	        	   dialog.dismiss();
    	           }
    	       })
    	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	builder.show();	
	}
	
	public void renameTaskList(final long taskListId)
	{		
    	if(taskListId == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "invalid taskListId.");
    		return;
    	}

		GooTaskList taskList = dbTLCHelper.read(taskListId);
		 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText input = new EditText(this); 
    	input.setHint("Task list title");
    	input.setText(taskList.title);
    	builder.setMessage("Rename list \"" + taskList.title + "\" to:")
    		   .setView(input)
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	           	   new RenameTaskList().execute(new RenameParam(taskListId, input.getText().toString()));
    	        	   input.getText().clear();
    	        	   dialog.dismiss();
    	           }
    	       })
    	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	builder.show();		
	}
	
	public void createTaskList()
	{				 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText input = new EditText(this); 
    	input.setHint("Task list title");
    	builder.setMessage("Create a new list named:")
    		   .setView(input)
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	           	   new CreateTaskList().execute(input.getText().toString());
    	        	   input.getText().clear();
    	        	   dialog.dismiss();
    	           }
    	       })
    	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	builder.show();		
	}
	
    private class RenameParam {
    	public RenameParam(long listId, String newTitle)
    	{
    		taskListId = listId;
    		title = newTitle;
    	}
    	long taskListId;
    	String title;
    }
    
	private class RenameTaskList extends AsyncTask<RenameParam, Void, Boolean> {
	     protected Boolean doInBackground(RenameParam... params) {
	    	 Boolean ret = true;
	    	 try
	    	 {
		    	 for (RenameParam param : params)
		    	 {
		    		 GooTaskList taskList = dbTLCHelper.read(param.taskListId);
		    		 taskList.title = param.title;
		    		 taskList.flagSyncState(GooSyncBase.SYNC_UPDATE);	
	        		 dbTLCHelper.update(taskList);
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

	private class CreateTaskList extends AsyncTask<String, Void, Boolean> {
	     protected Boolean doInBackground(String... titles) {
	    	 Boolean ret = true;
	    	 try
	    	 {
		    	 for (String title : titles)
		    	 {
		    		 GooTaskList taskList = new GooTaskList(mActiveAccountId, title);
		    		 taskList.flagSyncState(GooSyncBase.SYNC_CREATE);
		    		 dbTLCHelper.create(taskList);
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
	
	private class DeleteTaskList extends AsyncTask<Long, Void, Boolean> {
	     protected Boolean doInBackground(Long... ids) {
	    	 Boolean ret = true;
	    	 try
	    	 {
		    	 for (Long id : ids)
		    	 {
		    		 GooTaskList taskList = dbTLCHelper.read(id);
		    		 taskList.setSyncState(GooSyncBase.SYNC_DELETE);
		    		 dbTLCHelper.update(taskList);
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
					if (resultCode == TasksAppService.RESULT_SYNC_TASK_LISTS_SUCCESS) {
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
