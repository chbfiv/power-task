package com.mtelab.taskhack.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.TaskApplication;
import com.mtelab.taskhack.adapters.GooTaskListCollectionAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.auth.OAuthReceiver;
import com.mtelab.taskhack.base.ActivityHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooAccountsOpenHelper;
import com.mtelab.taskhack.database.GooTaskListCollectionOpenHelper;
import com.mtelab.taskhack.database.GooTaskListOpenHelper;
import com.mtelab.taskhack.database.TCTagMapOpenHelper;
import com.mtelab.taskhack.database.TCTagsOpenHelper;
import com.mtelab.taskhack.dialogs.TaskActionsDialog;
import com.mtelab.taskhack.dialogs.TaskListActionsDialog;
import com.mtelab.taskhack.helpers.SharedPrefUtil;
import com.mtelab.taskhack.models.GooAccount;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooSyncBase;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.services.TasksAppService;

public class GooTaskListCollectionActivity extends BaseActivity implements
	OnClickListener, OnLongClickListener {
	
	private static final String TAG = GooTaskListCollectionActivity.class.getName();
	
	private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);
	private final GooTaskListCollectionOpenHelper dbTLCHelper = new GooTaskListCollectionOpenHelper(this);
	private final GooTaskListOpenHelper dbTLHelper = new GooTaskListOpenHelper(this);
	private final TCTagMapOpenHelper dbTagMapHelper = new TCTagMapOpenHelper(this);
	private final TCTagsOpenHelper dbTagsHelper = new TCTagsOpenHelper(this);
	
	private final static int REQUEST_COMPOSE_LIST = 0;
	private ListView listView;
	private GooTaskListCollectionAdapter mAdapter;
	
	private TaskListActionsDialog mTaskListActionsDialog;
	private long mActiveAccountId = GooBase.INVALID_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		getOAuthHelper().onCreate(savedInstanceState);
    	
    	if(!dbACCHelper.initialize() ||
			!dbTLCHelper.initialize() ||
			!dbTLHelper.initialize() ||
			!dbTagMapHelper.initialize() ||
			!dbTagsHelper.initialize())
    	{
    		Log.e(TAG, "onCreate - db failed to initialize.");
    		return;    		
    	}
    	
	    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
	    mActiveAccountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);
	    
    	if(mActiveAccountId == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "onCreate - invalid account id.");
    		goAccounts();
    		return;
    	}
    	
    	GooAccount account = dbACCHelper.read(mActiveAccountId);
    	if(account == null)
    	{
    		Log.e(TAG, "onCreate - account is null");
    		goAccounts();
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
		
		// get the task application to store the adapter which will act as the
		// task storage
		// for this demo.
		TaskApplication app = (TaskApplication) getApplication();
		mAdapter = app.getTaskListCollectionAdapter(this);
		
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
		if (dbTLCHelper != null) {
			dbTLCHelper.close();
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
		getMenuInflater().inflate(R.menu.create_task_list_menu_item, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_sync: {
				syncTaskListCollection();
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
	
	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooTaskList taskList = mAdapter.getItem(position);
			viewTaskList(taskList.getId());
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooTaskList taskList = mAdapter.getItem(position);
			showTaskListActionsDialog(taskList.getId());
		}	
		return false;
	}
	
	public void viewTaskList(long taskListId)
	{
		Intent intent = new Intent(this, GooTaskListActivity.class);
		intent.putExtra(GooTaskListActivity.EXTRA_ACTIVE_TASK_LIST_ID, taskListId);
		startActivity(intent);
	}
	
	public void showTaskListActionsDialog(long taskListId)
	{		
		showDialog(TaskListActionsDialog.DIALOG_ID);		
		mTaskListActionsDialog.setTaskListId(taskListId);		
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
		    		 mAdapter.set(dbTLCHelper.query(mActiveAccountId, GooSyncBase.SYNC_DELETE));
		    	 }
	    	 }
	    	 catch(Exception ex)
	    	 {
	    		 ret = false;
	    	 }
			return ret;	    	 
	     }
	
	     protected void onPostExecute(Boolean result) {
    		 mAdapter.notifyDataSetChanged();			
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
		    		 long taskListId = dbTLCHelper.create(taskList);
		    		 
		    		 taskList = dbTLCHelper.read(taskListId);
		    		 mAdapter.create(taskList);
		    	 }
	    	 }
	    	 catch(Exception ex)
	    	 {
	    		 ret = false;
	    	 }
			return ret;	    	 
	     }
	
	     protected void onPostExecute(Boolean result) {
	         mAdapter.notifyDataSetChanged();
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
		    		 taskList.flagSyncState(GooSyncBase.SYNC_DELETE);
		    		 dbTLCHelper.update(taskList);
		    	 }
	    		 mAdapter.set(dbTLCHelper.query(mActiveAccountId, GooSyncBase.SYNC_DELETE));
	    	 }
	    	 catch(Exception ex)
	    	 {
	    		 ret = false;
	    	 }
			return ret;	    	 
	     }
	
	     protected void onPostExecute(Boolean result) {
	         mAdapter.notifyDataSetChanged();
	     }
	}
	public void goAccounts() {
//		final Intent intent = new Intent(this, AccountsActivity.class);
//		startActivity(intent);
//		overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		final Intent intent = new Intent(this, ManageAccountsActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		finish();
	}

	public void syncTaskListCollection() {
	    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
	    final long accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);
	    
    	if(accountId == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "OAuthReceiver - invalid account id.");
    		return;
    	}
    	
		mAdapter.set(dbTLCHelper.query(accountId, GooSyncBase.SYNC_DELETE));
		Intent intent = new Intent(this, TasksAppService.class);
		intent.setFlags(TasksAppService.REQUEST_SYNC_TASK_LISTS);
		intent.putExtra(TasksAppService.REQUEST_RECEIVER_EXTRA, mLoadTaskListReceiver);
		startService(intent);
	}

	public void composeTaskList() {
		
	}

	// Manage UI Screens

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_COMPOSE_LIST:
			if (resultCode == Activity.RESULT_OK) {
				
			}
			break;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			    
		Intent intent = new Intent(this, GooTaskListActivity.class);
		intent.putExtra(GooTaskListActivity.EXTRA_ACTIVE_TASK_LIST_ID, id);
		startActivity(intent);
	}

//	@Override
//	public void onFocusChange(View v, boolean hasFocus) {
////		if(hasFocus)
////		{
////			mAddTaskListEdit.setText(null);       
////			mAddTaskListLayout.setVisibility(View.VISIBLE);
////			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
////	        imm.showSoftInput(mAddTaskListEdit, 0);
////		}
////		else
////		{
////			mAddTaskListLayout.setVisibility(View.GONE);
////		}
//	}
	
	private OAuthReceiver mOAuthReceiver = new OAuthReceiver() {	
	    
	    @Override
	    public void onAuthToken(Context context, String authToken) {	

		    TaskApplication app = (TaskApplication)getApplication();
	    	app.setAccessToken(authToken);  
	    	
	    	syncTaskListCollection();
	    }
	};
	
	private ResultReceiver mLoadTaskListReceiver = new ResultReceiver(null) {
	    @Override
	    protected void onReceiveResult(final int resultCode, final Bundle resultData) {			    	
				runOnUiThread(new Runnable() {
					public void run() {		
					    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
					    final long accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);			
						if (resultCode == TasksAppService.RESULT_SYNC_TASK_LISTS_SUCCESS) {
							getOAuthHelper().resetAuthAttempts();
							mAdapter.set(dbTLCHelper.query(accountId, GooSyncBase.SYNC_DELETE));
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
