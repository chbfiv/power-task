package com.mtelab.tasktags.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.mtelab.tasktags.R;
import com.mtelab.tasktags.TaskApplication;
import com.mtelab.tasktags.adapters.TCTagListAdapter;
import com.mtelab.tasktags.database.GooTasksOpenHelper;
import com.mtelab.tasktags.database.TCTagMapOpenHelper;
import com.mtelab.tasktags.database.TCTagsOpenHelper;
import com.mtelab.tasktags.models.GooBase;
import com.mtelab.tasktags.models.GooTask;
import com.mtelab.tasktags.models.TCTagItem;

public class TCTagListActivity extends Activity implements
		OnItemClickListener, OnCheckedChangeListener {
	
	private static final String TAG = TCTagListActivity.class.getName();
	
	private final GooTasksOpenHelper dbTLHelper = new GooTasksOpenHelper(this);
	private final TCTagMapOpenHelper dbTagMapHelper = new TCTagMapOpenHelper(this);
	private final TCTagsOpenHelper dbTagsHelper = new TCTagsOpenHelper(this);

	public static final String EXTRA_TASK_ID = "taskId";
	
	private ListView listView;
	private TCTagListAdapter adapter;
	
	private static long mTaskId = GooBase.INVALID_ID;
	public final static int REQUEST_TAGS = 900;
	
	public static final int DIALOG_CREATE_TAG = 4411;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	if(!dbTLHelper.initialize() || !dbTagMapHelper.initialize() || !dbTagsHelper.initialize())
    	{
    		Log.e(TAG, "onCreate - db failed to initialize.");
    		return;    		
    	}
    	
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e(TAG, "onCreate - failed to get intent bundle.");
    		finish();
		}
    	
		mTaskId = extras.getLong(EXTRA_TASK_ID, GooBase.INVALID_ID);
		    	
    	GooTask task = dbTLHelper.read(mTaskId);
    	if(task == null)
    	{
    		Log.e(TAG, "onCreate - task is null");
    		finish();
    	}    	
    	
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View headerContainer = inflater.inflate(R.layout.tag_item, null);
		TextView header = (TextView)headerContainer.findViewById(R.id.tagItem_name);
		CheckBox options = (CheckBox)headerContainer.findViewById(R.id.tagItem_checkBox);
		ImageButton imgBtn = (ImageButton)headerContainer.findViewById(R.id.tagItem_colorPicker);
		imgBtn.setVisibility(View.INVISIBLE);
		headerContainer.setEnabled(false);
		header.setText("Create a new Tag");
		options.setEnabled(false);
		options.setVisibility(View.INVISIBLE);
		header.setClickable(true);
		header.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CREATE_TAG);	
			}
		});
		
		
		setContentView(R.layout.tag_list);

		listView = (ListView) findViewById(R.id.tag_list);

		listView.addHeaderView(headerContainer);
		
		TaskApplication app = (TaskApplication) getApplication();
		adapter = app.getTagListAdapter(this);
		listView.setAdapter(adapter);	    

		listView.setOnItemClickListener(this);			
		
		adapter.set(dbTagMapHelper.queryItems(mTaskId));
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dbTLHelper != null) dbTLHelper.close(); 
		if (dbTagMapHelper != null) dbTagMapHelper.close(); 
		if (dbTagsHelper != null) dbTagsHelper.close(); 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case DIALOG_CREATE_TAG:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	final EditText input = new EditText(this); 
	    	input.setHint("Tag name");
	    	builder.setMessage("New Tag")
	    		   .setView(input)
	    	       .setCancelable(false)
	    	       .setPositiveButton("Save", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   dbTagsHelper.create(input.getText().toString());	    	       		   
	    	        	   adapter.set(dbTagMapHelper.queryItems(mTaskId));
	    	        	   input.getText().clear();
	    	        	   dialog.dismiss();
	    	           }
	    	       })
	    	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                dialog.cancel();
	    	           }
	    	       });
	    	dialog = builder.create();
	    	break;
	    default:
	        dialog = null;
	        break;
	    }
	    return dialog;
	}	

	public static void go(Activity activity, long taskId) {		
		final Intent intent = new Intent(activity, TCTagListActivity.class);
		intent.putExtra(EXTRA_TASK_ID, taskId);
		activity.startActivityForResult(intent, REQUEST_TAGS);  
		activity.overridePendingTransition(R.anim.fade, R.anim.hold);
	}
	
	// List Actions

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		System.out.print("ASDasd");
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		int position = (Integer)v.getTag();
		if(position != ListView.INVALID_POSITION)
		{
			TCTagItem tagItem = adapter.getItem(position);
//			
			//task add/remove Star Tag
			if(isChecked)
				dbTagMapHelper.replace(tagItem.getId(), mTaskId);
			else
				dbTagMapHelper.delete(tagItem.getId(), mTaskId);			
//			
			tagItem.setChecked(isChecked);
			adapter.notifyDataSetChanged();

//			adapter.set(dbTagMapHelper.queryItems(mTaskId));
		}
	}
}
