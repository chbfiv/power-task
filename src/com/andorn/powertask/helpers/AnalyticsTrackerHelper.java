package com.andorn.powertask.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

	public static final int CUSTOM_INDEX_1 = 1;
	public static final int CUSTOM_INDEX_2 = 2;
	public static final int CUSTOM_INDEX_3 = 3;
	public static final int CUSTOM_INDEX_4 = 4;
	public static final int CUSTOM_INDEX_5 = 5;

	public static final int CUSTOM_SCOPE_VISITOR = 1;
	public static final int CUSTOM_SCOPE_SESSION = 2;
	public static final int CUSTOM_SCOPE_PAGE = 3;
	
	public static final String CUSTOM_NAME_SCREEN_ORIENTATION = "screen_orientation";
	
	public static final String CUSTOM_VALUE_SCREEN_ORIENTATION_UNDEFINED = "undefined";
	public static final String CUSTOM_VALUE_SCREEN_ORIENTATION_LAND = "landscape";
	public static final String CUSTOM_VALUE_SCREEN_ORIENTATION_PORT = "portrait";
	public static final String CUSTOM_VALUE_SCREEN_ORIENTATION_SQUARE = "square";	
    
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
    
    public void setCustomVar(int index, String name, String value, int opt_scope)
    {
    	if (mGoogleAnalytics) mTracker.setCustomVar(index, name, value, opt_scope);
    }
    
    public void setScreenOrientationCustomVar(Context context)
    {
    	String orientation = CUSTOM_VALUE_SCREEN_ORIENTATION_UNDEFINED;
    	switch(context.getResources().getConfiguration().orientation)
    	{
    	case Configuration.ORIENTATION_LANDSCAPE:
    		orientation = CUSTOM_VALUE_SCREEN_ORIENTATION_LAND;
    		break;
    	case Configuration.ORIENTATION_PORTRAIT:
    		orientation = CUSTOM_VALUE_SCREEN_ORIENTATION_PORT;
    		break;
    	case Configuration.ORIENTATION_SQUARE:
    		orientation = CUSTOM_VALUE_SCREEN_ORIENTATION_SQUARE;
    		break;    	
    	}
    	
    	setCustomVar(CUSTOM_INDEX_1, CUSTOM_NAME_SCREEN_ORIENTATION, orientation, CUSTOM_SCOPE_SESSION);
    }    
}
