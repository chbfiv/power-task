package com.andorn.powertask.adapters;

import com.andorn.powertask.database.GooTaskSortType;
import com.andorn.powertask.fragments.GooTasksFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class GooTasksPagerAdapter extends FragmentStatePagerAdapter {
	
	private static final String TAG = GooTasksPagerAdapter.class.getName();
	
	private GooTasksFragment mCurrentFragment;
	
    public GooTasksPagerAdapter(FragmentManager fm) {
		super(fm);		
	}

	@Override
	public Fragment getItem(int position) {
		mCurrentFragment = GooTasksFragment.create(GooTaskSortType.getSortTypeFromPosition(position));;
		return mCurrentFragment;
	}

	@Override
	public int getCount() {
		return GooTaskSortType.COUNT;
	}
	
	public GooTasksFragment getCurrentFragment()
	{	
		return mCurrentFragment;
	}
}
