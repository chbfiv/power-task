package com.andorn.powertask.fragments;

import com.andorn.powertask.activities.BaseActivity;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class BaseListFragment extends ListFragment { 

	@SuppressWarnings("unused")
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
