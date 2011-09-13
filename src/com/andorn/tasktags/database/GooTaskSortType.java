package com.andorn.tasktags.database;

public class GooTaskSortType {
	
    private static final String TAG = GooTaskSortType.class.getName();

    public static final int COUNT = 5;
    
    public static final int CUSTOM_POSITION = 1;
    public static final int TITLE_ASCENDING = 2;
    public static final int TITLE_DESCENDING = 4;
    public static final int DATE_ASCENDING = 8;
    public static final int DATE_DESCENDING = 16;
    
    public static String getSortType(int taskSortType)
    {
    	String label = null;
		if(GooTaskSortType.isCustomPosition(taskSortType))
		{
			label = "custom";		
		}
		if(GooTaskSortType.isTitleAscending(taskSortType))
		{
			if(label != null) label += ", ";
			label = "title asc";		
		}
		if(GooTaskSortType.isTitleDescending(taskSortType))
		{
			if(label != null) label += ", ";
			label = "title desc";			
		}
		if(GooTaskSortType.isDateAscending(taskSortType))
		{
			if(label != null) label += ", ";
			label = "date asc";		
		}
		if(GooTaskSortType.isDateDescending(taskSortType))
		{
			if(label != null) label += ", ";
			label = "date desc";			
		}
		return label;
    }
    
	public static int getSortTypeFromPosition(int position)
	{
		int sortType;
		if(position == 0) sortType = CUSTOM_POSITION;
		else if(position == 1) sortType = TITLE_ASCENDING;
		else if(position == 2) sortType = TITLE_DESCENDING;
		else if(position == 3) sortType = DATE_ASCENDING;
		else if(position == 4) sortType = DATE_DESCENDING;
		else if(position == 5) sortType = TITLE_ASCENDING | DATE_ASCENDING;
		else if(position == 6) sortType = TITLE_ASCENDING | DATE_DESCENDING;
		else if(position == 7) sortType = TITLE_DESCENDING | DATE_ASCENDING;
		else if(position == 8) sortType = TITLE_DESCENDING | DATE_DESCENDING;
		else sortType = CUSTOM_POSITION;
		return sortType;
	}
	
	public static boolean isCustomPosition(int value)
	{
		return (value & CUSTOM_POSITION) == CUSTOM_POSITION;
	}
	
	public static boolean isTitleAscending(int value)
	{
		return (value & TITLE_ASCENDING) == TITLE_ASCENDING;
	}
	
	public static boolean isTitleDescending(int value)
	{
		return (value & TITLE_DESCENDING) == TITLE_DESCENDING;
	}
	
	public static boolean isDateAscending(int value)
	{
		return (value & DATE_ASCENDING) == DATE_ASCENDING;
	}
	
	public static boolean isDateDescending(int value)
	{
		return (value & DATE_DESCENDING) == DATE_DESCENDING;
	}
	
	public static int flagCustomPosition(int value)
	{
		value = unflagTitleAscending(value);
		value = unflagTitleDescending(value);
		value = unflagDateAscending(value);
		value = unflagDateDescending(value);
		return value | CUSTOM_POSITION;
	}
	
	public static int flagTitleAscending(int value)
	{
		value = unflagCustomPosition(value);
		value = unflagTitleDescending(value);
		return value | TITLE_ASCENDING;
	}
	
	public static int flagTitleDescending(int value)
	{
		value = unflagCustomPosition(value);
		value = unflagTitleAscending(value);
		return value | TITLE_DESCENDING;
	}
	
	public static int flagDateAscending(int value)
	{
		value = unflagCustomPosition(value);
		value = unflagDateDescending(value);
		return value | DATE_ASCENDING;
	}
	
	public static int flagDateDescending(int value)
	{
		value = unflagCustomPosition(value);
		value = unflagDateAscending(value);
		return value | DATE_DESCENDING;
	}
	
	public static int unflagCustomPosition(int value)
	{
		return value & ~CUSTOM_POSITION;
	}
	
	public static int unflagTitleAscending(int value)
	{
		return value & ~TITLE_ASCENDING;
	}
	
	public static int unflagTitleDescending(int value)
	{
		return value & ~TITLE_DESCENDING;
	}
	
	public static int unflagDateAscending(int value)
	{
		return value & ~DATE_ASCENDING;
	}
	
	public static int unflagDateDescending(int value)
	{
		return value & ~DATE_DESCENDING;
	}
}