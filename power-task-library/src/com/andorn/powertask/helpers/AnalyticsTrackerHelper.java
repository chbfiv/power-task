package com.andorn.powertask.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

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
    private LayoutInflater mInflater;
    
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
	public static final String CATEGORY_ALERT = "alert";
	
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
	public static final String ACTION_CLEAR_COMPLETED_TASK_LIST = "clear_completed_task_list";		
	public static final String ACTION_TRIAL_ENDED = "trial_ended";
	public static final String ACTION_TRIAL_ENDED_PURCHASE_APP = "trial_ended_purchase_app";	
	public static final String ACTION_TRIAL_ENDED_NO_THANKS = "trial_ended_no_thanks";	
	
    private final static int TRIAL_END_DAYS_UNTIL_PROMPT = 10;
    private final static int TRIAL_END_DAYS_UNTIL_PROMPT_MS = (TRIAL_END_DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000);
    
    private final static int TRIAL_END_NUMBER_OF_DAYS_EXTENTION = 1;
    private final static int TRIAL_END_NUMBER_OF_DAYS_EXTENTION_MS = (TRIAL_END_NUMBER_OF_DAYS_EXTENTION * 24 * 60 * 60 * 1000);
    
    public static AnalyticsTrackerHelper create(Activity activity, Context context) {
        return new AnalyticsTrackerHelper(activity, context);                
    }

    protected AnalyticsTrackerHelper(Activity activity, Context context) {
    	mActivity = activity;
    	mContext = context;
		mInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
    }
    
    public void onCreate(Bundle savedInstanceState)
    {			    
    	mTracker = GoogleAnalyticsTracker.getInstance();
    }
    
    public void onResume()
    {    	
    	refresh();
    	
    	if(sActivityCount == 0)
    	{
    		mTracker.startNewSession(GOOGLE_ANALYTICS_WEB_PROPERTY_ID, mContext);
    		setScreenOrientationCustomVar(mContext);
    		
    	}
    	
    	sActivityCount = sActivityCount < 0 ? 0 : sActivityCount; //reset to 0
    	sActivityCount++;    	
    	
		verifyTermsOfServiceAndTrial();
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
    
    public TaskApplication app()
    {    	
    	return TaskApplication.app(mActivity);
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
	    final SharedPreferences prefs = mSharedPrefUtil.getSharedPref();	  
	    mGoogleAnalytics = prefs.getBoolean(SharedPrefUtil.PREF_GOOGLE_ANALYTICS, DEFAULT_GOOGLE_ANALYTICS); 
	    
    	mTracker.setDebug(app().isDebug());
    	mTracker.setDryRun(app().isDebug());    	
    }
    
    private void verifyTermsOfServiceAndTrial()
    {
	    final SharedPreferences prefs = mSharedPrefUtil.getSharedPref();	
	    
	    // Terms of Service
	    boolean termsOfService = prefs.getBoolean(SharedPrefUtil.PREF_TERMS_OF_SERVICE, false); 	    
    	if(!termsOfService) showTermsOfServiceDialog();    	
    	
    	// Trial Period
    	if (app().isTrial())
	    {
    	    final SharedPreferences.Editor editor = mSharedPrefUtil.getEditor();
    	    
	        Long trialEndDate = prefs.getLong(SharedPrefUtil.PREF_TRIAL_END_DATE, 0);
	        // never set, set now
	        if (trialEndDate == 0) {
	            trialEndDate = System.currentTimeMillis() + TRIAL_END_DAYS_UNTIL_PROMPT_MS;
	            editor.putLong(SharedPrefUtil.PREF_TRIAL_END_DATE, trialEndDate);
	            editor.commit();
	        }
	        
	        // too far out, maybe tampering? reset to normal end date
	        if (System.currentTimeMillis() + TRIAL_END_DAYS_UNTIL_PROMPT_MS +
	        		TRIAL_END_NUMBER_OF_DAYS_EXTENTION_MS  < trialEndDate) {
	            trialEndDate = System.currentTimeMillis() + TRIAL_END_DAYS_UNTIL_PROMPT_MS;
	            editor.putLong(SharedPrefUtil.PREF_TRIAL_END_DATE, trialEndDate);
	            editor.commit();
	        }
	        
	        boolean trialEnded = System.currentTimeMillis() >= trialEndDate;
	        if (trialEnded) showTrialEndedDialog();
    	}
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
    
    private void setScreenOrientationCustomVar(Context context)
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
    
    private void showTermsOfServiceDialog()
    {    	
    	View termsOfService = mInflater.inflate(R.layout.terms_of_service, null);
    	
    	new AlertDialog.Builder(mActivity)
    	.setTitle("Terms of Service")
        .setView(termsOfService)
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
    
    private void showTrialEndedDialog() {
    	
	    final SharedPreferences.Editor editor = mSharedPrefUtil.getEditor();
    	final String tag = mActivity.getClass().getName();    	

        Long trialEndDate = System.currentTimeMillis() + TRIAL_END_NUMBER_OF_DAYS_EXTENTION_MS;
        editor.putLong(SharedPrefUtil.PREF_TRIAL_END_DATE, trialEndDate);
        editor.commit();

		mTracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_ALERT, 
				AnalyticsTrackerHelper.ACTION_TRIAL_ENDED, tag, 0);
    	
    	new AlertDialog.Builder(mActivity)
    	.setTitle("Purchase " + TaskApplication.APP_TITLE)
        .setMessage("If you enjoy using " + TaskApplication.APP_TITLE + ", please take a moment to purchase it.\n\n Thanks for your support!")
        .setPositiveButton("Purchase " + TaskApplication.APP_TITLE,  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
				mTracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_TRIAL_ENDED_PURCHASE_APP, tag, 0);
           	   mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TaskApplication.APP_PNAME)));
           }
        })
        .setNegativeButton("No, thanks. (1 day extention)",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               mTracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_TRIAL_ENDED_NO_THANKS, tag, 0);
               Toast.makeText(mContext, "No problem! Your trial has been extended " + " day(s). We hope you enjoy using " + TaskApplication.APP_TITLE + "!", 5);
           }
        })
        .setOnCancelListener(new OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
              mTracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_TRIAL_ENDED_NO_THANKS, tag, 0);
              Toast.makeText(mContext, "No problem! Your trial has been extended " + " day(s). We hope you enjoy using " + TaskApplication.APP_TITLE + "!", 5);
          }
        })
        .show();   
    }
}
