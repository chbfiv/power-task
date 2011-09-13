package com.andorn.tasktags.interfaces;

import com.andorn.tasktags.database.GooTasksOpenHelper;

public interface IGooTaskHost {
	long getActiveTaskId();
	void setActiveTaskId(long taskId);
	GooTasksOpenHelper getDbhTasks();
	void onDbChange();
}
