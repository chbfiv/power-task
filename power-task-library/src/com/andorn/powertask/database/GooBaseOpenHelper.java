package com.andorn.powertask.database;

import com.andorn.powertask.TaskApplication;
import com.andorn.powertask.helpers.SharedPrefUtil;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class GooBaseOpenHelper extends SQLiteOpenHelper {
	
	private static final String TAG = SharedPrefUtil.class.getName();
    
    protected static final String KEY_id = "_id";
    protected static final String KEY_created = "created";
    protected static final String KEY_modified = "modified";  

    protected static final int INDEX_id = 0;
    protected static final int INDEX_created = 1;
    protected static final int INDEX_modified = 2;
    
    protected String etag = "";
    
    public boolean initialized = false;
    private SQLiteDatabase dbro;    
    private SQLiteDatabase dbrw;    
    
    public abstract String getTableCreate();
    
    private final Context mContext; 
    
    public GooBaseOpenHelper(Context context) {
        super(context, GooDbUtil.DATABASE_NAME, null, GooDbUtil.DATABASE_VERSION);
        mContext = context;
    }

	
	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}	
    
	public boolean initialize()
	{
		if(!initialized)
		{
	    	try
	    	{
	    		dbro = getReadableDatabase();
				dbrw = getWritableDatabase();
				dbrw.execSQL("PRAGMA foreign_keys=ON;");
	    		dbrw.execSQL(getTableCreate());
	    		initialized = true;
			}
			catch(SQLException sqle)
			{
				Log.e(TAG, "SQL exception - " + sqle.getMessage());	
		  		return false;			
			}
		}
		return true;
	}

    public TaskApplication app()
    {    	
    	return TaskApplication.app(mContext);
    }
    
	public SQLiteDatabase getDbReadOnly()
	{
		SQLiteDatabase ret = null;
		
		initialize();
		
		if(dbro != null && dbro.isOpen())
		{
			ret = dbro;
		}
		else			
		{
			if (dbro != null) dbro.close();
    		dbro = getReadableDatabase();
		}
		
		if(dbro == null || !dbro.isOpen())
		{
			Log.e(TAG, "getDbReadOnly - null. should already be opened...");	
			throw new NullPointerException("getDbReadOnly - null. should already be opened...");
		}		
		return ret;
	}

	public SQLiteDatabase getDbReadWrite()
	{
		SQLiteDatabase ret = null;
		
		initialize();
		
		if(dbrw != null && dbrw.isOpen())
		{
			ret = dbrw;
		}
		else			
		{
			if (dbrw != null) dbrw.close();
			dbrw = getWritableDatabase();
		}
		
		if(dbrw == null || !dbrw.isOpen())
		{
			Log.e(TAG, "getDbReadWrite - null. should already be opened...");	
			throw new NullPointerException("getDbReadWrite - null. should already be opened...");
		}
		return ret;
	}
}