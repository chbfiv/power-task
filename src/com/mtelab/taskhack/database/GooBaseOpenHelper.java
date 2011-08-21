package com.mtelab.taskhack.database;

import com.mtelab.taskhack.helpers.SharedPrefUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GooBaseOpenHelper extends SQLiteOpenHelper {
	
    @SuppressWarnings({ "unused" })
	private static final String TAG = SharedPrefUtil.class.getName();

    protected static final String KEY_id = "id";
    protected static final String KEY_created = "created";
    protected static final String KEY_modified = "modified";  

    protected static final int INDEX_id = 0;
    protected static final int INDEX_created = 1;
    protected static final int INDEX_modified = 2;
    
    protected String etag = "";
    
    public GooBaseOpenHelper(Context context) {
        super(context, GooDbUtil.DATABASE_NAME, null, GooDbUtil.DATABASE_VERSION);
    }
	
	public boolean initialize()
	{
		return true;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}