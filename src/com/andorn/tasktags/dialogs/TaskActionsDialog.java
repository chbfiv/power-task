package com.andorn.tasktags.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.R;

public class TaskActionsDialog extends Dialog {
	private static final String TAG = TaskActionsDialog.class.getName();

	public static final int DIALOG_ID = 48911;
    public static final String EXTRA_TASK_ID = "taskId";

	private Context mContext;
	private long mTaskId = GooBase.INVALID_ID;

	public TaskActionsDialog(Context context)
	{
		super(context);
		mContext = context;
	}
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			View v = LayoutInflater.from(mContext).inflate(R.layout.task_actions_dialog, null);
			setContentView(v);
			
//			LayoutParams params = getWindow().getAttributes(); 
//            params.width = LayoutParams.FILL_PARENT; 
//            getWindow().setAttributes(params);
            
            
		} catch (Exception ex) {
			Log.e(TAG, "Exception " + ex.getMessage());
		}
	}
	
	public void setTaskId(long taskId)
	{
		mTaskId = taskId;
		if(mTaskId == GooBase.INVALID_ID)
		{
			Log.e(TAG, "this dialog is required to have a bundle taskId Extra.");			
			dismiss();			
		}
	}
	
	public long getTaskId()
	{
		return mTaskId;
	}
}
