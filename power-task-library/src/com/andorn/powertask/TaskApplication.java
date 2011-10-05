package com.andorn.powertask;

import java.util.ArrayList;

import com.andorn.powertask.adapters.TCTagListAdapter;
import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.models.TCTagItem;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.andorn.powertask.R;

import android.app.Application;
import android.content.Context;

public class TaskApplication extends Application {

    private static final boolean RELEASE = false;
    private static final boolean TRIAL = false;
    
    public final static String APP_TITLE = "Power Task";
    public final static String APP_PNAME = "com.andorn.powertask";

    public final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    public final JacksonFactory jsonFactory = new JacksonFactory();
    public final Tasks service = new Tasks(APP_PNAME, transport, jsonFactory);
    
    private TCTagListAdapter mTagListAdapter;
    
    public static TaskApplication get(Context context)
    {
    	TaskApplication app = null;    	
    	if(context.getApplicationContext() instanceof TaskApplication)
    	{
    		app = (TaskApplication)context.getApplicationContext();
    	}    	
    	return app;
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
        service.accessKey = OAuthHelper.GOOGLE_API_KEY;  	
    }

    public Tasks getTasksService()
    {
    	return service;
    }
    
    @SuppressWarnings("deprecation")
	public void setAccessToken(String authToken)
    {
    	service.setAccessToken(authToken);  
    }
    
    public TCTagListAdapter getTagListAdapter() {
        return mTagListAdapter;
    }   
    
    public TCTagListAdapter getTagListAdapter(Context context) {
        if (mTagListAdapter == null) {
        	mTagListAdapter = new TCTagListAdapter(context,  R.layout.tag_item, new ArrayList<TCTagItem>());
        }
        return mTagListAdapter;
    }  
}
