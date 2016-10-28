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
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;

public class TimetrackerComponent implements TimetrackerManager {

  private static final int DATE_LENGTH = 7;

  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public TimetrackerComponent(
      final TimeTrackingConfiguration timeTrackingConfiguration) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
  }

  private int countDaysInDateSet(final List<String> weekDaysAsString, final Set<String> dateSet) {
    int counter = 0;
    for (String weekDay : weekDaysAsString) {
      if (dateSet.contains(weekDay)) {
        counter++;
      }
    }
    return counter;
  }

  @Override
  public double countRealWorkDaysInWeek(final List<String> weekDaysAsString,
      final Set<String> excludeDatesSet, final Set<String> includeDatesSet) {
    int exludeDates = countDaysInDateSet(weekDaysAsString, excludeDatesSet);
    int includeDates = countDaysInDateSet(weekDaysAsString, includeDatesSet);
    return (timeTrackingConfiguration.getDaysPerWeek().doubleValue() - exludeDates) + includeDates;
  }

  @Override
  public Date firstMissingWorklogsDate(final Set<String> excludeDatesSet,
      final Set<String> includeDatesSet) throws GenericEntityException {
    Calendar scannedDate = Calendar.getInstance();
    // one week
    scannedDate.set(Calendar.DAY_OF_YEAR,
        scannedDate.get(Calendar.DAY_OF_YEAR) - DateTimeConverterUtil.DAYS_PER_WEEK);
    for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
      // convert date to String
      Date scanedDateDate = scannedDate.getTime();
      String scanedDateString = DateTimeConverterUtil.dateToFixFormatString(scanedDateDate);
      // check excludse - pass
      if (excludeDatesSet.contains(scanedDateString)) {
        scannedDate.set(Calendar.DAY_OF_YEAR, scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!includeDatesSet.contains(scanedDateString)
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
  public List<String> getExcludeDaysOfTheMonth(final Date date, final Set<String> excludeDatesSet) {
    return getExtraDaysOfTheMonth(date, excludeDatesSet);
  }

  private List<String> getExtraDaysOfTheMonth(final Date date, final Set<String> dates) {
    String fixDate = DateTimeConverterUtil.dateToFixFormatString(date);
    List<String> resultExtraDays = new ArrayList<>();
    for (String extraDate : dates) {
      // TODO this if not handle the 2013-4-04 date..... this is wrong or
      // not? .... think about it.
      if (extraDate.startsWith(fixDate.substring(0, DATE_LENGTH))) {
        resultExtraDays.add(extraDate.substring(extraDate.length() - 2));
      }
    }
    return resultExtraDays;
  }

  @Override
  public List<String> getIncludeDaysOfTheMonth(final Date date, final Set<String> includeDatesSet) {
    return getExtraDaysOfTheMonth(date, includeDatesSet);
  }

  @Override
  public List<String> getLoggedDaysOfTheMonth(final Date date)
      throws GenericEntityException {
    List<String> resultDays = new ArrayList<>();
    int dayOfMonth = 1;
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTime(date);
    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    Date start = startCalendar.getTime();

    while (dayOfMonth <= DateTimeConverterUtil.LAST_DAY_OF_MONTH) {
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
