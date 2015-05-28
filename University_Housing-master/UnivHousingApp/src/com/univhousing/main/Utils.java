package com.univhousing.main;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Utils {

	public static String convertToString(int number) {
		return String.valueOf(number);
	}
	
	public static int convertToInteger(String value) {
		return Integer.parseInt(value);
	}
	
	public static String changeUtilDateToString(java.util.Date utilDate)
	{
		DateFormat df = getDateFormat();
		return df.format(utilDate);
	}
	
	public static String convertSQLDateToString(java.util.Date sqlDate)
	{
		DateFormat df = getDateFormat();
		return df.format(sqlDate);
	}
	
	public static java.util.Date convertStringToUtilDateFormat(String date) throws ParseException
	{
		DateFormat df = getDateFormat();
		java.util.Date dateValue = df.parse(date);
		return dateValue;
	}
	
	public static java.sql.Date convertStringToSQLDateFormat(String date) throws ParseException
	{
		java.sql.Date sqlDate = convertUtilDateToSQLDate(date);
		return sqlDate;
	}
	
	public static java.sql.Date convertUtilDateToSQLDate(String date) throws ParseException
	{
		DateFormat df = getDateFormat();
		java.util.Date dateValue = df.parse(date);
		java.sql.Date sqlDate = new Date(dateValue.getTime()); 
		return sqlDate;
	}
	
	private static DateFormat getDateFormat()
	{
		return new SimpleDateFormat("MM/dd/yyyy");
	}
	
	/**
	 * @param max Maximum value
	 * @param min Minimum Value
	 * @return random number wetween max and min, both inclusive
	 */
	public static int randomizeStaff(int max, int min)
	{
		int staffNo = 0;
		Random rand = new Random();
		staffNo = rand.nextInt((max-min)+1)+min	;
		return staffNo;
	}
	
}
