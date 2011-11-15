package com.andorn.powertask.interfaces;

public interface IGooTasksHost {
	long getActiveTaskListId();
	void setActiveTaskListId(long taskListId);
	void refresh();
}
