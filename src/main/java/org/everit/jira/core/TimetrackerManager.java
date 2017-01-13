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
package org.everit.jira.core;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.joda.time.DateTime;

/**
 * Provides information for timetracker.
 */
public interface TimetrackerManager {

  /**
   * Count the real work days in a week.
   *
   * @param weekDaysAsString
   *          the days of the week in yyyy-hh-mm format.
   *
   * @return the counted real work days number.
   */
  double countRealWorkDaysInWeek(final List<DateTime> weekDaysAsString,
      final Set<DateTime> exludeDates, final Set<DateTime> includeDates);

  /**
   * Give back the date of the first day where missing worklogs. Use the properties files includes
   * and excludes date settings.
   *
   * @param exludeDates
   *          the excluded dates.
   * @param includeDates
   *          the included dates.
   *
   * @return The Date representation of the day.
   */
  Date firstMissingWorklogsDate(final Set<DateTime> excludeDatesSet,
      final Set<DateTime> includeDatesSet);

  /**
   * The method find the exclude dates of the given date month.
   *
   * @param date
   *          The date in User TimeZone.
   * @param exludeDates
   *          the excluded dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<DateTime> getExcludeDaysOfTheMonth(DateTime date, Set<DateTime> exludeDates);

  /**
   * The method find the include dates of the given date month.
   *
   * @param dateTime
   *          The date in User TimeZone.
   * @param includeDates
   *          the include dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<DateTime> getIncludeDaysOfTheMonth(DateTime dateTime, Set<DateTime> includeDates);

  /**
   * The method find the logged days of the given date month.
   *
   * @param date
   *          The date in DateTime with User Time Zone.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getLoggedDaysOfTheMonth(DateTimeServer date);

  /**
   * Give back the biggest end time of the date after worklogs method. Or give back 08:00.
   *
   * @param worklogs
   *          The worklogs.
   * @return The last end time.
   * @throws ParseException
   *           When can't parse the worklog date.
   */
  String lastEndTime(List<EveritWorklog> worklogs) throws ParseException;
}
