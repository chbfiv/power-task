package com.andorn.powertask.helpers;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class FontHelper {
    private static FontHelper _instance;
	public Typeface CuprumRegular;

    public static synchronized FontHelper getInstance()
    {
		  return _instance;   	    
    }
    
    public static synchronized FontHelper createInstance(AssetManager mgr)
    {
    	  if (_instance == null && mgr != null)   
    	  {
	    	  _instance = new FontHelper(); 
	    	  _instance.Initialize(mgr);
    	  }
    	  return _instance;    
    }
    
    private void Initialize(AssetManager mgr)
    {
    	if(mgr != null)
    	{
    		CuprumRegular = Typeface.createFromAsset(mgr, "Comic_Book.ttf");
    	}
    }
    
    public boolean IsInitialized()
    {
    	return _instance != null;    
    }
    
}
