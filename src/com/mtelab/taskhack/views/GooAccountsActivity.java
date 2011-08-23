package com.mtelab.taskhack.views;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.adapters.GooAccountsCursorAdapter;
import com.mtelab.taskhack.auth.OAuthHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooAccountsOpenHelper;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.services.TasksAppService;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class GooAccountsActivity extends BaseActivity 
	implements OnClickListener, OnCheckedChangeListener {
	
	private static final String TAG = GooAccountsActivity.class.getName();

	private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);

    private ListView mListView;    
    private GooAccountsCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.manage_accounts);	
        
		mListView = (ListView) findViewById(R.id.manageAccounts_list);
		
//		if (c.getCount() <= 0) {	 	
//			addAccount(); 		
//		}	
    }

    @Override
    protected void onResume() {
        super.onResume();        

		mListView.setAdapter(null);	
		Cursor c = dbACCHelper.queryCursor();
		mAdapter = new GooAccountsCursorAdapter(this, c, true);
		mListView.setAdapter(mAdapter);	
		
        sync();		
    }    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mAdapter != null) mAdapter.getCursor().close();
		if (dbACCHelper != null) dbACCHelper.close(); 
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
    
    public static void go(Activity activity) 
    {
    	go(activity, true);
    }
    
	public static void go(Activity activity, boolean finishActivity) {
		final Intent intent = new Intent(activity, GooAccountsActivity.class);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
		if(finishActivity) activity.finish();
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

	@Override
	public void sync(boolean withRefresh) { 	
		if(withRefresh) mAdapter.requery();
		
		Intent intent = new Intent(this, TasksAppService.class);
		intent.setFlags(TasksAppService.REQUEST_SYNC_ACCOUNTS);
		intent.putExtra(TasksAppService.REQUEST_RECEIVER_EXTRA, mAccountsReceiver);
		startService(intent);
    }

	@Override
	public void onClick(View v) {
		long accountId = (Long)v.getTag();
		if(accountId != GooBase.INVALID_ID)
		{
			GooTaskListsActivity.go(this, false, accountId);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean val) {
		long accountId = (Long)v.getTag();
		if(accountId != GooBase.INVALID_ID)
		{
			dbACCHelper.update(accountId, val);
		}
	} 	

	private ResultReceiver mAccountsReceiver = new ResultReceiver(null) {
		
	    @Override
	    protected void onReceiveResult(final int resultCode, final Bundle resultData) {			    	
			runOnUiThread(new Runnable() {
				public void run() {			
					if (resultCode == TasksAppService.RESULT_SYNC_ACCOUNTS_SUCCESS) {							  		 
		        		mAdapter.requery();
			        }
					else if (resultCode == TasksAppService.RESULT_FAILED_UNAUTHORIZED) {
						
					}
					else if (resultCode == TasksAppService.RESULT_LOADING)
					{
						
					}
					else if (resultCode == TasksAppService.RESULT_LOADING_COMPLETE)
					{
						
					}
				}							
			});
	    }
	};
}
