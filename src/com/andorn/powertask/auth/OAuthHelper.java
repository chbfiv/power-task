package com.andorn.powertask.auth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.models.GooAccount;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class OAuthHelper {

    protected Activity mActivity;
    
	private final GooAccountsOpenHelper dbACCHelper;
    
    public static OAuthHelper createInstance(Activity activity) {
        return new OAuthHelper(activity);                
    }

    public OAuthHelper(Activity activity) {
        mActivity = activity;
        dbACCHelper = new GooAccountsOpenHelper(activity);
    }
    
    private static final String TAG = OAuthHelper.class.getName();
    
	public static final String GOOGLE_API_KEY = "AIzaSyA_VcWSjcbbagRI8DW2jr1Iliy1D9P6IUg";
	public static final String GOOGLE_TASKS_AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";
//	protected static final String GOOGLE_TASKS_REDIRECT_URL = "urn:ietf:wg:oauth:2.0:oob";
//	protected static final String GOOGLE_TASKS_CLIENT_ID = "1069291121197.apps.googleusercontent.com";
//	protected static final String GOOGLE_TASKS_CLIENT_SECRET = "_rKdUMaS5Ga9xQHeOu3YHSOZ";
    
//    protected static final String AUTH_COOKIE_NAME = "SACSID";
	public static final String PREF_SYNC = "sync";
	public static final String PREF_AUTH_TOKEN = "auth_token";
//    protected static final String PREF_AUTH_COOKIE = "auth_cookie";
	public static final int MAX_AUTH_ATTEMPTS = 3;

	public static final String HASH = "#";

	public static final String INTENT_ON_AUTH = OAuthHelper.class.getName() + ".ON_AUTH";
	public static final String INTENT_EXTRA_AUTH_TOKEN = "auth_token";
    
	public static final int REQUEST_ADD_ACCOUNT = 10000;
	public static final int REQUEST_SYNC_SETTINGS = 10001;
	public static final int REQUEST_AUTHENTICATE = 10002;
    
	public GoogleAccessProtectedResource accessProtectedResource;
    public GoogleAccountManager accountManager;
    
    public void onCreate(Bundle savedInstanceState) {
	    accountManager = new GoogleAccountManager(mActivity);	    

		final SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);	 
	    prefs.edit().putInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, 0).commit();
	    
        Logger.getLogger("com.google.api.client.http").setLevel(Level.CONFIG);
	}
	
	public void onResume() {
        resetAuthAttempts();
        updateTokenExpiration(false);
    }

	public void onStart() {

    }
	
	public void onStop() {

    }
	
	public void onPause() {

    }
	
	public void onDestroy() {
        resetAuthAttempts();        
		if (dbACCHelper != null) dbACCHelper.close();
    }
    
	public void updateTokenExpiration(boolean tokenExpired) {
	    SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);
	    long accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, -1);
	    int authAttempts = prefs.getInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, 0);
	    GooAccount gooAccount = dbACCHelper.read(accountId);	    
	    
	    if (gooAccount != null) {
	      if (tokenExpired) {
	        accountManager.invalidateAuthToken(gooAccount.getAuthToken());	       
	        gooAccount.setAuthToken(null);
	        dbACCHelper.update(gooAccount);
	      }
	      
	      if(authAttempts >= MAX_AUTH_ATTEMPTS)	      
	    	  Log.e(TAG, "Attempted Auth " + authAttempts + " times.");	      
	      else	      
		      authorizeAccount(gooAccount);	    	  
	      
	      return;
	    }
	    //otherwise take them to the accounts page
