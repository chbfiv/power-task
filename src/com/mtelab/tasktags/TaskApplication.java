package com.mtelab.tasktags;

import java.util.ArrayList;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.mtelab.tasktags.R;
import com.mtelab.tasktags.adapters.TCTagListAdapter;
import com.mtelab.tasktags.auth.OAuthHelper;
import com.mtelab.tasktags.models.TCTagItem;

import android.app.Application;
import android.content.Context;

public class TaskApplication extends Application {

    interface TaskListener {
        void onTaskUpdated(String message, long id);
    }

    public final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    public final JacksonFactory jsonFactory = new JacksonFactory();
    public final Tasks service = new Tasks("7ask-7ags/1.0", transport, jsonFactory);
    
    private TaskListener listener;
    
    private TCTagListAdapter mTagListAdapter;
    	
    private OAuthHelper mOAuthHelper;
    
    @Override
    public void onCreate() {
    	super.onCreate();
        service.accessKey = OAuthHelper.GOOGLE_API_KEY;  	
    }
        
    public void setTaskListener(TaskListener listener) {
        this.listener = listener;
    }

    public Tasks getTasksService()
    {
    	return service;
    }
    
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
