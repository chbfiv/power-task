package com.andorn.powertask.adapters;

import com.andorn.powertask.activities.GooTaskListsActivity;
import com.andorn.powertask.database.GooTaskListsOpenHelper;
import com.andorn.powertask.models.GooTaskList;
import com.andorn.powertask.R;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


public class GooTaskListsCursorAdapter extends CursorAdapter {

    private static final String TAG = GooTaskListsCursorAdapter.class.getName();
    
	private final static class ViewHolder {
    	View row;
    	TextView title;
    }

	private final GooTaskListsActivity mActivity;
    private final LayoutInflater inflater;
    
    public GooTaskListsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mActivity = (GooTaskListsActivity)context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
	}    

    public boolean requery()
    {
    	boolean ret = false;
    	try
    	{
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
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		catch(Exception ex)
		{
	    	  Log.e(TAG, "exception - " + ex.getMessage());				
		}
    	return ret;
    }
    
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
    	ViewHolder holder = (ViewHolder)view.getTag();
        holder.row.setOnLongClickListener(null);
        holder.row.setOnClickListener(null);
        
    	GooTaskList list = GooTaskListsOpenHelper.read(cursor);

        holder.row.setTag(list.getId());
        holder.title.setTag(list.getId());
        
        holder.title.setText(list.title);   
        holder.row.setOnClickListener(mActivity);
        holder.row.setOnLongClickListener(mActivity);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView = inflater.inflate(R.layout.task_list_item, null);
       	ViewHolder holder = new ViewHolder();
        holder.row = newView.findViewById(R.id.taskListItem_row);
        holder.title = (TextView) newView.findViewById(R.id.taskListItem_title);
        newView.setTag(holder);
		return newView;
	}
}
