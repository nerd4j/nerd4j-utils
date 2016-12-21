/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2013 Nerd4j
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.nerd4j.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class to perform operations over {@link Date} and {@link Calendar} objects.
 * 
 * <p>
 * Performs operations like add and remove time from dates
 * and allows to round dates to a specific {@link TimeUnit}
 * in a easy way.
 * 
 * @author Nerd4j Team
 */
public class SimpleDateHandler
{
	
	/**
	 * Returns the given {@link Date} truncated to the given
	 * {@link TimeUnit}, ie:
	 * <ul>
	 *  <li><b>timeUnit=SECOND</b> returns the same date with the milliseconds set to 0.</li>
	 *  <li><b>timeUnit=MINUTE</b> returns the same date with seconds and milliseconds set to 0.</li>
	 *  <li><b>timeUnit=HOUR</b> returns the same date with minutes, seconds and milliseconds set to 0.</li>
	 *  <li><b>timeUnit=DAY</b> returns the same date with hours, minutes, seconds and milliseconds set to 0.</li>
	 *  <li>etc...</li>
	 * </ul>
	 * 
	 * @param  date     date to be truncated.
	 * @param  timeUnit time unit value.
	 * @return the same date truncated to the given time unit.
	 */
	public static Date truncate( Date date, TimeUnit timeUnit )
	{
	    
	    return truncate( getCalendar(date, null), timeUnit ).getTime();
	    
	}
	
	/**
	 * Returns the given {@link Calendar} truncated to the given
	 * {@link TimeUnit}, ie:
	 * <ul>
	 *  <li><b>timeUnit=SECOND</b> returns the same date with the milliseconds set to 0.</li>
	 *  <li><b>timeUnit=MINUTE</b> returns the same date with seconds and milliseconds set to 0.</li>
	 *  <li><b>timeUnit=HOUR</b> returns the same date with minutes, seconds and milliseconds set to 0.</li>
	 *  <li><b>timeUnit=DAY</b> returns the same date with hours, minutes, seconds and milliseconds set to 0.</li>
	 *  <li>etc...</li>
	 * </ul>
	 * 
	 * @param  calendar calendar to be truncated.
	 * @param  timeUnit time unit value.
	 * @return the same calendar truncated to the given time unit.
	 */
	public static Calendar truncate( Calendar calendar, TimeUnit timeUnit )
	{
	    
	    return getStartOf( timeUnit, calendar );
	    
	}

	/**
     * Returns the given {@link Date} truncated to the given
     * {@link TimeUnit}, ie:
     * <ul>
     *  <li><b>timeUnit=SECOND</b> returns the same date with the milliseconds set to 0.</li>
     *  <li><b>timeUnit=MINUTE</b> returns the same date with seconds and milliseconds set to 0.</li>
     *  <li><b>timeUnit=HOUR</b> returns the same date with minutes, seconds and milliseconds set to 0.</li>
     *  <li><b>timeUnit=DAY</b> returns the same date with hours, minutes, seconds and milliseconds set to 0.</li>
     *  <li>etc...</li>
     * </ul>
     * 
     * @param date     date to be truncated.
     * @param timeUnit time unit value.
     * @param timeZone a specific {@code TimeZone}.
     * @return the same date truncated to the given time unit.
     */
	public static Date truncate( Date date, TimeUnit timeUnit, TimeZone timeZone )
	{
	    
	    return truncate( getCalendar(date, timeZone), timeUnit ).getTime();
	    
	}
	
	/**
	 * Adds the given amount of the given {@link TimeUnit}
	 * to the given {@link Date}. If the amount is negative
	 * such amount will be removed from the given {@link Date}.
	 * 
	 * @param amount   amount of {@link TimeUnit} to add.
	 * @param timeUnit time unit value.
	 * @param date     date to be modified.
	 * @return the modified date.
	 */
	public static Date add( int amount, TimeUnit timeUnit, Date date )
	{
		
		return add( amount, timeUnit, getCalendar(date, null) ).getTime();
		
	}

	/**
     * Adds the given amount of the given {@link TimeUnit}
     * to the given {@link Date}. If the amount is negative
     * such amount will be removed from the given {@link Date}.
     * 
     * @param amount   amount of {@link TimeUnit} to add.
     * @param timeUnit time unit value.
     * @param date     date to be modified.
     * @param timeZone a specific {@code TimeZone}.
     * @return the modified date.
     */
	public static Date add( int amount, TimeUnit timeUnit, Date date, TimeZone timeZone )
	{
		
		return add( amount, timeUnit, getCalendar(date, timeZone) ).getTime();
		
	}
	
	/**
	 * Adds the given amount of the given {@link TimeUnit}
	 * to the given {@link Calendar}. If the amount is negative
	 * such amount will be removed from the given {@link Calendar}.
	 * 
	 * @param amount   amount of {@link TimeUnit} to add.
	 * @param timeUnit time unit value.
	 * @param calendar calendar to be modified.
	 * @return the modified calendar.
	 */
	public static Calendar add( int amount, TimeUnit timeUnit, Calendar calendar )
	{
		
		calendar.add( timeUnit.getCalendarCode(), amount );
		return calendar;
		
	}
	
