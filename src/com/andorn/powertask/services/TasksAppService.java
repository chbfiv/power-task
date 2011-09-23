package com.andorn.powertask.services;

import java.io.IOException;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.helpers.GeneralHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.models.GooAccount;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.models.GooTask;
import com.andorn.powertask.models.GooTaskList;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;
import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.Tasks;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

public class TasksAppService extends IntentService {

	private static final String TAG = TasksAppService.class.getName();

	private final SharedPrefUtil mSharedPref = SharedPrefUtil.createInstance(this);
	
	public static final String REQUEST_RECEIVER_EXTRA = TasksAppService.class + ".extra";
	
	public static final String EXTRA_TASK_LIST_ID = "task_list_id";
	public static final String EXTRA_TASK_ID = "task_id";
	public static final String EXTRA_ACCOUNT_ID = "account_id";
	
	public static final int RESULT_RECEIVER_ = 20000;
    
	public static final int REQUEST_SYNC_TASK_LISTS = 30000;
	public static final int REQUEST_SYNC_TASKS = 30001;
	public static final int REQUEST_SYNC_ACCOUNTS = 30002;
    
	public static final int RESULT_SYNC_SUCCESS_TASK_LISTS = 40000;
	public static final int RESULT_SYNC_SUCCESS_TASKS = 40001;
	public static final int RESULT_SYNC_SUCCESS_ACCOUNTS = 40002;

    public static final int RESULT_SYNC_FAILED = 49999;
	public static final int RESULT_SYNC_LOADING = 49998;
	public static final int RESULT_SYNC_LOADING_COMPLETE = 49997;
    public static final int RESULT_SYNC_FAILED_UNAUTHORIZED = 49996;
    
    public static final String RESULT_TASK_LISTS = "result_task_lists";

    private final GooAccountsOpenHelper dbhAccounts = new GooAccountsOpenHelper(this);
    private final GooTaskListsOpenHelper dbhTaskLists = new GooTaskListsOpenHelper(this);
	
    private com.google.api.services.tasks.v1.Tasks taskService;
    
    public TasksAppService()
	{
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	
	    long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, GooBase.INVALID_ID);
	    if(accountId == GooBase.INVALID_ID)
	    {
		    SharedPreferences prefs = SharedPrefUtil.getSharedPref(this);	    
		    accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);
	    }
	    
	    //if still invalid exit cleanly
	    if(intent.getFlags() != REQUEST_SYNC_ACCOUNTS && accountId == GooBase.INVALID_ID)
	    {
	    	TLog(TAG + " accountId is invalid " + intent.getAction());	    	
	    	return;
	    }
	    
	    GooAccount account = dbhAccounts.read(accountId);
	    //if still null exit cleanly
	    if(intent.getFlags() != REQUEST_SYNC_ACCOUNTS && account == null)
	    {
	    	TLog(TAG + " account is null " + intent.getAction());	    	
	    	return;
	    }
	    
	    if(intent.getFlags() != REQUEST_SYNC_ACCOUNTS && !account.getSync())
	    {
	    	return;
	    }	    
	    
	    final long taskListId = intent.getLongExtra(EXTRA_TASK_LIST_ID, GooBase.INVALID_ID);
