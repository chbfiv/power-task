package com.andorn.powertask.adapters;

import com.andorn.powertask.database.GooTaskSortType;
import com.andorn.powertask.fragments.GooTasksFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;

public class GooTasksPagerAdapter extends FragmentStatePagerAdapter
	implements OnPageChangeListener {
	
	private static final String TAG = GooTasksPagerAdapter.class.getName();
	
	private GooTasksFragment[] mFragments = new GooTasksFragment[GooTaskSortType.COUNT];
	private int mCurrentPosition = POSITION_NONE;
	
    public GooTasksPagerAdapter(FragmentManager fm) {
		super(fm);		
	}

	@Override
	public Fragment getItem(int position) {		
		return GooTasksFragment.create(GooTaskSortType.getSortTypeFromPosition(position));
	}

	@Override
	public int getCount() {
		return GooTaskSortType.COUNT;
	}
	
	public GooTasksFragment getCurrentFragment()
	{			
		GooTasksFragment frag = null;
		try
		{
			if(mCurrentPosition >= 0 && mCurrentPosition < mFragments.length)
				frag = mFragments[mCurrentPosition];
		}
		catch (Exception ex)
		{
			Log.e(TAG, "getCurrentFragment " + ex.getMessage());			
		}
		return frag;
	}
	
	public GooTasksFragment[] getFragments()
	{
		return mFragments;		
	}

	@Override
	public Object instantiateItem(View container, int position) {
		Object obj = super.instantiateItem(container, position);		
		if(obj instanceof GooTasksFragment)
		{
			if(position >= 0 && position < mFragments.length) mFragments[position] = (GooTasksFragment)obj;
			if(mCurrentPosition == POSITION_NONE) mCurrentPosition = position;
		}
		else
		{
			Log.e(TAG, "instantiateItem - was expecting GooTasksFragment");	
		}
		return obj;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		super.destroyItem(container, position, object);
		mFragments[position] = null;	
	}	
	
	@Override
	public void onPageScrollStateChanged(int state) {
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentPosition = position;
	}
}
