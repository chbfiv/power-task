package com.mtelab.taskhack.adapters;

import java.util.List;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.drawables.ColorStrip;
import com.mtelab.taskhack.helpers.DateTimeHelper;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.models.TCTag;
import com.mtelab.taskhack.views.ColorStripItem;
import com.mtelab.taskhack.views.GooTaskListActivity;
import com.mtelab.taskhack.views.ManageAccountsActivity;
import com.mtelab.taskhack.views.TCTagListActivity;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class GooTaskListAdapter extends ArrayAdapter<GooTask> {

    private final static class ViewHolder {
    	TextView title;
    	TextView notes;
    	TextView dueDate;
    	CheckBox statusCheckBox;
    	CheckBox starCheckBox;
    	LinearLayout colorStrip;
    }

	private final GooTaskListActivity mActivity;
    private final LayoutInflater inflater;
    private static float mScale;
    
    public GooTaskListAdapter(Context context, int textViewResourceId, List<GooTask> task) {
		super(context, textViewResourceId, task);
		mActivity = (GooTaskListActivity)context;
		mScale = mActivity.getResources().getDisplayMetrics().density; 
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}    
    
    public void set(List<GooTask> tasks) {
        clear();
        for (GooTask task : tasks) {
     	   add(task);
        }
        notifyDataSetChanged();
     }

    public long getItemId(int position) {
    	GooTask task = getItem(position);
        return task.getId();
    }

    public View getView(int position, View convertView, ViewGroup view) {
    	ViewHolder holder;
    	GooTask task = getItem(position);
    	
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_item, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.taskItem_title);
            holder.notes = (TextView) convertView.findViewById(R.id.taskItem_notes);
            holder.dueDate = (TextView) convertView.findViewById(R.id.taskItem_dueDate);
            holder.statusCheckBox = (CheckBox) convertView.findViewById(R.id.taskItem_statusCheckBox);
            holder.starCheckBox = (CheckBox) convertView.findViewById(R.id.taskItem_starCheckBox);
            holder.colorStrip = (LinearLayout) convertView.findViewById(R.id.taskItem_colorStrip);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
            holder.statusCheckBox.setOnCheckedChangeListener(null);
            holder.title.setOnClickListener(null);
            holder.title.setOnLongClickListener(null);
            holder.starCheckBox.setOnCheckedChangeListener(null);
        }

        holder.statusCheckBox.setTag(position);
        holder.title.setTag(position);
        holder.colorStrip.setTag(position);
        holder.starCheckBox.setTag(position);
        holder.notes.setTag(position);
        holder.dueDate.setTag(position);

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
        
        
        return convertView;
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
