package com.mtelab.taskhack.adapters;

import java.util.List;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.database.GooTaskListCollectionOpenHelper;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.services.TasksAppService;
import com.mtelab.taskhack.views.GooTaskListActivity;
import com.mtelab.taskhack.views.GooTaskListCollectionActivity;
import com.mtelab.taskhack.views.ManageAccountsActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class GooTaskListCursorAdapter extends ArrayAdapter<GooTaskList> {

    private final static class ViewHolder {
    	TextView view;
    }

	private final GooTaskListCollectionActivity mActivity;
    private final LayoutInflater inflater;
    
    public GooTaskListCursorAdapter(Context context, int textViewResourceId, List<GooTaskList> lists) {
		super(context, textViewResourceId, lists);
		mActivity = (GooTaskListCollectionActivity)context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}    
    
    public void set(List<GooTaskList> lists) {
        clear();
        for (GooTaskList list : lists) {
     	   add(list);
        }
        notifyDataSetChanged();
     }

    public long getItemId(int position) {
    	GooTaskList list = getItem(position);
        return list.getId();
    }
    
    public void create(GooTaskList taskList) {
    	if(taskList != null)
    	{
			 add(taskList);
			 notifyDataSetChanged();
    	}
    }
    
    public View getView(int position, View convertView, ViewGroup view) {
    	ViewHolder holder;
    	GooTaskList list = getItem(position);
    	
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list_item, null);

            holder = new ViewHolder();
            holder.view = (TextView) convertView.findViewById(R.id.taskListItem_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
            holder.view.setOnLongClickListener(null);
            holder.view.setOnClickListener(null);
        }

        holder.view.setTag(position);
        
        holder.view.setText(list.title);   
        holder.view.setOnClickListener(mActivity);
        holder.view.setOnLongClickListener(mActivity);
        
        return convertView;
    }
}
