package com.mtelab.taskhack;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.mtelab.taskhack.adapters.GooAccountsAdapter;
import com.mtelab.taskhack.adapters.GooTaskListAdapter;
import com.mtelab.taskhack.adapters.GooTaskListCollectionAdapter;
import com.mtelab.taskhack.adapters.TCTagListAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.models.GooAccount;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.models.TCTagItem;
import com.mtelab.taskhack.shared.TaskChange;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TaskApplication extends Application {

    interface TaskListener {
        void onTaskUpdated(String message, long id);
    }

    public final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    public final JacksonFactory jsonFactory = new JacksonFactory();
    public final Tasks service = new Tasks("7ask-7ags/1.0", transport, jsonFactory);
    
    private TaskListener listener;
    
    private GooTaskListAdapter mTaskListAdapter;
    private GooTaskListCollectionAdapter mTaskListCollectionAdapter;
    private GooAccountsAdapter mAccountAdapter;
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
    
    public GooTaskListAdapter getTaskListAdapter() {
        return mTaskListAdapter;
    }
    
    public GooTaskListAdapter getTaskListAdapter(Context context) {
        if (mTaskListAdapter == null) {
        	mTaskListAdapter = new GooTaskListAdapter(context,  R.layout.task_item, new ArrayList<GooTask>());
        }
        return mTaskListAdapter;
    }
    
    public GooTaskListCollectionAdapter getTaskListCollectionAdapter() {
        return mTaskListCollectionAdapter;
    }
    
    public GooTaskListCollectionAdapter getTaskListCollectionAdapter(Context context) {
        if (mTaskListCollectionAdapter == null) {
        	mTaskListCollectionAdapter = new GooTaskListCollectionAdapter(context,  R.layout.task_list_item, new ArrayList<GooTaskList>());
        }
        return mTaskListCollectionAdapter;
    }
    
    public GooAccountsAdapter getAccountsAdapter() {
        return mAccountAdapter;
    } 
    
    public GooAccountsAdapter getAccountsAdapter(Context context) {
        if (mAccountAdapter == null) {
        	mAccountAdapter = new GooAccountsAdapter(context, R.layout.account, new ArrayList<GooAccount>());
        }
        return mAccountAdapter;
    }
    
    public void notifyListener(Intent intent) {
        if (listener != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String message = (String) extras.get("message");
                String[] messages = message.split(Pattern.quote(TaskChange.SEPARATOR));
                listener.onTaskUpdated(messages[0], Long.parseLong(messages[1]));
            }
        }
    }
}