//	    final long taskId = intent.getLongExtra(EXTRA_TASK_ID, GooBase.INVALID_ID);
	    
	    final ResultReceiver receiver = (ResultReceiver)intent.getParcelableExtra(REQUEST_RECEIVER_EXTRA);

    	boolean success = false;
	    //final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
	    try
	    {	    
		    if(receiver != null) receiver.send(RESULT_SYNC_LOADING, Bundle.EMPTY);
		    
		    int requestType = intent.getFlags();
		    
		    if (requestType == REQUEST_SYNC_TASK_LISTS) 
		    	success = dbhTaskLists.sync(this, accountId);
		    else if (requestType == REQUEST_SYNC_TASKS) 
		    {
	    		GooTaskList localList = dbhTaskLists.read(taskListId);
		    	success = dbhTaskLists.getDbhTasks().sync(this, localList);
		    }
		    else if (requestType == REQUEST_SYNC_ACCOUNTS) 
		    	processAccountsRequest(receiver);    
		    else success = true; //default
		    
		    if(receiver != null) 
	    	{
		    	if(!success) receiver.send(RESULT_SYNC_FAILED, Bundle.EMPTY);
		    	else if (requestType == REQUEST_SYNC_TASK_LISTS) receiver.send(RESULT_SYNC_SUCCESS_TASK_LISTS, Bundle.EMPTY);
		    	else if (requestType == REQUEST_SYNC_TASKS) receiver.send(RESULT_SYNC_SUCCESS_TASKS, Bundle.EMPTY);		    	
	    	}
	    }
	    catch (Exception e)
	    {
    		handleException(e, receiver);    	
	    }
	    if(receiver != null) receiver.send(RESULT_SYNC_LOADING_COMPLETE, Bundle.EMPTY);
	}
	
	private void processAccountsRequest(ResultReceiver receiver)
	{
		Bundle bundle = new Bundle(); 
    	try
    	{   		
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            
            for (Account account : accounts) {
            	GooAccount gooAccount = dbhAccounts.findAccountByName(account.name);
            	if(gooAccount != null)
            	{
            		//Required to know which google accounts have been removed to allow
            		//for removal from local cache.
            		gooAccount.localAccountFound = Boolean.TRUE;
            	}
            	else
            	{
            		//create a new local cache account (currently unauthorized to sync)
                	gooAccount = new GooAccount(account.name, account.type, true);  
                	dbhAccounts.create(gooAccount);
            	}
            }  
    		
	    	if(receiver != null) receiver.send(RESULT_SYNC_SUCCESS_ACCOUNTS, bundle);
    	} 
    	catch(Exception e)
    	{
    		handleException(e, receiver);
    	}
	}
	
	public static void handleException(Exception e, ResultReceiver receiver) {
	    if (e instanceof HttpResponseException) {
	      HttpResponse response = ((HttpResponseException) e).response;
	      int statusCode = response.statusCode;
	      try {
	        response.ignore();
	      } catch (IOException ioe) {
              Log.w(TAG, "Got IOException " + Log.getStackTraceString(ioe));
              Log.w(TAG, Log.getStackTraceString(ioe));
          } 
          
	      if (statusCode == 401) {
	    	  if(receiver != null) receiver.send(RESULT_SYNC_FAILED_UNAUTHORIZED, Bundle.EMPTY);
	    	  return;
	      }
	    }
        Log.w(TAG, "Got Exception " + e);
        Log.e(TAG, Log.getStackTraceString(e));
    	if(receiver != null) receiver.send(RESULT_SYNC_FAILED, Bundle.EMPTY);	
	}
	
	public static void handleException(Exception e) {
	    if (e instanceof HttpResponseException) {
	      HttpResponse response = ((HttpResponseException) e).response;
	      int statusCode = response.statusCode;
	      try {
	        response.ignore();
	      } catch (IOException ioe) {
              Log.w(TAG, "Got IOException " + Log.getStackTraceString(ioe));
              Log.w(TAG, Log.getStackTraceString(ioe));
          } 
          
	      if (statusCode == 401) {
	    	  return;
	      }
	    }
        Log.w(TAG, "Got Exception " + e);
        Log.e(TAG, Log.getStackTraceString(e));
	}
	
	@Override
	public void onCreate() {
		TLog(TAG + " service onCreate.");
		super.onCreate();
		
		TaskApplication app = (TaskApplication)getApplication();
		taskService = app.getTasksService();
	}	
	
	@Override
	public void onDestroy() {
		TLog(TAG + " service onDestroy.");
		super.onDestroy();
		
		if (dbhAccounts != null) dbhAccounts.close();
		if (dbhTaskLists != null) dbhTaskLists.close();
	}
	
	public static void syncAccounts(Context context, ResultReceiver receiver)
	{
		Intent intent = new Intent(context, TasksAppService.class);
		intent.setFlags(TasksAppService.REQUEST_SYNC_ACCOUNTS);
		intent.putExtra(TasksAppService.REQUEST_RECEIVER_EXTRA, receiver);
		context.startService(intent);
	}	
	
	public static void syncTaskLists(Context context, ResultReceiver receiver)
	{
		final SharedPrefUtil sharedPref = SharedPrefUtil.createInstance(context);
	    boolean offlineMode = sharedPref.getSharedPref().getBoolean(SharedPrefUtil.PREF_OFFLINE_MODE, false);	
	    
	    if(!offlineMode)
	    {
			Intent intent = new Intent(context, TasksAppService.class);
			intent.setFlags(REQUEST_SYNC_TASK_LISTS);
			intent.putExtra(REQUEST_RECEIVER_EXTRA, receiver);
			context.startService(intent);
	    }
	}	
	
	public static void syncTasks(Context context, long taskListId, ResultReceiver receiver)
	{
		final SharedPrefUtil sharedPref = SharedPrefUtil.createInstance(context);	    
	    boolean offlineMode = sharedPref.getSharedPref().getBoolean(SharedPrefUtil.PREF_OFFLINE_MODE, false);

	    if(!offlineMode)
	    {
			Intent intent = new Intent(context, TasksAppService.class);
			intent.setFlags(REQUEST_SYNC_TASKS);
			intent.putExtra(EXTRA_TASK_LIST_ID, taskListId);		
			intent.putExtra(REQUEST_RECEIVER_EXTRA, receiver);
			context.startService(intent);		
	    }
	}	
	
	// Task Lists
	
	public TaskLists queryRemoteTaskLists()
	{
		TaskLists result = null;
    	try
    	{   		
    		result = taskService.tasklists.list().execute();		
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}
	
	public TaskList createRemoteTaskList(GooTaskList local)
	{
		TaskList remote = new TaskList();
		TaskList result = null;
    	try
    	{   		
    		remote.title = local.title;
    		result = taskService.tasklists.insert(remote).execute();			
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}
	
	public TaskList updateRemoteTaskList(GooTaskList local)
	{
		TaskList remote = null;
		TaskList result = null;
    	try
    	{   		
    		remote = taskService.tasklists.get(local.remoteId).execute();
    		remote.title = local.title;
    		result = taskService.tasklists.update(remote.id, remote).execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}
	
	public boolean deleteRemoteTaskList(GooTaskList local)
	{
		boolean ret = false;
    	try
    	{   		
    		taskService.tasklists.delete(local.remoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
    	}
    	return ret;
	}	
	
	// Tasks
	
	public Tasks queryRemoteTasks(String taskListRemoteId)
	{
		Tasks result = null;
    	try
    	{   		
			if(GeneralHelper.isNullOrEmpty(taskListRemoteId)) return result;
			
    		result = taskService.tasks.list(taskListRemoteId).execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}

	public Task createRemoteTask(GooTask local)
	{
		Task remote = new Task();
		Task result = null;
    	try
    	{
			String taskListRemoteId = dbhTaskLists.getTaskListRemoteId(local.taskListId);

			if(taskListRemoteId ==  "") return result;
			
    		remote.title = local.title;
    		remote.notes = local.notes;
    		remote.status = local.status;
    		remote.due = local.due;
    		remote.completed = local.completed;
    		
			result = taskService.tasks.insert(taskListRemoteId, remote).execute();	
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}
	
	public Task updateRemoteTask(GooTask local)
	{
		Task remote = null;
		Task result = null;
    	try
    	{   
			String taskListRemoteId = dbhTaskLists.getTaskListRemoteId(local.taskListId);
			String taskRemoteId = local.remoteId;

			if(GeneralHelper.isNullOrEmpty(taskListRemoteId) ||
					GeneralHelper.isNullOrEmpty(taskRemoteId)) return result;
			
    		remote = taskService.tasks.get(taskListRemoteId, taskRemoteId).execute();

    		remote.title = local.title;
    		remote.notes = local.notes;
    		remote.status = local.status;
    		remote.due = local.due;
    		remote.completed = local.completed;

    		result = taskService.tasks.update(taskListRemoteId, taskRemoteId, remote).execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return result;
	}
	
	public boolean deleteRemoteTask(GooTask local)
	{
		boolean ret = false;
    	try
    	{   		
			String taskListRemoteId = dbhTaskLists.getTaskListRemoteId(local.taskListId);
			String taskRemoteId = local.remoteId;

			if(GeneralHelper.isNullOrEmpty(taskListRemoteId) || 
					GeneralHelper.isNullOrEmpty(taskRemoteId)) return ret;
			
    		taskService.tasks.delete(taskListRemoteId, taskRemoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
    	}
    	return ret;
	}	
	
	// Helpers
	
	private void TLog(String msg)
	{
	    boolean debug = mSharedPref.getSharedPref().getBoolean(SharedPrefUtil.PREF_DEBUG, false);	
	    
	    if(debug)
	    {
		    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	        Log.i(TAG, msg);
	    }
	}		
}
