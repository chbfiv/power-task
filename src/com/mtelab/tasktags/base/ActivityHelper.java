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

package com.mtelab.tasktags.base;

import com.mtelab.tasktags.R;
import com.mtelab.tasktags.activities.GooAccountsActivity;
import com.mtelab.tasktags.activities.GooTaskListsActivity;
import com.mtelab.tasktags.helpers.FontHelper;
import com.mtelab.tasktags.helpers.SimpleMenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A class that handles some common activity-related functionality in the app, such as setting up
 * the action bar. This class provides functioanlity useful for both phones and tablets, and does
 * not require any Android 3.0-specific features.
 */
public class ActivityHelper {
	public static final int ACTIONBAR_TASK_LIST = 1;
	public static final int ACTIONBAR_TASK_COMPOSE = 2;
	public static final int ACTIONBAR_ACCOUNTS = 3;
	public static final int ACTIONBAR_TASK_LIST_COLLECTION = 4;
	public static final int ACTIONBAR_TASK_VIEW = 5;
	public static final int ACTIONBAR_TASK_EDIT = 6;
	
    protected Activity mActivity;

    /**
     * Factory method for creating {@link ActivityHelper} objects for a given activity. Depending
     * on which device the app is running, either a basic helper or Honeycomb-specific helper will
     * be returned.
     */
    public static ActivityHelper createInstance(Activity activity) {
        return new ActivityHelper(activity);                
    }

