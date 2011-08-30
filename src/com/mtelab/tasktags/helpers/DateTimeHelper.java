package com.mtelab.tasktags.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;
import android.text.format.DateUtils;

public class DateTimeHelper {
	private static final String TAG = DateTimeHelper.class.getName();
	
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	

	
	public static String prettyDueDate(String dateStr)
	{
		return prettyDueDate(parseDateRFC3339(dateStr));
	}
	
	public static String prettyDueDate(Calendar date)
	{
		String ret = "";
		if(date != null)
		{
//			Date now = new Date();
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTime(now);
//			calendar.roll(Calendar.DAY_OF_YEAR, 7);
//			Date weekFromNow = calendar.getTime();
//			calendar.setTime(now);
//			calendar.roll(Calendar.DAY_OF_YEAR, -7);
//			Date weekAgo = calendar.getTime();
//			
//			if(date.getYear() == now.getYear() && date.getMonth() == now.getMonth() && date.getDay() == now.getDay())
//			{
//				// Show Today
//				ret = "Today";
//			}
//			else if(date.before(weekFromNow) && date.after(now))
//			{
//				//Show dates of the week		
//				ret = DateFormat.format("EEEE", date).toString();				
//			}
//			else
//			{
//				ret = DateFormat.format("E, MMM dd, yyyy", date).toString();				
//			}
			if(DateUtils.isToday(date.getTimeInMillis()))
			{
				ret = "Today";
			}
			else
			{
				ret = DateFormat.format("E, MMM dd, yyyy", date).toString();
			}
		}		
		return ret;		
	}
	
	public static boolean isRFC3339Date(String date)
	{
		boolean ret = false;	
		try
		{
			if(date != null && date.length() > 0)
			{
				Time t = new Time();
				t.parse3339(date);	
				ret = true;
			}
		}
		catch(TimeFormatException tfex)
		{
		}
		return ret;
	}
	
	public static Calendar parseDateRFC3339(String date)
	{
		Calendar c = null;
		try
		{			
			if(isRFC3339Date(date))
			{		
				Time t = new Time();				
				t.parse3339(date);				
				c = Calendar.getInstance();
				c.set(t.year, t.month, t.monthDay, 0, 0, 0);
			}
		}
		catch(TimeFormatException tfex)
		{
	        Log.w(TAG, tfex);	
		}
		return c;
	}
	
	public static String formatDateRFC3339(int year, int month, int dayOfMonth)
	{	
		String d = null;
		try
		{
			Time t = new Time();
			t.set(dayOfMonth, month, year);
			t.normalize(false);
			d = t.format3339(false);	
		}
		catch(TimeFormatException tfex)
		{
	        Log.w(TAG, tfex);	
		}
		return d;
	}
	
//	public static String formatDateRFC3339(Calendar date)
//	{
//		//"2008-10-13T16:00:00.000Z"
//		String tmp = DateFormat.format("yyyy-MM-ddT00:00:00.0000", date).toString();
//		int zone = (date.get(Calendar.ZONE_OFFSET)/(60*60*1000));
//		
//		return tmp;
//	}
	
	//Date d = new Date(year, monthOfYear, dayOfMonth);
	
//	public static String formatDate3339(Date date)
//	{
//		return new StringBuilder()
//        // Month is 0 based so add 1
//        .append(date.getMonth() + 1).append("/")
//        .append(date.getDay()).append("/")
//        .append(date.getYear()).toString();
//	}
}
