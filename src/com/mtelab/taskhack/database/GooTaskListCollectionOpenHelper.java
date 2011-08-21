package com.mtelab.taskhack.database;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;
import com.mtelab.taskhack.helpers.SharedPrefUtil;
import com.mtelab.taskhack.models.GooSyncBase;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.services.TasksAppService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GooTaskListCollectionOpenHelper extends GooSyncBaseOpenHelper {
	
    private static final String TAG = GooTaskListCollectionOpenHelper.class.getName();

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
    
    public GooTaskListCollectionOpenHelper(Context context) {
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
	
	@Override
	public boolean initialize() {
		boolean ret = false;
    	try
    	{
    		getWritableDatabase().execSQL(TABLE_CREATE);
    		ret = true;
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public List<GooTaskList> query(long accountId, int syncStateFilter) {
		List<GooTaskList> list = new ArrayList<GooTaskList>();
		Cursor c = null;
		try
		{
			c = getReadableDatabase().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_accountId + " = ? AND (" + KEY_syncState + " & " + syncStateFilter + ") != " + syncStateFilter,     // The columns for the where clause
			           new String[] { String.valueOf(accountId) }, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTaskList tl = new GooTaskList(c.getLong(INDEX_accountId), c.getString(INDEX_remoteId),
							c.getString(INDEX_kind), c.getString(INDEX_title), c.getString(INDEX_selfLink));
					tl.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
					tl.setSync(c.getInt(INDEX_syncState), c.getString(INDEX_eTag));
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
		try
		{
			c = getReadableDatabase().query(
						TABLE_NAME,            // The database to query
						PROJECTION,    // The columns to return from the query
						KEY_accountId + " = ? ",     // The columns for the where clause
			           new String[] { String.valueOf(accountId) }, // The values for the where clause
			           null,          // don't group the rows
			           null,          // don't filter by row groups
			           null        // The sort order
			       );
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTaskList tl = new GooTaskList(c.getLong(INDEX_accountId), c.getString(INDEX_remoteId),
							c.getString(INDEX_kind), c.getString(INDEX_title), c.getString(INDEX_selfLink));
					tl.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
					tl.setSync(c.getInt(INDEX_syncState), c.getString(INDEX_eTag));
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
	
	public GooTaskList read(long id) {
		GooTaskList tl = null;
		Cursor c = null;
		try
		{
			c = getReadableDatabase().query(
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
		finally
		{
			if(c != null) c.close();			
		}
		return tl;
	}
	
	public GooTaskList read(String remoteId) {
		GooTaskList tl = null;
		Cursor c = null;
		try
		{
			c = getReadableDatabase().query(
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
		finally
		{
			if(c != null) c.close();			
		}
		return tl;
	}
	
	/*public boolean createOrUpdateRange(long accountId, TaskLists list) 
	{
		boolean ret = false;
		try
		{
			if(list != null)
			{
				ret = true;
				addSyncStateByAccount(accountId, SYNC_REMOTE_RECORD_MISSING);
				for (TaskList item : list.items) {
					createOrUpdate(accountId, item);
				}		
				deleteBySyncState(accountId, SYNC_REMOTE_RECORD_MISSING);
			}
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}*/
	
	public boolean sync(TasksAppService service, long accountId, TaskLists remoteLists, String eTag) 
	{
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
	
	public long create(GooTaskList item) 
	{
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
			 ret = getWritableDatabase().insert(TABLE_NAME, null, values);	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	/*public long createOrUpdate(long accountId, TaskList item) 
	{
		GooTaskList list = new GooTaskList(accountId, item.id, item.kind, item.title, item.selfLink);
		list.setETag(item.etag);
		return createOrUpdate(list);
	}
	
	public long createOrUpdate(GooTaskList item) 
	{
		long ret = -1;
		try
		{
			GooTaskList tl = read(item.remoteId);	
			item.setSyncState(item.getSyncState() & ~SYNC_REMOTE_RECORD_MISSING);
			if(tl == null)
			{
				ret = create(item);
			}
			else
			{
				long localId = tl.getId();
				item.setId(localId);
				update(item);
				ret = localId;
			}
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}*/
	
	public boolean update(GooTaskList item) 
	{
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
			 ret = getWritableDatabase().update(TABLE_NAME, values, KEY_id + " = " + item.getId(), null) > 0;				
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
			 ret = getWritableDatabase().delete(TABLE_NAME, KEY_id + " = " + rowId, null) > 0;				
		 }
		 catch(SQLException sqle)
		 {
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());	   			
		 }
		 return ret;
     }
	 
	/*public boolean addSyncStateByAccount(long accountId, int state) 
	{
		boolean ret = false;
		try
		{
			 String sql = "UPDATE " + TABLE_NAME + " SET " + KEY_syncState 
			 	+ " = (" + KEY_syncState + " | " + state + ")"
			 	+ " WHERE " + KEY_accountId + " = " + accountId; 
			 getWritableDatabase().execSQL(sql);
			 ret = true;							
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean removeSyncStateByAccount(long accountId, int state) 
	{
		boolean ret = false;
		try
		{
			 String sql = "UPDATE " + TABLE_NAME + " SET " + KEY_syncState 
			 	+ " = (" + KEY_syncState + " & " + ~state + ")"
			 	+ " WHERE " + KEY_accountId + " = " + accountId; 
			 getWritableDatabase().execSQL(sql);
			 ret = true;							
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean deleteBySyncState(long accountId, int state) 
	{
		boolean ret = false;
		try
		{
			 String sql = "DELETE FROM " + TABLE_NAME + " WHERE " 
			 	+ state + " = (" + KEY_syncState + " & " + state + ")"
			 	+ " AND " + KEY_accountId + " = " + accountId; 
			 getWritableDatabase().execSQL(sql);
			 ret = true;							
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}*/
	
	public String getTaskListRemoteId(long taskListId)
	{
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
}