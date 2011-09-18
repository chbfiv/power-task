package com.andorn.powertask.database;

import com.andorn.powertask.helpers.ColorHelper;
import com.andorn.powertask.helpers.SharedPrefUtil;
import com.andorn.powertask.models.TCTag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TCTagsOpenHelper extends GooBaseOpenHelper {
	
    private static final String TAG = SharedPrefUtil.class.getName();

    protected static final String TABLE_NAME = "tc_tags";
    protected static final String KEY_name = "name";
    protected static final String KEY_color = "color";
    
    protected static final String[] PROJECTION = new String[] {
    	KEY_id, // 0
    	KEY_created, // 1
    	KEY_modified, // 2
    	KEY_name, // 3
    	KEY_color // 4
    };

    protected static final int INDEX_name = 3;
    protected static final int INDEX_color = 4;
    
    protected static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_created + " NUMERIC, " +
                KEY_modified + " NUMERIC, " +
                KEY_name + " TEXT, " +
    			KEY_color + " INTEGER);"; 
    
    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public TCTagsOpenHelper(Context context) {
        super(context);
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
	
	public TCTag read(String name) {
		initialize();
		TCTag tag = null;
		Cursor c = null;
		try
		{ 
			name = formatTagName(name);
			c = getDbReadOnly().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_name + " = ? ",     // The columns for the where clause
						new String[] { name }, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				tag = new TCTag(c.getString(INDEX_name), c.getInt(INDEX_color));
				tag.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
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
		return tag;
	}
	
	public TCTag read(long id) {
		initialize();
		TCTag tag = null;
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_id + " = " + id,     // The columns for the where clause
			           null, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				tag = new TCTag(c.getString(INDEX_name), c.getInt(INDEX_color));
				tag.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
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
		return tag;
	}

	public long create(TCTag tag) 
	{
		return create(tag.getName());
	}	
	
	public long create(String name) 
	{
		initialize();
		long ret = -1;
		try
		{
			 name = formatTagName(name);
			 int color = name.equals("blue-star") ? 0xff6699ff : ColorHelper.getRandomColor();
			 
			 TCTag tag = read(name);
			 
			 if(tag == null)
			 {			 
				 ContentValues values = new ContentValues();
				 values.put(KEY_name, name);
				 values.put(KEY_color, color);
				 ret = getDbReadWrite().insert(TABLE_NAME, null, values);
			 }
			 else
			 {
				 ret = tag.getId();
			 }
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}	

	public boolean update(TCTag tag) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 String name = formatTagName(tag.getName());
			 ContentValues values = new ContentValues();
			 values.put(KEY_name, name);
			 values.put(KEY_color, tag.getColor());
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + tag.getId(), null) > 0;				
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean delete(long rowId) {
		initialize();
		boolean ret = false;
		 try
		 {
			 ret = getDbReadWrite().delete(TABLE_NAME, KEY_id + " = " + rowId, null) > 0;				
		 }
		 catch(SQLException sqle)
		 {
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());	   			
		 }
		 return ret;
     } 
	
	public static String formatTagName(String name)
	{
		name = name.trim();
		name = name.replace(' ','-');
		name = name.toLowerCase();		
		return name;
	}	
}