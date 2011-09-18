package com.andorn.powertask.database;

import java.util.ArrayList;
import java.util.List;

import com.andorn.powertask.models.GooAccount;
import com.andorn.powertask.models.GooBase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GooAccountsOpenHelper extends GooSyncBaseOpenHelper {
	
    private static final String TAG = GooAccountsOpenHelper.class.getName();

    private static final String TABLE_NAME = "goo_accounts";
    private static final String KEY_name = "name";
    private static final String KEY_type = "type";
    private static final String KEY_sync = "sync";
    private static final String KEY_authToken = "authToken";
    
    private static final String[] PROJECTION = new String[] {
    	KEY_id, // 0
    	KEY_created, // 1
    	KEY_modified, // 2
    	KEY_name, // 3
    	KEY_type, // 4
    	KEY_sync, // 5
    	KEY_authToken, // 6
    };

    protected static final int INDEX_name = 3;
    protected static final int INDEX_type = 4;
    protected static final int INDEX_sync = 5;
    protected static final int INDEX_authToken = 6;
    
    private static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_created + " NUMERIC, " +
                KEY_modified + " NUMERIC, " +
                KEY_name + " TEXT, " +
                KEY_type + " TEXT, " +
                KEY_sync + " INTEGER, " +
                KEY_authToken + " TEXT);"; 
    
    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public GooAccountsOpenHelper(Context context) {
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
	//"\"SR04dT8X4VWULOsDkL4X4Vd0UDQ/1IJzu4yfnShYlBKFS9vA4Irm3Dk\""
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	public List<GooAccount> query() {
		initialize();
		List<GooAccount> list = new ArrayList<GooAccount>();
		Cursor c = null;
		
		try
		{
			c = queryCursor();
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooAccount acc = read(c);
					list.add(acc);
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
		return list;
	}
	
	public Cursor queryCursor() {
		initialize();
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
					TABLE_NAME,            // The database to query
					PROJECTION,    // The columns to return from the query
		           null,     // The columns for the where clause
		           null, // The values for the where clause
		           null,          // don't group the rows
		           null,          // don't filter by row groups
		           null        // The sort order
		       );
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return c;
	}
	
	public static GooAccount read(Cursor c) {
		GooAccount acc = null;
		try
		{
			if(c != null)
			{
				boolean sync = c.getInt(INDEX_sync) > 0;
				acc = new GooAccount(c.getLong(INDEX_id), c.getString(INDEX_name), c.getString(INDEX_type), sync);
				acc.setCreated(c.getLong(INDEX_created));
				acc.setModified(c.getLong(INDEX_modified));
				acc.setAuthToken(c.getString(INDEX_authToken));
			}
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return acc;
	}
	
	public GooAccount read(long id) {
		initialize();
		GooAccount acc = null;
		Cursor c = null;
		
		try
		{
			c = getDbReadOnly().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_id + " = ? ",     // The columns for the where clause
						new String[] { String.valueOf(id) }, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				acc = read(c);
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
		return acc;
	}
	
	public GooAccount findAccountByName(String name)
	{
		initialize();
		GooAccount acc = null;
		Cursor c = null;
		
		try
		{
			c =	getDbReadOnly().query(
					TABLE_NAME,            // The database to query
					PROJECTION,    // The columns to return from the query
					KEY_name + " = ?",     // The columns for the where clause
					new String[] { name }, // The values for the where clause
		           null,          // don't group the rows
		           null,          // don't filter by row groups
		           null        // The sort order
		       );
			if(c != null && c.moveToFirst())
			{
				acc = read(c);
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
		return acc;		
	}
	
	public long create(String name, String type, boolean sync) 
	{
		return create(new GooAccount(name, type, sync));
	}
	
	public long create(GooAccount account) 
	{
		initialize();
		long rowId = GooBase.INVALID_ID;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_name, account.getName());
			 values.put(KEY_type, account.getType());
			 values.put(KEY_sync, account.getSync());
			 values.put(KEY_authToken, account.getAuthToken());
			 rowId = getDbReadWrite().insert(TABLE_NAME, null, values);			
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return rowId;	
	}
	
	public boolean update(long id, String name, String type, boolean sync) 
	{
		return update(new GooAccount(id, name, type, sync));
	}
	
	public boolean update(GooAccount account) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_name, account.getName());
			 values.put(KEY_type, account.getType());
			 values.put(KEY_sync, account.getSync());
			 values.put(KEY_authToken, account.getAuthToken());
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + account.getId(), null) > 0;			
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}	
		return ret;
	}
	
	public boolean update(long id, String authToken) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_authToken, authToken);
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + id, null) > 0;		
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}	
		return ret;		
	}
	
	public boolean update(long id, boolean sync) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_sync, sync);
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + id, null) > 0;		
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
}