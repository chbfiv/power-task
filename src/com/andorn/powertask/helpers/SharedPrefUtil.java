package com.andorn.powertask.helpers;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {

	@SuppressWarnings("unused")
    private static final String TAG = SharedPrefUtil.class.getName();

	public static final String PREF_ACTIVE_ACCOUNT_ID = "active_account_id";
	public static final String PREF_ACTIVE_TASKS_PAGE_POSITION = "active_tasks_page_position";
	public static final String PREF_TASKS_PAGE_SORT_TYPE = "tasks_page_sort_type";
	public static final String PREF_AUTH_ATTEMPTS = "auth_attempts";
	
	public static final String PREF_OFFLINE_MODE = "offline_mode";
	public static final String PREF_GOOGLE_ANALYTICS = "google_analytics";
	public static final String PREF_TERMS_OF_SERVICE = "terms_of_service";
	public static final String PREF_APP_RATING_NO_PROMPT = "app_rating_no_prompt";
	public static final String PREF_APP_RATING_LAUNCH_COUNT = "app_rating_launch_count";
	public static final String PREF_APP_RATING_FIRST_LAUNCH_DATE = "app_rating_first_launch_date";
	
    protected Context mContext;

    public static SharedPrefUtil create(Context context) {
        return new SharedPrefUtil(context);                
    }

    protected SharedPrefUtil(Context context) {
    	mContext = context;
    }    

    //Shared preferences
    private static final String SHARED_PREFS = "taskhack".toUpperCase(Locale.ENGLISH) + "_PREFS";

    public static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, 0);
    }
    
    public SharedPreferences getSharedPref() {
        return mContext.getSharedPreferences(SHARED_PREFS, 0);
    }
    
    public SharedPreferences.Editor getEditor() {
        return mContext.getSharedPreferences(SHARED_PREFS, 0).edit();
    }
}
