package com.andorn.tasktags.interfaces;

import com.andorn.tasktags.database.GooTaskListsOpenHelper;
import com.andorn.tasktags.database.GooTasksOpenHelper;

public interface IGooTasksHost {
	long getActiveTaskListId();
	void setActiveTaskListId(long taskListId);
	GooTasksOpenHelper getDbhTasks();
	GooTaskListsOpenHelper getDbhTaskLists();
	void onDbChange();
}
