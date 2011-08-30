package com.mtelab.tasktags.models;

public class TCTagItem extends TCTag {
	
	protected boolean mChecked = false;
	
	public TCTagItem(String tag, int color)
	{
		super(tag, color);
		mChecked = true;
	}	
	
	public TCTagItem(String tag, int color, int checked)
	{
		super(tag, color);
		mChecked = checked != 0;
	}	
	
	public boolean isChecked()
	{
		return mChecked;
	}
	
	public void setChecked(boolean checked)
	{
		mChecked = checked;
	}
}
