package com.mtelab.tasktags.activities;

import java.util.Calendar;
import java.util.Date;

import com.mtelab.tasktags.R;
import com.mtelab.tasktags.base.ActivityHelper;
import com.mtelab.tasktags.base.BaseActivity;
import com.mtelab.tasktags.database.GooTasksOpenHelper;
import com.mtelab.tasktags.helpers.DateTimeHelper;
import com.mtelab.tasktags.models.GooBase;
import com.mtelab.tasktags.models.GooSyncBase;
import com.mtelab.tasktags.models.GooTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class GooTaskEditActivity extends BaseActivity  {

	private static final String TAG = GooTaskEditActivity.class.getName();
	
	private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
	
    private EditText mTitle;
    private EditText mNotes;
    private Button mDateClear;
    private TextView mDate;
    private CheckBox mStatusCheckBox;
    
    static final int DATE_DIALOG_ID = 0;
    
    private GooTask editTask;

	public static final String EXTRA_ACTIVE_TASK_ID = "active_task_id";
	public static final String EXTRA_ACTIVE_TASK_LIST_ID = "active_task_list_id";
	
	private long mActiveTaskId = GooBase.INVALID_ID;
	private long mActiveTaskListId = GooBase.INVALID_ID;
    
	private boolean mDueDateChanged = false;
	private boolean mCreate = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
			return;
		}
    	
		mActiveTaskId = extras.getLong(EXTRA_ACTIVE_TASK_ID, GooBase.INVALID_ID);
		mActiveTaskListId = extras.getLong(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);
		
        setContentView(R.layout.task_edit);
        
        if(mActiveTaskId == GooBase.INVALID_ID)        
        	getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_COMPOSE);
        else
        	getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_EDIT);
        
        mTitle = (EditText) findViewById(R.id.taskEdit_titleField);
        mNotes = (EditText) findViewById(R.id.taskEdit_detialsField);
        mDateClear = (Button) findViewById(R.id.taskEdit_dateClear);        
        mDate = (TextView) findViewById(R.id.taskEdit_dateField);   
        mStatusCheckBox = (CheckBox) findViewById(R.id.taskEdit_statusCheckBox);           
        
        refreshTask(true);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    	refreshTask(false);
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dbTLHelper != null) dbTLHelper.close(); 
	}
	
    public static void go(Activity activity, long taskListId, long taskId) 
    {
    	go(activity, true, taskListId, taskId);
    }
    
    public static void go(Activity activity, long taskId) 
    {
    	go(activity, true, taskId);
    }
    
	public static void go(Activity activity, boolean finishActivity, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskEditActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_LIST_ID, GooBase.INVALID_ID);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade, R.anim.hold);
		if(finishActivity) activity.finish();
	}
	
	public static void go(Activity activity, boolean finishActivity, long taskListId, long taskId) {		
		final Intent intent = new Intent(activity, GooTaskEditActivity.class);
		intent.putExtra(EXTRA_ACTIVE_TASK_LIST_ID, taskListId);
		intent.putExtra(EXTRA_ACTIVE_TASK_ID, taskId);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.fade, R.anim.hold);
		if(finishActivity) activity.finish();
	}
	
    private void refreshTask(boolean full)
    {
    	if(mActiveTaskId == GooBase.INVALID_ID)
    	{
    		if(mActiveTaskListId != GooBase.INVALID_ID)
    		{
    			mActiveTaskId = dbTLHelper.create(mActiveTaskListId);
	    		editTask = dbTLHelper.read(mActiveTaskId);
	    		mCreate = true; //for sync/save
    		}
    		else
    		{
        		Log.e(TAG, "mActiveTaskListId - task null.");
        		finish();
    		}
    	}    	
    	else if(full)
    	{
    		editTask = dbTLHelper.read(mActiveTaskId);    		
    	}    	
    	
    	if(editTask == null)
    	{
    		Log.e(TAG, "editTask - task null.");
    		finish();
    	}
    	
		mTitle.setText(editTask.title);
		mNotes.setText(editTask.notes);
		
        mStatusCheckBox.setChecked(editTask.isCompleted());   
        
		if(editTask.hasDueDate())
        {
	    	mDate.setText(DateTimeHelper.prettyDueDate(editTask.due));
	        mDateClear.setVisibility(View.VISIBLE);   
        } 
		else
		{      
			mDate.setText(null);
	        mDateClear.setVisibility(View.GONE);			
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu_items, menu);
        getMenuInflater().inflate(R.menu.discard_menu_items, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    		case R.id.menu_discard: 
    		{
	        	cancelTaskEdit();
	            return true;
	        }
    		case R.id.menu_save: 
    		{
    			saveTaskEdit();
	            return true;
	        }
    	}
        return super.onOptionsItemSelected(item);
    }
    
    public void pickDate_OnClick(View view)
    {    	
    	try
    	{
            int tmpYear;
            int tmpMonth;
            int tmpDay;
            Calendar c = editTask.hasDueDate() ? editTask.getDueDate() : Calendar.getInstance();	        	
            tmpYear = c.get(Calendar.YEAR);
            tmpMonth = c.get(Calendar.MONTH);
            tmpDay = c.get(Calendar.DAY_OF_MONTH);     
        	
            DatePickerDialog dialog = new DatePickerDialog(this, new OnDateSetListener() {				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {					
					editTask.setDueDate(year, monthOfYear, dayOfMonth);
                    mDate.setText(DateTimeHelper.prettyDueDate(editTask.due));
                    mDateClear.setVisibility(View.VISIBLE);
                    mDueDateChanged = true; //used for save/sync logic
				}
			}, tmpYear, tmpMonth, tmpDay);
            dialog.show();
    	}
    	catch(Exception ex)
    	{
    		 Log.w(TAG, ex);	
    	}
    }

    public void clearDate_OnClick(View view)
    {        
    	editTask.due = null;
    	mDate.setText(null);
    	mDateClear.setVisibility(View.GONE);
    }
    
    public void cancelTaskEdit() {
    	if(mCreate)
    	{    		
    		dbTLHelper.delete(mActiveTaskId);	
    	}
    	
        setResult(Activity.RESULT_CANCELED);
        finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);	
    }

    public void saveTaskEdit() {    	
    	
    	int dbAction = mCreate ? GooSyncBase.SYNC_CREATE : GooSyncBase.SYNC_UPDATE;
    	
		GooTask.Status status = mStatusCheckBox.isChecked() ? GooTask.Status.completed : GooTask.Status.needsAction;
		if (status != editTask.getStatus()) 
		{
			editTask.setStatus(status);
        	editTask.flagSyncState(dbAction);
		}
		
        String title = mTitle.getText().toString();
        if (!isValueEqual(title, editTask.title)) 
        {
        	editTask.title = title;
        	editTask.flagSyncState(dbAction);
        }
        
        String notes = mNotes.getText().toString();
        if (!isValueEqual(notes, editTask.notes)) 
        {
        	editTask.notes = notes;
        	editTask.flagSyncState(dbAction);
        }
        
        if (mDueDateChanged) 
        {
        	editTask.flagSyncState(dbAction);
        }
        
        if (editTask != null) 
        {			
			dbTLHelper.update(editTask);		
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }

        finish();
    }
    
    public static boolean isValueEqual(String x, String y)
    {
    	boolean ret = false;
    	if(x == null && y == null)
    	{
    		ret = true;
    	}
    	else if(x != null && x.equals(y))
    	{
    		ret = true;
    	}    	
    	return ret;    
    }
}
