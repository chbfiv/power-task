package com.andorn.powertask.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.R;

public class TaskListActionsDialog extends Dialog {
	private static final String TAG = TaskListActionsDialog.class.getName();

	public static final int DIALOG_ID = 489551;
    public static final String EXTRA_TASK_LIST_ID = "taskListId";

	private Context mContext;
	private long mTaskListId = GooBase.INVALID_ID;

	public TaskListActionsDialog(Context context)
	{
		super(context);
		mContext = context;
	}
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			View v = LayoutInflater.from(mContext).inflate(R.layout.task_list_actions_dialog, null);
			
//			LayoutParams params = getWindow().getAttributes(); 
//            params.width = LayoutParams.FILL_PARENT; 
//            getWindow().setAttributes(params);

			setContentView(v);            
		} catch (Exception ex) {
			Log.e(TAG, "Exception " + ex.getMessage());
		}
	}
	
	public void setTaskListId(long taskListId)
	{
		mTaskListId = taskListId;
		if(mTaskListId == GooBase.INVALID_ID)
		{
			Log.e(TAG, "this dialog is required to have a bundle taskListId Extra.");			
			dismiss();			
		}
	}
	
	public long getTaskListId()
	{
		return mTaskListId;
	}
}
