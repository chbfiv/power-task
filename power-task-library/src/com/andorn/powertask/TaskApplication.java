package com.andorn.powertask;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksRequest;

import android.app.Application;
import android.content.Context;

public class TaskApplication extends Application {

    private static final boolean RELEASE = false;
    private static final boolean TRIAL = false;

    private static final Level LOGGING_LEVEL = Level.OFF;
    
    public final static String APP_TITLE = "Power Task";
    public final static String APP_PNAME = "com.andorn.powertask";

    private static final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private static final JacksonFactory jsonFactory = new JacksonFactory();
    private static final GoogleAccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(null);
    
	public static Tasks service;
    
	private final GooAccountsOpenHelper dbhAccounts = new GooAccountsOpenHelper(this);
	private final GooTaskListsOpenHelper dbhTaskLists = new GooTaskListsOpenHelper(this);
    private final GooTasksOpenHelper dbhTasks = new GooTasksOpenHelper(this);

    private static TaskApplication mInstance = null; 
    
    public static TaskApplication app(Context context)
    {  	
    	if(mInstance == null && context.getApplicationContext() instanceof TaskApplication)
    	{
    		mInstance = (TaskApplication)context.getApplicationContext();
    		service =
            Tasks.builder(transport, jsonFactory).setApplicationName(APP_PNAME)
                .setHttpRequestInitializer(accessProtectedResource)
                .setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {

                  public void initialize(JsonHttpRequest request) throws IOException {
                    TasksRequest tasksRequest = (TasksRequest) request;
                    tasksRequest.setKey(OAuthHelper.GOOGLE_API_KEY);
                  }
                }).build();
    	    Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);
    	}    	
    	return mInstance;
    }
    
    @Override
    public void onTerminate() {
    	super.onTerminate();
    	if (dbhAccounts != null) dbhAccounts.close();
    	if (dbhTaskLists != null) dbhTaskLists.close();
    	if (dbhTasks != null) dbhTasks.close();    	
    }
    
    public boolean isRelease()
    {
    	return RELEASE;
    }
    
    public boolean isDebug()
    {
    	return !RELEASE;
    }
    
    public boolean isTrial()
    {
    	return TRIAL;
    }
    
    interface TaskListener {
        void onTaskUpdated(String message, long id);
    }
    
	@Override
    public void onCreate() {
    	super.onCreate();
    }

    public Tasks getTasksService()
    {
    	return service;
    }
    
	public void setAccessToken(String authToken)
    {
    	accessProtectedResource.setAccessToken(authToken);
    }
    
	public GooAccountsOpenHelper getDbhAccounts() {
		return dbhAccounts;
	}
	
	public GooTaskListsOpenHelper getDbhTaskLists() {
		return dbhTaskLists;
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTasks;
	}
}
