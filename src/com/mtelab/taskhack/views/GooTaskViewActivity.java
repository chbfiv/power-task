package com.mtelab.taskhack.views;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.base.ActivityHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooTaskListOpenHelper;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GooTaskViewActivity extends BaseActivity {

	private static final String TAG = GooTaskViewActivity.class.getName();
	
	private final GooTaskListOpenHelper dbTLHelper = new GooTaskListOpenHelper(this);
	
    private TextView mTitle;
    private TextView mNotes;
    private TextView taskEditDateField;
    private View mDetialsCollapsed;
    private View mDetials;
    private LinearLayout mDetialsLayout;
    
    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;    

	public static final String EXTRA_ACTIVE_TASK_ID = "active_task_id";
	private long mActiveTaskId = GooBase.INVALID_ID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
			return;
		}
    	
		mActiveTaskId = extras.getLong(EXTRA_ACTIVE_TASK_ID, GooBase.INVALID_ID);
		
        setContentView(R.layout.task_view);
        getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_VIEW);
        
        mTitle = (TextView) findViewById(R.id.taskView_titleField);
        mNotes = (TextView) findViewById(R.id.taskView_detialsField);        
        taskEditDateField = (TextView) findViewById(R.id.taskView_dateField);        
        mDetialsCollapsed = findViewById(R.id.taskView_detials_collapsed);           
        mDetials = findViewById(R.id.taskView_detials);  
        mDetialsLayout = (LinearLayout) findViewById(R.id.taskView_detialsLayout);  

        mDetials.setVisibility(View.GONE);
        
        mYear = 0;
        mMonth = 0;
        mDay = 0;
    	
    	refreshTask();
    }    
    
    @Override
    protected void onResume() {
    	super.onResume();

    	refreshTask();    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_mode_menu_item, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
			case R.id.menu_edit_mode: {
				editTask();
				return true;
			}
    	}
        return super.onOptionsItemSelected(item);
    }

    private void refreshTask()
    {
    	if(mActiveTaskId == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "refreshTaskList - invalid task list id.");
    		return;
    	}
    	
		GooTask task = dbTLHelper.read(mActiveTaskId);
    	if(task == null)
    	{
    		Log.e(TAG, "refreshTaskList - task null.");
    		return;
    	}
    	
		mTitle.setText(task.title);
		mNotes.setText(task.notes);
		
		
    }

    private void editTask()
    {
		Intent intent = new Intent(this, GooTaskComposeActivity.class);
		intent.putExtra(GooTaskComposeActivity.EXTRA_ACTIVE_TASK_ID, mActiveTaskId);
		startActivity(intent);    
		overridePendingTransition(R.anim.fade, R.anim.hold);	
    }
    
    public void collapseDetials(View v)
    {
        mDetialsCollapsed.setVisibility(View.VISIBLE);
        mDetials.setVisibility(View.GONE);    	
    }
    
    public void expandDetials(View v)
    {
        mDetialsCollapsed.setVisibility(View.GONE);
        mDetials.setVisibility(View.VISIBLE); 
    }
}
