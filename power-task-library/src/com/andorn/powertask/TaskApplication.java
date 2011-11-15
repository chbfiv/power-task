package com.andorn.powertask;

import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import android.app.Application;
import android.content.Context;

public class TaskApplication extends Application {

    private static final boolean RELEASE = false;
    private static final boolean TRIAL = false;
    
    public final static String APP_TITLE = "Power Task";
    public final static String APP_PNAME = "com.andorn.powertask";

    public final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    public final JacksonFactory jsonFactory = new JacksonFactory();
    @SuppressWarnings("deprecation")
	public final Tasks service = new Tasks(APP_PNAME, transport, jsonFactory);
    
	private final GooAccountsOpenHelper dbhAccounts = new GooAccountsOpenHelper(this);
	private final GooTaskListsOpenHelper dbhTaskLists = new GooTaskListsOpenHelper(this);
    private final GooTasksOpenHelper dbhTasks = new GooTasksOpenHelper(this);

    private static TaskApplication mInstance = null; 
    
    public static TaskApplication app(Context context)
    {  	
    	if(mInstance == null && context.getApplicationContext() instanceof TaskApplication)
    	{
    		mInstance = (TaskApplication)context.getApplicationContext();
    	}    	
    	return mInstance;
    }
    
    public static TaskApplication app()
    {    	
    	return mInstance;
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
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate() {
    	super.onCreate();
        service.setKey(OAuthHelper.GOOGLE_API_KEY);
    }

    public Tasks getTasksService()
    {
    	return service;
    }
    
    @SuppressWarnings("deprecation")
	public void setAccessToken(String authToken)
    {
    	service.setOauthToken(authToken); 
    }
    
	public GooAccountsOpenHelper getDbhAccounts() {
		dbhAccounts.initialize();
		return dbhAccounts;
	}
	
	public GooTaskListsOpenHelper getDbhTaskLists() {
		dbhTaskLists.initialize();
		return dbhTaskLists;
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		dbhTasks.initialize();
		return dbhTasks;
	}
}
