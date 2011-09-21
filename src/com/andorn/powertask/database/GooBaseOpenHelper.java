package com.andorn.powertask.database;

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
    
    public GooBaseOpenHelper(Context context) {
        super(context, GooDbUtil.DATABASE_NAME, null, GooDbUtil.DATABASE_VERSION);
    }

	
	@Override
	public void onCreate(SQLiteDatabase db) {
    	if(!initialize())
    	{
    		Log.e(TAG, "onCreate - db failed to initialize.");
    		return;    		
    	}
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
	    		getDbReadWrite().execSQL(getTableCreate());
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
	
	public SQLiteDatabase getDbReadOnly()
	{
		SQLiteDatabase ret = null;
		if(dbro != null)
		{
			ret = dbro;
		}
		else
		{
	    	  Log.e(TAG, "getDbReadOnly - null. should already be opened...");	
	    	  throw new NullPointerException("getDbReadOnly - null. should already be opened...");
		}
		return ret;
	}

	public SQLiteDatabase getDbReadWrite()
	{
		SQLiteDatabase ret = null;
		if(dbrw != null)
		{
			ret = dbrw;
		}
		else
		{
	    	  Log.e(TAG, "getDbReadWrite - null. should already be opened...");	
	    	  throw new NullPointerException("getDbReadWrite - null. should already be opened..");
		}
		return ret;
	}
}