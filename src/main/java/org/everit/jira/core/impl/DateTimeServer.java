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

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;

/**
 * Server DateTime in different TimeZones and times.
 */
public class DateTimeServer {
  // DONE calculate variables based on the given one time.
  // DONE user DTCU methods
  // DONE cal sytem based user
  // DONE cal user based system (for worklogs etc.)
  // DONE add simple userTZ cons
  // DONE add simple userTZ + startTime cons
  // TODO add SYSTEM/USER settings

  private DateTime systemTimeZone;

  private DateTime systemTimeZoneDayStart;

  private DateTime userTimeZone;

  private DateTime userTimeZoneDayStart;

  private DateTimeServer() {
  }

  // FIXME userTimeZone as long?
  public DateTimeServer(final DateTime userTimeZone) {
    this.userTimeZone = userTimeZone;
    calculateBasedOnUserTimeZone();
  }

  /**
   * Constuctor for DateTimeServer. Set the user DateTime time to startTime value. Make the
   * calculations based on user TimeZone dateTime.
   *
   * @param userTimeZone
   *          The date in the user Time Zone.
   * @param startTime
   *          The new time value.
   */
  // FIXME start tome maybe Date?
  public DateTimeServer(final DateTime userTimeZone, final String startTime) {
    this.userTimeZone = userTimeZone;
    // TODO add startTime to userTZ
    calculateBasedOnUserTimeZone();
  }

  private void calculateBasedOnSystemTimeZone() {
    userTimeZone = DateTimeConverterUtil.convertDateZoneToUserTimeZone(systemTimeZone);
    // TODO do i have to convert day starts?
    systemTimeZoneDayStart = DateTimeConverterUtil.setDateToDayStart(systemTimeZone);
    userTimeZoneDayStart =
        DateTimeConverterUtil.convertDateZoneToSystemTimeZone(systemTimeZoneDayStart);
  }

  private void calculateBasedOnUserTimeZone() {
    systemTimeZone = DateTimeConverterUtil.convertDateZoneToSystemTimeZone(userTimeZone);
    userTimeZoneDayStart = DateTimeConverterUtil.setDateToDayStart(userTimeZone);
    systemTimeZoneDayStart =
        DateTimeConverterUtil.convertDateZoneToSystemTimeZone(userTimeZoneDayStart);
  }

  public DateTime getSystemTimeZone() {
    return systemTimeZone;
  }

  public DateTime getSystemTimeZoneDayStart() {
    return systemTimeZoneDayStart;
  }

  public DateTime getUserTimeZone() {
    return userTimeZone;
  }

  public DateTime getUserTimeZoneDayStart() {
    return userTimeZoneDayStart;
  }

  public void setSystemTimeZone(final DateTime systemTimeZone) {
    this.systemTimeZone = systemTimeZone;
    calculateBasedOnSystemTimeZone();
  }

  public void setUserTimeZone(final DateTime userTimeZone) {
    this.userTimeZone = userTimeZone;
    calculateBasedOnUserTimeZone();
  }

}