	/**
	 * Verifies if the {@link Date} to be evaluated
	 * is between the start {@link Date} and the end {@link Date}.
	 * <p>
	 * A date is considered to be included if:
	 * <pre>
	 *  start &lt;= evaluated &lt; end
	 * </pre>
	 * 
	 * @param evaluated date to be evaluated.
	 * @param start     start of the period (inclusive)
	 * @param end       end of the period (exclusive)
	 * @return {@code true} if the date is included;<br>
	 *         {@code false} otherwise.
	 */
	public static boolean isBetween( Date evaluated, Date start, Date end )
	{
		
		return isBetween( evaluated.getTime(), start.getTime(), end.getTime() );
		
	}

	/**
     * Verifies if the unix time stamp to be evaluated
     * is between the start unix time stamp and
     * the end unix time stamp.
     * <p>
     * The given values are supposed to follow the Java specifications ie:
     * <i>milliseconds elapsed since 01/01/1970 00:00:00 UTC</i>;
     * <p>
     * A date is considered to be included if:
     * <pre>
     *  start &lt;= evaluated &lt; end
     * </pre>
     * 
     * @param evaluated time stamp to be evaluated.
     * @param start     start of the period (inclusive)
     * @param end       end of the period (exclusive)
     * @return {@code true} if the date is included;<br>
     *         {@code false} otherwise.
     */
	public static boolean isBetween( long evaluated, long start, long end )
	{
		
		return evaluated >= start && evaluated < end;
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */

	
	/**
	 * Returns the {@link Calendar} adjusted for the given {@code TimeZone}.
	 * <p>
	 * If the given identifier is inconsistent, {@code null} or does
	 * not match any defined {@code TimeZone}, the default {@link Calendar}
	 * will be returned.
	 * 
	 * @param date      the date to initialize the calendar with.
	 * @param timeZonea specific {@code TimeZone}.
	 * @return {@link Calendar} adjusted for the given {@code TimeZone}.
	 */
	private static Calendar getCalendar( Date date, TimeZone timeZone )
	{
		
	    final Calendar calendar;
		if( timeZone != null )
		    calendar = Calendar.getInstance( timeZone );
		else
		    calendar = Calendar.getInstance();
		
		calendar.setTime( date );
		return calendar;
		
	}
		
	/**
     * Sets the given {@link Calendar} to represent the start
     * of the current day ie it sets milliseconds, seconds
     * minutes and hours to 0. 
     * 
     * @param calendar the calendar to be modified.
     */
    private static void setStartOfTheDay( Calendar calendar )
    {
        
        calendar.set( Calendar.MILLISECOND, 0 );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        
    }
    	
	/**
     * Returns the given {@link Calendar} rounded to the given
     * {@link TimeUnit}, ie:
     * <ul>
     *  <li><b>timeUnit=SECOND</b> returns the same date with the milliseconds set to 0.</li>
     *  <li><b>timeUnit=MINUTE</b> returns the same date with seconds and milliseconds set to 0.</li>
     *  <li><b>timeUnit=HOUR</b> returns the same date with minutes, seconds and milliseconds set to 0.</li>
     *  <li><b>timeUnit=DAY</b> returns the same date with hours, minutes, seconds and milliseconds set to 0.</li>
     *  <li>etc...</li>
     * </ul>
     * 
     * @param timeUnit time unit value.
     * @param calendar calendar to be used to perform the changes.
     * @return the same calendar rounded to the given time unit.
     */
	private static Calendar getStartOf( TimeUnit timeUnit, Calendar calendar )
	{
	    
	    switch( timeUnit )
	    {

	    case ERA:
	        calendar.setTimeInMillis( 0 );
	        break;
	    
	    case YEAR:
	        calendar.set( Calendar.DAY_OF_YEAR,  1 );
	        setStartOfTheDay( calendar );
	        break;
	        
	    case MONTH:
	        calendar.set( Calendar.DAY_OF_MONTH, 1 );
	        setStartOfTheDay( calendar );
	        break;

	    case WEEK:
	        int startOfWeek = calendar.getFirstDayOfWeek();
	        calendar.set( Calendar.DAY_OF_WEEK, startOfWeek );
	        setStartOfTheDay( calendar );
	        break;
	    
	    case DAY:    calendar.set( Calendar.HOUR_OF_DAY,  0 );
	    case HOUR:   calendar.set( Calendar.MINUTE,       0 );
	    case MINUTE: calendar.set( Calendar.SECOND,       0 );
	    case SECOND: calendar.set( Calendar.MILLISECOND,  0 );
	    	break;
	    
	    default: break;
	    
	    }
	    
	    return calendar;
	    
	}
	
}