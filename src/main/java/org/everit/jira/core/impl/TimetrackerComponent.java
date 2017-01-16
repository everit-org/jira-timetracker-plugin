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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
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

  private final TimeTrackerSettingsHelper settingsHelper;

  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public TimetrackerComponent(
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.settingsHelper = settingsHelper;
  }

  private int countDaysInDateTimeSet(final List<DateTime> weekDays, final Set<DateTime> dateSet) {
    int counter = 0;
    for (DateTime weekDay : weekDays) {
      if (TimetrackerUtil.containsSetTheSameDay(dateSet, weekDay)) {
        counter++;
      }
    }
    return counter;
  }

  @Override
  public double countRealWorkDaysInWeek(final List<DateTime> weekDays,
      final Set<DateTime> excludeDatesSet, final Set<DateTime> includeDatesSet) {
    int exludeDates = countDaysInDateTimeSet(weekDays, excludeDatesSet);
    int includeDates = countDaysInDateTimeSet(weekDays, includeDatesSet);
    return (timeTrackingConfiguration.getDaysPerWeek().doubleValue() - exludeDates) + includeDates;
  }

  // TODO what about the lots of convert in the fMWD method
  @Override
  public DateTime firstMissingWorklogsDate(final Set<DateTime> excludeDatesSet,
      final Set<DateTime> includeDatesSet, final DateTime currentDay) {
    DateTime scannedDate = currentDay;
    // one week
    scannedDate = scannedDate.minusDays(DateTimeConverterUtil.DAYS_PER_WEEK);
    for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
      // check excludse - pass
      if (TimetrackerUtil.containsSetTheSameDay(excludeDatesSet, scannedDate)) {
        scannedDate = scannedDate.plusDays(1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!TimetrackerUtil.containsSetTheSameDay(includeDatesSet, scannedDate)
          && ((scannedDate.getDayOfWeek() == DateTimeConstants.SUNDAY)
              || (scannedDate.getDayOfWeek() == DateTimeConstants.SATURDAY))) {
        scannedDate = scannedDate.plusDays(1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      scannedDate = DateTimeConverterUtil.setDateToDayStart(scannedDate);
      boolean isDateContainsWorklog = TimetrackerUtil
          .isContainsWorklog(new Date(scannedDate.getMillis()));
      if (!isDateContainsWorklog) {
        return scannedDate;
      } else {
        scannedDate = scannedDate.plusDays(1);
      }
    }
    // if we find everything all right then return with the current date
    return scannedDate;
  }

  @Override
  public List<DateTime> getExcludeDaysOfTheMonth(final DateTime date,
      final Set<DateTime> excludeDatesSet) {
    return getExtraDaysOfTheMonth(date, excludeDatesSet);
  }

  private List<DateTime> getExtraDaysOfTheMonth(final DateTime dateForMonth,
      final Set<DateTime> extraDates) {
    List<DateTime> resultExtraDays = new ArrayList<>();
    for (DateTime extraDate : extraDates) {
      if ((extraDate.getMonthOfYear() == dateForMonth.getMonthOfYear())
          && (extraDate.getYear() == dateForMonth.getYear())
          && (extraDate.getEra() == dateForMonth.getEra())) {
        resultExtraDays.add(extraDate);
      }
    }
    return resultExtraDays;
  }

  @Override
  public List<DateTime> getIncludeDaysOfTheMonth(final DateTime date,
      final Set<DateTime> includeDatesSet) {
    return getExtraDaysOfTheMonth(date, includeDatesSet);
  }

  @Override
  public List<String> getLoggedDaysOfTheMonth(final DateTimeServer date) {
    List<String> resultDays = new ArrayList<>();
    int dayOfMonth = 1;
    int maxDayOfMonth = date.getUserTimeZone().dayOfMonth().getMaximumValue();
    DateTimeServer startCalendar = DateTimeServer.getInstanceBasedOnUserTimeZone(
        date.getUserTimeZone().withDayOfMonth(dayOfMonth));

    while (dayOfMonth <= maxDayOfMonth) {
      if (TimetrackerUtil.isContainsWorklog(
          DateTimeConverterUtil.setDateToDayStart(startCalendar.getUserTimeZone()).toDate())) {
        resultDays.add(Integer.toString(dayOfMonth));
      }
      DateTime plusDays = startCalendar.getUserTimeZone().plusDays(1);
      startCalendar = DateTimeServer.getInstanceBasedOnUserTimeZone(plusDays);
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