//		final Intent intent = new Intent(mActivity, GooAccountsActivity.class);
//		mActivity.startActivity(intent);
//		mActivity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
	}
	
	public void authorizeAccount(GooAccount gooAccount) {		
		
		SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);	 
	    int authAttempts = prefs.getInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, 0);	    
	    prefs.edit().putInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, authAttempts + 1).commit();
	    
	    //make sure this is a real account
	    Account account = accountManager.getAccountByName(gooAccount.getName());
	    if(account == null)	      
	    {
	    	  Log.e(TAG, "Account null for " + gooAccount.getName() + "; unauthorized?");
	    	  return;
	    }
	    
	    accountManager.manager.getAuthToken(
	        account, GOOGLE_TASKS_AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>() {

	          public void run(AccountManagerFuture<Bundle> future) {
	            try {
	              Bundle bundle = future.getResult();
	              if (bundle.containsKey(AccountManager.KEY_INTENT)) {
	                Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
	                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
	                mActivity.startActivityForResult(intent, REQUEST_AUTHENTICATE);
	              } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {   
	            	  String authToken = bundle.get(AccountManager.KEY_AUTHTOKEN).toString();
	                  onAuthToken(authToken);
	              }
	            } catch (Exception e) {
	              handleException(e);
	            }
	          }
	        }, null);
	}
	
	public void onAuthToken(String authToken) {
	    new GoogleAccessProtectedResource(authToken) {
	        @Override
	        protected void onAccessToken(String accessToken) {
	        	updateTokenExpiration(true);
	        }
	      };
	      
		final SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);	   
		long accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, -1);
		if(dbACCHelper.update(accountId, authToken))
		{	    
			final Intent intent = new Intent(INTENT_ON_AUTH);
			intent.putExtra(INTENT_EXTRA_AUTH_TOKEN, authToken);
			mActivity.sendBroadcast(intent);
		}
		else
		{			
			Log.e(TAG, "Failed to update onAuthToken for accountId = '" + accountId + "'.");			
		}
	}
	  
	public void resetAuthAttempts()
	{
		final SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);	
	    prefs.edit().putInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, 0).commit();		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	String tmp;
//		switch (requestCode) {
//			case REQUEST_ADD_ACCOUNT:
//				if (resultCode == Activity.RESULT_OK) {
//					tmp = "asd1";						
//				}
//				else
//				{					
//					tmp = "asd2";					
//				}
//				break;
//			case REQUEST_SYNC_SETTINGS:
//				if (resultCode == Activity.RESULT_OK) {
//					tmp = "asd3";					
//				}
//				else
//				{				
//					tmp = "asd4";					
//				}
//				break;
//			case REQUEST_AUTHENTICATE:
//				if (resultCode == Activity.RESULT_OK) {
//					//gotAuthToken(account);	
//				}
//				else
//				{				
//					tmp = "asd4";					
//				}
//				break;
//		}
	}

	public void addAccount() {
		Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.contacts"
		});
		mActivity.startActivityForResult(intent, REQUEST_ADD_ACCOUNT);		
	}
	
	public void syncSettings() {
		Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.contacts"
		});
		mActivity.startActivityForResult(intent, REQUEST_SYNC_SETTINGS);		
	}
	
	public void handleException(Exception e) {
	    if (e instanceof HttpResponseException) {
	      HttpResponse response = ((HttpResponseException) e).response;
	      int statusCode = response.statusCode;
	      try {
	        response.ignore();
	      } catch (IOException ioe) {
              Log.w(TAG, "Got IOException " + Log.getStackTraceString(ioe));
              Log.w(TAG, Log.getStackTraceString(ioe));
          } 
          
	      // TODO: should only try this once to avoid infinite loop
	      if (statusCode == 401) {
	    	  updateTokenExpiration(true);
	    	  return;
	      }
	    }
        Log.w(TAG, "Got Exception " + e);
        Log.e(TAG, Log.getStackTraceString(e));
	}
	
	public static String getSyncPrefField(String accountName)
	{
		return accountName + HASH + PREF_SYNC;
	}    
	
	public static String getAuthTokenPrefField(String accountName)
	{
		return accountName + HASH + PREF_AUTH_TOKEN;
	}
	
//	public static String getAuthCookiePrefField(String accountName)
//	{
//		return accountName + HASH + PREF_AUTH_COOKIE;
//	}

}
