package com.andorn.powertask.adapters;

import com.andorn.powertask.activities.GooAccountsActivity;
import com.andorn.powertask.database.GooAccountsOpenHelper;
import com.andorn.powertask.models.GooAccount;
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

public class GooAccountsCursorAdapter extends CursorAdapter {

    private static final String TAG = GooAccountsCursorAdapter.class.getName();
    
	private final static class ViewHolder {
    	View row;
    	TextView name;
    }

	private final GooAccountsActivity mActivity;
    private final LayoutInflater inflater;
    
    public GooAccountsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mActivity = (GooAccountsActivity)context;
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
        holder.row.setOnClickListener(null);
        
        GooAccount account = GooAccountsOpenHelper.read(cursor);

        holder.row.setTag(account.getId());
        holder.name.setTag(account.getId());

	    holder.name.setText(account.getName());     
        
	    holder.row.setOnClickListener(mActivity);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView = inflater.inflate(R.layout.account_list_item, null);
       	ViewHolder holder = new ViewHolder();
       	holder.row = newView.findViewById(R.id.accountListItem_row);
        holder.name = (TextView)newView.findViewById(R.id.accountListItem_name);
        newView.setTag(holder);
		return newView;
	}
}
