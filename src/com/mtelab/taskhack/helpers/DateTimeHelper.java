package com.mtelab.taskhack.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.text.format.DateFormat;
import android.text.format.Time;

public class DateTimeHelper {
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	public static String prettyDueDate(String date)
	{
		Date myDate = null;
		Time t = new Time();
		t.parse3339(date);
		myDate = new Date(t.toMillis(false));
		return prettyDueDate(myDate);
	}
	
	public static String prettyDueDate(Date date)
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
			ret = DateFormat.format("E, MMM dd, yyyy", date).toString();
		}
		
		return ret;		
	}
}
