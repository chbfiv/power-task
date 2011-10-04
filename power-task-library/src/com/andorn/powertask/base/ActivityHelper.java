package com.andorn.powertask.base;

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

import com.andorn.powertask.activities.BaseActivity;
import com.andorn.powertask.activities.GooAccountsActivity;
import com.andorn.powertask.helpers.AnalyticsTrackerHelper;
import com.andorn.powertask.helpers.FontHelper;
import com.andorn.powertask.helpers.SimpleMenu;
import com.andorn.powertask.R;

public class ActivityHelper {
	@SuppressWarnings("unused")
	private static final String TAG = ActivityHelper.class.getName();
	
	public static final int ACTIONBAR_TASKS = 1;
	public static final int ACTIONBAR_TASK_COMPOSE = 2;
	public static final int ACTIONBAR_ACCOUNTS = 3;
	public static final int ACTIONBAR_TASK_LISTS = 4;
	public static final int ACTIONBAR_TASK_VIEW = 5;
	public static final int ACTIONBAR_TASK_EDIT = 6;
	public static final int ACTIONBAR_ACCOUNT_LIST = 7;
	
    protected BaseActivity mActivity;
    protected float displayDensity;
    protected int height;
    protected int width;
    protected int scaledHeight;
    protected int scaledWidth;
    
    public static ActivityHelper create(BaseActivity activity) {
        return new ActivityHelper(activity);                
    }

    protected ActivityHelper(BaseActivity activity) {
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
    
    public void onCreate(Bundle savedInstanceState)
    {
    	displayDensity = mActivity.getResources().getDisplayMetrics().density;
    	width = mActivity.getResources().getDisplayMetrics().heightPixels;
    	height = mActivity.getResources().getDisplayMetrics().widthPixels;
    	scaledWidth = (int)(width * displayDensity + 0.5f);
    	scaledHeight = (int)(height * displayDensity + 0.5f);    	
    }
    
    public void onPostCreate(Bundle savedInstanceState) {
    	
        displayDensity = mActivity.getResources().getDisplayMetrics().density; 
        height = mActivity.getResources().getDisplayMetrics().heightPixels; 
        width = mActivity.getResources().getDisplayMetrics().widthPixels; 
        
        // Create the action bar
        SimpleMenu menu = new SimpleMenu(mActivity);
        mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
        // TODO: call onPreparePanelMenu here as well
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if (item.getItemId() != R.id.menu_general_settings &&
        		item.getItemId() != R.id.menu_add_account) 
            {
            	addActionButtonCompatFromMenuItem(item);
            }
        }
    }
    
    public void onDestroy() {
    	
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        mActivity.getMenuInflater().inflate(R.menu.menu_item_search, menu);
        return false;
    }
    
    public void goUp() {
    	if(mActivity.isTaskRoot())
    		GooAccountsActivity.go(mActivity, true);
    	else
    		mActivity.finish();

    	mActivity.getTrackerHelper().trackEvent(AnalyticsTrackerHelper.CATEGORY_UI_INTERACTION, 
				AnalyticsTrackerHelper.ACTION_UP, mActivity.getClass().getName(), 0);
    }
    
    public void setupActionBar(int actionBarId) {
    	
        final ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat == null) {
            return;
        }
        
        LinearLayout.LayoutParams normalLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        
        View.OnClickListener goUpClickListener = new View.OnClickListener() {
            public void onClick(View view) {
            	goUp();
            }
        };
        
        TextView titleText;
        ImageButton logo;
        String title;
        
