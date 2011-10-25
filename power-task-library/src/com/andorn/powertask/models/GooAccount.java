package com.andorn.powertask.models;

public class GooAccount extends GooSyncBase {
	
	private String mName;
	private String mType;
	private boolean mSync = false;
	private String mAuthToken;
  
	public GooAccount(String name, String type, boolean sync) {
		this.mName = name;
		this.mType = type;
		this.mSync = sync;
	}
	
	public GooAccount(long id, String name, String type, boolean sync) {
		this.setId(id);
		this.mName = name;
		this.mType = type;
		this.mSync = sync;
	}
	
	public void setName(String val)
	{
		mName = val;		
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setType(String val)
	{
		mType = val;		
	}
	
	public String getType()
	{
		return mType;
	}
	
	public void setSync(boolean val)
	{
		mSync = val;		
	}
	
	public boolean getSync()
	{
		return mSync;
	}
	
	public void setAuthToken(String val)
	{
		mAuthToken = val;		
	}
	
	public String getAuthToken()
	{
		return mAuthToken;
	}
}