package com.mtelab.taskhack.database;

import com.mtelab.taskhack.helpers.SharedPrefUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GooSyncBaseOpenHelper extends GooBaseOpenHelper {
	
    @SuppressWarnings("unused")
	private static final String TAG = SharedPrefUtil.class.getName();

    protected static final String KEY_syncState = "syncState";   
    protected static final String KEY_eTag = "eTag";   

    protected static final int INDEX_syncState = 3;     
    protected static final int INDEX_eTag = 4;      

    public static final int SYNC_REQUIRED = 1;
    public static final int SYNC_REMOTE_RECORD_MISSING = 2;

    public static final String SYNC_OP_OR = " | ";  
    public static final String SYNC_OP_AND = " & ";  
    
    public GooSyncBaseOpenHelper(Context context) {
    	super(context);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		

	}
}