package com.andorn.tasktags.adapters;

import java.util.List;

import com.andorn.tasktags.activities.TCTagListActivity;
import com.andorn.tasktags.drawables.ColorPicker;
import com.andorn.tasktags.models.TCTagItem;
import com.andorn.tasktags.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

public class TCTagListAdapter extends ArrayAdapter<TCTagItem> {
	
	private static final String TAG = TCTagListAdapter.class.getName();
	
    private final static class ViewHolder {
    	TextView name;
    	CheckBox checkbox;
    	ImageButton imgButton;
    	ColorPicker shape;
    }
    
    private static float mScale;
 
	private final TCTagListActivity mActivity;
    private final LayoutInflater inflater;
    
    public TCTagListAdapter(Context context, int textViewResourceId, List<TCTagItem> tagItem) {
		super(context, textViewResourceId, tagItem);
		mActivity = (TCTagListActivity)context;
		mScale = mActivity.getResources().getDisplayMetrics().density; 
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}    
    
    public void set(List<TCTagItem> tagItems) {
        clear();
        for (TCTagItem tagItem : tagItems) {
     	   add(tagItem);
        }
        notifyDataSetChanged();
     }

    public long getItemId(int position) {
    	TCTagItem tagItem = getItem(position);
        return tagItem.getId();
    }

    public View getView(int position, View convertView, ViewGroup view) {
    	ViewHolder holder;
    	TCTagItem tagItem = getItem(position);
    	
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.tag_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tagItem_name);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.tagItem_checkBox);
            holder.imgButton = (ImageButton) convertView.findViewById(R.id.tagItem_colorPicker);
            holder.shape = new ColorPicker(tagItem.getColor(), mScale);
            holder.imgButton.setImageDrawable(holder.shape);
            
//            holder.shape =  (GradientDrawable) holder.imgButton.getResources().getDrawable(R.drawable.tag_color_full);
            convertView.setTag(holder);
            holder.checkbox.setOnCheckedChangeListener(null);
        } else {
            holder = (ViewHolder)convertView.getTag();
            holder.checkbox.setOnCheckedChangeListener(null);
        }
        
        holder.name.setTag(position);
        holder.name.setText(tagItem.getName());  
        
        holder.checkbox.setTag(position);
        holder.checkbox.setChecked(tagItem.isChecked());   
        holder.checkbox.setOnCheckedChangeListener(mActivity);

        holder.shape.setColor(tagItem.getColor());

        return convertView;
    }
}
