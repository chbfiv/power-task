package com.mtelab.taskhack.models;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;
import com.google.api.services.tasks.v1.model.Tasks;

public class GooTask extends GooSyncBase {

	public enum Status {
		needsAction,
		completed
	}
	
	public long taskListId;
	public String remoteId;
	public String kind;
	public String title;
	public String selfLink;	
	public String parent;	
	public String position;	
	public String notes;	
	public String status;	
	public String due;	
	public String completed;	
	public boolean deleted;
	public boolean hidden;
	
	private List<TCTag> mTags;
	
	public GooTask(long taskListId, String remoteId, String kind,
			String title, String selfLink, String parent,
			String position, String notes, String status,
			String due, String completed, boolean deleted,
			boolean hidden)
	{
		super();
		this.taskListId = taskListId;
		this.remoteId = remoteId;
		this.kind = kind;
		this.title = title;
		this.selfLink = selfLink;
		this.parent = parent;
		this.position = position;
		this.notes = notes;
		this.status = status;
		this.due = due;
		this.completed = completed;
		this.deleted = deleted;
		this.hidden = hidden;
		
		mTags = new ArrayList<TCTag>();
		
		this.completed = isCompleted() ? this.completed : null;
	}	
	
	public GooTask(long taskListId, String remoteId, String kind,
			String title, String selfLink, String parent,
			String position, String notes, String status,
			String due, String completed, int deleted,
			int hidden)
	{
		super();		
		this.taskListId = taskListId;
		this.remoteId = remoteId;
		this.kind = kind;
		this.title = title;
		this.selfLink = selfLink;
		this.parent = parent;
		this.position = position;
		this.notes = notes;
		this.status = status;
		this.due = due;
		this.completed = completed;
		this.deleted = deleted != 0;
		this.hidden = hidden != 0;
		
		mTags = new ArrayList<TCTag>();
		
		this.completed = isCompleted() ? this.completed : null;
	}	
	
	public void setTags(List<TCTag> tags)
	{
		mTags.clear();
		mTags = tags;
	}
	
	public List<TCTag> getTags()
	{
		return mTags;
	}	
	
	public Status getStatus()
	{
		Status val;
		try
		{
			val = Status.valueOf(status);
		}
		catch (IllegalArgumentException iaex) {
			 val = Status.needsAction;
		}
		return val;
	}	
	
	public void setStatus(Status val)
	{
		status = val.toString();
		completed = isCompleted() ? completed : null;
	}	
	
	public boolean isCompleted()
	{
		return getStatus() == Status.completed;
	}

	public static GooTask Convert(long taskListId, Task remoteTask, String eTag)
	{
		boolean deleted = remoteTask.deleted != null ? remoteTask.deleted : false;
		boolean hidden = remoteTask.hidden != null ? remoteTask.hidden : false;
		
		GooTask localTask = new GooTask(taskListId, remoteTask.id, remoteTask.kind,
				remoteTask.title, remoteTask.selfLink, remoteTask.parent,
				remoteTask.position, remoteTask.notes, remoteTask.status,
				remoteTask.due, remoteTask.completed, deleted,
				hidden);
		localTask.completed = localTask.isCompleted() ? localTask.completed : null;
		localTask.setETag(eTag);
		return localTask;
	}
	
	public boolean find(Tasks remoteTasks)
	{
		boolean ret = false;
		if(remoteTasks != null)
		{				
			for (Task remoteTask : remoteTasks.items) {
				if(remoteTask.id.equals(remoteId))
				{
					ret = true;
					break;
				}
			}	
		}
		return ret;
	}
}
