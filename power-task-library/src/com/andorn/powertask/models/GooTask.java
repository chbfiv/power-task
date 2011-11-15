package com.andorn.powertask.models;

import java.util.Calendar;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

import com.andorn.powertask.helpers.DateTimeHelper;
import com.andorn.powertask.helpers.GeneralHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;

public class GooTask extends GooSyncBase {
	private static final String TAG = GooTask.class.getName();

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
	
	public GooTask(long taskListId)
	{
		this.taskListId = taskListId;	
		this.status = status != null ? status : Status.needsAction.toString();	
		this.completed = isCompleted() ? this.completed : null;
	}
	
	public GooTask(long taskListId, String remoteId, String kind,
			String title, String selfLink, String parent,
			String position, String notes, String status,
			DateTime due, DateTime completed, Boolean deleted,
			Boolean hidden)
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
		this.status = status != null ? status : Status.needsAction.toString();
		this.due = due != null ? due.toStringRfc3339() : null;
		this.completed = completed != null ? completed.toStringRfc3339() : null;
		this.deleted = deleted != null ? deleted : false;
		this.hidden = hidden != null ? hidden : false;
		
		this.completed = isCompleted() ? this.completed : null;
	}	
	
	public GooTask(long taskListId, String remoteId, String kind,
			String title, String selfLink, String parent,
			String position, String notes, String status,
			DateTime due, DateTime completed, int deleted,
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
		this.status = status != null ? status : Status.needsAction.toString();
		this.due = due != null ? due.toStringRfc3339() : null;
		this.completed = completed != null ? completed.toStringRfc3339() : null;
		this.deleted = deleted != 0;
		this.hidden = hidden != 0;
		
		this.completed = isCompleted() ? this.completed : null;
	}		
	
	public Status getStatus()
	{
		Status val;
		try
		{
			val =  status != null ? Status.valueOf(status) : Status.needsAction;
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

	public boolean hasDueDate()
	{
		return DateTimeHelper.isRFC3339Date(due);
	}
	
	public Calendar getDueDate()
	{
		return DateTimeHelper.parseDateRFC3339(due);	
	}
	
	public void setDueDate(int year, int month, int day)
	{
		try
		{
			due = DateTimeHelper.formatDateRFC3339(year, month, day);
		}
		catch(TimeFormatException tfex)
		{
	        Log.w(TAG, tfex);	
		}
	}
	
	public void setDueDate(Calendar date)
	{
		try
		{
			Time t = new Time();
			t.set(date.getTimeInMillis());
			due = t.format3339(false);
		}
		catch(TimeFormatException tfex)
		{
	        Log.w(TAG, tfex);	
		}
	}
	
	public void setDueDate(String date)
	{
		try
		{
			if(DateTimeHelper.isRFC3339Date(date))
			{
				due = date;				
			}
			else
			{
				Time t = new Time();
				t.parse3339(date);
				due = t.format3339(false);
			}
		}
		catch(TimeFormatException tfex)
		{
	        Log.w(TAG, tfex);	
		}
	}
	
	public boolean hasNotes()
	{
		return notes != null && notes.length() > 0;
	}
	
	public static GooTask Convert(long taskListId, Task remoteTask, String eTag)
	{		
		GooTask localTask = new GooTask(taskListId, remoteTask.getId(), remoteTask.getKind(),
				remoteTask.getTitle(), remoteTask.getSelfLink(), remoteTask.getParent(),
				remoteTask.getPosition(), remoteTask.getNotes(), remoteTask.getStatus(),
				remoteTask.getDue(), remoteTask.getCompleted(),
				remoteTask.getDeleted(), remoteTask.getHidden());
		localTask.completed = localTask.isCompleted() ? localTask.completed : null;
		localTask.setETag(eTag);
		return localTask;
	}
	
	public static boolean find(String remoteId, Tasks remoteTasks)
	{				
		if(GeneralHelper.isNullOrEmpty(remoteId)) return false;
		if(remoteTasks == null) return false;

		boolean ret = false;
		
		for (Task remoteTask : remoteTasks.getItems()) {
			if(remoteTask.getId().equals(remoteId))
			{
				ret = true;
				break;
			}
		}			
		return ret;
	}	
	
	public static boolean shouldDelete(String remoteId, Tasks remoteTasks)
	{
		if(GeneralHelper.isNullOrEmpty(remoteId)) return false;
		if(remoteTasks == null || remoteTasks.getItems() == null) return false;
		return !find(remoteId, remoteTasks);
	}
}
