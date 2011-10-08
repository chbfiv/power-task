package com.andorn.powertask.trial;

import com.andorn.powertask.TaskApplication;

public class PowerTaskTrialApplication extends TaskApplication {
	
	private static final boolean RELEASE = true;
	private static final boolean TRIAL = true;
    
    @Override
    public boolean isRelease()
    {
    	return RELEASE;
    }
    
    @Override
    public boolean isDebug()
    {
    	return !RELEASE;
    }
    
    @Override
    public boolean isTrial()
    {
    	return TRIAL;
    }
}