package com.andorn.powertask.database;

import java.util.ArrayList;
import java.util.List;

import com.andorn.powertask.models.GooBase;
import com.andorn.powertask.models.GooTaskList;
import com.andorn.powertask.services.TasksAppService;
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

    private final GooTasksOpenHelper dbhTasks;
    
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
                KEY_syncState + " INTEGER, " +
                KEY_eTag + " TEXT, " +
                KEY_accountId + " INTEGER, " +
                KEY_remoteId + " TEXT, " +
    			KEY_kind + " TEXT, " +
    			KEY_title + " TEXT, " +
    			KEY_selfLink + " TEXT, " +
			    "FOREIGN KEY (" + KEY_accountId + ") " + 
			    "REFERENCES " + GooAccountsOpenHelper.TABLE_NAME + "("+ GooAccountsOpenHelper.KEY_id + ") ON DELETE CASCADE ON UPDATE CASCADE);"; 

    
    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public GooTaskListsOpenHelper(Context context) {
        super(context);
        dbhTasks = new GooTasksOpenHelper(context);
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
	
	@Override
	public synchronized void close() {
		if(dbhTasks != null) dbhTasks.close();
		super.close();
	}
	
	public GooTasksOpenHelper getDbhTasks() {
		return dbhTasks;
	}
	
	public List<GooTaskList> query(long accountId, int syncStateFilter) {
		List<GooTaskList> list = new ArrayList<GooTaskList>();
		Cursor c = null;
		if(!initialize()) return list;
		
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
		List<GooTaskList> list = new ArrayList<GooTaskList>();
		Cursor c = null;
		if(!initialize()) return list;
		
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
		Cursor c = null;
		if(!initialize()) return c;
		
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
		Cursor c = null;
		if(!initialize()) return c;
		
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
		GooTaskList tl = null;
		Cursor c = null;
		if(!initialize()) return tl;
		
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
		GooTaskList tl = null;
		Cursor c = null;
		if(!initialize()) return tl;
		
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
		long ret = GooBase.INVALID_ID;
		if(!initialize()) return ret;
		
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
		boolean ret = false;
		if(!initialize()) return ret;
		
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
		boolean ret = false;
		if(!initialize()) return ret;
		
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
		String ret = "";
		GooTaskList list;
		if(!initialize()) return ret;
		
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
	
	public boolean sync(TasksAppService service, long accountId) throws Exception 
	{
		boolean ret = false;
		if(!initialize()) return ret;
		
		try
		{		
			// pull - Task Lists
			TaskLists remoteLists = service.queryRemoteTaskLists(); 
			if(remoteLists == null) return false;
			
			boolean remoteTaskListChanges = 
					service.shouldMergeTaskLists(remoteLists.etag, service.getGooAccountETag(accountId));
			
			if (remoteTaskListChanges)
			{
				// merge - Task Lists						
				int count = remoteLists.items != null ?  remoteLists.items.size() : 0;
				for(int i = 0; i < count; i++)
				{
					TaskList remoteList = remoteLists.items.get(i);
					
					// merge - Task List
					// required to get etag per Task List
					remoteList = service.readRemoteTaskList(remoteList.id);
					GooTaskList localList = read(remoteList.id);
					
					if (localList == null)
					{
						//doesn't exist locally, create
						GooTaskList newList = GooTaskList.Convert(accountId, remoteList, remoteList.etag);						
						long id = create(newList);
						newList = read(id);
						
						// sync - Tasks
						getDbhTasks().sync(service, newList);	
					}
					else if(localList.remoteSyncRequired(remoteList.etag))
					{		
						//if already cached etag; skip update	
						GooTaskList newList = GooTaskList.Convert(accountId, remoteList, remoteList.etag);
						newList.setId(localList.getId());
						update(newList);	
	
						// sync - Tasks
						getDbhTasks().sync(service, newList);	
					}
				}	
				
				// merge complete
				service.setGooAccountETag(accountId, remoteLists.etag);
			}
			
			// push - TaskLists
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
						
						// sync - Tasks
						getDbhTasks().sync(service,  newList);	
					}
				}
				else if (remoteTaskListChanges && GooTaskList.shouldDelete(localList.remoteId, remoteLists)) 
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