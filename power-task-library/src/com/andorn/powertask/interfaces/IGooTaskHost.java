package com.andorn.powertask.interfaces;

public interface IGooTaskHost {
	long getActiveTaskId();
	void setActiveTaskId(long taskId);
	void refresh();
}
