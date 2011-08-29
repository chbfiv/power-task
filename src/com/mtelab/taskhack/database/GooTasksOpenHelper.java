package com.mtelab.taskhack.database;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;
import com.google.api.services.tasks.v1.model.Tasks;
import com.mtelab.taskhack.models.GooTask;
import com.mtelab.taskhack.models.GooTaskList;
import com.mtelab.taskhack.models.TCTag;
import com.mtelab.taskhack.services.TasksAppService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GooTasksOpenHelper extends GooSyncBaseOpenHelper {
	
    private static final String TAG = GooTasksOpenHelper.class.getName();
    
	private final TCTagMapOpenHelper dbTagMapHelper;

    protected static final String TABLE_NAME = "goo_tasks";
    protected static final String KEY_taskListId = "taskListId";
    protected static final String KEY_remoteId = "remoteId";
    protected static final String KEY_kind = "kind";
    protected static final String KEY_title = "title";
    protected static final String KEY_selfLink = "selfLink";
    protected static final String KEY_parent = "parent";
    protected static final String KEY_position = "position";
    protected static final String KEY_notes = "notes";
    protected static final String KEY_status = "status";
    protected static final String KEY_due = "due";
    protected static final String KEY_completed = "completed";
    protected static final String KEY_deleted = "deleted";
    protected static final String KEY_hidden = "hidden";
    
    protected static final String[] PROJECTION = new String[] {
    	KEY_id, // 0
    	KEY_created, // 1
    	KEY_modified, // 2
    	KEY_syncState, // 3
    	KEY_eTag, // 4
    	KEY_taskListId, // 5
    	KEY_remoteId, // 6
    	KEY_kind, // 7
    	KEY_title, // 8
    	KEY_selfLink, // 9
    	KEY_parent, // 10
    	KEY_position, // 11
    	KEY_notes, // 12
    	KEY_status, // 13
    	KEY_due, // 14
    	KEY_completed, // 15
    	KEY_deleted, // 16
    	KEY_hidden // 17
    };

    protected static final int INDEX_taskListId = 5;
    protected static final int INDEX_remoteId = 6;
    protected static final int INDEX_kind = 7;
    protected static final int INDEX_title = 8;
    protected static final int INDEX_selfLink = 9;
    protected static final int INDEX_parent = 10;
    protected static final int INDEX_position = 11;
    protected static final int INDEX_notes = 12;
    protected static final int INDEX_status = 13;
    protected static final int INDEX_due = 14;
    protected static final int INDEX_completed = 15;
    protected static final int INDEX_deleted = 16;
    protected static final int INDEX_hidden = 17;
    
    protected static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                KEY_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_created + " NUMERIC, " +
                KEY_modified + " NUMERIC, " +
                KEY_taskListId + " INTEGER, " +
                KEY_syncState + " INTEGER, " +
                KEY_eTag + " TEXT, " +
                KEY_remoteId + " TEXT, " +
    			KEY_kind + " TEXT, " +
    			KEY_title + " TEXT, " +
    			KEY_selfLink + " TEXT, " +
    			KEY_parent + " TEXT, " +
    			KEY_position + " TEXT, " +
    			KEY_notes + " TEXT, " +
    			KEY_status + " INTEGER, " +
    			KEY_due + " NUMERIC, " +
    			KEY_completed + " NUMERIC, " +
    			KEY_deleted + " INTEGER, " +
    			KEY_hidden + " INTEGER, " +
			    "FOREIGN KEY(" + KEY_taskListId + ") " + 
			    "REFERENCES " + GooTaskListsOpenHelper.TABLE_NAME + "("+ GooTaskListsOpenHelper.KEY_id + ") ON DELETE CASCADE ON UPDATE CASCADE);"; 

    @Override
    public String getTableCreate() {
    	return TABLE_CREATE;
    }
    
    public GooTasksOpenHelper(Context context) {
        super(context);
        dbTagMapHelper = new TCTagMapOpenHelper(context);
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
	
	public List<GooTask> query(long taskListId, int syncStateFilter) {
		initialize();
		List<GooTask> taskList = new ArrayList<GooTask>();
		Cursor c = null;
		try
		{
			c = queryCursor(taskListId, syncStateFilter);
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTask task = read(c);									
					List<TCTag> tags = dbTagMapHelper.query(task.getId());
					task.setTags(tags);			
					taskList.add(task);
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
		return taskList;
	}	
	
	public List<GooTask> query(long taskListId) {
		initialize();
		List<GooTask> taskList = new ArrayList<GooTask>();
		Cursor c = null;
		try
		{
			c = queryCursor(taskListId);
			if(c != null && c.moveToFirst())
			{
				do
				{
					GooTask task = read(c);								
					List<TCTag> tags = dbTagMapHelper.query(task.getId());
					task.setTags(tags);	
					taskList.add(task);
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
		return taskList;
	}	
	
	public Cursor queryCursor(long taskListId, int syncStateFilter)  {
		initialize();
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
					TABLE_NAME,            // The database to query
					PROJECTION,    // The columns to return from the query
					KEY_taskListId + " = ? AND (" + KEY_syncState + " & " + syncStateFilter + ") != " + syncStateFilter,     // The columns for the where clause
		           new String[] { String.valueOf(taskListId) }, // The values for the where clause
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
	
	public Cursor queryCursor(long taskListId) {
		initialize();
		Cursor c = null;
		try
		{
			c = getDbReadOnly().query(
					TABLE_NAME,            // The database to query
					PROJECTION,    // The columns to return from the query
					KEY_taskListId + " = ? ",     // The columns for the where clause
		           new String[] { String.valueOf(taskListId) }, // The values for the where clause
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
	
	public static GooTask read(Cursor c) {
		GooTask task = null;
		try
		{
			if(c != null)
			{
				task = new GooTask(
						c.getLong(INDEX_taskListId), c.getString(INDEX_remoteId), c.getString(INDEX_kind),
						c.getString(INDEX_title), c.getString(INDEX_selfLink), c.getString(INDEX_parent),
						c.getString(INDEX_position), c.getString(INDEX_notes), c.getString(INDEX_status),
						c.getString(INDEX_due), c.getString(INDEX_completed), c.getInt(INDEX_deleted),
						c.getInt(INDEX_hidden));
				task.setBase(c.getLong(INDEX_id), c.getLong(INDEX_created), c.getLong(INDEX_modified));
				task.setSync(c.getInt(INDEX_syncState), c.getString(INDEX_eTag));
			}
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return task;
	}
	
	public GooTask read(long id) {
		initialize();
		GooTask task = null;			
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
				task = read(c);						
				List<TCTag> tags = dbTagMapHelper.query(task.getId());
				task.setTags(tags);	
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
		return task;
	}
	
	public GooTask read(String remoteId) {
		initialize();
		GooTask task = null;
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
				task = read(c);						
				List<TCTag> tags = dbTagMapHelper.query(task.getId());
				task.setTags(tags);	
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
		return task;
	}
	
	public long create(long taskListId) 
	{
		initialize();
		long ret = -1;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_taskListId, taskListId);
			 ret = getDbReadWrite().insert(TABLE_NAME, null, values);	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public long create(GooTask task) 
	{
		initialize();
		long ret = -1;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_taskListId, task.taskListId);
			 values.put(KEY_syncState, task.getSyncState());
			 values.put(KEY_eTag, task.getETag());
			 values.put(KEY_remoteId, task.remoteId);
			 values.put(KEY_kind, task.kind);
			 values.put(KEY_title, task.title);
			 values.put(KEY_selfLink, task.selfLink);
			 values.put(KEY_parent, task.parent);
			 values.put(KEY_position, task.position);
			 values.put(KEY_notes, task.notes);
			 values.put(KEY_status, task.status);
			 values.put(KEY_due, task.due);
			 values.put(KEY_completed, task.completed);
			 values.put(KEY_deleted, task.deleted);
			 values.put(KEY_hidden, task.hidden);
			 ret = getDbReadWrite().insert(TABLE_NAME, null, values);	
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean update(GooTask task) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_taskListId, task.taskListId);
			 values.put(KEY_syncState, task.getSyncState());
			 values.put(KEY_eTag, task.getETag());
			 values.put(KEY_remoteId, task.remoteId);
			 values.put(KEY_kind, task.kind);
			 values.put(KEY_title, task.title);
			 values.put(KEY_selfLink, task.selfLink);
			 values.put(KEY_parent, task.parent);
			 values.put(KEY_position, task.position);
			 values.put(KEY_notes, task.notes);
			 values.put(KEY_status, task.status);
			 values.put(KEY_due, task.due);
			 values.put(KEY_completed, task.completed);
			 values.put(KEY_deleted, task.deleted);
			 values.put(KEY_hidden, task.hidden);
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + task.getId(), null) > 0;				
		}
		catch(SQLException sqle)
		{
	    	  Log.e(TAG, "SQL exception - " + sqle.getMessage());				
		}
		return ret;
	}
	
	public boolean update(long taskId, GooTask.Status status) 
	{
		initialize();
		boolean ret = false;
		try
		{
			 ContentValues values = new ContentValues();
			 values.put(KEY_status, status.toString());
			 ret = getDbReadWrite().update(TABLE_NAME, values, KEY_id + " = " + taskId, null) > 0;				
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
	 
	 public boolean sync(TasksAppService service, long taskListId, Tasks remoteTasks, String eTag) 
		{
			initialize();
			boolean ret = false;
			try
			{
				if(remoteTasks != null)
				{				
					for (Task remoteTask : remoteTasks.items) {
						GooTask localTask = read(remoteTask.id);
						if(localTask != null)
						{
							//if already cached etag; skip update
							if(localTask.remoteSyncRequired(eTag))
							{
								GooTask newTask = GooTask.Convert(taskListId, remoteTask, eTag);
								newTask.setId(localTask.getId());
								update(newTask);
							}		
						}
						else
						{
							//doesn't exist locally, create
							GooTask newTask = GooTask.Convert(taskListId, remoteTask, eTag);					
							create(newTask);
						}
					}	
				}
				
				for (GooTask localTask : query(taskListId)) {
					Task remoteTask = null;

					if(localTask.isSyncCreate())
					{
						remoteTask = service.createRemoteTask(localTask);
						if(remoteTask != null)
						{
							GooTask newTask = GooTask.Convert(taskListId, remoteTask, eTag);	
							newTask.setId(localTask.getId());
							update(newTask);
						}
					}
					else if (!localTask.find(remoteTasks)) 
					{
						//dont need to create it, thus it was deleted remotely
						delete(localTask.getId());
					}
					else if (localTask.isSyncDelete()) 
					{
						if(service.deleteRemoteTask(localTask))
						{
							delete(localTask.getId());
						}
					}	
					else if (localTask.isSyncUpdate()) 
					{
						remoteTask = service.updateRemoteTask(localTask);							
						if(remoteTask != null)
						{
							GooTask newTask = GooTask.Convert(taskListId, remoteTask, eTag);
							newTask.setId(localTask.getId());
							update(newTask);
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