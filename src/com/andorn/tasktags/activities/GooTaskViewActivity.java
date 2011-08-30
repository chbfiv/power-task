package com.andorn.tasktags.activities;

import com.andorn.tasktags.base.ActivityHelper;
import com.andorn.tasktags.base.BaseActivity;
import com.andorn.tasktags.database.GooTasksOpenHelper;
import com.andorn.tasktags.helpers.DateTimeHelper;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTask;
import com.andorn.tasktags.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class GooTaskViewActivity extends BaseActivity 
	implements OnCheckedChangeListener {

	private static final String TAG = GooTaskViewActivity.class.getName();
	
	private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
	
    private TextView mTitle;
    private TextView mNotes;
    private TextView mDay;
    private TextView mDate;
    private View mDetialsCollapsed;
    private View mDetials;
    private View mDetialsSeperator;
    private CheckBox mStatusCheckBox;
    
    static final int DATE_DIALOG_ID = 0;  

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

        mDay = (TextView) findViewById(R.id.taskView_dayField);  
        mDate = (TextView) findViewById(R.id.taskView_dateField);    
        
        mDetials = findViewById(R.id.taskView_detials);  
        mDetialsCollapsed = findViewById(R.id.taskView_detials_collapsed);   
        mDetialsSeperator = findViewById(R.id.taskView_detials_seperator);  

        mDetials.setVisibility(View.GONE);
        mStatusCheckBox = (CheckBox) findViewById(R.id.taskView_statusCheckBox);
    }    
    
    @Override
    protected void onResume() {
    	super.onResume();

    	refreshTask();    	
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dbTLHelper != null) dbTLHelper.close(); 
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
				GooTaskEditActivity.go(this, false, mActiveTaskId);
				return true;
			}
    	}
        return super.onOptionsItemSelected(item);
    }
    
    public static void go(Activity activity, long taskId) 
    {
    	go(activity, true, taskId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskViewActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		if(finishActivity) activity.finish();
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

        mStatusCheckBox.setOnCheckedChangeListener(null);
        mStatusCheckBox.setChecked(task.isCompleted());   
        mStatusCheckBox.setOnCheckedChangeListener(this);        
        
        mDetials.setVisibility(View.GONE);  
        mDetialsCollapsed.setVisibility(View.GONE);   
        mDetialsSeperator.setVisibility(View.GONE);   
    	mDay.setVisibility(View.GONE);     
    	mDate.setVisibility(View.GONE);         
        
        if(task.hasDueDate() && task.hasTags())
        {
	    	mDay.setText(DateTimeHelper.prettyDueDate(task.due));
	    	mDate.setText(DateTimeHelper.prettyDueDate(task.due));
        	mDay.setVisibility(View.VISIBLE);     
        	mDate.setVisibility(View.VISIBLE);             	
            mDetialsCollapsed.setVisibility(View.VISIBLE);   
            mDetialsSeperator.setVisibility(View.VISIBLE);  
        }        
        else if(task.hasDueDate())
        {
	    	mDay.setText(DateTimeHelper.prettyDueDate(task.due));
	    	mDate.setText(DateTimeHelper.prettyDueDate(task.due));
        	mDay.setVisibility(View.VISIBLE);     
        	mDate.setVisibility(View.VISIBLE);       
        	mDetialsCollapsed.setVisibility(View.VISIBLE);   
            mDetialsSeperator.setVisibility(View.VISIBLE);    
        }      
        else if(task.hasTags())
        {
        	
        }  
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {		
		if(mActiveTaskId != GooBase.INVALID_ID)
		{
			GooTask task = dbTLHelper.read(mActiveTaskId);					

			GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
			task.setStatus(status);
			task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
			dbTLHelper.update(task);		
		}	
	}
}
