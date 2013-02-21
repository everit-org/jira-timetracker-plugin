package org.everit.jira.timetracker.plugin;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * The utility class of date and time conversions.
 */
public final class DateTimeConverterUtil {

    /**
     * The GMT time zone.
     */
    public static final String TIME_ZONE_GMT = "GMT";

    /**
     * The time format.
     */
    public static final String TIME_FORMAT = "HH:mm";

    /**
     * The date format.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * The date time format.
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm";

    /**
     * The begin of year.
     */
    public static final int BEGIN_OF_YEAR = 1900;

    /**
     * The number of days per week.
     */
    public static final int DAYS_PER_WEEK = 7;

    /**
     * The number of work hours per day.
     */
    public static final int WORK_HOURS_PER_DAY = 8;

    /**
     * The number of minutes per hour.
     */
    public static final int MINUTES_PER_HOUR = 60;

    /**
     * The number of seconds per minute.
     */
    public static final int SECONDS_PER_MINUTE = 60;

    /**
     * The number of milliseconds per seconds.
     */
    public static final int MILLISECONDS_PER_SECOND = 1000;

    /**
     * The last day of a month.
     */
    public static final int LAST_DAY_OF_MONTH = 31;

    /**
     * The last hour of a day.
     */
    public static final int LAST_HOUR_OF_DAY = 23;

    /**
     * The last minute of an hour.
     */
    public static final int LAST_MINUTE_OF_HOUR = 59;

    /**
     * The last second of a minute.
     */
    public static final int LAST_SECOND_OF_MINUTE = 59;

    /**
     * The last millisecond of a second.
     */
    public static final int LAST_MILLISECOND_OF_SECOND = 0;

    /**
     * The 24 hours pattern.
     */
    public static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    /**
     * Count the worklog end time.
     * 
     * @param start
     *            When start the worklog.
     * @param spentMilliseconds
     *            The spent time in milliseconds.
     * @return The string format of the end time.
     * @throws ParseException
     *             When can't parse the time.
     */
    public static String countEndTime(final String start, final long spentMilliseconds) throws ParseException {
        long startMillisecond = DateTimeConverterUtil.stringTimeToDateTimeGMT(start).getTime();
        long endMillisecond = startMillisecond + spentMilliseconds;
        return DateTimeConverterUtil.millisecondConvertToStringTime(endMillisecond);
    }

    /**
     * Convert date to string ({@value #DATE_TIME_FORMAT}).
     * 
     * @param dateAndTime
     *            The date to convert.
     * @return The result String.
     */
    public static String dateAndTimeToString(final Date dateAndTime) {
        DateFormat formatterDateAndTime = new SimpleDateFormat(DATE_TIME_FORMAT);
        String stringDateAndTime = formatterDateAndTime.format(dateAndTime);
        return stringDateAndTime;
    }

    /**
     * Convert the date time to string ({@value #TIME_FORMAT}).
     * 
     * @param date
     *            The time to convert.
     * @return The result string.
     */
    public static String dateTimeToString(final Date date) {
        DateFormat formatterTime = new SimpleDateFormat(TIME_FORMAT);
        String timeString = formatterTime.format(date);
        return timeString;
    }

    /**
     * Convert the date to String ({@value #DATE_FORMAT}).
     * 
     * @param date
     *            The Date to convert.
     * @return The result time.
     */
    public static String dateToString(final Date date) {
        DateFormat formatterDate = new SimpleDateFormat(DATE_FORMAT);
        String dateString = formatterDate.format(date);
        return dateString;
    }

    /**
     * Check the Time is valid to the {@value #TIME24HOURS_PATTERN} pattern.
     * 
     * @param time
     *            The time to validate.
     * @return If valid then true else false.
     */
    public static boolean isValidTime(final String time) {
        return Pattern.matches(TIME24HOURS_PATTERN, time);
    }

    /**
     * Convert the millisecond to String time.
     * 
     * @param milliseconds
     *            The time in milliseconds.
     * @return The result String.
     */
    public static String millisecondConvertToStringTime(final long milliseconds) {
        DateFormat formatterTimeGMT = new SimpleDateFormat(TIME_FORMAT);
        formatterTimeGMT.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
        Date date = new Date();
        date.setTime(milliseconds);
        String timeString = formatterTimeGMT.format(date);
        return timeString;
    }

    /**
     * Convert the seconds to jira format (1h 30m) String.
     * 
     * @param spentSeconds
     *            The spent seconds.
     * @return The result String.
     */
    public static String secondConvertToString(final long spentSeconds) {
        String summaryString = "";
        long spentMin = spentSeconds / SECONDS_PER_MINUTE;
        long spentHour = spentMin / MINUTES_PER_HOUR;
        long days = spentHour / WORK_HOURS_PER_DAY;
        long hours = spentHour % WORK_HOURS_PER_DAY;
        long mins = spentMin % MINUTES_PER_HOUR;
        if (days != 0) {
            summaryString = days + "d " + hours + "h " + mins + "m";
        } else if (hours != 0) {
            summaryString = hours + "h " + mins + "m";
        } else {
            summaryString = mins + "m";
        }
        return summaryString;
    }

    /**
     * Convert String Date to String Time.
     * 
     * @param dateString
     *            The String date.
     * @return The String Time.
     * @throws ParseException
     *             If can't parse the date.
     */
    public static String stringDateToStringTime(final String dateString) throws ParseException {
        Date date = DateTimeConverterUtil.stringToDateAndTime(dateString);
        String time = DateTimeConverterUtil.dateTimeToString(date);
        return time;
    }

    /**
     * Convert String ({@value #TIME_FORMAT}) to Time.
     * 
     * @param time
     *            The String time.
     * @return The result date.
     * @throws ParseException
     *             If can't parse the date.
     */
    public static Date stringTimeToDateTime(final String time) throws ParseException {
        DateFormat formatterTime = new SimpleDateFormat(TIME_FORMAT);
        Date dateTime = formatterTime.parse(time);
        return dateTime;
    }

    /**
     * Convert String ({@value #TIME_FORMAT}) to ({@value #TIME_ZONE_GMT}) Time.
     * 
     * @param time
     *            The Sting date and time.
     * @return The result Date.
     * @throws ParseException
     *             If can't parse the date.
     */
    public static Date stringTimeToDateTimeGMT(final String time) throws ParseException {
        DateFormat formatterTime = new SimpleDateFormat(TIME_FORMAT);
        formatterTime.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
        Date dateTime = formatterTime.parse(time);
        return dateTime;
    }

    /**
     * Convert String ({@value #DATE_FORMAT}) to Date.
     * 
     * @param dateString
     *            The String date to convert.
     * @return The result Date.
     * @throws ParseException
     *             If can't parse the date.
     */
    public static Date stringToDate(final String dateString) throws ParseException {
        DateFormat formatterDate = new SimpleDateFormat(DATE_FORMAT);
        Date date = formatterDate.parse(dateString);
        return date;
    }

    /**
     * Convert String ({@value #DATE_TIME_FORMAT}) to date and time.
     * 
     * @param dateAndTimeString
     *            The date time string to convert.
     * @return The result Date.
     * @throws ParseException
     *             if can't parse the date.
     */
    public static Date stringToDateAndTime(final String dateAndTimeString) throws ParseException {
        DateFormat formatterDateAndTime = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date date = formatterDateAndTime.parse(dateAndTimeString);
        return date;
    }

    /**
     * Private constructor.
     */
    private DateTimeConverterUtil() {
    }

}
