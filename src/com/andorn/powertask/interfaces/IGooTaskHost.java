package com.andorn.powertask.interfaces;

import com.andorn.powertask.database.GooTasksOpenHelper;

public interface IGooTaskHost {
	long getActiveTaskId();
	void setActiveTaskId(long taskId);
	GooTasksOpenHelper getDbhTasks();
	void onDbChange();
}
