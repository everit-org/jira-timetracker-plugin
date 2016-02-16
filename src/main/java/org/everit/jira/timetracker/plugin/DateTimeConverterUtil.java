/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.timetracker.plugin;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * The utility class of date and time conversions.
 */
public final class DateTimeConverterUtil {

  /**
   * The begin of year.
   */
  public static final int BEGIN_OF_YEAR = 1900;

  // /**
  // * The date format.
  // */
  // public static final String DATE_FORMAT = "yyyy-MM-dd";

  /**
   * The date time format.
   */
  // public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm";

  /**
   * The number of days per week.
   */
  public static final int DAYS_PER_WEEK = 7;

  private static final int HOURS_GROUP = 8;

  private static final int HOURS_GROUP_2 = 2;

  /**
   * The JIRA duration pattern.
   */
  public static final String JIRA_DURATION_PATTERN = "(([01]?[0-9]|2[0-3])[h]*[\\s]+(([0-9]{1,3}|"
      + "1[0-3][0-9]{2}|14[0-3][0-9])[m])*)|"
      + "(([0-9]{1,3}|1[0-3][0-9]{2}|14[0-3][0-9])[m])+|(([01]?[0-9]|2[0-3])[h])+";

  /**
   * The last day of a month.
   */
  public static final int LAST_DAY_OF_MONTH = 31;

  /**
   * The last hour of a day.
   */
  public static final int LAST_HOUR_OF_DAY = 23;

  /**
   * The last millisecond of a second.
   */
  public static final int LAST_MILLISECOND_OF_SECOND = 0;

  /**
   * The last minute of an hour.
   */
  public static final int LAST_MINUTE_OF_HOUR = 59;

  /**
   * The last second of a minute.
   */
  public static final int LAST_SECOND_OF_MINUTE = 59;

  private static final int MILLISEC_IN_SECOND = 1000;

  /**
   * The number of milliseconds per seconds.
   */
  public static final int MILLISECONDS_PER_SECOND = 1000;

  private static final int MINUTES_GROUP = 6;

  private static final int MINUTES_GROUP_2 = 4;

  /**
   * The number of minutes per hour.
   */
  public static final int MINUTES_PER_HOUR = 60;

  /**
   * The number of seconds per minute.
   */
  public static final int SECONDS_PER_MINUTE = 60;

  /**
   * The time format.
   */
  public static final String TIME_FORMAT = "HH:mm";

  /**
   * The GMT time zone.
   */
  public static final String TIME_ZONE_GMT = "GMT";

  /**
   * The 24 hours pattern.
   */
  public static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

  /**
   * Count the worklog end time.
   *
   * @param start
   *          When start the worklog.
   * @param spentMilliseconds
   *          The spent time in milliseconds.
   * @return The string format of the end time.
   * @throws ParseException
   *           When can't parse the time.
   */
  public static String countEndTime(final String start, final long spentMilliseconds)
      throws ParseException {
    long startMillisecond = DateTimeConverterUtil.stringTimeToDateTimeGMT(start).getTime();
    long endMillisecond = startMillisecond + spentMilliseconds;
    return DateTimeConverterUtil.millisecondConvertToStringTime(endMillisecond);
  }