    protected ActivityHelper(Activity activity) {
        mActivity = activity;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                goSearch();
                return true;
        }
        return false;
    }
    
    public void goSearch() {
        mActivity.startSearch(null, false, Bundle.EMPTY, false);
    }
    
    public void onPostCreate(Bundle savedInstanceState) {
        // Create the action bar
        SimpleMenu menu = new SimpleMenu(mActivity);
        mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
        // TODO: call onPreparePanelMenu here as well
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            addActionButtonCompatFromMenuItem(item);
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        mActivity.getMenuInflater().inflate(R.menu.search_menu_items, menu);
        return false;
    }
    
    public void goBack() {
		mActivity.finish();
    }
    
    public void setupActionBar(int actionBarId) {
    	
        final ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat == null) {
            return;
        }
        
        LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
        springLayoutParams.weight = 1;
        
        View.OnClickListener accountsClickListener = new View.OnClickListener() {
            public void onClick(View view) {
            	GooAccountsActivity.go(mActivity, true);
            }
        };
        
        View.OnClickListener taskListCollectionClickListener = new View.OnClickListener() {
            public void onClick(View view) {
            	goBack();
            }
        };
        
        View.OnClickListener taskListClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                goBack();
            }
        };
        
        TextView titleText;
        View spring;
        ImageButton logo;
        
        switch (actionBarId) {	        
        case ACTIONBAR_TASK_LIST_COLLECTION:
        	// Add logo
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(accountsClickListener);
            actionBarCompat.addView(logo);
            
            // Add spring (dummy view to align future children to the right)
//            spring = new View(mActivity);
//            spring.setLayoutParams(springLayoutParams);
//            actionBarCompat.addView(spring);
            //visiblility = View.VISIBLE;

            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(accountsClickListener);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText("Task Lists");
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
			break;
        case ACTIONBAR_TASK_LIST:
        	// Add logo
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(taskListCollectionClickListener);
            actionBarCompat.addView(logo);
            
            // Add spring (dummy view to align future children to the right)
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(taskListCollectionClickListener);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText("Tasks");
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
			break;
        case ACTIONBAR_TASK_COMPOSE:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(taskListClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(taskListClickListener);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText("Compose Task");
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        case ACTIONBAR_TASK_EDIT:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(taskListClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(taskListClickListener);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText("Edit Task");
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        case ACTIONBAR_TASK_VIEW:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(taskListClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(taskListClickListener);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText("Task");
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        case ACTIONBAR_ACCOUNTS:
        	
        	break;
        default:
        	return;
        }      
    }

    /**


    /**
     * Sets the action bar title to the given string.
     */
    public void setActionBarTitle(CharSequence title) {
        ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return;
        }

        TextView titleText = (TextView) actionBar.findViewById(R.id.actionbar_compat_text);
        if (titleText != null) {
            titleText.setText(title);
        }
    }

    /**
     * Returns the {@link ViewGroup} for the action bar on phones (compatibility action bar).
     * Can return null, and will return null on Honeycomb.
     */
    public ViewGroup getActionBarCompat() {
        return (ViewGroup) mActivity.findViewById(R.id.actionbar_compat);
    }

    /**
     * Adds an action bar button to the compatibility action bar (on phones).
     */
    private View addActionButtonCompat(int iconResId, int textResId,
            View.OnClickListener clickListener, boolean separatorAfter) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator
        ImageView separator = new ImageView(mActivity, null, R.attr.actionbarCompatSeparatorStyle);
        separator.setLayoutParams(
                new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

        // Create the button
        ImageButton actionButton = new ImageButton(mActivity, null,
                R.attr.actionbarCompatButtonStyle);
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height),
                ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageResource(iconResId);
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(mActivity.getResources().getString(textResId));
        actionButton.setOnClickListener(clickListener);

        // Add separator and button to the action bar in the desired order

        if (!separatorAfter) {
            actionBar.addView(separator);
        }

        actionBar.addView(actionButton);

        if (separatorAfter) {
            actionBar.addView(separator);
        }

        return actionButton;
    }

    /**
     * Adds an action button to the compatibility action bar, using menu information from a
     * {@link MenuItem}. If the menu item ID is <code>menu_refresh</code>, the menu item's state
     * can be changed to show a loading spinner using
     * {@link ActivityHelper#setSyncing(boolean)}.
     */
    private View addActionButtonCompatFromMenuItem(final MenuItem item) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator
        ImageView separator = new ImageView(mActivity, null, R.attr.actionbarCompatSeparatorStyle);
        separator.setLayoutParams(new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

        // Create the button
        ImageButton actionButton = new ImageButton(mActivity, null, R.attr.actionbarCompatButtonStyle);
        actionButton.setId(item.getItemId());
        actionButton.setLayoutParams(new ViewGroup.LayoutParams((int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height), ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });

        actionBar.addView(separator);
        actionBar.addView(actionButton);

        if (item.getItemId() == R.id.menu_sync) {
            // Refresh buttons should be stateful, and allow for indeterminate progress indicators,
            // so add those.
            int btnStdHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
            int btnSideLength = (int) (btnStdHeight / 1.5f);
            int btnSideDiff =  (int) ((btnStdHeight - btnSideLength) / 2);
            ProgressBar indicator = new ProgressBar(mActivity, null, R.attr.actionbarCompatProgressIndicatorStyle);
            LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(btnSideLength, btnSideLength);
            indicatorLayoutParams.setMargins(btnSideDiff, 0, btnSideDiff, 0);
            indicatorLayoutParams.gravity = Gravity.CENTER;
            indicator.setLayoutParams(indicatorLayoutParams);
            //indicator.setLayoutParams(new ViewGroup.LayoutParams((int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height), ViewGroup.LayoutParams.MATCH_PARENT));
            //actionButton.setScaleType(ImageView.ScaleType.CENTER);
            indicator.setVisibility(View.GONE);
            indicator.setId(R.id.menu_sync_progress);
            actionBar.addView(indicator);
        }
        
        

        return actionButton;
    }

    public void setSyncing(boolean syncing) {
        View syncButton = mActivity.findViewById(R.id.menu_sync);
        View syncIndicator = mActivity.findViewById(R.id.menu_sync_progress);

        if (syncButton != null) {
            syncButton.setVisibility(syncing ? View.GONE : View.VISIBLE);
        }
        if (syncIndicator != null) {
            syncIndicator.setVisibility(syncing ? View.VISIBLE : View.GONE);
        }
    }
}
