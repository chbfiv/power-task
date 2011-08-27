package com.mtelab.taskhack.adapters;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.database.GooTaskListsOpenHelper;
import com.mtelab.taskhack.database.GooTasksOpenHelper;
import com.mtelab.taskhack.helpers.DateTimeHelper;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.models.TCTag;
import com.mtelab.taskhack.views.ColorStripItem;
import com.mtelab.taskhack.views.GooTaskListsActivity;
import com.mtelab.taskhack.views.GooTasksActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class GooTasksCursorAdapter extends CursorAdapter {

    private static final String TAG = GooTasksCursorAdapter.class.getName();
    
	private final static class ViewHolder {
    	TextView title;
    	TextView notes;
    	TextView dueDate;
    	CheckBox statusCheckBox;
    	CheckBox starCheckBox;
    	LinearLayout colorStrip;
    }

	private final GooTasksActivity mActivity;
    private final LayoutInflater inflater;
    private static float mScale;
    
    public GooTasksCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mActivity = (GooTasksActivity)context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		mScale = mActivity.getResources().getDisplayMetrics().density; 	
	}    

    public boolean requery()
    {
    	boolean ret = false;
    	Cursor c = getCursor();
    	if(c != null)
    	{
    		ret = c.requery();    		
    	}
    	else
    	{
    	    Log.e(TAG, "requery null");	
    		throw new NullPointerException();
    	}
    	return ret;
    }
    
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
    	ViewHolder holder = (ViewHolder)view.getTag();
        holder.statusCheckBox.setOnCheckedChangeListener(null);
        holder.title.setOnClickListener(null);
        holder.title.setOnLongClickListener(null);
        holder.starCheckBox.setOnCheckedChangeListener(null);

    	GooTask task = GooTasksOpenHelper.read(cursor);

        holder.statusCheckBox.setTag(task.getId());
        holder.title.setTag(task.getId());
        holder.colorStrip.setTag(task.getId());
        holder.starCheckBox.setTag(task.getId());
        holder.notes.setTag(task.getId());
        holder.dueDate.setTag(task.getId());
        
        holder.starCheckBox.setChecked(hasBlueStarTag(task));   
        holder.starCheckBox.setOnCheckedChangeListener(mActivity);
        
        holder.statusCheckBox.setChecked(task.isCompleted());   
        holder.statusCheckBox.setOnCheckedChangeListener(mActivity);
        
        holder.title.setText(task.title);      		
        holder.title.setOnClickListener(mActivity);
        holder.title.setOnLongClickListener(mActivity);
        if(task.isCompleted())
        	holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    	else
        	holder.title.setPaintFlags(holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);  

        if(task.getTags().size() > 0)
        {
        	holder.colorStrip.removeAllViews();
        	 for (TCTag tag : task.getTags()) {
        		 ColorStripItem colorstripItem = new ColorStripItem(mActivity, tag.getColor(), mScale);
        		 holder.colorStrip.addView(colorstripItem);
        	 }
        	holder.colorStrip.setVisibility(View.VISIBLE);   
        }
        else
        {
        	holder.colorStrip.setVisibility(View.GONE);        	
        }
                
        if(task.notes != null && task.notes.length() > 0)
        {
        	holder.notes.setText(task.notes);
        	holder.notes.setVisibility(View.VISIBLE);
        }
        else
        {
        	holder.notes.setVisibility(View.GONE);
        }        

        if(task.due != null && task.due.length() > 0)
        {
        	holder.dueDate.setText(DateTimeHelper.prettyDueDate(task.due));
        	holder.dueDate.setVisibility(View.VISIBLE);
        }
        else
        {
        	holder.dueDate.setVisibility(View.GONE);
        }   
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView = inflater.inflate(R.layout.task_item, null);
       	ViewHolder holder = new ViewHolder();
        holder.title = (TextView) newView.findViewById(R.id.taskItem_title);
        holder.notes = (TextView) newView.findViewById(R.id.taskItem_notes);
        holder.dueDate = (TextView) newView.findViewById(R.id.taskItem_dueDate);
        holder.statusCheckBox = (CheckBox) newView.findViewById(R.id.taskItem_statusCheckBox);
        holder.starCheckBox = (CheckBox) newView.findViewById(R.id.taskItem_starCheckBox);
        holder.colorStrip = (LinearLayout) newView.findViewById(R.id.taskItem_colorStrip);
        newView.setTag(holder);
		return newView;
	}
    
    private boolean hasBlueStarTag(GooTask task)
    {
    	boolean ret = false;
    	for (TCTag tag : task.getTags()) {
    		if(tag.getName().equalsIgnoreCase("blue-star"))
    			return true;
    	}
    	return ret;
    }  
}
