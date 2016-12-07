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
package org.everit.jira.core.impl;

import java.util.Date;

import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;

/**
 * Server DateTime in different TimeZones and times.
 */
public final class DateTimeServer {
  // DONE calculate variables based on the given one time.
  // DONE user DTCU methods
  // DONE cal sytem based user
  // DONE cal user based system (for worklogs etc.)
  // DONE add simple userTZ cons
  // DONE add simple userTZ + startTime cons
  // TODO add SYSTEM/USER settings

  /**
   * Simple instance generator. Create the systemTimezone based on the given Date Time.
   *
   * @param systemTimeZone
   *          The Date Time in user Time Zone
   *
   * @return A new DateTimeServer object.
   */
  public static DateTimeServer getInstanceBasedOnSystemTimeZone(final DateTime systemTimeZone) {
    DateTimeServer dateTimeServer = new DateTimeServer();
    dateTimeServer.setSystemTimeZone(systemTimeZone);
    return dateTimeServer;
  }

  /**
   * Simple instance generator. Create the userTimeZone based on the given milliseconds.
   *
   * @param systemTimeZoneInMillis
   *          The user DateTime in millis.
   *
   * @return A new DateTimeServer object.
   */
  public static DateTimeServer getInstanceBasedOnSystemTimeZone(final long systemTimeZoneInMillis) {
    DateTime systemTimeZoneFromMilis = new DateTime(systemTimeZoneInMillis);
    systemTimeZoneFromMilis =
        systemTimeZoneFromMilis.withZoneRetainFields(TimetrackerUtil.getSystemTimeZone());
    DateTimeServer dateTimeServer = new DateTimeServer();
    dateTimeServer.setSystemTimeZone(systemTimeZoneFromMilis);
    return dateTimeServer;
  }

  /**
   * Simple instance generator. Create the userTimeZone based on the given Date Time.
   *
   * @param userTimeZone
   *          The Date Time in user Time Zone
   *
   * @return A new DateTimeServer object.
   */
  public static DateTimeServer getInstanceBasedOnUserTimeZone(final DateTime userTimeZone) {
    DateTimeServer dateTimeServer = new DateTimeServer();
    dateTimeServer.setUserTimeZone(userTimeZone);
    return dateTimeServer;
  }

  /**
   * Simple instance generator. Create the userTimeZone based on the given milliseconds.
   *
   * @param userTimeZoneInMillis
   *          The user DateTime in millis.
   *
   * @return A new DateTimeServer object.
   */
  public static DateTimeServer getInstanceBasedOnUserTimeZone(final long userTimeZoneInMillis) {
    DateTime userTimeZoneFromMillis = new DateTime(userTimeZoneInMillis);
    userTimeZoneFromMillis =
        userTimeZoneFromMillis.withZoneRetainFields(TimetrackerUtil.getLoggedUserTimeZone());
    DateTimeServer dateTimeServer = new DateTimeServer();
    dateTimeServer.setUserTimeZone(userTimeZoneFromMillis);
    return dateTimeServer;
  }

  /**
   * Get a new instace of the DateTimeServer baed on the actual server time.
   * 
   * @return A new DateTimeServer object.
   */
  public static DateTimeServer getInstanceSystemNow() {
    DateTimeServer dateTimeServer = new DateTimeServer();
    dateTimeServer.setSystemTimeZone(new DateTime(TimetrackerUtil.getSystemTimeZone()));
    return dateTimeServer;
  }

  private DateTime systemTimeZone;

  private DateTime systemTimeZoneDayStart;

  private DateTime userTimeZone;

  private DateTime userTimeZoneDayStart;

  private DateTimeServer() {

  }

  /**
   * Add startTime (date time part) to DateTimeServer. Make the calculations based on user TimeZone
   * dateTime.
   *
   * @param startTime
   *          The new time value.
   * @return The modified DateTimeServer.
   */
  public DateTimeServer addStartTime(final String startTime) {
    setUserTimeZone(DateTimeConverterUtil.stringToDateAndTime(userTimeZone, startTime));
    return this;
  }

  private void calculateBasedOnSystemTimeZone() {
    userTimeZone = DateTimeConverterUtil.convertDateZoneToUserTimeZone(systemTimeZone);
    // TODO do i have to convert day starts?
    systemTimeZoneDayStart = DateTimeConverterUtil.setDateToDayStart(systemTimeZone);
    userTimeZoneDayStart =
        DateTimeConverterUtil.convertDateZoneToSystemTimeZone(systemTimeZoneDayStart);
  }

  private void calculateBasedOnUserTimeZone() {
    // UTZ +14
    // STZ +1
    // userTZ = 15:00
    // systemTZ = 02:00
    // usersDS = 00:00
    // systemDS = 11:00 (day before)
    systemTimeZone = DateTimeConverterUtil.convertDateZoneToSystemTimeZone(userTimeZone);
    userTimeZoneDayStart = DateTimeConverterUtil.setDateToDayStart(userTimeZone);
    systemTimeZoneDayStart =
        DateTimeConverterUtil.convertDateZoneToSystemTimeZone(userTimeZoneDayStart);
  }

  public DateTime getSystemTimeZone() {
    return systemTimeZone;
  }

  public Date getSystemTimeZoneDate() {
    return DateTimeConverterUtil.convertDateTimeToDate(systemTimeZone);
  }

  public DateTime getSystemTimeZoneDayStart() {
    return systemTimeZoneDayStart;
  }

  public Date getSystemTimeZoneDayStartDate() {
    return DateTimeConverterUtil.convertDateTimeToDate(systemTimeZoneDayStart);
  }

  public DateTime getUserTimeZone() {
    return userTimeZone;
  }

  public Date getUserTimeZoneDate() {
    return DateTimeConverterUtil.convertDateTimeToDate(userTimeZone);
  }

  public DateTime getUserTimeZoneDayStart() {
    return userTimeZoneDayStart;
  }

  public Date getUserTimeZoneDayStartDate() {
    return DateTimeConverterUtil.convertDateTimeToDate(userTimeZoneDayStart);
  }

  private void setSystemTimeZone(final DateTime systemTimeZone) {
    this.systemTimeZone = systemTimeZone;
    calculateBasedOnSystemTimeZone();
  }

  private void setUserTimeZone(final DateTime userTimeZone) {
    this.userTimeZone = userTimeZone;
    calculateBasedOnUserTimeZone();
  }

}
