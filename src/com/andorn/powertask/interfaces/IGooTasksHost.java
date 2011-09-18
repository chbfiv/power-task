package com.andorn.powertask.interfaces;

import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.database.GooTasksOpenHelper;

public interface IGooTasksHost {
	long getActiveTaskListId();
	void setActiveTaskListId(long taskListId);
	GooTasksOpenHelper getDbhTasks();
	GooTaskListsOpenHelper getDbhTaskLists();
	void onDbChange();
}
