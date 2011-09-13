package com.andorn.tasktags.helpers;

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
}
