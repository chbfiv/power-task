package com.andorn.tasktags.fragments;

import com.andorn.tasktags.R;
import com.andorn.tasktags.adapters.GooTasksCursorAdapter;
import com.andorn.tasktags.database.GooTaskSortType;
import com.andorn.tasktags.interfaces.IGooTaskHost;
import com.andorn.tasktags.interfaces.IGooTasksFrag;
import com.andorn.tasktags.interfaces.IGooTasksHost;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTask;
import com.andorn.tasktags.models.GooTaskList;
import com.andorn.tasktags.services.TasksAppService;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GooTasksFragment extends BaseListFragment
	implements IGooTasksFrag { 
	
	private static final String TAG = GooTasksFragment.class.getName();

	public static final String EXTRA_TASK_SORT_TYPE = "task_sort_type";		

	private View mRoot;
	private ListView mList;
	private TextView mTaskListTitle;
	
	private GooTasksCursorAdapter mAdapter;
    private LayoutInflater inflater;
	
    private int mTaskSortType;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_tasks, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//setHasOptionsMenu(true);
    	
    	mTaskSortType = getArguments() != null ? getArguments().getInt(EXTRA_TASK_SORT_TYPE) : GooTaskSortType.CUSTOM_POSITION;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);   
		
		setListAdapter(null);	 
		
		inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		mRoot = getView();
        mList = getListView();
        
    	GooTaskList list = host().getDbhTaskLists().read(host().getActiveTaskListId());
    	String listTitle = "default list title";
    	if(list != null) listTitle = list.title;

		mTaskListTitle = (TextView) mRoot.findViewById(R.id.tasks_header_title);
		mTaskListTitle.setText(listTitle);
		
    	View headerContainer = inflater.inflate(R.layout.tasks_sort_header, null);
		headerContainer.setEnabled(false);
    	TextView left = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_left);
    	TextView center = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_center);
    	TextView right = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_right);
    	
		left.setText(null);
		center.setText(GooTaskSortType.getSortType(mTaskSortType));
		right.setText(null);	
		
		mList.addHeaderView(headerContainer);        
    }
    
    @Override
    public void onResume() {
    	super.onResume();    	
		setListAdapter(null);	 
		 
 		Cursor c = host().getDbhTasks().queryCursor(host().getActiveTaskListId(), GooSyncBase.SYNC_DELETE, mTaskSortType);  
 		mAdapter = new GooTasksCursorAdapter(mActivity, this, c, true);		
 		setListAdapter(mAdapter); 	
		mAdapter.requery();	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
		if(mAdapter.getCursor() != null) mAdapter.getCursor().deactivate();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();    	
		if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().close();		
    }    
	
    public static GooTasksFragment create(int sortType) {
    	GooTasksFragment f = new GooTasksFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_SORT_TYPE, sortType);
        f.setArguments(args);
        return f;
    }
    
    public IGooTasksHost host()
    {    	
    	return BaseFragment.<IGooTasksHost>host(mActivity);
    }
    
    public IGooTasksFrag frag()
    {    	
    	return BaseFragment.<IGooTasksFrag>frag(this);
    }
    
	public GooTasksCursorAdapter getTasksAdapter() {
		return mAdapter;
	}

    public class TaskClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			long taskId = (Long)v.getTag();
			if(taskId != GooBase.INVALID_ID)
			{
				BaseFragment.<IGooTaskHost>host(mActivity).setActiveTaskId(taskId);
			}
		}
	}    
    
    public class TaskLongClick implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {	
			long taskId = (Long)v.getTag();
			if(taskId != GooBase.INVALID_ID)
			{
				TaskActionsDialogFragment.showDialog(GooTasksFragment.this, taskId);
			}	
			return false;
		}
	}
    
    public class TaskStatusCheckedChanged implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			long taskId = (Long)v.getTag();
			if(taskId != GooBase.INVALID_ID)
			{
				GooTask task = host().getDbhTasks().read(taskId);
				GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
				task.setStatus(status);			
				task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
				host().getDbhTasks().update(task);
				mAdapter.requery();	
			}
		}
	}

    public class TaskStarCheckedChanged implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		    Toast.makeText(mActivity, "this code is under development", Toast.LENGTH_SHORT).show();	
		    
//			long taskId = (Long)v.getTag();
//			if(taskId != GooBase.INVALID_ID)
//			{
//				GooTask task = dbTLHelper.read(taskId);
//				if(isChecked) dbTagMapHelper.replace("blue-star", taskId);
//				else dbTagMapHelper.delete("blue-star", taskId);	
//				task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
//				dbTLHelper.update(task);	
//				mAdapter.requery();			
//			}
		}
	}
}
