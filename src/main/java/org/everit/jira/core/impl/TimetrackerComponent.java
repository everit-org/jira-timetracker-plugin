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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;

/**
 * Implementation of {@link TimetrackerManager}.
 */
public class TimetrackerComponent implements TimetrackerManager {

  private final TimetrackerSettingsHelper settingsHelper;

  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public TimetrackerComponent(
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final TimetrackerSettingsHelper settingsHelper) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.settingsHelper = settingsHelper;
  }

  private int countDaysInDateSet(final List<Date> weekDays, final Set<Date> dateSet) {
    int counter = 0;
    for (Date weekDay : weekDays) {
      if (TimetrackerUtil.containsSetTheSameDay(dateSet, weekDay)) {
        counter++;
      }
    }
    return counter;
  }

  @Override
  public double countRealWorkDaysInWeek(final List<Date> weekDaysAsString,
      final Set<Date> excludeDatesSet, final Set<Date> includeDatesSet) {
    int exludeDates = countDaysInDateSet(weekDaysAsString, excludeDatesSet);
    int includeDates = countDaysInDateSet(weekDaysAsString, includeDatesSet);
    return (timeTrackingConfiguration.getDaysPerWeek().doubleValue() - exludeDates) + includeDates;
  }

  // TODO what about the lots of convert in the fMWD method
  @Override
  public Date firstMissingWorklogsDate(final Set<Date> excludeDatesSet,
      final Set<Date> includeDatesSet) {
    DateTime scannedDate = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
    // one week
    scannedDate = scannedDate.withDayOfYear(
        scannedDate.getDayOfYear() - DateTimeConverterUtil.DAYS_PER_WEEK);
    for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
      // convert date to String
      Date scanedDateDate = DateTimeConverterUtil.convertDateTimeToDate(scannedDate);
      // check excludse - pass
      if (TimetrackerUtil.containsSetTheSameDay(excludeDatesSet, scanedDateDate)) {
        scannedDate = scannedDate.withDayOfYear(scannedDate.getDayOfYear() + 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!TimetrackerUtil.containsSetTheSameDay(includeDatesSet, scanedDateDate)
          && ((scannedDate.getDayOfWeek() == DateTimeConstants.SUNDAY)
              || (scannedDate.getDayOfWeek() == DateTimeConstants.SATURDAY))) {
        scannedDate = scannedDate.withDayOfYear(scannedDate.getDayOfYear() + 1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      scannedDate = DateTimeConverterUtil.setDateToDayStart(scannedDate);
      boolean isDateContainsWorklog = TimetrackerUtil
          .isContainsWorklog(
              DateTimeConverterUtil.convertDateTimeToDate(
                  DateTimeConverterUtil.convertDateZoneToSystemTimeZone(scannedDate)));
      if (!isDateContainsWorklog) {
        return scanedDateDate;
      } else {
        scannedDate = scannedDate.withDayOfYear(scannedDate.getDayOfYear() + 1);
      }
    }
    // if we find everything all right then return with the current date
    return DateTimeConverterUtil.convertDateTimeToDate(scannedDate);
  }

  @Override
  public List<Date> getExcludeDaysOfTheMonth(final DateTimeServer date,
      final Set<Date> excludeDatesSet) {
    return getExtraDaysOfTheMonth(date.getUserTimeZoneDate(), excludeDatesSet);
  }

  private List<Date> getExtraDaysOfTheMonth(final Date dateForMonth, final Set<Date> extraDates) {
    List<Date> resultExtraDays = new ArrayList<>();
    Calendar dayInMonth = Calendar.getInstance();
    dayInMonth.setTime(dateForMonth);
    for (Date extraDate : extraDates) {
      Calendar currentExtraDate = Calendar.getInstance();
      currentExtraDate.setTime(extraDate);
      if ((currentExtraDate.get(Calendar.ERA) == dayInMonth.get(Calendar.ERA))
          && (currentExtraDate.get(Calendar.YEAR) == dayInMonth.get(Calendar.YEAR))
          && (currentExtraDate.get(Calendar.MONTH) == dayInMonth.get(Calendar.MONTH))) {
        resultExtraDays.add(extraDate);
      }
    }
    return resultExtraDays;
  }

  @Override
  public List<Date> getIncludeDaysOfTheMonth(final DateTimeServer date,
      final Set<Date> includeDatesSet) {
    return getExtraDaysOfTheMonth(date.getUserTimeZoneDate(), includeDatesSet);
  }

  @Override
  public List<String> getLoggedDaysOfTheMonth(final DateTimeServer date) {
    List<String> resultDays = new ArrayList<>();
    int dayOfMonth = 1;
    int maxDayOfMonth = date.getUserTimeZone().dayOfMonth().getMaximumValue();
    DateTimeServer startCalendar =
        new DateTimeServer(date.getUserTimeZone().withDayOfMonth(dayOfMonth));

    while (dayOfMonth <= maxDayOfMonth) {
      if (TimetrackerUtil.isContainsWorklog(startCalendar.getSystemTimeZoneDayStartDate())) {
        resultDays.add(Integer.toString(dayOfMonth));
      }
      DateTime plusDays = startCalendar.getUserTimeZone().plusDays(1);
      startCalendar = new DateTimeServer(plusDays);
      dayOfMonth++;
    }

    return resultDays;
  }

  @Override
  public String lastEndTime(final List<EveritWorklog> worklogs)
      throws IllegalArgumentException {
    if ((worklogs == null) || (worklogs.size() == 0)) {
      TimeTrackerUserSettings userSettings = settingsHelper.loadUserSettings();
      return userSettings.getDefaultStartTime();
    }
    String endTime = worklogs.get(0).getEndTime();
    for (int i = 1; i < worklogs.size(); i++) {
      Date endTimeDate = DateTimeConverterUtil.stringTimeToDateTime(endTime);
      Date actualDate = DateTimeConverterUtil.stringTimeToDateTime(worklogs
          .get(i).getEndTime());
      if (endTimeDate.compareTo(actualDate) == -1) {
        endTime = worklogs.get(i).getEndTime();
      }
    }
    return endTime;
  }
}
