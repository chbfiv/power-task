package com.andorn.powertask.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsTrackerHelper {

    public static final String GOOGLE_ANALYTICS_WEB_PROPERTY_ID = "UA-25987520-1"; //chbfiv@andornsoftware.com
    
    protected Context mContext;   
    protected GoogleAnalyticsTracker mTracker;
    protected SharedPrefUtil mSharedPrefUtil;
    private boolean mDebug = false;
    private boolean mGoogleAnalytics = false;
    
    private static volatile int sActivityCount = 0;
    
	public static final String CATEGORY_UI_INTERACTION = "ui_interaction";
	public static final String CATEGORY_BACKGROUND_PROCESS = "background_process";
	
	public static final String ACTION_SYNC = "sync";	
	
    public static AnalyticsTrackerHelper create(Context context) {
        return new AnalyticsTrackerHelper(context);                
    }

    protected AnalyticsTrackerHelper(Context context) {
    	mContext = context;
    }
    
    public void onCreate(Bundle savedInstanceState)
    {			    
    	mTracker = GoogleAnalyticsTracker.getInstance();
    }
    
    public void onResume()
    {    	
    	if(sActivityCount == 0)
    		mTracker.startNewSession(GOOGLE_ANALYTICS_WEB_PROPERTY_ID, mContext);
    	
    	sActivityCount = sActivityCount < 0 ? 0 : sActivityCount; //reset to 0
    	sActivityCount++;
    	
    	refresh();
    }
    
    public void onStart()
    {
    	
    }
    
    public void onPause()
    {
    	
    }

    public void onStop() {	
		//TasksAppService.dispatchAnalytics(mContext, null);  
    	
    	sActivityCount--;
    	
		//try to upload analytics via service
		if (mGoogleAnalytics && sActivityCount <= 0) mTracker.dispatch();   
		
    	if(sActivityCount <= 0) mTracker.stopSession();     		  
    }
    
    public void onDestroy() 
    {		
    }  
    
    public void setSharedPrefUtil(SharedPrefUtil sharedPref)
    {
    	mSharedPrefUtil = sharedPref;
    }
    
    public static int getActivityCount()
    {
    	return sActivityCount;
    }    
    
    public void refresh()
    {
	    SharedPreferences prefs = mSharedPrefUtil.getSharedPref();	
	    mDebug = prefs.getBoolean(SharedPrefUtil.PREF_DEBUG, false);   
	    mGoogleAnalytics = prefs.getBoolean(SharedPrefUtil.PREF_GOOGLE_ANALYTICS, false);    

    	mTracker.setDebug(mDebug);
    	mTracker.setDryRun(mDebug);    	
    }
    
    public void trackPageView(String opt_pageURL)
    {
    	if (mGoogleAnalytics) mTracker.trackPageView(opt_pageURL);
    }
    
    public void trackEvent(String category, String action, String opt_label, int opt_value)
    {
    	if (mGoogleAnalytics) mTracker.trackEvent(category, action, opt_label, opt_value);
    }  
}
