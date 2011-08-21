package com.mtelab.taskhack.views;

import java.util.List;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.mtelab.taskhack.R;
import com.mtelab.taskhack.TaskApplication;
import com.mtelab.taskhack.adapters.GooAccountsAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooAccountsOpenHelper;
import com.mtelab.taskhack.helpers.SharedPrefUtil;
import com.mtelab.taskhack.models.GooAccount;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ManageAccountsActivity extends BaseActivity 
	implements OnClickListener, OnCheckedChangeListener {
	
    @SuppressWarnings("unused")
	private static final String TAG = ManageAccountsActivity.class.getName();

	private final GooAccountsOpenHelper dbHelper = new GooAccountsOpenHelper(this);

    private Context mContext = this;
    private ListView mListView;    
    private GooAccountsAdapter mAdapter;
    @SuppressWarnings("unused")
	private GoogleAccountManager accountManager; 


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.manage_accounts);
        accountManager = new GoogleAccountManager(this);	 
        setupGoogleAccountsList();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.account_settings_menu_items, menu);
		getMenuInflater().inflate(R.menu.add_account_menu_items, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add_account: {
				addAccount();
				return true;
			}
			case R.id.menu_account_settings: {
				syncSettings();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onResume() {
        super.onResume();
		List<GooAccount> accounts = getGoogleAccounts();
		if (accounts.size() > 0) {	
			TaskApplication taskApplication = (TaskApplication) getApplication();
			mAdapter = taskApplication.getAccountsAdapter(this);
			mAdapter.set(accounts);
			mAdapter.notifyDataSetChanged();
		}
    }
    

	private void setupGoogleAccountsList()
	{ 
		List<GooAccount> accounts = getGoogleAccounts();
		if (accounts.size() == 0) {			
			addAccount();
		}
		else
		{		
			mListView = (ListView) findViewById(R.id.manageAccounts_list);
			
			TaskApplication taskApplication = (TaskApplication) getApplication();
			mAdapter = taskApplication.getAccountsAdapter(this);
			mAdapter.set(accounts);
			mListView.setAdapter(mAdapter);	  
			//mAccountsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			//mListView.setOnItemClickListener(this);			
		}	
	}

    
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////		CheckedTextView checkView = (CheckedTextView)view;
////		Boolean needToRegister = !checkView.isChecked();
////		checkView.setChecked(needToRegister);
////		
////		final SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
////		editor.putBoolean(OAuthHelper.getSyncPrefField(checkView.getText().toString()), checkView.isChecked());
////		editor.commit();
////		if(needToRegister)
////		{
////			registerAccount(checkView.getText().toString());
////		}
//	}	  
	
    private void registerAccount(final String accountName) {
        // Store the account name in shared preferences
//		final SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
//        editor.putString(OAuthHelper.PREF_ACTIVE_ACCOUNT_NAME, null);
//        editor.putString(OAuthHelper.getAuthTokenPrefField(accountName), null);
//        editor.commit();
//
//        // Obtain an auth token and register
//        Account acct = accountManager.getAccountByName(accountName);
//        
//        if (acct != null) {
//            editor.putString(OAuthHelper.PREF_ACTIVE_ACCOUNT_NAME, accountName);
//            editor.commit();
//        }
    }
    
	protected void addAccount() {
		Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.contacts"
		});
		startActivityForResult(intent, OAuthHelper.REQUEST_ADD_ACCOUNT);		
	}
	
	protected void syncSettings() {
		Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
			    "com.android.contacts"
		});
		startActivityForResult(intent, OAuthHelper.REQUEST_SYNC_SETTINGS);		
	}
	
    private List<GooAccount> getGoogleAccounts() {    	
    	      
        Account[] accounts = AccountManager.get(mContext).getAccountsByType("com.google");
        
        for (Account account : accounts) {
        	GooAccount gooAccount = dbHelper.findAccountByName(account.name);
        	if(gooAccount != null)
        	{
        		//Required to know which google accounts have been removed to allow
        		//for removal from local cache.
        		gooAccount.localAccountFound = Boolean.TRUE;
        	}
        	else
        	{
        		//create a new local cache account (currently unauthorized to sync)
            	gooAccount = new GooAccount(account.name, account.type, false);  
            	dbHelper.create(gooAccount);
        	}
        }        
        
        return dbHelper.query();
    }

	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooAccount account = mAdapter.getItem(position);	
			if(account != null && account.getSync()) 
			{
			    SharedPreferences.Editor editor = getSharedPrefUtil().getEditor();
			    editor.putLong(SharedPrefUtil.PREF_ACTIVE_ACCOUNT_ID, account.getId());
			    editor.commit();
			    
				final Intent intent = new Intent(this, GooTaskListCollectionActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean val) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			GooAccount account = mAdapter.getItem(position);
			account.setSync(val);
			dbHelper.update(account.getId(), account.getSync());
		}
	} 
}
