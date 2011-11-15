package com.andorn.powertask.auth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.andorn.powertask.TaskApplication;
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
import android.widget.Toast;

public class OAuthHelper {

    protected Activity mActivity;
        
    public static OAuthHelper create(Activity activity) {
        return new OAuthHelper(activity);                
    }

    public OAuthHelper(Activity activity) {
        mActivity = activity;
    }
    
    private static final String TAG = OAuthHelper.class.getName();

//    private static final Level LOGGING_LEVEL = Level.OFF;
    
	//public static final String GOOGLE_API_KEY = "AIzaSyA_VcWSjcbbagRI8DW2jr1Iliy1D9P6IUg"; //chbfiv@gmail.com
    public static final String GOOGLE_API_KEY = "AIzaSyBK2Xe1teznQckJpZSGuFc51PIUGA0zZ_s"; //chbfiv@andornsoftware.com 
	public static final String GOOGLE_TASKS_AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";
    
	public static final String PREF_AUTH_TOKEN = "auth_token";
	public static final int MAX_AUTH_ATTEMPTS = 3;

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
    }

    public TaskApplication app()
    {    	
    	return TaskApplication.app(mActivity);
    }
    
	public void updateTokenExpiration(boolean tokenExpired) {
	    SharedPreferences prefs = SharedPrefUtil.getSharedPref(mActivity);
	    long accountId = prefs.getLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, -1);
	    int authAttempts = prefs.getInt(SharedPrefUtil.PREF_AUTH_ATTEMPTS, 0);
	    GooAccount gooAccount = app().getDbhAccounts().read(accountId);	    
	    
	    if (gooAccount != null) {
	      if (tokenExpired) {
	        accountManager.invalidateAuthToken(gooAccount.getAuthToken());	       
	        gooAccount.setAuthToken(null);
	        app().getDbhAccounts().update(gooAccount);
	      }
	      
	      if(authAttempts >= MAX_AUTH_ATTEMPTS)	      
	      {
	    	  Log.e(TAG, "Attempted Auth " + authAttempts + " times.");	 
	    	  Toast.makeText(mActivity, "Failed to authorize account. \n A manual sync will reset authorization attempts.", Toast.LENGTH_SHORT).show();
	      }     
	      else	 
	      {
		      authorizeAccount(gooAccount);	    	  
	      }
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
		if(app().getDbhAccounts().updateAuthToken(accountId, authToken))
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
	      HttpResponse response = ((HttpResponseException) e).getResponse();
	      int statusCode = response.getStatusCode();
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
}
