package com.andorn.tasktags.fragments;

import com.andorn.tasktags.R;
import com.andorn.tasktags.interfaces.IGooTasksFrag;
import com.andorn.tasktags.interfaces.IGooTasksHost;
import com.andorn.tasktags.models.GooBase;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActionsDialogFragment extends DialogFragment {
	
	private static final String TAG = TaskActionsDialogFragment.class.getName();
	
    public static final String EXTRA_TASK_ID = "taskId";
    public static final String FRAG_TAG_DIALOG = "dialog";
    
	private long mTaskId = GooBase.INVALID_ID;

	private FragmentActivity mActivity;
	private Fragment mFragment;
	
	private TextView mReadTextView;
	private TextView mEditTextView;
	private TextView mDeleteTextView;
	private TextView mChangeTagsTextView;
	private TextView mHelpTextView;
	
	public TaskActionsDialogFragment()
	{		
		
	}
	
	public TaskActionsDialogFragment(Fragment fragment)
	{		
		mFragment = fragment;
		mActivity = fragment.getActivity();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskId = getArguments().getLong(EXTRA_TASK_ID);
        
        setStyle(STYLE_NO_TITLE, 0);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View v = inflater.inflate(R.layout.fragment_dialog_task_actions, null);
    	mReadTextView = (TextView) v.findViewById(R.id.taskAction_read);
    	mEditTextView = (TextView) v.findViewById(R.id.taskAction_edit);
    	mDeleteTextView = (TextView) v.findViewById(R.id.taskAction_delete);
    	mChangeTagsTextView = (TextView) v.findViewById(R.id.taskAction_changeTags);
    	mHelpTextView = (TextView) v.findViewById(R.id.taskAction_help);
    	
    	mReadTextView.setOnClickListener(new ReadClick());
    	mEditTextView.setOnClickListener(new EditClick());
    	mDeleteTextView.setOnClickListener(new DeleteClick());
    	mChangeTagsTextView.setOnClickListener(new ChangeTagsClick());
    	mHelpTextView.setOnClickListener(new HelpClick());
        return v;
    }
    
    public IGooTasksHost host()
    {    	
    	return BaseFragment.<IGooTasksHost>host(mActivity);
    }
    
    public IGooTasksFrag frag()
    {    	
    	return BaseFragment.<IGooTasksFrag>frag(mFragment);
    }
    
    public static TaskActionsDialogFragment create(Fragment fragment, long taskId) {
    	TaskActionsDialogFragment f = new TaskActionsDialogFragment(fragment);
        Bundle args = new Bundle();
        args.putLong(EXTRA_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }
    
    public static DialogFragment showDialog(Fragment fragment, long taskId) {
    	
		FragmentActivity activity = fragment.getActivity();
		
	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
	    Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(FRAG_TAG_DIALOG);
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    // Create and show the dialog.
	    DialogFragment newFragment = create(fragment, taskId);
	    newFragment.show(ft, FRAG_TAG_DIALOG);
	    return newFragment;
	}
    
    private class ReadClick implements OnClickListener {
		@Override
		public void onClick(View v) {
//			mTaskActionsDialog.dismiss();
//			GooTaskViewActivity.go(this, false, mTaskActionsDialog.getTaskId());	
			dismiss();			
		}
	}
    
    private class EditClick implements OnClickListener {
		@Override
		public void onClick(View v) {
//			mTaskActionsDialog.dismiss();
//			GooTaskEditActivity.go(this, false, mTaskActionsDialog.getTaskId());
			dismiss();				
		}
	}
    
    private class DeleteClick implements OnClickListener {
		@Override
		public void onClick(View v) {
     	    new DeleteTask().execute(mTaskId);
			dismiss();		
		}
	}
    
    private class ChangeTagsClick implements OnClickListener {
		@Override
		public void onClick(View v) {
		    Toast.makeText(mActivity, "this code is under development", Toast.LENGTH_SHORT).show();
			dismiss();		
		}
	}
    
    private class HelpClick implements OnClickListener {
		@Override
		public void onClick(View v) {
		    Toast.makeText(mActivity, "this code is under development", Toast.LENGTH_SHORT).show();
			dismiss();		
		}
	}
    
	private class DeleteTask extends AsyncTask<Long, Void, Boolean> {
	     protected Boolean doInBackground(Long... ids) {
	    	 Boolean ret = true;
	    	 try
	    	 {
		    	 for (Long id : ids)
		    	 {
		    		 GooTask task = host().getDbhTasks().read(id);
		    		 task.setSyncState(GooSyncBase.SYNC_DELETE);
		    		 host().getDbhTasks().update(task);
		    	 }   		 
	    	 }
	    	 catch(Exception ex)
	    	 {
	    		 ret = false;
	    	 }
			return ret;	    	 
	     }
	
	     protected void onPostExecute(Boolean result) {	    	 
	    	 frag().getTasksAdapter().requery();
	     }
	}
}
