package com.mtelab.taskhack.models;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.v1.model.Task;

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
	}	
	
	public boolean isCompleted()
	{
		return getStatus() == Status.completed;
	}
	
	public Task Sync(Task task)
	{	
		task.title = title;
		task.notes = notes;
		task.status = status;
		task.due = due;
		task.completed = isCompleted() ? completed : null;
		return task;
	}
}
