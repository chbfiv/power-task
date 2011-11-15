package com.andorn.powertask.services;

import java.io.IOException;
import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.helpers.ConnectivityHelper;
import com.andorn.powertask.helpers.GeneralHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.models.GooAccount;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.models.GooTask;
import com.andorn.powertask.models.GooTaskList;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.DateTime;

import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class TasksAppService extends IntentService {

	private static final String TAG = TasksAppService.class.getName();
	
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
	
    private com.google.api.services.tasks.Tasks taskService;
    
    // Service 
    
    public TasksAppService()
	{
		super(TAG);
	}

    public TaskApplication app()
    {    	
    	return TaskApplication.app(this);
    }
    
	@Override
	protected void onHandleIntent(Intent intent)
	{
		if (processLocalRequest(intent)) return;
		if (processRemoteRequests(intent)) return;	
	}
	
	private boolean processLocalRequest(Intent intent)
	{
		boolean localRequest = false;
    	boolean success = false;
		
	    final ResultReceiver receiver = (ResultReceiver)intent.getParcelableExtra(REQUEST_RECEIVER_EXTRA);
	    final int requestType = intent.getFlags();

	    if(requestType == REQUEST_SYNC_ACCOUNTS)
	    {
	    	localRequest = true;
		    try
		    {	    
			    if(receiver != null) receiver.send(RESULT_SYNC_LOADING, Bundle.EMPTY);			    
			    
			    success = app().getDbhAccounts().sync(this);
			    
			    if(receiver != null) 
		    	{
			    	if(!success) receiver.send(RESULT_SYNC_FAILED, Bundle.EMPTY);
			    	else receiver.send(RESULT_SYNC_SUCCESS_ACCOUNTS, Bundle.EMPTY);    	
		    	}
		    }
		    catch (Exception e)
		    {
	    		handleException(e, receiver);    	
		    }
		    if(receiver != null) receiver.send(RESULT_SYNC_LOADING_COMPLETE, Bundle.EMPTY);	
	    }
	    return localRequest;
	}
	
	private boolean processRemoteRequests(Intent intent)
	{
		boolean remoteRequest = false;
    	boolean success = false;
    	
	    final long taskListId = intent.getLongExtra(EXTRA_TASK_LIST_ID, GooBase.INVALID_ID);	    
	    final ResultReceiver receiver = (ResultReceiver)intent.getParcelableExtra(REQUEST_RECEIVER_EXTRA);
    	
		if (ConnectivityHelper.isAirplaneMode(this))
		{
	    	TLog(TAG + " currently in airplane mode; no network connectivity.");	    
		    //Toast.makeText(this, "currently in airplane mode; no network connectivity.", Toast.LENGTH_SHORT).show();		
	    	return remoteRequest;
		}
		
		if (!ConnectivityHelper.hasConnectivity(this))
		{
	    	TLog(TAG + " currently no network connectivity.");	    
		    //Toast.makeText(this, "currently no network connectivity.", Toast.LENGTH_SHORT).show();	
	    	return remoteRequest;
		}
		
	    long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, GooBase.INVALID_ID);
	    if(accountId == GooBase.INVALID_ID)
	    {
		    SharedPreferences prefs = SharedPrefUtil.getSharedPref(this);	    
		    accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);
	    }
	    
	    //if still invalid exit cleanly
	    if(accountId == GooBase.INVALID_ID)
	    {
	    	TLog(TAG + " accountId is invalid " + intent.getAction());	    	
	    	return remoteRequest;
	    }
	    
	    GooAccount account = app().getDbhAccounts().read(accountId);
	    //if still null exit cleanly
	    if(account == null)
	    {
	    	TLog(TAG + " account is null " + intent.getAction());	    	
	    	return remoteRequest;
	    }  	    
    	
	    try
	    {	    
		    if(receiver != null) receiver.send(RESULT_SYNC_LOADING, Bundle.EMPTY);
		    
		    int requestType = intent.getFlags();
		    
		    if (requestType == REQUEST_SYNC_TASK_LISTS) 
		    {
		    	remoteRequest = true;
		    	success = app().getDbhTaskLists().sync(this, account);
		    }
		    else if (requestType == REQUEST_SYNC_TASKS) 
		    {
		    	remoteRequest = true;
	    		GooTaskList localList = app().getDbhTaskLists().read(taskListId);
		    	success = app().getDbhTasks().sync(this, localList);
		    }
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
	    return remoteRequest;	
	}
		
	@Override
	public void onCreate() {
		//TLog(TAG + " service onCreate.");
		super.onCreate();
		
		taskService = app().getTasksService();
	}	
	
	@Override
	public void onDestroy() {
		//TLog(TAG + " service onDestroy.");
		super.onDestroy();
	}
	
	// Static Sync Requests
	
	public static void syncAccounts(Context context, ResultReceiver receiver)
	{
		Intent intent = new Intent(context, TasksAppService.class);
		intent.setFlags(REQUEST_SYNC_ACCOUNTS);
		intent.putExtra(REQUEST_RECEIVER_EXTRA, receiver);
		context.startService(intent);
	}	
	
	public static void syncTaskLists(Context context, ResultReceiver receiver)
	{
		final SharedPrefUtil sharedPref = SharedPrefUtil.create(context);
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
		final SharedPrefUtil sharedPref = SharedPrefUtil.create(context);	    
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
	
	// Accounts
	
	public String getGooAccountETag(long accountId)
	{
		String result = null;
    	try
    	{   		
    		GooAccount account = app().getDbhAccounts().read(accountId);
    		if(account != null) result = account.getETag();
    	} 
    	catch(Exception e)
    	{  		
            Log.e(TAG, "getGooAccountETag " + e.getMessage());   
    	}
    	return result;		
	}
	
	public String setGooAccountETag(long accountId, String eTag)
	{
		String result = null;
    	try
    	{  
    		app().getDbhAccounts().updateETag(accountId, eTag);
    	} 
    	catch(Exception e)
    	{  		
            Log.e(TAG, "getGooAccountETag " + e.getMessage());   
    	}
    	return result;		
	}
	
	public boolean shouldMergeTaskLists(String remoteListsETag, String accountETag)
	{
		if (GeneralHelper.isNullOrEmpty(remoteListsETag) ||
			GeneralHelper.isNullOrEmpty(accountETag)) return true;
		else return !remoteListsETag.equals(accountETag);
	}
	
	// Task Lists

	public TaskLists queryRemoteTaskLists() throws Exception
	{
		TaskLists result = null;
    	try    	
    	{  
    		com.google.api.services.tasks.Tasks.Tasklists.List request = 
    				taskService.tasklists().list();
    		request.setFields("etag,items(etag,id,title)"); 
    		result = request.execute();		
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
            throw e;     
    	}
    	return result;
	}
	
	public TaskList readRemoteTaskList(String taskListRemoteId) throws Exception
	{
		TaskList result = null;
    	try
    	{
			if(GeneralHelper.isNullOrEmpty(taskListRemoteId)) return result;
			com.google.api.services.tasks.Tasks.Tasklists.Get request = 
					taskService.tasklists().get(taskListRemoteId);
			request.setFields("etag,id,title");
    		result = request.execute();		
    	} 
    	catch(Exception e)
    	{
    		handleException(e);   
            throw e;      		
    	}
    	return result;
	}
	
	public TaskList createRemoteTaskList(GooTaskList local) throws Exception
	{
		TaskList remote = new TaskList();
		TaskList result = null;
    	try
    	{   		
    		remote.setTitle(local.title);
    		com.google.api.services.tasks.Tasks.Tasklists.Insert request =
    				taskService.tasklists().insert(remote);
    		request.setFields("etag,id,title");
    		result = request.execute();			
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    	
            throw e;     	
    	}
    	return result;
	}
	
	public TaskList updateRemoteTaskList(GooTaskList local) throws Exception
	{
		TaskList remote = null;
		TaskList result = null;
    	try
    	{   		
    		remote = taskService.tasklists().get(local.remoteId).execute();
    		remote.setTitle(local.title);
    		com.google.api.services.tasks.Tasks.Tasklists.Update request =
    				taskService.tasklists().update(remote.getId(), remote);
    		request.setFields("etag,id,title");
    		result = request.execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);  
            throw e;       		
    	}
    	return result;
	}
	
	public boolean deleteRemoteTaskList(GooTaskList local) throws Exception
	{
		boolean ret = false;
    	try
    	{   		
    		taskService.tasklists().delete(local.remoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
            throw e;     
    	}
    	return ret;
	}	
	
	public boolean clearRemoteTaskList(GooTaskList local) throws Exception
	{
		boolean ret = false;
    	try
    	{   		
    		taskService.tasks().clear(local.remoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
            throw e;     
    	}
    	return ret;
	}	
	
	// Tasks
	
	public Tasks queryRemoteTasks(String taskListRemoteId) throws Exception
	{
		Tasks result = null;
    	try
    	{   		
			if(GeneralHelper.isNullOrEmpty(taskListRemoteId)) return result;
			com.google.api.services.tasks.Tasks.TasksOperations.List request =
					taskService.tasks().list(taskListRemoteId);
    		request.setFields("etag,items(completed,deleted,due,etag,hidden,id,notes,parent,position,status,title,updated)");
    		request.setShowCompleted(true);
    		request.setShowDeleted(true);
    		request.setShowHidden(true);
    		result = request.execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);   
            throw e;      		
    	}
    	return result;
	}

	public Task createRemoteTask(GooTask local) throws Exception
	{
		Task remote = new Task();
		Task result = null;
    	try
    	{
			String taskListRemoteId = app().getDbhTaskLists().getTaskListRemoteId(local.taskListId);

			if(taskListRemoteId ==  "") return result;
			
    		remote.setTitle(local.title);
    		remote.setNotes(local.notes);
    		remote.setStatus(local.status);
    		remote.setDue(local.due != null ? DateTime.parseRfc3339(local.due) : null);
    		remote.setCompleted(local.completed != null ? DateTime.parseRfc3339(local.completed) : null);
    		com.google.api.services.tasks.Tasks.TasksOperations.Insert request =
    				taskService.tasks().insert(taskListRemoteId, remote);    		
    		request.setFields("completed,deleted,due,etag,hidden,id,notes,parent,position,status,title,updated");
			result = request.execute();	
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    	
            throw e;     	
    	}
    	return result;
	}
	
	public Task updateRemoteTask(GooTask local) throws Exception
	{
		Task remote = null;
		Task result = null;
    	try
    	{   
			String taskListRemoteId = app().getDbhTaskLists().getTaskListRemoteId(local.taskListId);
			String taskRemoteId = local.remoteId;

			if(GeneralHelper.isNullOrEmpty(taskListRemoteId) ||
					GeneralHelper.isNullOrEmpty(taskRemoteId)) return result;

    		com.google.api.services.tasks.Tasks.TasksOperations.Get request =
    				taskService.tasks().get(taskListRemoteId, taskRemoteId);
    		request.setFields("completed,deleted,due,etag,hidden,id,notes,parent,position,status,title,updated");
    		remote = request.execute();

    		remote.setTitle(local.title);
    		remote.setNotes(local.notes);
    		remote.setStatus(local.status);
    		remote.setDue(local.due != null ? DateTime.parseRfc3339(local.due) : null);
    		remote.setCompleted(local.completed != null ? DateTime.parseRfc3339(local.completed) : null);

    		com.google.api.services.tasks.Tasks.TasksOperations.Update requestUpdate =
    				taskService.tasks().update(taskListRemoteId, taskRemoteId, remote);
    		requestUpdate.setFields("completed,deleted,due,etag,hidden,id,notes,parent,position,status,title,updated");
    		remote = requestUpdate.execute();
    		
    		result = taskService.tasks().update(taskListRemoteId, taskRemoteId, remote).execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);  
            throw e;       		
    	}
    	return result;
	}
	
	public boolean deleteRemoteTask(GooTask local) throws Exception
	{
		boolean ret = false;
    	try
    	{   		
			String taskListRemoteId = app().getDbhTaskLists().getTaskListRemoteId(local.taskListId);
			String taskRemoteId = local.remoteId;

			if(GeneralHelper.isNullOrEmpty(taskListRemoteId) || 
					GeneralHelper.isNullOrEmpty(taskRemoteId)) return ret;
			
    		taskService.tasks().delete(taskListRemoteId, taskRemoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
            throw e;     
    	}
    	return ret;
	}
	
	
	// Helpers

	public static void handleException(Exception e, ResultReceiver receiver) {
	    if (e instanceof HttpResponseException) {
	      HttpResponse response = ((HttpResponseException) e).getResponse();
	      int statusCode = response.getStatusCode();
	      try {
	        response.ignore();
	      } catch (IOException ioe) {
              Log.w(TAG, "Got IOException " + Log.getStackTraceString(ioe));
              Log.w(TAG, Log.getStackTraceString(ioe));
          } 
          
	      if (statusCode == 401) {
	    	  Log.e(TAG, "401 Http Response Exception - " + response.getStatusMessage());	 
	    	  if(receiver != null) receiver.send(RESULT_SYNC_FAILED_UNAUTHORIZED, Bundle.EMPTY);
	    	  return;
	      }
	    }
        Log.w(TAG, "Got Exception " + e);
        Log.e(TAG, Log.getStackTraceString(e));
    	if(receiver != null) receiver.send(RESULT_SYNC_FAILED, Bundle.EMPTY);	
	}
	
	public static void handleException(Exception e) throws Exception {
	    if (e instanceof HttpResponseException) {
	      HttpResponse response = ((HttpResponseException) e).getResponse();
	      int statusCode = response.getStatusCode();
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
	
	private void TLog(String msg)
	{
	    //if(TaskApplication.DEBUG) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();	    
        Log.i(TAG, msg);
	}		
}
