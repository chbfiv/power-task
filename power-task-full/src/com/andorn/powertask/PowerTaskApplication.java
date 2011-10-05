package com.andorn.powertask;

public class PowerTaskApplication extends TaskApplication {
    private static final boolean RELEASE = false;
    
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
}