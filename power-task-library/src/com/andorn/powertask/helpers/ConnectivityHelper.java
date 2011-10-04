package com.andorn.powertask.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;

public class ConnectivityHelper {
	
	public static boolean isAirplaneMode(Context context) 
	{
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
	
	public static boolean hasConnectivity(Context context) 
	{
		boolean ret = true;
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(manager != null && manager.getActiveNetworkInfo() != null)
		{			
			ret = manager.getActiveNetworkInfo().isConnectedOrConnecting();
		}
		return ret;
    }
}
