package com.andorn.powertask.models;

import com.andorn.powertask.helpers.ColorHelper;

public class TCTag extends GooBase {
	
	protected String mName;
	protected int mColor;
	
	public TCTag(String tag, int color)
	{
		mName = tag;
		mColor = color;
	}	
	
	public TCTag(String tag)
	{
		mName = tag;
		mColor = ColorHelper.getRandomColor();
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String tag)
	{
		mName = tag;
	}
	
	public int getColor()
	{
		return mColor;
	}
	
	public void setColor(int color)
	{
		mColor = color;
	}
}
