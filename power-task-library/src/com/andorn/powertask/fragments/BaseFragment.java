package com.andorn.powertask.fragments;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.activities.BaseActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class BaseFragment extends Fragment { 
	
	private static final String TAG = BaseFragment.class.getName();

	protected BaseActivity mActivity;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	mActivity = (BaseActivity) getActivity();    	
    }
    
    public TaskApplication app()
    {    	
    	return TaskApplication.app(mActivity);
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T host(FragmentActivity activity)
    {    	
    	T ret = null;
    	try
    	{
    		ret = (T)activity;    		
    	}
    	catch (Exception e) {
			 Log.e(TAG, "activity doesnt implement the host " + ret.getClass().getName());
		}
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T frag(Fragment fragment)
    {    	
    	T ret = null;
    	try
    	{
    		ret = (T)fragment;    		
    	}
    	catch (Exception e) {
			 Log.e(TAG, "fragment doesnt implement the host " + ret.getClass().getName());
		}
    	return ret;
    }    
    
    
//    public static IGooTaskFrag frag(Fragment fragment)
//    {    	
//    	IGooTaskFrag frag = null;		
//		 if(fragment instanceof IGooTaskFrag)
//		 {
//			 frag = (IGooTaskFrag) fragment;
//		 }
//		 else
//		 {			 
//			 Log.e(TAG, "activity doesnt implement IGooTasksFrag");
//		 }    	
//		 return frag;
//    }
//    
//    public IGooTaskFrag frag()
//    {    	
//    	return frag(this);
//    }
//    
//	public void sync() {	
//		sync(true);
//	}
//	
//	public void sync(boolean withRefresh) {	
//		
//	}
}
