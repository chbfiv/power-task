package com.andorn.powertask.models;

import java.text.ParseException;
import java.util.Date;

import com.andorn.powertask.helpers.DateTimeHelper;

public class GooBase {
	
	private long mId;
	private Date mCreated;
	private Date mModified;

	public static final int INVALID_ID = -1;
	
	public GooBase()
	{
		mId = -1;
		mCreated = new Date();
		mModified = new Date();
	}		
	
	public void setBase(long id, Date created, Date modified)
	{
		mId = id;
		mCreated = created;
		mModified = modified;
	}
	
	public void setBase(long id, String created, String modified) throws ParseException
	{
		mId = id;
		mCreated = DateTimeHelper.simpleDateFormat.parse(created);
		mModified =  DateTimeHelper.simpleDateFormat.parse(modified);
	}
	
	public void setBase(long id, long created, long modified)
	{
		mId = id;
		mCreated =  new Date(created);
		mModified =   new Date(modified);
	}
	
	public long getId()
	{
		return mId;
	}
	
	public void setId(long id)
	{
		mId = id;
	}
	
	public Date getCreated()
	{
		return mCreated;
	}
	
	public void setCreated(Date date)
	{
		mCreated = date;
	}
	
	public void setCreated(long date)
	{
		mCreated = new Date(date);
	}
	
	public void setCreated(String date) throws ParseException
	{
		mCreated = DateTimeHelper.simpleDateFormat.parse(date);
	}
	
	public Date getModified()
	{
		return mModified;
	}
	
	public void setModified(Date date)
	{
		mModified = date;
	}
	
	public void setModified(long date)
	{
		mModified = new Date(date);
	}
	
	public void setModified(String date) throws ParseException
	{
		mModified = DateTimeHelper.simpleDateFormat.parse(date);
	}
}
