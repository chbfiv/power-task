package com.mtelab.tasktags.helpers;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {
	
    private static final String TAG = SharedPrefUtil.class.getName();

	public static final String PREF_ACTIVE_ACCOUNT_ID = "active_account_id";
	public static final String PREF_AUTH_ATTEMPTS = "auth_attempts";
	
    protected Context mContext;

    public static SharedPrefUtil createInstance(Context context) {
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
