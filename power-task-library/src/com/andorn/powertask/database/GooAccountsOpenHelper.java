package com.andorn.powertask.database;

import java.util.ArrayList;
import java.util.List;

import com.andorn.powertask.models.GooAccount;
import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.services.TasksAppService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GooAccountsOpenHelper extends GooSyncBaseOpenHelper {
	
    private static final String TAG = GooAccountsOpenHelper.class.getName();

    protected static final String TABLE_NAME = "goo_accounts";
    protected static final String KEY_name = "name";
    protected static final String KEY_type = "type";
    protected static final String KEY_sync = "sync";
    protected static final String KEY_authToken = "authToken";
    
    protected static final String[] PROJECTION = new String[] {
    	KEY_id, // 0
    	KEY_created, // 1
    	KEY_modified, // 2
    	KEY_syncState, // 3
    	KEY_eTag, // 4
    	KEY_name, // 5
    	KEY_type, // 6
    	KEY_sync, // 7
    	KEY_authToken // 8
    };

    protected static final int INDEX_name = 5;
    protected static final int INDEX_type = 6;
    protected static final int INDEX_sync = 7;
    protected static final int INDEX_authToken = 8;
    
    private static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_created + " NUMERIC, " +
                KEY_modified + " NUMERIC, " +
                KEY_syncState + " INTEGER, " +
                KEY_eTag + " TEXT, " +
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

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
	
	public List<GooAccount> query() {
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
				acc.setSync(c.getInt(INDEX_syncState), c.getString(INDEX_eTag));
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
		long rowId = GooBase.INVALID_ID;
		
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_eTag, account.getETag());
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
	
	public boolean update(GooAccount account) 
	{
		boolean ret = false;
		
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_eTag, account.getETag());
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
	
	public boolean updateAuthToken(long id, String authToken) 
	{
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
	
	public boolean updateETag(long id, String eTag) 
	{
		boolean ret = false;
		
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_eTag, eTag);
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + id, null) > 0;		
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}	
		return ret;		
	}
	
	public boolean updateSync(long id, boolean sync) 
	{
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
	 
	public boolean sync(TasksAppService service) throws Exception 
	{		
		boolean ret = false;

		try
		{	
	        Account[] localAccounts = AccountManager.get(service).getAccountsByType("com.google");
	
	        // add missing accounts
	        for (Account localAccount : localAccounts) {
	        	GooAccount gooAccount = findAccountByName(localAccount.name);
	        	if(gooAccount == null)
	        	{
	        		//create a new local cache account (currently unauthorized to sync)
	            	gooAccount = new GooAccount(localAccount.name, localAccount.type, true);  
	            	create(gooAccount);
	        	}
	        }  
	        
	        // purge missing accounts
			List<GooAccount> cacheAccounts = query();
			for (GooAccount cacheAccount : cacheAccounts) {
				boolean missing = true;
				for (Account localAccount : localAccounts) {
					if(localAccount.name != null && localAccount.name.equals(cacheAccount.getName()))
					{
						missing = false;
						break;
					}
				}
				
				if(missing)
				{
					delete(cacheAccount.getId());
				}
			}
	    	ret = true;
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}		
		return ret;
	}		
}