package com.andorn.powertask.loaders;

import android.content.Context;
import android.database.Cursor;

public class GooTasksLoader extends DbCursorLoader {

	public GooTasksLoader(Context context) {
		super(context);
		
	}

	@Override
	public Cursor loadInBackground() {
		return null;
	}

}
