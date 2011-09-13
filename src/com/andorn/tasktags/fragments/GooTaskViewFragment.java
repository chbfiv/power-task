package com.andorn.tasktags.fragments;

import com.andorn.tasktags.R;
import com.andorn.tasktags.activities.GooTaskEditActivity;
import com.andorn.tasktags.helpers.DateTimeHelper;
import com.andorn.tasktags.interfaces.IGooTaskFrag;
import com.andorn.tasktags.interfaces.IGooTaskHost;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTask;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GooTaskViewFragment extends BaseFragment
	implements IGooTaskFrag { 
	
	private static final String TAG = GooTaskViewFragment.class.getName();

	private View mRoot;

    private View mTaskFound;
    private View mTaskNotFound;
    
    private TextView mTitle;
    private TextView mNotes;
    private TextView mDay;
    private TextView mDate;
    private CheckBox mStatusCheckBox;
    
    private View mDetialsCollapsed;
    private View mDetials;
    private View mDetialsSeperator;

    private ImageView mDetialsBtn;
    private ImageView mDetialsCollapsedBtn;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_task_view, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);   
    				
		mRoot = getView();

		mTaskFound = mRoot.findViewById(R.id.taskView_taskFound);
		mTaskNotFound = mRoot.findViewById(R.id.taskView_taskNotFound);
        
        mTitle = (TextView) mRoot.findViewById(R.id.taskView_titleField);
        mNotes = (TextView) mRoot.findViewById(R.id.taskView_detialsField);        

        mDay = (TextView) mRoot.findViewById(R.id.taskView_dayField);  
        mDate = (TextView) mRoot.findViewById(R.id.taskView_dateField);    
        mStatusCheckBox = (CheckBox) mRoot.findViewById(R.id.taskView_statusCheckBox);
        
        mDetials = mRoot.findViewById(R.id.taskView_detials);  
        mDetialsCollapsed = mRoot.findViewById(R.id.taskView_detials_collapsed);   
        mDetialsSeperator = mRoot.findViewById(R.id.taskView_detials_seperator);  
        mDetials.setOnClickListener(new DetialsClick());
        mDetialsCollapsed.setOnClickListener(new DetialsCollapsedClick()); 

        mDetialsBtn = (ImageView) mRoot.findViewById(R.id.taskView_detials_btn);  
        mDetialsCollapsedBtn = (ImageView) mRoot.findViewById(R.id.taskView_detials_collapsed_btn);  
        mDetials.setVisibility(View.GONE);
    }	

    @Override
    public void onResume() {
    	super.onResume();
    	reload();    	
    } 
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.edit_mode_menu_item, menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
			case R.id.menu_edit_mode: {
				GooTaskEditActivity.go(mActivity, false, host().getActiveTaskId());
				return true;
			}
    	}
        return super.onOptionsItemSelected(item);
	}	
    
    public IGooTaskHost host()
    {    	
    	return BaseFragment.<IGooTaskHost>host(mActivity);
    }
    
    public IGooTaskFrag frag()
    {    	
    	return BaseFragment.<IGooTaskFrag>frag(this);
    }
    
	private void taskFound(boolean found)
	{
		if(found)
		{
			mTaskFound.setVisibility(View.VISIBLE);
			mTaskNotFound.setVisibility(View.GONE);
		}
		else
		{
			mTaskNotFound.setVisibility(View.VISIBLE);
			mTaskFound.setVisibility(View.GONE);			
		}		
	}
	
	public void reload()
    {
    	if(host().getActiveTaskId() == GooBase.INVALID_ID)
    	{
    		Log.e(TAG, "refreshTaskList - invalid task list id.");
    		taskFound(false);
    		return;
    	}
    	
		GooTask task = host().getDbhTasks().read(host().getActiveTaskId());
    	if(task == null)
    	{
    		Log.e(TAG, "refreshTaskList - task null.");
    		taskFound(false);
    		return;
    	}

		taskFound(true);
		
		mTitle.setText(task.title);
		mNotes.setText(task.notes);		

        mStatusCheckBox.setOnCheckedChangeListener(null);
        mStatusCheckBox.setChecked(task.isCompleted());   
        mStatusCheckBox.setOnCheckedChangeListener(new TaskStatusCheckedChanged());        
        
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
    
    public class DetialsClick implements OnClickListener {
		@Override
		public void onClick(View v) {
	        mDetialsCollapsed.setVisibility(View.VISIBLE);
	        mDetials.setVisibility(View.GONE);    				
		}    	
    }
    
    public class DetialsCollapsedClick implements OnClickListener {
		@Override
		public void onClick(View v) {
	        mDetialsCollapsed.setVisibility(View.GONE);
	        mDetials.setVisibility(View.VISIBLE); 
		}    	
    }
    
    public class TaskStatusCheckedChanged implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			if(host().getActiveTaskId() != GooBase.INVALID_ID)
			{
				GooTask task = host().getDbhTasks().read(host().getActiveTaskId());					

				GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
				task.setStatus(status);
				task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
				host().getDbhTasks().update(task);	
				host().onDbChange();
			}
		}
	}
}