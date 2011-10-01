package com.andorn.powertask.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;

//http://www.androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
public class AppRaterHelper {
	
	private static final String TAG = AppRaterHelper.class.getName();
	
    private final static String APP_TITLE = "Power Task";
    private final static String APP_PNAME = "com.andorn.powertask";
    
    private final static int DAYS_UNTIL_PROMPT = 15;
    private final static int LAUNCHES_UNTIL_PROMPT = 10;
    
    public static void app_launched(Context context, final AnalyticsTrackerHelper tracker) {
        SharedPreferences prefs = SharedPrefUtil.getSharedPref(context);
        if (prefs.getBoolean(SharedPrefUtil.PREF_APP_RATING_NO_PROMPT, false)) { return ; }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong(SharedPrefUtil.PREF_APP_RATING_LAUNCH_COUNT, 0) + 1;
        editor.putLong(SharedPrefUtil.PREF_APP_RATING_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(SharedPrefUtil.PREF_APP_RATING_FIRST_LAUNCH_DATE, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(SharedPrefUtil.PREF_APP_RATING_FIRST_LAUNCH_DATE, date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + 
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, editor, tracker);
            }
        }
        
        editor.commit();
    }   
    
    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor, final AnalyticsTrackerHelper tracker) {
    	
    	new AlertDialog.Builder(context)
    	.setTitle("Rate " + APP_TITLE)
        .setMessage("If you enjoy using " + APP_TITLE + ", please take a moment to rate it.\n\n Thanks for your support!")
        .setPositiveButton("Rate " + APP_TITLE,  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               if (editor != null) {
                   editor.putBoolean(SharedPrefUtil.PREF_APP_RATING_NO_PROMPT, true);
                   editor.commit();
               }
				tracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_APP_RATE, TAG, 0);
           	   context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
           }
        })
        .setNeutralButton("Remind me later",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               if (editor != null) {
                   long date_firstLaunch = System.currentTimeMillis();
                   editor.putLong(SharedPrefUtil.PREF_APP_RATING_FIRST_LAUNCH_DATE, date_firstLaunch);
                   editor.commit();
               }
				tracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_APP_RATE_REMIND_ME_LATER, TAG, 0);
           }
        })
        .setNegativeButton("No, thanks",  new OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               if (editor != null) {
                   editor.putBoolean(SharedPrefUtil.PREF_APP_RATING_NO_PROMPT, true);
                   editor.commit();
               }
				tracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_APP_RATE_NO_THANKS, TAG, 0);
           }
        })
        .setOnCancelListener(new OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
              if (editor != null) {
                  long date_firstLaunch = System.currentTimeMillis();
                  editor.putLong(SharedPrefUtil.PREF_APP_RATING_FIRST_LAUNCH_DATE, date_firstLaunch);
                  editor.commit();
              }
				tracker.trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
						AnalyticsTrackerHelper.ACTION_APP_RATE_NO_THANKS, TAG, 0);
          }
        })
        .show();   
    }
}