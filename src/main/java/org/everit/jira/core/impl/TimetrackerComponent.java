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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;

/**
 * Implementation of {@link TimetrackerManager}.
 */
public class TimetrackerComponent implements TimetrackerManager {

  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public TimetrackerComponent(
      final TimeTrackingConfiguration timeTrackingConfiguration) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
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

  @Override
  public Date firstMissingWorklogsDate(final Set<Date> excludeDatesSet,
      final Set<Date> includeDatesSet) {
    Calendar scannedDate = Calendar.getInstance();
    // one week
    scannedDate.set(Calendar.DAY_OF_YEAR,
        scannedDate.get(Calendar.DAY_OF_YEAR) - DateTimeConverterUtil.DAYS_PER_WEEK);
    for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
      // convert date to String
      Date scanedDateDate = scannedDate.getTime();
      // check excludse - pass
      if (TimetrackerUtil.containsSetTheSameDay(excludeDatesSet, scanedDateDate)) {
        scannedDate.set(Calendar.DAY_OF_YEAR, scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!TimetrackerUtil.containsSetTheSameDay(includeDatesSet, scanedDateDate)
          && ((scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
              || (scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))) {
        scannedDate.set(Calendar.DAY_OF_YEAR,
            scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      boolean isDateContainsWorklog = TimetrackerUtil.isContainsWorklog(scanedDateDate);
      if (!isDateContainsWorklog) {
        return scanedDateDate;
      } else {
        scannedDate.set(Calendar.DAY_OF_YEAR,
            scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
      }
    }
    // if we find everything all right then return with the current date
    return scannedDate.getTime();
  }

  @Override
  public List<Date> getExcludeDaysOfTheMonth(final Date date, final Set<Date> excludeDatesSet) {
    return getExtraDaysOfTheMonth(date, excludeDatesSet);
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
  public List<Date> getIncludeDaysOfTheMonth(final Date date, final Set<Date> includeDatesSet) {
    return getExtraDaysOfTheMonth(date, includeDatesSet);
  }

  @Override
  public List<String> getLoggedDaysOfTheMonth(final Date date) {
    List<String> resultDays = new ArrayList<>();
    int dayOfMonth = 1;
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTime(date);
    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    Date start = startCalendar.getTime();

    int maxDayOfMonth = startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    while (dayOfMonth <= maxDayOfMonth) {
      if (TimetrackerUtil.isContainsWorklog(start)) {
        resultDays.add(Integer.toString(dayOfMonth));
      }
      startCalendar.set(Calendar.DAY_OF_MONTH, ++dayOfMonth);
      start = startCalendar.getTime();
    }

    return resultDays;
  }

  @Override
  public String lastEndTime(final List<EveritWorklog> worklogs)
      throws ParseException {
    if ((worklogs == null) || (worklogs.size() == 0)) {
      return "08:00";
    }
    String endTime = worklogs.get(0).getEndTime();
    for (int i = 1; i < worklogs.size(); i++) {
      Date first = DateTimeConverterUtil.stringTimeToDateTime(worklogs
          .get(i - 1).getEndTime());
      Date second = DateTimeConverterUtil.stringTimeToDateTime(worklogs
          .get(i).getEndTime());
      if (first.compareTo(second) == 1) {
        endTime = worklogs.get(i - 1).getEndTime();
      } else {
        endTime = worklogs.get(i).getEndTime();
      }
    }
    return endTime;
  }
}