  /**
   * Convert date to string ({@value #DATE_TIME_FORMAT}).
   *
   * @param dateAndTime
   *          The date to convert.
   * @return The result String.
   */
  public static String dateAndTimeToString(final Date dateAndTime) {
    String dateTimeFormat =
        getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_COMPLETE);
    DateFormat formatterDateAndTime = new SimpleDateFormat(dateTimeFormat);
    String stringDateAndTime = formatterDateAndTime.format(dateAndTime);
    return stringDateAndTime;
  }

  /**
   * Convert the date time to string ({@value #TIME_FORMAT}).
   *
   * @param date
   *          The time to convert.
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
   *          The Date to convert.
   * @return The result time.
   */
  public static String dateToString(final Date date) {
    String dateFormat = getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_DMY);
    DateFormat formatterDate = new SimpleDateFormat(dateFormat);
    String dateString = formatterDate.format(date);
    return dateString;
  }

  /**
   * Get the difference between to date in seconds.
   *
   * @param firstDate
   *          The fisrt date.
   * @param secondDate
   *          The second date.
   * @return The difference between dates.
   */
  public static long getDateDifference(final Date firstDate, final Date secondDate) {
    long diffInMillies = secondDate.getTime() - firstDate.getTime();
    return TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
  }

  private static String getJiraDefaultDateAndTimeJavaFormat(final String formatKey) {
    ApplicationProperties applicationProperties =
        ComponentAccessor.getComponent(ApplicationProperties.class);
    return applicationProperties.getDefaultBackedString(formatKey);
  }

  /**
   * Check the Time is valid to the {@value #JIRA_DURATION_PATTERN} pattern.
   *
   * @param time
   *          The time to validate.
   * @return If valid then true else false.
   */
  public static boolean isValidJiraTime(final String time) {
    return Pattern.matches(JIRA_DURATION_PATTERN, time);
  }

  /**
   * Check the Time is valid to the {@value #TIME24HOURS_PATTERN} pattern.
   *
   * @param time
   *          The time to validate.
   * @return If valid then true else false.
   */
  public static boolean isValidTime(final String time) {
    return Pattern.matches(TIME24HOURS_PATTERN, time);
  }

  /**
   * Converts JIRA duration style strings to seconds.
   *
   * @param duration
   *          JIRA duration style string
   * @return The calculated seconds.
   */
  public static int jiraDurationToSeconds(final String duration) {
    Pattern durPatt = Pattern.compile(DateTimeConverterUtil.JIRA_DURATION_PATTERN);
    Matcher m = durPatt.matcher(duration);
    int seconds = 0;
    if (m.matches()) {
      if (m.group(HOURS_GROUP) != null) {
        seconds = Integer.parseInt(m.group(HOURS_GROUP)) * DateTimeConverterUtil.MINUTES_PER_HOUR
            * DateTimeConverterUtil.SECONDS_PER_MINUTE;
      } else if (m.group(MINUTES_GROUP) != null) {
        seconds = Integer.parseInt(m.group(MINUTES_GROUP))
            * DateTimeConverterUtil.SECONDS_PER_MINUTE;
      } else if ((m.group(HOURS_GROUP_2) != null) && (m.group(MINUTES_GROUP_2) != null)) {
        seconds = Integer.parseInt(m.group(HOURS_GROUP_2)) * DateTimeConverterUtil.MINUTES_PER_HOUR
            * DateTimeConverterUtil.SECONDS_PER_MINUTE;
        seconds += Integer.parseInt(m.group(MINUTES_GROUP_2))
            * DateTimeConverterUtil.SECONDS_PER_MINUTE;
      }
    }
    return seconds;
  }

  /**
   * Convert the millisecond to String time.
   *
   * @param milliseconds
   *          The time in milliseconds.
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
   *          The spent seconds.
   * @return The result String.
   */
  public static String secondConvertToRoundedDuration(final long spentSeconds) {
    TimeTrackingConfiguration timeTrackingConfiguration =
        ComponentAccessor.getComponent(TimeTrackingConfiguration.class);
    double workDaysPerWeek = timeTrackingConfiguration.getDaysPerWeek().doubleValue();
    double workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    return DurationFormatter.roundedDuration(spentSeconds, workDaysPerWeek, workHoursPerDay);
  }

  /**
   * Convert the seconds to jira format (1h 30m) String.
   *
   * @param spentSeconds
   *          The spent seconds.
   * @return The result String.
   */
  public static String secondConvertToString(final long spentSeconds) {
    TimeTrackingConfiguration timeTrackingConfiguration =
        ComponentAccessor.getComponent(TimeTrackingConfiguration.class);
    double workDaysPerWeek = timeTrackingConfiguration.getDaysPerWeek().doubleValue();
    double workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    return DurationFormatter.exactDuration(spentSeconds, workDaysPerWeek, workHoursPerDay);
  }

  /**
   * Return a Calendar with time set by the start of the given day.
   *
   * @param date
   *          The time to set the calendar
   * @return The calendar which represents the start of the day
   */
  public static Calendar setDateToDayStart(final Date date) {
    Calendar startDate = Calendar.getInstance();
    startDate.setTime(date);
    startDate.set(Calendar.HOUR_OF_DAY, 0);
    startDate.set(Calendar.MINUTE, 0);
    startDate.set(Calendar.SECOND, 0);
    startDate.set(Calendar.MILLISECOND, 0);
    return startDate;
  }

  /**
   * Convert String Date to String Time.
   *
   * @param dateString
   *          The String date.
   * @return The String Time.
   * @throws ParseException
   *           If can't parse the date.
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
   *          The String time.
   * @return The result date.
   * @throws ParseException
   *           If can't parse the date.
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
   *          The Sting date and time.
   * @return The result Date.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static Date stringTimeToDateTimeGMT(final String time) throws ParseException {
    DateFormat formatterTime = new SimpleDateFormat(TIME_FORMAT);
    formatterTime.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
    Date dateTime = formatterTime.parse(time);
    return dateTime;
  }

  /**
   * Covert String time (hh:mm) to jira format (1h 30m) String. Use the
   * {@link DateTimeConverterUtil} methods.
   *
   * @param time
   *          The time.
   * @return The new formated String.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static String stringTimeToString(final String time) throws ParseException {
    long seconds = DateTimeConverterUtil.stringTimeToDateTimeGMT(
        time).getTime() / MILLISEC_IN_SECOND;
    String result = DateTimeConverterUtil.secondConvertToString(seconds);
    return result;
  }

  /**
   * Convert String ({@value #DATE_FORMAT}) to Date.
   *
   * @param dateString
   *          The String date to convert.
   * @return The result Date.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static Date stringToDate(final String dateString) throws ParseException {
    String dateFormat = getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_DMY);
    DateFormat formatterDate = new SimpleDateFormat(dateFormat);
    Date date = formatterDate.parse(dateString);
    return date;
  }

  /**
   * Convert String ({@value #DATE_TIME_FORMAT}) to date and time.
   *
   * @param dateAndTimeString
   *          The date time string to convert.
   * @return The result Date.
   * @throws ParseException
   *           if can't parse the date.
   */
  public static Date stringToDateAndTime(final String dateAndTimeString) throws ParseException {
    String dateTimeFormat =
        getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_COMPLETE);
    DateFormat formatterDateAndTime = new SimpleDateFormat(dateTimeFormat);
    Date date = formatterDateAndTime.parse(dateAndTimeString);
    return date;
  }

  /**
   * Private constructor.
   */
  private DateTimeConverterUtil() {
  }

}
