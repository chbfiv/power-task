package com.andorn.powertask.fragments;

import com.andorn.powertask.adapters.GooTasksCursorAdapter;
import com.andorn.powertask.database.GooTaskSortType;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.interfaces.IGooTaskHost;
import com.andorn.powertask.interfaces.IGooTasksFrag;
import com.andorn.powertask.interfaces.IGooTasksHost;
import com.andorn.powertask.loaders.GooTasksLoader;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.models.GooSyncBase;
import com.andorn.powertask.models.GooTask;
import com.andorn.powertask.models.GooTaskList;
import com.andorn.powertask.R;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class GooTasksFragment extends BaseListFragment
	implements IGooTasksFrag, LoaderManager.LoaderCallbacks<Cursor> { 

	@SuppressWarnings("unused")
	private static final String TAG = GooTasksFragment.class.getName();
	
	public static final String EXTRA_TASK_PAGE_POSITION = "task_page_position";	
	public static final String EXTRA_TASK_SORT_TYPE = "task_sort_type";	
	public static final int LOADER_TASKS = 0;	

	private View mRoot;
	private ListView mList;
	private TextView mTaskListTitle;
	
	private GooTasksCursorAdapter mAdapter;
    private LayoutInflater inflater;

    private int mPagePos;
    private int mTaskSortType;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_tasks, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//setHasOptionsMenu(true);

    	mPagePos = getArguments() != null ? getArguments().getInt(EXTRA_TASK_PAGE_POSITION) : 0;    	 
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);   
		
		setListAdapter(null);	 
		
		inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		mRoot = getView();
        mList = getListView();
        
    	GooTaskList list = app().getDbhTaskLists().read(host().getActiveTaskListId());
    	String listTitle = "default list title";
    	if(list != null) listTitle = list.title;

		mTaskListTitle = (TextView) mRoot.findViewById(R.id.tasks_header_title);
		mTaskListTitle.setText(listTitle);
		
    	View headerContainer = inflater.inflate(R.layout.tasks_sort_header, null);
		headerContainer.setEnabled(false);
    	ImageView previous = (ImageView) headerContainer.findViewById(R.id.tasks_sort_header_prevArrow);
    	ImageView next = (ImageView) headerContainer.findViewById(R.id.tasks_sort_header_nextArrow);
    	TextView left = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_left);
    	TextView center = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_center);
    	TextView right = (TextView) headerContainer.findViewById(R.id.tasks_sort_header_right);  

		int defaultTaskSortType = GooTaskSortType.getDefaultFromPosition(mPagePos);
    	
	    final SharedPrefUtil prefs = SharedPrefUtil.create(mActivity);
	    mTaskSortType = prefs.getSharedPref().getInt(SharedPrefUtil.PREF_TASKS_PAGE_SORT_TYPE + ":" + mPagePos, GooTaskSortType.INVALID);
	    
	    //save if invalid
	    if(mTaskSortType == GooTaskSortType.INVALID)
	    {
	    	mTaskSortType = defaultTaskSortType;
	    	
	    	Editor editor = prefs.getEditor();
		    if(editor != null)
		    {
			    editor.putInt(SharedPrefUtil.PREF_TASKS_PAGE_SORT_TYPE + ":" + mPagePos, mTaskSortType);
			    editor.commit();
		    }
	    }	   

		left.setText(null);
		right.setText(null);	
		
	    if(mPagePos == 0) 
    	{
    		previous.setVisibility(View.GONE);
    		left.setVisibility(View.GONE);
    	}    	
    	if(mPagePos == GooTaskSortType.COUNT - 1) 
    	{
    		next.setVisibility(View.GONE);
    		right.setVisibility(View.GONE);
    	}    	
    	if(mPagePos > 0)
    	{
    		int defaultLeftSortType = GooTaskSortType.getDefaultFromPosition(mPagePos - 1);
    		int leftSortType = prefs.getSharedPref().getInt(SharedPrefUtil.PREF_TASKS_PAGE_SORT_TYPE + ":" + (mPagePos - 1), defaultLeftSortType);
    		left.setText(GooTaskSortType.getTitle(leftSortType));
    	}
    	if(mPagePos < GooTaskSortType.COUNT - 1)
    	{
    		int defaultRightSortType = GooTaskSortType.getDefaultFromPosition(mPagePos + 1);
    		int rightSortType = prefs.getSharedPref().getInt(SharedPrefUtil.PREF_TASKS_PAGE_SORT_TYPE + ":" + (mPagePos + 1), defaultRightSortType);
    		right.setText(GooTaskSortType.getTitle(rightSortType));
    	}
    	
		center.setText(GooTaskSortType.getTitle(mTaskSortType));
		
		mList.addHeaderView(headerContainer);   
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOADER_TASKS, null, this);		
 		  
 		mAdapter = new GooTasksCursorAdapter(mActivity, this, null, true);		
 		setListAdapter(mAdapter); 
    }
    
    @Override
    public void onResume() {
    	super.onResume();   
		refresh();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
		//if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().deactivate();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();    	
    	if(isAdded()) getLoaderManager().destroyLoader(LOADER_TASKS);
		//if(mAdapter != null && mAdapter.getCursor() != null) mAdapter.getCursor().close();		
    }    
	
    public static GooTasksFragment create(int pagePosition) {    	
    	GooTasksFragment f = new GooTasksFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_PAGE_POSITION, pagePosition);
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

	public int getTaskSortType() {
		return mTaskSortType;
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
				GooTask task = app().getDbhTasks().read(taskId);
				GooTask.Status status = isChecked ? GooTask.Status.completed : GooTask.Status.needsAction;
				task.setStatus(status);			
				task.flagSyncState(GooSyncBase.SYNC_UPDATE);				
				app().getDbhTasks().update(task);
				host().refresh();
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

    public void refresh()
    {
    	if(isAdded()) getLoaderManager().restartLoader(LOADER_TASKS, null, this);
    }
    
    //Instantiate and return a new Loader for the given ID.
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		return new GooTasksLoader(mActivity, this);
	}

	// Called when a previously created loader has finished its load.
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {    
		// Swap the new cursor in.  (The framework will take care of closing the
	    // old cursor once we return.)
		mAdapter.swapCursor(data);
	}

	//Called when a previously created loader is being reset, thus making its data unavailable.
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
	    // above is about to be closed.  We need to make sure we are no
	    // longer using it.
	    mAdapter.swapCursor(null);
	}
}
