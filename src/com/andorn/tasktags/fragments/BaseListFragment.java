package com.andorn.tasktags.fragments;

import com.andorn.tasktags.activities.BaseActivity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class BaseListFragment extends ListFragment { 
	
	private static final String TAG = BaseListFragment.class.getName();

	protected BaseActivity mActivity;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	mActivity = (BaseActivity) getActivity();    	
    }
    
//    
//	public void sync() {	
//		sync(true);
//	}
//	
//	public void sync(boolean withRefresh) {	
//		
//	}
}
