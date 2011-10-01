package com.andorn.powertask.helpers;

public class GeneralHelper {
    
    public static boolean isValueEqual(String x, String y)
    {
    	boolean ret = false;
    	if(x == null && y == null)
    	{
    		ret = true;
    	}
    	else if(x != null && x.equals(y))
    	{
    		ret = true;
    	}    	
    	return ret;    
    }
	
	public static boolean isNullOrEmpty(String val)
	{
		return val == null || val.equals("");
	}
}
