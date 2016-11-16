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

import org.everit.jira.timetracker.plugin.dto.EveritWorklog;

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
  double countRealWorkDaysInWeek(final List<Date> weekDaysAsString,
      final Set<Date> exludeDates, final Set<Date> includeDates);

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
  Date firstMissingWorklogsDate(Set<Date> exludeDates, Set<Date> includeDates);

  /**
   * The method find the exclude dates of the given date month.
   *
   * @param date
   *          The date.
   * @param exludeDates
   *          the excluded dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<Date> getExcludeDaysOfTheMonth(Date date, Set<Date> exludeDates);

  /**
   * The method find the include dates of the given date month.
   *
   * @param date
   *          The date.
   * @param exludeDates
   *          the excluded dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<Date> getIncludeDaysOfTheMonth(Date date, Set<Date> includeDates);

  /**
   * The method find the logged days of the given date month.
   *
   * @param date
   *          The date.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getLoggedDaysOfTheMonth(Date date);

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
