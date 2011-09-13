package com.andorn.tasktags.adapters;

import com.andorn.tasktags.database.GooTaskSortType;
import com.andorn.tasktags.fragments.GooTasksFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class GooTasksPagerAdapter extends FragmentStatePagerAdapter {
	
	private static final String TAG = GooTasksPagerAdapter.class.getName();
	
	private int mCurrentItemPosition = POSITION_NONE;
	
    public GooTasksPagerAdapter(FragmentManager fm) {
		super(fm);		
	}

	@Override
	public Fragment getItem(int position) {
		mCurrentItemPosition = position;
		return GooTasksFragment.create(GooTaskSortType.getSortTypeFromPosition(position));
	}

	@Override
	public int getCount() {
		return GooTaskSortType.COUNT;
	}
	
	public GooTasksFragment getCurrentFragment()
	{		
		GooTasksFragment item = null;
		try
		{
			if(mCurrentItemPosition != POSITION_NONE)
			{
				item = (GooTasksFragment) getItem(mCurrentItemPosition);
			}
		}
		catch(Exception ex)
		{
	        Log.w(TAG, ex.getMessage());		
		}
		return item;
	}
}
