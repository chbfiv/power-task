package com.andorn.powertask.models;

import com.andorn.powertask.helpers.GeneralHelper;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

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
		GooTaskList localList = new GooTaskList(accountId, remoteList.getId(), remoteList.getKind(), remoteList.getTitle(), remoteList.getSelfLink());
		localList.setETag(eTag);
		return localList;
	}
	
	public static boolean find(String remoteId, TaskLists remoteLists)
	{				
		if(GeneralHelper.isNullOrEmpty(remoteId)) return false;
		if(remoteLists == null) return false;

		boolean ret = false;
		for (TaskList remoteList : remoteLists.getItems()) {
			if(remoteList.getId().equals(remoteId))
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
		if(remoteLists == null || remoteLists.getItems() == null || remoteLists.getItems().size() == 0) return false;
		return !find(remoteId, remoteLists);
	}
}