        switch (actionBarId) {	         
        case ACTIONBAR_ACCOUNT_LIST:
        	// Add logo
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            actionBarCompat.addView(logo);

            title = mActivity.getString(R.string.description_accounts);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            
			break;     
        case ACTIONBAR_TASK_LISTS:
        	// Add logo
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(goUpClickListener);
            actionBarCompat.addView(logo);
            
            // Add spring (dummy view to align future children to the right)
//            spring = new View(mActivity);
//            spring.setLayoutParams(springLayoutParams);
//            actionBarCompat.addView(spring);
            //visiblility = View.VISIBLE;

            title = mActivity.getString(R.string.description_task_lists);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(goUpClickListener);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
			break;
        case ACTIONBAR_TASKS:
        	// Add logo
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(goUpClickListener);
            actionBarCompat.addView(logo);
            
            // Add spring (dummy view to align future children to the right)
            title = mActivity.getString(R.string.description_tasks);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(goUpClickListener);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
			break;
        case ACTIONBAR_TASK_COMPOSE:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(goUpClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            title = mActivity.getString(R.string.description_task_compose);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(goUpClickListener);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        case ACTIONBAR_TASK_EDIT:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(goUpClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            title = mActivity.getString(R.string.description_task_edit);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(goUpClickListener);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        case ACTIONBAR_TASK_VIEW:
        	// Add Home button
            logo = new ImageButton(mActivity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(goUpClickListener);
            actionBarCompat.addView(logo);
            
        	// Add title text
            title = mActivity.getString(R.string.description_task);
            titleText = new TextView(mActivity, null, R.attr.actionbarCompatTextStyle);
            titleText.setOnClickListener(goUpClickListener);
            titleText.setLayoutParams(normalLayoutParams);
            titleText.setText(title);
            titleText.setTypeface(FontHelper.getInstance().CuprumRegular);
            actionBarCompat.addView(titleText);
            break;
        default:
        	return;
        }      
        
        LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
        springLayoutParams.weight = 1;
        View springView = new View(mActivity);
        springView.setLayoutParams(springLayoutParams);
        actionBarCompat.addView(springView);
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
//    private View addActionButtonCompat(int iconResId, int textResId,
//            View.OnClickListener clickListener, boolean separatorAfter) {
//        final ViewGroup actionBar = getActionBarCompat();
//        if (actionBar == null) {
//            return null;
//        }
//
//        // Create the separator
//        ImageView separator = new ImageView(mActivity, null, R.attr.actionbarCompatSeparatorStyle);
//        separator.setLayoutParams(
//                new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));
//
//        // Create the button
//        ImageButton actionButton = new ImageButton(mActivity, null,
//                R.attr.actionbarCompatButtonStyle);
//        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
//                (int) mActivity.getResources().getDimension(R.dimen.actionbar_compat_height),
//                ViewGroup.LayoutParams.FILL_PARENT));
//        actionButton.setImageResource(iconResId);
//        actionButton.setScaleType(ImageView.ScaleType.CENTER);
//        actionButton.setContentDescription(mActivity.getResources().getString(textResId));
//        actionButton.setOnClickListener(clickListener);
//
//        // Add separator and button to the action bar in the desired order
//
//        if (!separatorAfter) {
//            actionBar.addView(separator);
//        }
//
//        actionBar.addView(actionButton);
//
//        if (separatorAfter) {
//            actionBar.addView(separator);
//        }
//
//        return actionButton;
//    }

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

//        for(int i = 0; i < actionBar.getChildCount(); i++)
//        {
//        	View child = actionBar.getChildAt(i);
//        	if(child != null && child.getId() == item.getItemId())
//        	{
//    			return null;
//        	}
//        }
        
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
    
    public boolean isXLargeDip()
    {
		//    	xlarge screens are at least 960dp x 720dp
		return 	(scaledWidth >= 960 && scaledHeight >= 720) ||
			   	(scaledHeight >= 960 && scaledWidth >= 720);
    }
    
    public boolean isLarge()
    {
    	//    	large screens are at least 640dp x 480dp
		return 	(scaledWidth >= 640 && scaledHeight >= 480) ||
	   	(scaledHeight >= 640 && scaledWidth >= 480);
    }
    
    public boolean isNormal()
    {
		//    	normal screens are at least 470dp x 320dp
		return 	(scaledWidth >= 470 && scaledHeight >= 320) ||
	   	(scaledHeight >= 470 && scaledWidth >= 320);
    }
    
    public boolean isSmall()
    {
		//    	small screens are at least 426dp x 320dp	
		return 	(scaledWidth >= 426 && scaledHeight >= 320) ||
	   	(scaledHeight >= 426 && scaledWidth >= 320);
    }
}
