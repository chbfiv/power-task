package com.mtelab.taskhack.views;

import java.util.Calendar;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.R.id;
import com.mtelab.taskhack.R.layout;
import com.mtelab.taskhack.R.menu;
import com.mtelab.taskhack.base.ActivityHelper;
import com.mtelab.taskhack.base.BaseActivity;
import com.mtelab.taskhack.database.GooTasksOpenHelper;
import com.mtelab.taskhack.models.GooBase;
import com.mtelab.taskhack.models.GooTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class GooTaskComposeActivity extends BaseActivity {

	private static final String TAG = GooTaskComposeActivity.class.getName();
	
	private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
	
    private EditText mTitle;
    private EditText mNotes;
    private Button taskEditDateClear;
    private EditText taskEditDateField;
    
    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean hasDate;
    

	public static final String EXTRA_ACTIVE_TASK_ID = "active_task_id";
	private long mActiveTaskId = GooBase.INVALID_ID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	if(!dbTLHelper.initialize())
    	{
    		Log.e(TAG, "onCreate - db failed to initialize.");
    		return;    		
    	}
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
			return;
		}
    	
		mActiveTaskId = extras.getLong(EXTRA_ACTIVE_TASK_ID, GooBase.INVALID_ID);
		
        setContentView(R.layout.task_compose);
        getActivityHelper().setupActionBar(ActivityHelper.ACTIONBAR_TASK_COMPOSE);
        
        mTitle = (EditText) findViewById(R.id.taskEdit_titleField);
        mNotes = (EditText) findViewById(R.id.taskEdit_detialsField);
        taskEditDateClear = (Button) findViewById(R.id.taskEdit_dateClear);        
        taskEditDateField = (EditText) findViewById(R.id.taskEdit_dateField);        
                
        mYear = 0;
        mMonth = 0;
        mDay = 0;
        hasDate = false;
    	taskEditDateClear.setVisibility(View.GONE);
    	
    	refreshTask();
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dbTLHelper != null) dbTLHelper.close(); 
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
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            final Calendar c = Calendar.getInstance();
            int tmpYear = mYear > 0 ? mYear : c.get(Calendar.YEAR);
            int tmpMonth = mMonth >= 0 ? mMonth : c.get(Calendar.MONTH);
            int tmpDay = mDay > 0 ? mDay : c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(this, new OnDateSetListener() {				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;		
                    hasDate = true;
                    taskEditDateClear.setVisibility(View.VISIBLE);
                    
                    taskEditDateField.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mMonth + 1).append("/")
                    .append(mDay).append("/")
                    .append(mYear));
				}
			}, tmpYear, tmpMonth, tmpDay);
        }
        return null;
    }
    
    public void pickDate_OnClick(View view)
    {
    	showDialog(DATE_DIALOG_ID);    
    }

    public void clearDate_OnClick(View view)
    {        
	    mYear = 0;
	    mMonth = 0;
	    mDay = 0;
    	hasDate = false;
    	taskEditDateField.setText(null);
    	taskEditDateClear.setVisibility(View.GONE);
    }
    
    public void cancelTaskEdit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);	
    }

    public void saveTaskEdit() {
        String taskName = mTitle.getText().toString();
        if (taskName.length() > 0) {
//            Intent t = new Intent();
//            t.putExtra("task", taskName);
//            t.putExtra("details", taskEditDetialsField.getText().toString());
//
//            t.putExtra("day", mDay);
//            t.putExtra("month", mMonth);
//            t.putExtra("year", mYear);
	          
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }

        finish();
    }
}
