package com.mintos.weather.task.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

public class Util {

	public static boolean validateIPAddress(String ip)
	{
		Pattern pattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	public static boolean validateJsonArrayParam(String param)
	{
		Pattern pattern = Pattern.compile("\\d");
		Matcher m = pattern.matcher(param);
		return m.matches();
	}
	
	public static Date returnDate(String dateStr, boolean from)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		try {
			Date date = simpleDateFormat.parse(dateStr);
			
			DateTime dateTime = new DateTime(date);
			
			if(from)
				dateTime = dateTime.millisOfDay().setCopy(60 * 60 * 1000);
			else
			{
				dateTime = dateTime.hourOfDay().setCopy(23);
				dateTime = dateTime.minuteOfHour().setCopy(59);
				dateTime = dateTime.secondOfMinute().setCopy(59);
				dateTime = dateTime.millisOfSecond().setCopy(59);
				
			}
			
			return dateTime.toDate();
		} catch (Exception e) {
			return null;
		}
	}
}
