/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andorn.tasktags.base;

import com.andorn.tasktags.auth.OAuthHelper;
import com.andorn.tasktags.helpers.FontHelper;
import com.andorn.tasktags.helpers.SharedPrefUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A base activity that defers common functionality across app activities to an
 * {@link ActivityHelper}. This class shouldn't be used directly; instead, activities should
 * inherit from {@link BaseSinglePaneActivity} or {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends Activity {
	
    final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
    final OAuthHelper mOAuthHelper = OAuthHelper.createInstance(this);
    final SharedPrefUtil mSharedPref = SharedPrefUtil.createInstance(this);
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        FontHelper.createInstance(getAssets());
	}
    
    protected ActivityHelper getActivityHelper() {
        return mActivityHelper;
    }    
    
    protected OAuthHelper getOAuthHelper() {
        return mOAuthHelper;
    }    
    
    protected SharedPrefUtil getSharedPrefUtil() {
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
    
	public void sync()
	{
		sync(true);
	}
	
	public void sync(boolean withRefresh) {
	
	}
}
