package com.andorn.powertask.fragments;

import java.util.Calendar;

import com.andorn.powertask.helpers.DateTimeHelper;
import com.andorn.powertask.helpers.GeneralHelper;
import com.andorn.powertask.interfaces.IGooTaskEditFrag;
import com.andorn.powertask.interfaces.IGooTaskEditHost;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.models.GooSyncBase;
import com.andorn.powertask.models.GooTask;
import com.andorn.powertask.R;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class GooTaskEditFragment extends BaseFragment 
	implements IGooTaskEditFrag { 
	
	private static final String TAG = GooTaskEditFragment.class.getName();

	private View mRoot;
	
    private EditText mTitle;
    private EditText mNotes;
    private Button mDateClear;
    private TextView mDate;
    private CheckBox mStatusCheckBox;

    private GooTask editTask;
    
	private boolean mCreate = false;
	private boolean mDueDateChanged = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_task_edit, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);   

		mRoot = getView();
		
        mTitle = (EditText) mRoot.findViewById(R.id.taskEdit_titleField);
        mNotes = (EditText) mRoot.findViewById(R.id.taskEdit_detialsField);
        mDateClear = (Button) mRoot.findViewById(R.id.taskEdit_dateClear);        
        mDate = (TextView) mRoot.findViewById(R.id.taskEdit_dateField);   
        mStatusCheckBox = (CheckBox) mRoot.findViewById(R.id.taskEdit_statusCheckBox);       
        
        mDate.setOnClickListener(new DueDateClick());
        mDateClear.setOnClickListener(new DueDateClearClick());
    }	

    @Override
    public void onResume() {
    	super.onResume();
    	reload();    	
    } 
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.save_menu_items, menu);
    	inflater.inflate(R.menu.discard_menu_items, menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    		case R.id.menu_discard: 
    		{
	        	discard();
	            return true;
	        }
    		case R.id.menu_save: 
    		{
    			save();
	            return true;
	        }
    	}
		return super.onOptionsItemSelected(item);
	}

    public IGooTaskEditHost host()
    {    	
    	return BaseFragment.<IGooTaskEditHost>host(mActivity);
    }
    
    public IGooTaskEditFrag frag()
    {    	
    	return BaseFragment.<IGooTaskEditFrag>frag(this);
    }
    
    public class DueDateClick implements OnClickListener {
		@Override
		public void onClick(View v) {
	    	try
	    	{
	            int tmpYear;
	            int tmpMonth;
	            int tmpDay;
	            Calendar c = editTask.hasDueDate() ? editTask.getDueDate() : Calendar.getInstance();	        	
	            tmpYear = c.get(Calendar.YEAR);
	            tmpMonth = c.get(Calendar.MONTH);
	            tmpDay = c.get(Calendar.DAY_OF_MONTH);     
	        	
	            DatePickerDialog dialog = new DatePickerDialog(mActivity, new OnDateSetListener() {				
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
	}    

    public class DueDateClearClick implements OnClickListener {
		@Override
		public void onClick(View v) {
	    	editTask.due = null;
	    	mDate.setText(null);
	    	mDateClear.setVisibility(View.GONE);
		}
	}    

	@Override
	public void reload() {
		long taskId = host().getActiveTaskId();
		long taskListId = host().getActiveTaskListId();
		
    	if(taskId == GooBase.INVALID_ID)
    	{
    		if(taskListId == GooBase.INVALID_ID)
    		{
        		Log.e(TAG, "reload - task list invalid.");
        		mActivity.finish();
    		}
    		else
    		{
    			editTask = new GooTask(taskListId);
    			mCreate = true;
    		}
    	}    	
    	else 
    	{
    		editTask = host().getDbhTasks().read(taskId);    		
    	}    	
    	
    	if(editTask == null)
    	{
    		Log.e(TAG, "reload - edit task still task null.");
    		mActivity.finish();
    	}
    	
    	mDueDateChanged = false;
		
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

	public void save() {
    	int dbAction = mCreate ? GooSyncBase.SYNC_CREATE : GooSyncBase.SYNC_UPDATE;
    	
		GooTask.Status status = mStatusCheckBox.isChecked() ? GooTask.Status.completed : GooTask.Status.needsAction;
		if (status != editTask.getStatus()) 
		{
			editTask.setStatus(status);
        	editTask.flagSyncState(dbAction);
		}
		
        String title = mTitle.getText().toString();
        if (!GeneralHelper.isValueEqual(title, editTask.title)) 
        {
        	editTask.title = title;
        	editTask.flagSyncState(dbAction);
        }
        
        String notes = mNotes.getText().toString();
        if (!GeneralHelper.isValueEqual(notes, editTask.notes)) 
        {
        	editTask.notes = notes;
        	editTask.flagSyncState(dbAction);
        }

        if (mDueDateChanged) 
        {
        	editTask.flagSyncState(dbAction);
        }
        
        if (mCreate) 
        {			
			host().getDbhTasks().create(editTask);	
        } else {
			host().getDbhTasks().update(editTask);		
        }

        mActivity.finish();
        mActivity.overridePendingTransition(R.anim.fade, R.anim.hold);	
	}

	public void discard() {    	
        mActivity.finish();
        mActivity.overridePendingTransition(R.anim.fade, R.anim.hold);	
	}    
}
