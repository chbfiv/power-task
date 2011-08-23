package com.mtelab.taskhack.services;

import java.io.IOException;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;
import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.Tasks;
import com.mtelab.taskhack.TaskApplication;
import com.mtelab.taskhack.database.GooAccountsOpenHelper;
import com.mtelab.taskhack.database.GooTaskListsOpenHelper;
import com.mtelab.taskhack.database.GooTasksOpenHelper;
import com.mtelab.taskhack.database.TCTagMapOpenHelper;
import com.mtelab.taskhack.database.TCTagsOpenHelper;
import com.mtelab.taskhack.helpers.SharedPrefUtil;
import com.mtelab.taskhack.models.GooAccount;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

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
	
	public static final int CREATE_TASK = 30002;
	public static final int READ_TASK = 30003;
	public static final int UPDATE_TASK = 30004;
	public static final int DELETE_TASK = 30005;	
    
	public static final int RESULT_SYNC_TASK_LISTS_SUCCESS = 40000;
	public static final int RESULT_SYNC_TASKS_SUCCESS = 40001;
	public static final int RESULT_SYNC_ACCOUNTS_SUCCESS = 40002;
	
	public static final int RESULT_CREATE_TASK_SUCCESS = 40002;
	public static final int RESULT_READ_TASK_SUCCESS = 40003;
	public static final int RESULT_UPDATE_TASK_SUCCESS = 40004;
	public static final int RESULT_DELETE_TASK_SUCCESS = 40005;

	public static final int RESULT_FAILED = 49999;
	public static final int RESULT_LOADING = 49998;
	public static final int RESULT_LOADING_COMPLETE = 49997;
    public static final int RESULT_FAILED_UNAUTHORIZED = 49996;
    
    public static final String RESULT_TASK_LISTS = "result_task_lists";

    private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);
    private final GooTaskListsOpenHelper dbTLCHelper = new GooTaskListsOpenHelper(this);
    private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
    private final TCTagsOpenHelper dbTagsHelper = new TCTagsOpenHelper(this);
    private final TCTagMapOpenHelper dbTagMapHelper = new TCTagMapOpenHelper(this);
	
    private com.google.api.services.tasks.v1.Tasks taskService;
    
    public TasksAppService()
	{
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//TLog(TAG + " service onHandleIntent - started; action:" + intent.getAction());

		intent.getLongExtra(EXTRA_TASK_LIST_ID, GooBase.INVALID_ID);

	    long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, GooBase.INVALID_ID);
	    if(accountId == GooBase.INVALID_ID)
	    {
		    SharedPreferences prefs = SharedPrefUtil.getSharedPref(this);	    
		    accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, GooBase.INVALID_ID);
	    }
	    
	    final long taskListId = intent.getLongExtra(EXTRA_TASK_LIST_ID, GooBase.INVALID_ID);
	    final long taskId = intent.getLongExtra(EXTRA_TASK_ID, GooBase.INVALID_ID);
	    
	    final ResultReceiver receiver = (ResultReceiver)intent.getParcelableExtra(REQUEST_RECEIVER_EXTRA);
	    
	    //final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
	    if(receiver != null) receiver.send(RESULT_LOADING, Bundle.EMPTY);
	    try
	    {	    	
		    switch(intent.getFlags())
		    {
			    case REQUEST_SYNC_TASK_LISTS:
			    {
			    	processTaskListRequest(accountId, receiver);
			    	break;
			    }	  
			    case REQUEST_SYNC_TASKS:
			    {
			    	processTasksRequest(taskListId, receiver);
			    	break;
			    }	 
			    case REQUEST_SYNC_ACCOUNTS:
			    {
			    	processAccountsRequest(receiver);
			    	break;
			    }	    
			    case UPDATE_TASK:
			    {
			    	processUpdateTask(taskId, receiver);
			    	break;
			    }	
		    }	      
	    }
	    catch (Exception e)
	    {
    		handleException(e, receiver);
	    	if(receiver != null) receiver.send(RESULT_FAILED, Bundle.EMPTY);	    	
	    }
	    if(receiver != null) receiver.send(RESULT_LOADING_COMPLETE, Bundle.EMPTY);
	}
	
	private void processAccountsRequest(ResultReceiver receiver)
	{
		Bundle bundle = new Bundle(); 
    	try
    	{   		
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            
            for (Account account : accounts) {
            	GooAccount gooAccount = dbACCHelper.findAccountByName(account.name);
            	if(gooAccount != null)
            	{
            		//Required to know which google accounts have been removed to allow
            		//for removal from local cache.
            		gooAccount.localAccountFound = Boolean.TRUE;
            	}
            	else
            	{
            		//create a new local cache account (currently unauthorized to sync)
                	gooAccount = new GooAccount(account.name, account.type, false);  
                	dbACCHelper.create(gooAccount);
            	}
            }  
    		
	    	if(receiver != null) receiver.send(RESULT_SYNC_ACCOUNTS_SUCCESS, bundle);
    	} 
    	catch(Exception e)
    	{
    		handleException(e, receiver);
    	}
	}
	
	private void processTaskListRequest(long accountId, ResultReceiver receiver)
	{
		Bundle bundle = new Bundle(); 
    	try
    	{   		
			TaskLists lists = taskService.tasklists.list().execute();
			dbTLCHelper.sync(this, accountId, lists, lists.etag);
	    	if(receiver != null) receiver.send(RESULT_SYNC_TASK_LISTS_SUCCESS, bundle);
    	} 
    	catch(Exception e)
    	{
    		handleException(e, receiver);
    	}
	}
	
	private void processTasksRequest(long taskListId, ResultReceiver receiver)
	{
		Bundle bundle = new Bundle(); 
    	try
    	{   		
    		GooTaskList gooList = dbTLCHelper.read(taskListId);
    		if(gooList != null)
    		{
				Tasks list = taskService.tasks.list(gooList.remoteId).execute();
				dbTLHelper.createOrUpdateRange(taskListId, list);
		    	if(receiver != null) receiver.send(RESULT_SYNC_TASKS_SUCCESS, bundle);
    		}
    	} 
    	catch(Exception e)
    	{
    		handleException(e, receiver);
    	}
	}
	
	private void processUpdateTask(long taskId, ResultReceiver receiver) 
	{
		Bundle bundle = new Bundle(); 
    	try
    	{    		
    		GooTask gooTask = dbTLHelper.read(taskId);
    		if(gooTask != null)
    		{
    			String taskListRemoteId = dbTLCHelper.getTaskListRemoteId(gooTask.taskListId);
    			
	    		Task task = taskService.tasks.get(taskListRemoteId, gooTask.remoteId).execute();
	    		task = gooTask.Sync(task);
    			Task result = taskService.tasks.update(taskListRemoteId, task.id, task).execute();
		    	if(receiver != null) receiver.send(RESULT_UPDATE_TASK_SUCCESS, bundle);
    		}
    	} 
    	catch(Exception e)
    	{
    		handleException(e, receiver);
    	}
	}
	
	private void handleException(Exception e, ResultReceiver receiver) {
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
	    	  if(receiver != null) receiver.send(RESULT_FAILED_UNAUTHORIZED, Bundle.EMPTY);
	    	  return;
	      }
	    }
        Log.w(TAG, "Got Exception " + e);
        Log.e(TAG, Log.getStackTraceString(e));
	}
	
	private void handleException(Exception e) {
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
		super.onCreate();
		
		TaskApplication app = (TaskApplication)getApplication();
		taskService = app.getTasksService();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		//TLog(TAG + " service onStart.");
		super.onStart(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        TLog(TAG + " service start command.");
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		//TLog(TAG + " service onBind.");
		return super.onBind(intent);
	}
	
	@Override
	public void onRebind(Intent intent) {
		//TLog(TAG + " service onRebind.");
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		//TLog(TAG + " service onUnbind.");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		TLog(TAG + " service onDestroy.");
		super.onDestroy();
		if (dbTLCHelper != null) {
			dbTLCHelper.close();
	    }
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//TLog(TAG + " service onConfigurationChange.");
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onLowMemory() {
		//TLog(TAG + " service onLowMemory.");
		super.onLowMemory();
	}
	
	private void TLog(String msg)
	{
	    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.i(TAG, msg);	
	}	
	
	public static void updateTask(Context context, long taskId, ResultReceiver receiver)
	{
		Intent intent = new Intent(context, TasksAppService.class);
		intent.setFlags(UPDATE_TASK);
		intent.putExtra(EXTRA_TASK_ID, taskId);		
		intent.putExtra(REQUEST_RECEIVER_EXTRA, receiver);
		context.startService(intent);		
	}	
	
	public TaskList createRemoteTaskList(GooTaskList localList)
	{
		TaskList remoteList = new TaskList();
		TaskList resultList = null;
    	try
    	{   		
    		remoteList.title = localList.title;
    		resultList = taskService.tasklists.insert(remoteList).execute();			
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return resultList;
	}
	
	public TaskList updateRemoteTaskList(GooTaskList localList)
	{
		TaskList remoteList = null;
		TaskList resultList = null;
    	try
    	{   		
    		remoteList = taskService.tasklists.get(localList.remoteId).execute();
    		remoteList.title = localList.title;

    		resultList = taskService.tasklists.update(remoteList.id, remoteList).execute();
    	} 
    	catch(Exception e)
    	{
    		handleException(e);    		
    	}
    	return resultList;
	}
	
	public boolean deleteRemoteTaskList(GooTaskList localList)
	{
		boolean ret = false;
    	try
    	{   		
    		taskService.tasklists.delete(localList.remoteId).execute();
    		ret = true;
    	} 
    	catch(Exception e)
    	{
    		handleException(e);
    	}
    	return ret;
	}	
}
