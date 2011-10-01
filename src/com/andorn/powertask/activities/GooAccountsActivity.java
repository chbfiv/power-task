package com.andorn.powertask.activities;

import com.andorn.powertask.adapters.GooAccountsCursorAdapter;
import com.andorn.powertask.base.ActivityHelper;
import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.services.TasksAppService;
import com.andorn.powertask.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
    protected void onPause() {
    	super.onPause();
		if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().deactivate();
    }
    
    @Override
    protected void onResume() {
        super.onResume();        

		if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().close();
		mListView.setAdapter(null);	
		Cursor c = dbACCHelper.queryCursor();
		mAdapter = new GooAccountsCursorAdapter(this, c, true);
		mListView.setAdapter(mAdapter);	   
		
		sync();
		
		getTrackerHelper().trackPageView("/" + TAG);
    }    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().close();
		if (dbACCHelper != null) dbACCHelper.close(); 
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
    
	public void sync()
	{
		sync(true);
	}
	
	public void sync(boolean withRefresh) { 	
		if(mAdapter != null && withRefresh) mAdapter.requery();
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
					if (resultCode == TasksAppService.RESULT_SYNC_SUCCESS_ACCOUNTS) {							  		 
		        		if(mAdapter != null) mAdapter.requery();
			        }
					else if (resultCode == TasksAppService.RESULT_SYNC_FAILED_UNAUTHORIZED) {
						
					}
					else if (resultCode == TasksAppService.RESULT_SYNC_LOADING)
					{
						
					}
					else if (resultCode == TasksAppService.RESULT_SYNC_LOADING_COMPLETE)
					{
						
					}
				}							
			});
	    }
	};
}
