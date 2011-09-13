package com.andorn.tasktags.activities;

import com.andorn.tasktags.adapters.GooAccountsCursorAdapter;
import com.andorn.tasktags.auth.OAuthHelper;
import com.andorn.tasktags.base.ActivityHelper;
import com.andorn.tasktags.database.GooAccountsOpenHelper;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.services.TasksAppService;
import com.andorn.tasktags.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class GooAccountsActivity extends BaseActivity 
	implements OnClickListener {
	
	private static final String TAG = GooAccountsActivity.class.getName();

	private final GooAccountsOpenHelper dbACCHelper = new GooAccountsOpenHelper(this);

    private ListView mListView;    
    private GooAccountsCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.account_list);	
        
		getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_ACCOUNT_LIST);
        
		mListView = (ListView) findViewById(R.id.accountsList);			
    }

    @Override
    protected void onStop() {
    	super.onStop();
		synchronized(this)
		{
			if(mAdapter.getCursor() != null) mAdapter.getCursor().deactivate();
		}
    }
    
    @Override
    protected void onResume() {
        super.onResume();

		synchronized(this)
		{
			mListView.setAdapter(null);	
			Cursor c = dbACCHelper.queryCursor();
			mAdapter = new GooAccountsCursorAdapter(this, c, true);
			mListView.setAdapter(mAdapter);	
		}
		
        sync();		
    }    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();

		synchronized(this)
		{
			if(mAdapter.getCursor() != null) mAdapter.getCursor().close();
			if(mAdapter != null) mAdapter.getCursor().close();
			if (dbACCHelper != null) dbACCHelper.close(); 
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.general_settings_menu_item, menu);
		getMenuInflater().inflate(R.menu.add_account_menu_items, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_general_settings: {
				GeneralSettingsActivity.go(this, false);
				return true;
			}
			case R.id.menu_add_account: {
				GeneralSettingsActivity.addAccount(this, false);
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

	@Override
	public void sync(boolean withRefresh) { 	
		if(withRefresh) mAdapter.requery();
		TasksAppService.syncAccounts(this, mSyncReceiver);
    }

	@Override
	public void onClick(View v) {
		long accountId = (Long)v.getTag();
		if(accountId != GooBase.INVALID_ID)
		{
			GooTaskListsActivity.go(this, false, accountId);
		}
	}

	private ResultReceiver mSyncReceiver = new ResultReceiver(null) {
		
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
