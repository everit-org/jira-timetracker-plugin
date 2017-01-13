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
package org.everit.jira.timetracker.plugin.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.impl.WorklogComponent;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.exception.WorklogException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * The utility class of date and time conversions.
 */
public final class DateTimeConverterUtil {

  /**
   * The begin of year.
   */
  public static final int BEGIN_OF_YEAR = 1900;

  /**
   * The date time format.
   */
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

  /**
   * The number of days per week.
   */
  public static final int DAYS_PER_WEEK = 7;

  /**
   * The fix date time format for exclude and include dates.
   */
  public static final String FIX_DATE_TIME_FORMAT = "yyyy-MM-dd";

  /**
   * The fix date time format for duration value.
   */
  private static final String FIX_TIME_FORMAT = "HH:mm";

  /**
   * The hour eight.
   */
  public static final int HOUR_EIGHT = 8;

  private static final int HOURS_GROUP = 8;

  private static final int HOURS_GROUP_2 = 2;

  /**
   * The number of hours in day.
   */
  public static final int HOURS_IN_DAY = 24;

  /**
   * The JIRA duration pattern.
   */
  public static final String JIRA_DURATION_PATTERN = "(([01]?[0-9]|2[0-3])[h]*[\\s]+(([0-9]{1,3}|"
      + "1[0-3][0-9]{2}|14[0-3][0-9])[m])*)|"
      + "(([0-9]{1,3}|1[0-3][0-9]{2}|14[0-3][0-9])[m])+|(([01]?[0-9]|2[0-3])[h])+";

  private static final int MILLISEC_IN_SECOND = 1000;

  /**
   * The number of milliseconds per seconds.
   */
  public static final int MILLISECONDS_PER_SECOND = 1000;

  /**
   * The number of mins in quater.
   */
  public static final int MINS_IN_QUATER = 15;

  private static final int MINUTES_GROUP = 6;

  private static final int MINUTES_GROUP_2 = 4;

  /**
   * The number of minutes per hour.
   */
  public static final int MINUTES_PER_HOUR = 60;

  /**
   * The number of quaters in hour.
   */
  public static final int QUATERS_IN_HOUR = 4;

  /**
   * The number of seconds per minute.
   */
  public static final int SECONDS_PER_MINUTE = 60;

  /**
   * The 24 hours pattern.
   */
  public static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

  private static final int YEAR_1900 = 1900;

  /**
   * Convert the Timestamp to system timezone, cahnge the Timezone o user timezone and convert back
   * to a new Timestamp.
   *
   * @param systemTimestamp
   *          The original Timesatamp in system TimeZone.
   * @return The new Timestamp in user TimeZone.
   */
  public static Timestamp addTimeZoneToTimestamp(final Timestamp systemTimestamp) {
    if (systemTimestamp == null) {
      return null;
    }
    DateTimeServer converter =
        DateTimeServer.getInstanceBasedOnSystemTimeZone(systemTimestamp.getTime());
    return new Timestamp(converter.getUserTimeZoneDate().getTime());
  }

  /**
   * Convert joda DateTime to java Date. Convert the date and time without Time Zone correction.
   * (the joda DateTime toDate metod add the time zone).
   *
   * @param dateTime
   *          The dateTime.
   * @return The new date.
   */
  @SuppressWarnings("deprecation")
  public static Date convertDateTimeToDate(final DateTime dateTime) {
    return new Date(
        dateTime.getYear() - YEAR_1900,
        dateTime.getMonthOfYear() - 1,
        dateTime.getDayOfMonth(),
        dateTime.getHourOfDay(),
        dateTime.getMinuteOfHour(),
        dateTime.getSecondOfMinute());
  }

  /**
   * Convert the give date to the system TimeZone.
   *
   * @param date
   *          The origonal date
   * @return The date in the System TimeZone.
   */
  public static DateTime convertDateZoneToSystemTimeZone(final DateTime date) {
    DateTime inSystemTimeZone = date.withZone(TimetrackerUtil.getSystemTimeZone());
    return inSystemTimeZone;
  }

