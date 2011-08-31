package com.andorn.tasktags.activities;

import java.util.List;

import com.andorn.tasktags.adapters.GooAccountsCursorAdapter;
import com.andorn.tasktags.base.BaseActivity;
import com.andorn.tasktags.database.GooAccountsOpenHelper;
import com.andorn.tasktags.helpers.SharedPrefUtil;
import com.andorn.tasktags.models.GooAccount;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.R;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class GeneralSettingsActivity extends BaseActivity {
	
	private static final String TAG = GeneralSettingsActivity.class.getName();

	private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);

    private GooAccountsCursorAdapter mAdapter;

    private boolean mDebug;
    private boolean mOfflineMode;    
    
    private CheckBox debugModeCheckbox;
    private CheckBox offlineModeCheckbox;
    private LinearLayout accountsLayout;
    
    private LayoutInflater mInflater;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.general_settings);	        
        
        mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    debugModeCheckbox = (CheckBox) findViewById(R.id.generalSettings_debugCheckbox);
	    offlineModeCheckbox = (CheckBox) findViewById(R.id.generalSettings_offlineModeCheckbox);
	    accountsLayout = (LinearLayout) findViewById(R.id.generalSettings_accounts);
    }

    @Override
    protected void onResume() {
        super.onResume();     
        
	    SharedPreferences prefs = getSharedPrefUtil().getSharedPref();
	    mDebug = prefs.getBoolean(SharedPrefUtil.PREF_DEBUG, false);	
	    mOfflineMode = prefs.getBoolean(SharedPrefUtil.PREF_OFFLINE_MODE, false);	
	    
	    debugModeCheckbox.setOnCheckedChangeListener(null);
	    debugModeCheckbox.setChecked(mDebug);
	    debugModeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        	    SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
        	    editor.putBoolean(SharedPrefUtil.PREF_DEBUG, isChecked);
        	    editor.commit();	
			}
		});
		
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
	    
	    accountsLayout.removeAllViews();
	    
	    List<GooAccount> accounts = dbACCHelper.query();
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
						GooAccount tmpAccount = dbACCHelper.read(accountId);
						tmpAccount.setSync(isChecked);
						dbACCHelper.update(tmpAccount);
					}
				}
			});
	    	
	    	accountsLayout.addView(accountView);
	    }
    }    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mAdapter != null) mAdapter.getCursor().close();
		if (dbACCHelper != null) dbACCHelper.close(); 
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
}
