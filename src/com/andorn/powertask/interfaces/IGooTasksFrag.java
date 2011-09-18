package com.andorn.powertask.interfaces;

import com.andorn.powertask.adapters.GooTasksCursorAdapter;

public interface IGooTasksFrag {
	GooTasksCursorAdapter getTasksAdapter();
	int getTaskSortType();
}
