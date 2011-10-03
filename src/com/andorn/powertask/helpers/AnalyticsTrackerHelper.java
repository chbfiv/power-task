package com.andorn.powertask.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.andorn.powertask.R;
import com.andorn.powertask.TaskApplication;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsTrackerHelper {

    public static final String GOOGLE_ANALYTICS_WEB_PROPERTY_ID = "UA-25987520-1"; //chbfiv@andornsoftware.com

    protected Activity mActivity;  
    protected Context mContext;   
    protected GoogleAnalyticsTracker mTracker;
    protected SharedPrefUtil mSharedPrefUtil;
    private boolean mGoogleAnalytics = false;
    private boolean mTermsOfService = false;
    
	public static final boolean DEFAULT_GOOGLE_ANALYTICS = true;
    
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
	public static final String ACTION_UP = "up";	
	public static final String ACTION_APP_RATE = "app_rate";	
	public static final String ACTION_APP_RATE_REMIND_ME_LATER = "app_rate_remind_me_later";
	public static final String ACTION_APP_RATE_NO_THANKS = "app_rate_no_thanks";	
	public static final String ACTION_AGREE_TERMS_OF_SERVICE = "agree_terms_of_service";	
	public static final String ACTION_DISAGREE_TERMS_OF_SERVICE = "disagree_terms_of_service";		
	public static final String ACTION_CREATE_TASK_LIST = "create_task_list";	
	public static final String ACTION_RENAME_TASK_LIST = "rename_task_list";	
	public static final String ACTION_DELETE_TASK_LIST = "delete_task_list";	
	
    public static AnalyticsTrackerHelper create(Activity activity, Context context) {
        return new AnalyticsTrackerHelper(activity, context);                
    }

    protected AnalyticsTrackerHelper(Activity activity, Context context) {
    	mActivity = activity;
    	mContext = context;
    }
    
    public void onCreate(Bundle savedInstanceState)
    {			    
    	mTracker = GoogleAnalyticsTracker.getInstance();
    }
    
    public void onResume()
    {    	
    	refresh();
    	
    	if(sActivityCount == 0)
    		mTracker.startNewSession(GOOGLE_ANALYTICS_WEB_PROPERTY_ID, mContext);
    	
    	sActivityCount = sActivityCount < 0 ? 0 : sActivityCount; //reset to 0
    	sActivityCount++;    	
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
    	mTracker.setDebug(TaskApplication.DEBUG);
    	mTracker.setDryRun(TaskApplication.DEBUG);    	
    }
    
    public void verifyTermsOfService()
    {
	    SharedPreferences prefs = mSharedPrefUtil.getSharedPref();	  
	    mGoogleAnalytics = prefs.getBoolean(SharedPrefUtil.PREF_GOOGLE_ANALYTICS, DEFAULT_GOOGLE_ANALYTICS);    
	    mTermsOfService = prefs.getBoolean(SharedPrefUtil.PREF_TERMS_OF_SERVICE, false); 
	    
    	if(!mTermsOfService) ShowTermsOfServiceDialog();    	
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
    
    public void ShowTermsOfServiceDialog()
    {
    	String terms = mActivity.getString(R.string.terms_of_service_part_1);
    	
    	new AlertDialog.Builder(mActivity)
    	.setTitle("Terms of Service - part 1")
        .setMessage(terms)
        .setPositiveButton("Accept - More",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   ShowTermsOfServiceDialogPart2();	
           }
        })
        .setNegativeButton("Cancel",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
         	  mActivity.finish();

          	  trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
      				AnalyticsTrackerHelper.ACTION_DISAGREE_TERMS_OF_SERVICE, mActivity.getClass().getName(), 0);
           }
        })
        .setOnCancelListener(new OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
        	  mActivity.finish();
        	  
          	  trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
      				AnalyticsTrackerHelper.ACTION_DISAGREE_TERMS_OF_SERVICE, mActivity.getClass().getName(), 0);
          }
        })
        .show();	
    }    
    
    public void ShowTermsOfServiceDialogPart2()
    {
    	String terms = mActivity.getString(R.string.terms_of_service_part_2);
    	
    	new AlertDialog.Builder(mActivity)
    	.setTitle("Terms of Service - part 2")
        .setMessage(terms)
        .setPositiveButton("Accept",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
       	    	SharedPreferences.Editor editor = mSharedPrefUtil.getEditor();
	       	    editor.putBoolean(SharedPrefUtil.PREF_TERMS_OF_SERVICE, true);
	       	    editor.commit();	
	       	    
          	    trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
        				AnalyticsTrackerHelper.ACTION_AGREE_TERMS_OF_SERVICE, mActivity.getClass().getName(), 0);
           }
        })
        .setNegativeButton("Cancel",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
         	  mActivity.finish();
         	  
          	  trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
      				AnalyticsTrackerHelper.ACTION_DISAGREE_TERMS_OF_SERVICE, mActivity.getClass().getName(), 0);
           }
        })
        .setOnCancelListener(new OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
        	  mActivity.finish();
        	  
          	  trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
      				AnalyticsTrackerHelper.ACTION_DISAGREE_TERMS_OF_SERVICE, mActivity.getClass().getName(), 0);
          }
        })
        .show();	
    }    
}
