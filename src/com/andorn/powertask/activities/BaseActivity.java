package com.andorn.powertask.activities;

import com.andorn.powertask.auth.OAuthHelper;
import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.helpers.FontHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A base activity that defers common functionality across app activities to an
 * {@link ActivityHelper}. This class shouldn't be used directly; instead, activities should
 * inherit from {@link BaseSinglePaneActivity} or {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends FragmentActivity {
	
    final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
    final OAuthHelper mOAuthHelper = OAuthHelper.createInstance(this);
    final SharedPrefUtil mSharedPref = SharedPrefUtil.createInstance(this);
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mActivityHelper.onCreate(savedInstanceState);
		getOAuthHelper().onCreate(savedInstanceState);	

        FontHelper.createInstance(getAssets());
	}	

	@Override
	protected void onResume() {
		super.onResume();
		getOAuthHelper().onResume();		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getOAuthHelper().onPause();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		getOAuthHelper().onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		getOAuthHelper().onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		getOAuthHelper().onDestroy();
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
        return mActivityHelper.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }   
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActivityHelper.onPostCreate(savedInstanceState);
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActivityHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
