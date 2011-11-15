package com.andorn.powertask.activities;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.helpers.AnalyticsTrackerHelper;
import com.andorn.powertask.helpers.FontHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public abstract class BaseActivity extends FragmentActivity {

	private final ActivityHelper mActivityHelper = ActivityHelper.create(this);
	private final OAuthHelper mOAuthHelper = OAuthHelper.create(this);
	private final SharedPrefUtil mSharedPref = SharedPrefUtil.create(this);
	private AnalyticsTrackerHelper mTrackerHelper;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getActivityHelper().onCreate(savedInstanceState);
		getOAuthHelper().onCreate(savedInstanceState);	
		
		mTrackerHelper = AnalyticsTrackerHelper.create(this, getApplication());
		mTrackerHelper.onCreate(savedInstanceState);		
		mTrackerHelper.setSharedPrefUtil(mSharedPref);
		
        FontHelper.createInstance(getAssets());
	}	

	@Override
	protected void onResume() {
		super.onResume();
		getOAuthHelper().onResume();	
		getTrackerHelper().onResume();	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getOAuthHelper().onPause();
		getTrackerHelper().onPause();	
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		getOAuthHelper().onStart();
		getTrackerHelper().onStart();	
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		getOAuthHelper().onStop();
		getTrackerHelper().onStop();	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        getActivityHelper().onDestroy();
		getOAuthHelper().onDestroy();
		getTrackerHelper().onDestroy();
	}	
    
    public TaskApplication app()
    {    	
    	return TaskApplication.app(this);
    }
    
	public AnalyticsTrackerHelper getTrackerHelper() {
        return mTrackerHelper;
    }    
	
	public ActivityHelper getActivityHelper() {
        return mActivityHelper;
    }    
    
    public OAuthHelper getOAuthHelper() {
        return mOAuthHelper;
    }    
    
    public SharedPrefUtil getSharedPrefUtil() {
        return mSharedPref;
    }  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return getActivityHelper().onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }   
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().onPostCreate(savedInstanceState);
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return getActivityHelper().onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
