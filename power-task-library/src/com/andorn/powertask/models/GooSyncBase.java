package com.andorn.powertask.models;

public class GooSyncBase extends GooBase {

	public static final int SYNCED = 0;
	public static final int SYNC_CREATE = 1;
	public static final int SYNC_UPDATE = 2;
	public static final int SYNC_DELETE = 4;
	public static final int SYNC_HIDE = 8;
	
	public static final int SYNC_REQUIRED = SYNC_CREATE | SYNC_UPDATE | SYNC_DELETE | SYNC_HIDE;
	
	private int mSyncState = 0;
	private String mETag = "";
	
	public GooSyncBase() 
	{		
	}	
	
	public int getSyncState()
	{
		return mSyncState;
	}
	
	public boolean isSyncCreate()
	{
		return (mSyncState & SYNC_CREATE) == SYNC_CREATE;
	}
	
	public boolean isSyncUpdate()
	{
		return (mSyncState & SYNC_UPDATE) == SYNC_UPDATE;
	}
	
	public boolean isSyncDelete()
	{
		return (mSyncState & SYNC_DELETE) == SYNC_DELETE;
	}
	
	public boolean isSyncHide()
	{
		return (mSyncState & SYNC_HIDE) == SYNC_HIDE;
	}
	
	public void setSyncState(int syncState)
	{
		mSyncState = syncState;
	}
	
	public void flagSyncState(int syncState)
	{
		mSyncState = mSyncState | syncState;
	}
	
	public void unflagSyncState(int syncState)
	{
		mSyncState = mSyncState & ~syncState;
	}
	
	public String getETag()
	{
		return mETag;
	}
	
	public void setETag(String eTag)
	{
		mETag = eTag;
	}
	
	public void setSync(int syncState, String eTag)
	{
		mSyncState = syncState;
		mETag = eTag;
	}	
	
	public boolean localSyncRequired()
	{
		return (mSyncState & SYNC_REQUIRED) > 0;		
	}	
	
	public boolean remoteSyncRequired(String eTag)
	{
		boolean ret = true;
		if(mETag != null)
		{
			ret =  !mETag.equals(eTag);
		}	
		//don't allow sync to overwrite changes
		ret = ret && !localSyncRequired();
		return ret; 	
	}
}
