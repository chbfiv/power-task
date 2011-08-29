package com.mtelab.taskhack.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mtelab.taskhack.helpers.SharedPrefUtil;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.TCTag;
import com.mtelab.taskhack.models.TCTagItem;

public class TCTagMapOpenHelper extends GooBaseOpenHelper {
	
    private static final String TAG = SharedPrefUtil.class.getName();

	private final TCTagsOpenHelper dbTagsHelper;
	
    protected static final String TABLE_NAME = "tc_tagmap";
    protected static final String KEY_tagId = "tagId";
    protected static final String KEY_taskId = "taskId";
    
    protected static final String[] PROJECTION = new String[] {
    	KEY_tagId, // 0
    	KEY_taskId // 1
    };

    protected static final int INDEX_tagId = 0;
    protected static final int INDEX_taskId = 1;
    
    protected static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_tagId + " INTEGER NOT NULL, " +
                KEY_taskId + " INTEGER NOT NULL, " + 
                "UNIQUE(" + KEY_tagId + "," + KEY_taskId + ") ON CONFLICT REPLACE, " +
                "FOREIGN KEY(" + KEY_tagId + ") " + 
                "REFERENCES " + TCTagsOpenHelper.TABLE_NAME + "("+ TCTagsOpenHelper.KEY_id + ") ON DELETE CASCADE ON UPDATE CASCADE," +
			    "FOREIGN KEY(" + KEY_taskId + ") " + 
			    "REFERENCES " + GooTasksOpenHelper.TABLE_NAME + "("+ GooTasksOpenHelper.KEY_id + ") ON DELETE CASCADE ON UPDATE CASCADE);"; 
    
    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public TCTagMapOpenHelper(Context context) {
        super(context); 
        dbTagsHelper = new TCTagsOpenHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	super.onCreate(db);
    }    
    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
		super.onUpgrade(db, oldVersion, newVersion);
		
        // Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Kills the table and existing data
        //db.execSQL("DROP TABLE IF EXISTS notes");

        // Recreates the database with a new version
        //onCreate(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	public List<TCTag> query(long taskId) {
		initialize();
		List<TCTag> tagList = new ArrayList<TCTag>();
		Cursor c = null;
		try
		{
			String sql = " SELECT t._id AS id, t.created AS created, t.modified AS modified, t.name as tagName, t.color AS tagColor " +
				" FROM tc_tagmap AS tm, tc_tags AS t " +
				" WHERE t._id = tm.tagId AND tm.taskId = ? " +
				" GROUP BY t._id ";
			
			String[] args = new String[] { String.valueOf(taskId) };
			
			c = getDbReadOnly().rawQuery(sql, args);

			if(c != null && c.moveToFirst())
			{
				do
				{
					TCTag tag = new TCTag(c.getString(TCTagsOpenHelper.INDEX_name), c.getInt(TCTagsOpenHelper.INDEX_color));
					tag.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));					
					tagList.add(tag);
				}
				while(c.moveToNext());
			}	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		finally
		{
			if(c != null) c.close();			
		}
		return tagList;
	}	
	
	public List<TCTagItem> queryItems(long taskId) {
		initialize();
		List<TCTagItem> tagItemList = new ArrayList<TCTagItem>();
		Cursor c = null;
		try
		{
			String sql = " SELECT t._id AS id, t.created AS created, t.modified AS modified, t.name as tagName, t.color AS tagColor, SUM(CAST(tm.taskId = ? AS INTEGER)) AS checked " +
				" FROM tc_tags AS t " + 
				" LEFT OUTER JOIN tc_tagmap AS tm " +
				" ON t._id = tm.tagId " +
				" GROUP BY t._id ";
			
			String[] args = new String[] { String.valueOf(taskId) };
			
			c = getDbReadOnly().rawQuery(sql, args);

			if(c != null && c.moveToFirst())
			{
				do
				{
					TCTagItem tagItem = new TCTagItem(c.getString(TCTagsOpenHelper.INDEX_name), c.getInt(TCTagsOpenHelper.INDEX_color), c.getInt(5));
					tagItem.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));					
					tagItemList.add(tagItem);
				}
				while(c.moveToNext());
			}	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		finally
		{
			if(c != null) c.close();			
		}
		return tagItemList;
	}	
	
	public long replace(String tag, long taskId) 
	{		
		initialize();
		long ret = -1;
		long tagId = -1;
		try
		{
			//make sure tag exists
			TCTag localTag = dbTagsHelper.read(tag);	
			if(localTag == null)
			{
				tagId = dbTagsHelper.create(tag);
			}
			else
			{
				tagId = localTag.getId();
			}
			
			ret = replace(tagId, taskId);		
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public long create(TCTag tag, GooTask task) 
	{
		return create(tag.getId(), task.getId());
	}	
	
	public long create(long tagId, long taskId)
	{
		initialize();
		long ret = -1;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_tagId, tagId);
			 values.put(KEY_taskId, taskId);
			 ret = getDbReadWrite().insert(TABLE_NAME, null, values);	
		} 
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}	
	
	public long replace(long tagId, long taskId)
	{
		initialize();
		long ret = -1;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_tagId, tagId);
			 values.put(KEY_taskId, taskId);
			 ret = getDbReadWrite().replace(TABLE_NAME, null, values);	
		} 
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}	
	
	public boolean delete(String tag, long taskId) 
	{		
		initialize();
		boolean ret = false;
		long tagId = -1;
		try
		{
			//make sure tag exists
			TCTag localTag = dbTagsHelper.read(tag);	
			if(localTag == null)
			{
				tagId = dbTagsHelper.create(tag);
			}
			else
			{
				tagId = localTag.getId();
			}
			
			ret = delete(tagId, taskId);		
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean delete(long tagId, long taskId) {
		initialize();
		boolean ret = false;
		 try
		 {
			 ret = getDbReadWrite().delete(TABLE_NAME, KEY_tagId + " = " + tagId + " AND " + KEY_taskId + " = " + taskId , null) > 0;				
		 }
		 catch(SQLException sqle)
		 {
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());	   			
		 }
		 return ret;
     } 	
}