  /**
   * Convert the give date to the user TimeZone.
   *
   * @param date
   *          The origonal date
   * @return The date in the User TimeZone.
   */
  public static DateTime convertDateZoneToUserTimeZone(final DateTime date) {
    DateTime inUserTimeZone = date.withZone(TimetrackerUtil.getLoggedUserTimeZone());
    return inUserTimeZone;
  }

  /**
   * Count the worklog end time.
   *
   * @param start
   *          When start the worklog.
   * @param spentMilliseconds
   *          The spent time in milliseconds.
   * @return The string format of the end time.
   * @throws IllegalArgumentException
   *           When can't parse the time.
   */
  public static String countEndTime(final String start, final long spentMilliseconds)
      throws IllegalArgumentException {
    long startMillisecond = DateTimeConverterUtil.stringTimeToDateTime(start).getTime();
    long endMillisecond = startMillisecond + spentMilliseconds;
    return DateTimeConverterUtil.dateTimeToString(new Date(endMillisecond));
  }

  /**
   * Convert date to string use the {@link APKeys#JIRA_LF_DATE_COMPLETE}.
   *
   * @param dateAndTime
   *          The date to convert.
   * @return The result String.
   */
  public static String dateAndTimeToString(final Date dateAndTime) {
    String dateTimeFormat =
        DateTimeConverterUtil.getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_COMPLETE);
    DateFormat formatterDateAndTime =
        new SimpleDateFormat(dateTimeFormat, DateTimeConverterUtil.getLoggedUserLocal());
    String stringDateAndTime = formatterDateAndTime.format(dateAndTime);
    return stringDateAndTime;
  }

  /**
   * Convert the date time to string ({@link DateTimeStyle#TIME}).
   *
   * @param date
   *          The time to convert.
   * @return The result string.
   */
  public static String dateTimeToString(final Date date) {
    DateTimeFormatter dateTimeTimeFormatter = DateTimeConverterUtil.getDateTimeTimeFormatter();
    return dateTimeTimeFormatter.format(date);
  }

  /**
   * Convert the date time to string with ({@link FIX_TIME_FORMAT}).
   *
   * @param date
   *          The time to convert.
   * @return The result string.
   */
  public static String dateTimeToStringWithFixFormat(final Date date) {
    DateFormat formatterDate = new SimpleDateFormat(FIX_TIME_FORMAT);
    formatterDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    return formatterDate.format(date);
  }

  /**
   * Convert the date to String use the fix date format "YYYY-MM-DD".
   *
   * @param date
   *          The Date to convert.
   * @return The result time.
   */
  public static String dateToFixFormatString(final Date date) {
    DateFormat formatterDate = new SimpleDateFormat(FIX_DATE_TIME_FORMAT);
    String dateString = formatterDate.format(date);
    return dateString;
  }

  public static String dateToFixFormatString(final DateTime date) {
    DateFormat formatterDate = new SimpleDateFormat(FIX_DATE_TIME_FORMAT);
    String dateString = formatterDate.format(date);
    return dateString;
  }

  /**
   * Convert the date to String use the {@link APKeys#JIRA_LF_DATE_DMY}.
   *
   * @param date
   *          The Date to convert.
   * @return The result time.
   */
  public static String dateToString(final Date date) {
    String dateFormat =
        DateTimeConverterUtil.getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_DMY);
    DateFormat formatterDate =
        new SimpleDateFormat(dateFormat, DateTimeConverterUtil.getLoggedUserLocal());
    String dateString = formatterDate.format(date);
    return dateString;
  }

  /**
   * Convert String to Date use the fix date format "YYYY-MM-DD".
   *
   * @param dateString
   *          The String date to convert.
   * @return The result Date.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static Date fixFormatStringToDate(final String dateString) throws ParseException {
    DateFormat formatterDate = new SimpleDateFormat(FIX_DATE_TIME_FORMAT);
    formatterDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date date = formatterDate.parse(dateString);
    return date;
  }

  /**
   * Convert String to Date use the fix date format "YYYY-MM-DD".
   *
   * @param dateString
   *          The String date to convert.
   * @return The result Date.
   * @throws ParseException
   *           If can't parse the date and the date contains invalid value e.g. 15 as month.
   */
  public static Date fixFormatStringToDateWithValidation(final String dateString)
      throws ParseException {
    DateFormat formatterDate = new SimpleDateFormat(FIX_DATE_TIME_FORMAT);
    formatterDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date date = formatterDate.parse(dateString);
    if (!dateString.equals(formatterDate.format(date))) {
      throw new ParseException("Invalid date value:" + dateString, 0);
    }
    return date;
  }

  public static DateTime fixFormatStringToUTCDateTime(final String dateString)
      throws ParseException {
    Date fixFormatStringToDate = DateTimeConverterUtil.fixFormatStringToDate(dateString);
    return new DateTime(fixFormatStringToDate.getTime(), DateTimeZone.UTC);
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

  private static DateTimeFormatter getDateTimeTimeFormatter() {
    DateTimeFormatterFactory dateTimeFormatterFactory =
        ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
    return dateTimeFormatterFactory
        .formatter()
        .forLoggedInUser()
        .withSystemZone()
        .withStyle(DateTimeStyle.TIME);
  }

  private static String getJiraDefaultDateAndTimeJavaFormat(final String formatKey) {
    ApplicationProperties applicationProperties =
        ComponentAccessor.getComponent(ApplicationProperties.class);
    return applicationProperties.getDefaultBackedString(formatKey);
  }

  private static Locale getLoggedUserLocal() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    return authenticationContext.getI18nHelper().getLocale();
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
    boolean match24Format = Pattern.matches(TIME24HOURS_PATTERN, time);
    StringBuilder sb = new StringBuilder();
    sb.append("^([01]?[0-9]|2[0-3]):[0-5][0-9]( (");
    Locale locale = DateTimeConverterUtil.getLoggedUserLocal();
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
    String[] amPmStrings = dateFormatSymbols.getAmPmStrings();
    int index = 0;
    for (String string : amPmStrings) {
      sb.append(string + "|");
      sb.append(string.toLowerCase(locale));
      if (index < (amPmStrings.length - 1)) {
        sb.append("|");
      }
      index++;
    }
    sb.append("))$");
    boolean matchAmPmFormat = Pattern.matches(sb.toString(), time);
    return match24Format || matchAmPmFormat;

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
   * Return a Calendar with time set by the start of the given day.
   *
   * @param date
   *          The time to set the calendar
   * @return The calendar which represents the start of the day
   */
  public static DateTime setDateToDayStart(final DateTime date) {
    DateTime dateStartOfTheDay = date.withHourOfDay(0);
    dateStartOfTheDay = dateStartOfTheDay.withMinuteOfHour(0);
    dateStartOfTheDay = dateStartOfTheDay.withSecondOfMinute(0);
    dateStartOfTheDay = dateStartOfTheDay.withMillisOfSecond(0);
    return dateStartOfTheDay;
  }

  /**
   * Convert String ({@value DateTimeStyle#TIME}) to Time.
   *
   * @param time
   *          The String time.
   * @return The result date.
   */
  public static Date stringTimeToDateTime(final String time) throws IllegalArgumentException {
    DateTimeFormatter dateTimeTimeFormatter = DateTimeConverterUtil.getDateTimeTimeFormatter();
    return dateTimeTimeFormatter.parse(time);
  }

  /**
   * Convert String with ({@value FIX_TIME_FORMAT}) to Time.
   *
   * @param time
   *          The String time.
   * @return The result date.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static Date stringTimeToDateTimeWithFixFormat(final String time) throws ParseException {
    DateFormat formatterDate = new SimpleDateFormat(FIX_TIME_FORMAT);
    formatterDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    return formatterDate.parse(time);
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
    long seconds = DateTimeConverterUtil.stringTimeToDateTimeWithFixFormat(
        time).getTime() / MILLISEC_IN_SECOND;
    String result = new DurationFormatter().exactDuration(seconds);
    return result;
  }

  /**
   * Convert String to Date use the {@link APKeys#JIRA_LF_DATE_DMY}.
   *
   * @param dateString
   *          The String date to convert.
   * @return The result Date.
   * @throws ParseException
   *           If can't parse the date.
   */
  public static Date stringToDate(final String dateString) throws ParseException {
    String dateFormat =
        DateTimeConverterUtil.getJiraDefaultDateAndTimeJavaFormat(APKeys.JIRA_LF_DATE_DMY);
    DateFormat formatterDate =
        new SimpleDateFormat(dateFormat, DateTimeConverterUtil.getLoggedUserLocal());
    Date date = formatterDate.parse(dateString);
    return date;
  }

  /**
   * Convert String to date and time and merge them. Use the stringToDate and stringTimeToDateTime
   * methods.
   *
   * @param date
   *          The date.
   * @param time
   *          The time.
   * @return The result Date.
   */
  public static Date stringToDateAndTime(final Date date, final Date time) {
    Calendar dateCalendaer = Calendar.getInstance();
    dateCalendaer.setTime(date);
    Calendar timeCalendar = Calendar.getInstance();
    timeCalendar.setTime(time);
    dateCalendaer.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
    dateCalendaer.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
    return dateCalendaer.getTime();
  }

  /**
   * Convert String to date and time and merge them. Use the stringToDate and stringTimeToDateTime
   * methods.
   *
   * @param date
   *          The date.
   * @param timeString
   *          The time string to convert.
   * @return The result Date.
   */
  public static Date stringToDateAndTime(final Date date, final String timeString)
      throws IllegalArgumentException {
    return DateTimeConverterUtil.stringToDateAndTime(date,
        DateTimeConverterUtil.stringTimeToDateTime(timeString));
  }

  /**
   * Concat DateTime date and Date time to a DateTime date based on the originalDate param.
   *
   * @param originalDate
   *          The date.
   * @param time
   *          The time.
   * @return The concated date time.
   */
  public static DateTime stringToDateAndTime(final DateTime originalDate, final Date time) {
    DateTime date;
    try {
      date = originalDate.withHourOfDay(time.getHours());
      date = date.withMinuteOfHour(time.getMinutes());
    } catch (IllegalArgumentException e) {
      throw new WorklogException(WorklogComponent.PropertiesKey.DATE_PARSE,
          originalDate + " " + time);
    }
    return date;
  }

  /**
   * Concat DateTime date and String time to a DateTime date based on the originalDate param.
   *
   * @param originalDate
   *          The date.
   * @param time
   *          The time.
   * @return The concated date time.
   */
  public static DateTime stringToDateAndTime(final DateTime originalDate, final String time) {
    Date timeDate;
    DateTime date;
    try {
      timeDate = DateTimeConverterUtil.stringTimeToDateTime(time);
      date = DateTimeConverterUtil.stringToDateAndTime(originalDate, timeDate);
    } catch (IllegalArgumentException e) {
      throw new WorklogException(WorklogComponent.PropertiesKey.DATE_PARSE,
          originalDate + " " + time);
    }
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
    DateFormat formatterDateAndTime =
        new SimpleDateFormat(DATE_TIME_FORMAT, DateTimeConverterUtil.getLoggedUserLocal());
    Date date = formatterDateAndTime.parse(dateAndTimeString);
    return date;
  }

  /**
   * Private constructor.
   */
  private DateTimeConverterUtil() {
  }

}
