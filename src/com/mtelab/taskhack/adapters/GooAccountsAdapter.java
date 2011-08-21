package com.mtelab.taskhack.adapters;

import java.util.List;

import com.mtelab.taskhack.R;
import com.mtelab.taskhack.models.GooAccount;
import com.mtelab.taskhack.views.ManageAccountsActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;


public class GooAccountsAdapter extends ArrayAdapter<GooAccount> {

    private final static class ViewHolder {
    	TextView name;
    	CheckBox checkbox;
    	TextView info;
    }    

    @SuppressWarnings("unused")
	private final ManageAccountsActivity mActivity;
    private final LayoutInflater inflater;
    
    public GooAccountsAdapter(Context context, int textViewResourceId, List<GooAccount> accounts) {
		super(context, textViewResourceId, accounts);
		mActivity = (ManageAccountsActivity)context;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}    
    
    public void set(List<GooAccount> accounts) {
       clear();
       for (GooAccount account : accounts) {
    	   add(account);
       }
       notifyDataSetChanged();
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup view) {
    	ViewHolder holder;
    	GooAccount account = getItem(position);
    	
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.account, null);

            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.accountName);
            holder.checkbox = (CheckBox)convertView.findViewById(R.id.accountItem);
            holder.info = (TextView)convertView.findViewById(R.id.accountInfo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(account.getName());   
        holder.name.setTag(position);
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(account.getSync());   
        holder.checkbox.setTag(position);        
        String msg = account.getSync() ? "Authorized for Sync." : "Needs authorization for Sync.";
        holder.info.setText(msg);   
        
        holder.name.setOnClickListener(mActivity);
        holder.checkbox.setOnCheckedChangeListener(mActivity);
        return convertView;
    }
}
