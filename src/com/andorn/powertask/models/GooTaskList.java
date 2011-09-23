package com.andorn.powertask.models;

import com.andorn.powertask.helpers.GeneralHelper;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;

public class GooTaskList extends GooSyncBase {

	
	public long accountId;
	public String remoteId;
	public String kind;
	public String title;
	public String selfLink;	
	
	public GooTaskList(long accountId, String title)
	{
		super();
		this.accountId = accountId;
		this.title = title;
	}	
	
	public GooTaskList(long accountId, String remoteId, String kind, String title, String selfLink)
	{
		super();
		this.accountId = accountId;
		this.remoteId = remoteId;
		this.kind = kind;
		this.title = title;
		this.selfLink = selfLink;
	}	
	
	public static GooTaskList Convert(long accountId, TaskList remoteList, String eTag)
	{
		GooTaskList localList = new GooTaskList(accountId, remoteList.id, remoteList.kind, remoteList.title, remoteList.selfLink);
		localList.setETag(eTag);
		return localList;
	}
	
	public static boolean find(String remoteId, TaskLists remoteLists)
	{				
		if(GeneralHelper.isNullOrEmpty(remoteId)) return false;
		if(remoteLists == null || remoteLists.items == null) return false;

		boolean ret = false;
		for (TaskList remoteList : remoteLists.items) {
			if(remoteList.id.equals(remoteId))
			{
				ret = true;
				break;
			}
		}			
		return ret;
	}	
	
	public static boolean shouldDelete(String remoteId, TaskLists remoteLists)
	{
		if(GeneralHelper.isNullOrEmpty(remoteId)) return false;
		if(remoteLists == null || remoteLists.items == null) return false;
		return !find(remoteId, remoteLists);
	}
}
