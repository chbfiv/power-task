package com.andorn.powertask.loaders;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.fragments.BaseFragment;
import com.andorn.powertask.interfaces.IGooTasksFrag;
import com.andorn.powertask.interfaces.IGooTasksHost;
import com.andorn.powertask.models.GooSyncBase;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;

public class GooTasksLoader extends BaseCursorLoader {
	
	@SuppressWarnings("unused")
	private static final String TAG = GooTasksLoader.class.getName();
	
	public GooTasksLoader(Context context, Fragment fragment) {
		super(context, fragment);		
	}
	
    public TaskApplication app()
    {    	
    	return TaskApplication.app(mActivity);
    }
    
    public IGooTasksHost host()
    {    	
    	return BaseFragment.<IGooTasksHost>host(mActivity);
    }
    
    public IGooTasksFrag frag()
    {    	
    	return BaseFragment.<IGooTasksFrag>frag(mFragment);
    }
    
	@Override
	public Cursor loadInBackground() {
		return app().getDbhTasks().queryCursor(host().getActiveTaskListId(), 
				GooSyncBase.SYNC_DELETE, frag().getTaskSortType());  		
	}
}
