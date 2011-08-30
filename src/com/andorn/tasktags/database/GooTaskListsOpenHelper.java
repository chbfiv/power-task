package com.andorn.tasktags.database;

import java.util.ArrayList;
import java.util.List;

import com.andorn.tasktags.helpers.SharedPrefUtil;
import com.andorn.tasktags.models.GooSyncBase;
import com.andorn.tasktags.models.GooTaskList;
import com.andorn.tasktags.services.TasksAppService;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GooTaskListsOpenHelper extends GooSyncBaseOpenHelper {
	
    private static final String TAG = GooTaskListsOpenHelper.class.getName();

    protected static final String TABLE_NAME = "goo_tasklists";
    protected static final String KEY_accountId = "accountId";
    protected static final String KEY_remoteId = "remoteId";
    protected static final String KEY_kind = "kind";
    protected static final String KEY_title = "title";
    protected static final String KEY_selfLink = "selfLink";
    
    protected static final String[] PROJECTION = new String[] {
    	KEY_id, // 0
    	KEY_created, // 1
    	KEY_modified, // 2
    	KEY_syncState, // 3
    	KEY_eTag, // 4
    	KEY_accountId, // 5
    	KEY_remoteId, // 6
    	KEY_kind, // 7
    	KEY_title, // 8
    	KEY_selfLink // 9
    };

    protected static final int INDEX_accountId = 5;
    protected static final int INDEX_remoteId = 6;
    protected static final int INDEX_kind = 7;
    protected static final int INDEX_title = 8;
    protected static final int INDEX_selfLink = 9;
    
    protected static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_created + " NUMERIC, " +
                KEY_modified + " NUMERIC, " +
                KEY_accountId + " INTEGER, " +
                KEY_syncState + " INTEGER, " +
                KEY_eTag + " TEXT, " +
                KEY_remoteId + " TEXT, " +
    			KEY_kind + " TEXT, " +
    			KEY_title + " TEXT, " +
    			KEY_selfLink + " TEXT);"; 
    
    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public GooTaskListsOpenHelper(Context context) {
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
	
	public List<GooTaskList> query(long accountId, int syncStateFilter) {
		initialize();
		List<GooTaskList> list = new ArrayList<GooTaskList>();
		Cursor c = null;
		try
		{
			c = queryCursor(accountId, syncStateFilter);
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTaskList tl = read(c);
					list.add(tl);
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
	
	public List<GooTaskList> query(long accountId) {
		initialize();
		List<GooTaskList> list = new ArrayList<GooTaskList>();
		Cursor c = null;
		try
		{
			c = queryCursor(accountId);
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTaskList tl = read(c);
					list.add(tl);
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
	
	public Cursor queryCursor(long accountId, int syncStateFilter)  {
		initialize();
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
					TABLE_NAME,            // The database to query
					PROJECTION,    // The columns to return from the query
					KEY_accountId + " = ? AND (" + KEY_syncState + " & " + syncStateFilter + ") != " + syncStateFilter,     // The columns for the where clause
		           new String[] { String.valueOf(accountId) }, // The values for the where clause
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
	
	public Cursor queryCursor(long accountId) {
		initialize();
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_accountId + " = ? ",     // The columns for the where clause
			           new String[] { String.valueOf(accountId) }, // The values for the where clause
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
	
	public static GooTaskList read(Cursor c) {
		GooTaskList tl = null;
		try
		{
			if(c != null)
			{
				tl = new GooTaskList(c.getLong(INDEX_accountId), c.getString(INDEX_remoteId),
						c.getString(INDEX_kind), c.getString(INDEX_title), c.getString(INDEX_selfLink));
				tl.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
				tl.setSync(c.getInt(INDEX_syncState), c.getString(INDEX_eTag));
			}
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return tl;
	}
	
	public GooTaskList read(long id) {
		initialize();
		GooTaskList tl = null;
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
				tl = read(c);
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
		return tl;
	}
	
	public GooTaskList read(String remoteId) {
		initialize();
		GooTaskList tl = null;
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_remoteId + " = ? ",     // The columns for the where clause
			           new String[] { remoteId }, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				tl = read(c);
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
		return tl;
	}
	
	public long create(GooTaskList item) 
	{
		initialize();
		long ret = -1;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_accountId, item.accountId);
			 values.put(KEY_syncState, item.getSyncState());
			 values.put(KEY_eTag, item.getETag());
			 values.put(KEY_remoteId, item.remoteId);
			 values.put(KEY_kind, item.kind);
			 values.put(KEY_title, item.title);
			 values.put(KEY_selfLink, item.selfLink);
			 ret = getDbReadWrite().insert(TABLE_NAME, null, values);	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
		
	public boolean update(GooTaskList item) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_syncState, item.getSyncState());
			 values.put(KEY_eTag, item.getETag());
			 values.put(KEY_remoteId, item.remoteId);
			 values.put(KEY_kind, item.kind);
			 values.put(KEY_title, item.title);
			 values.put(KEY_selfLink, item.selfLink);
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + item.getId(), null) > 0;				
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
	 
	public String getTaskListRemoteId(long taskListId)
	{
		initialize();
		String ret = "";
		GooTaskList list;
		try
		{
			list = read(taskListId);
			ret = list.remoteId;
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}	
	
	public boolean sync(TasksAppService service, long accountId, TaskLists remoteLists, String eTag) 
	{
		initialize();
		boolean ret = false;
		try
		{
			if(remoteLists != null)
			{				
				for (TaskList remoteList : remoteLists.items) {
					GooTaskList localList = read(remoteList.id);
					if(localList != null)
					{
						//if already cached etag; skip update
						if(localList.remoteSyncRequired(eTag))
						{
							GooTaskList newList = GooTaskList.Convert(accountId, remoteList, eTag);
							newList.setId(localList.getId());
							update(newList);
						}		
					}
					else
					{
						//doesn't exist locally, create
						GooTaskList newList = GooTaskList.Convert(accountId, remoteList, eTag);						
						create(newList);
					}
				}	
			}
			
			for (GooTaskList localList : query(accountId)) {
				TaskList remoteList = null;

				if(localList.isSyncCreate())
				{
					remoteList = service.createRemoteTaskList(localList);
					if(remoteList != null)
					{
						GooTaskList newList = GooTaskList.Convert(accountId, remoteList, remoteList.etag);
						newList.setId(localList.getId());
						update(newList);
					}
				}
				else if (!localList.find(remoteLists)) 
				{
					//dont need to create it, thus it was deleted remotely
					delete(localList.getId());
				}
				else if (localList.isSyncDelete()) 
				{
					if(service.deleteRemoteTaskList(localList))
					{
						delete(localList.getId());
					}
				}	
				else if (localList.isSyncUpdate()) 
				{
					remoteList = service.updateRemoteTaskList(localList);							
					if(remoteList != null)
					{
						GooTaskList newList = GooTaskList.Convert(accountId, remoteList, remoteList.etag);
						newList.setId(localList.getId());
						update(newList);
					}					
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