package com.andorn.powertask.activities;

import java.util.List;

import com.andorn.powertask.adapters.GooAccountsCursorAdapter;
import com.andorn.powertask.helpers.AnalyticsTrackerHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.models.GooAccount;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class GeneralSettingsActivity extends BaseActivity {
	
	private static final String TAG = GeneralSettingsActivity.class.getName();

    private GooAccountsCursorAdapter mAdapter;

    private boolean mOfflineMode;   
    private boolean mGoogleAnalytics;  
    
    private CheckBox offlineModeCheckbox;
    private CheckBox googleAnalyticsCheckbox;
    private LinearLayout accountsLayout;
    
    private LayoutInflater mInflater;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_general_settings);	        
        
        mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    offlineModeCheckbox = (CheckBox) findViewById(R.id.generalSettings_offlineModeCheckbox);
	    googleAnalyticsCheckbox = (CheckBox) findViewById(R.id.generalSettings_googleAnalyticsCheckbox);
	    accountsLayout = (LinearLayout) findViewById(R.id.generalSettings_accounts);	    

	    View debug = findViewById(R.id.generalSettings_debug);
	    
	    if(app().isDebug()) debug.setVisibility(View.VISIBLE);
	    else debug.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();     
        
	    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
	    mOfflineMode = prefs.getBoolean(SharedPrefUtil.PREF_OFFLINE_MODE, false);	
	    mGoogleAnalytics = prefs.getBoolean(SharedPrefUtil.PREF_GOOGLE_ANALYTICS, AnalyticsTrackerHelper.DEFAULT_GOOGLE_ANALYTICS);	
	    
	    offlineModeCheckbox.setOnCheckedChangeListener(null);
	    offlineModeCheckbox.setChecked(mOfflineMode);
	    offlineModeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        	    SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
        	    editor.putBoolean(SharedPrefUtil.PREF_OFFLINE_MODE, isChecked);
        	    editor.commit();	
			}
		});
	    
	    googleAnalyticsCheckbox.setOnCheckedChangeListener(null);
	    googleAnalyticsCheckbox.setChecked(mGoogleAnalytics);
	    googleAnalyticsCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        	    SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
        	    editor.putBoolean(SharedPrefUtil.PREF_GOOGLE_ANALYTICS, isChecked);
        	    editor.commit();	
			}
		});
	    
	    accountsLayout.removeAllViews();
	    
	    List<GooAccount> accounts = app().getDbhAccounts().query();
	    for (GooAccount account : accounts)
	    {
	    	View accountView = mInflater.inflate(R.layout.account, null);
	    	
	    	CheckBox accCheckBox = (CheckBox) accountView.findViewById(R.id.accountItem);
	    	accCheckBox.setTag(account.getId());
	    	accCheckBox.setChecked(account.getSync());
	    	
	    	TextView accName = (TextView) accountView.findViewById(R.id.accountName);
	    	accName.setTag(account.getId());
	    	accName.setText(account.getName());
	    	
	    	accCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
				@Override
				public void onCheckedChanged(CompoundButton v, boolean isChecked) {
					long accountId = (Long)v.getTag();
					if(accountId != GooBase.INVALID_ID)
					{
						GooAccount tmpAccount = app().getDbhAccounts().read(accountId);
						tmpAccount.setSync(isChecked);
						app().getDbhAccounts().update(tmpAccount);
					}
				}
			});
	    	
	    	accountsLayout.addView(accountView);
	    }
		
		getTrackerHelper().trackPageView("/" + TAG);
    }    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().close();
	}
    
    public static void go(Activity activity) 
    {
    	go(activity, true);
    }
    
	public static void go(Activity activity, boolean finishActivity) {
		final Intent intent = new Intent(activity, GeneralSettingsActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		if(finishActivity) activity.finish();
	}
    
	public static void addAccount(Activity activity) {
		Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.calendar"
		});
		activity.startActivity(intent);		
	}
	
	protected void syncSettings(Activity activity, boolean finishActivity) {
		Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.contacts"
		});
		activity.startActivity(intent);		
		if(finishActivity) activity.finish();	
	}
	
	public void addAcount_onClick(View v)
	{
		addAccount(this);
	}	
}
