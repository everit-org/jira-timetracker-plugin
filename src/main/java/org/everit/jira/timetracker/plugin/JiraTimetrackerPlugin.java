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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.issue.Issue;

/**
 * The JiraTimetrackerPlugin interface.
 */
public interface JiraTimetrackerPlugin {
  /**
   * Count the real work days in a week.
   *
   * @param weekDaysAsString
   *          the days of the week in yyyy-hh-mm format.
   *
   * @return the counted real work days number.
   */
  double countRealWorkDaysInWeek(final List<String> weekDaysAsString,
      final Set<String> exludeDates, final Set<String> includeDates);

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
   * @throws GenericEntityException
   *           GenericEntityException
   */
  Date firstMissingWorklogsDate(Set<String> exludeDates, Set<String> includeDates)
      throws GenericEntityException;

  /**
   * Create a query and give back the list of dates where are no worklogs. The query examine the
   * days between the user creation date and the current date. The method not examine the weekends
   * and the properties file exclude dates but check the properties file include dates.
   *
   * @param selectedUser
   *          The selected User.
   * @param from
   *          The query from parameter.
   * @param to
   *          The query to parameter.
   * @param workingHours
   *          The report have to check the spent time or not.
   * @param nonWorking
   *          Exclude or not the non-working issues.
   * @param settings
   *          TODO
   * @return The list of the MissingsWorklogsDTO.
   * @throws GenericEntityException
   *           If GenericEntity Exception.
   */
  List<MissingsWorklogsDTO> getDates(String selectedUser, Date from, Date to, boolean workingHours,
      boolean nonWorking, TimeTrackerGlobalSettings settings) throws GenericEntityException;

  /**
   * The method find the exclude dates of the given date month.
   *
   * @param date
   *          The date.
   * @param exludeDates
   *          the excluded dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getExcludeDaysOfTheMonth(Date date, Set<String> exludeDates);

  /**
   * The method find the include dates of the given date month.
   *
   * @param date
   *          The date.
   * @param exludeDates
   *          the excluded dates.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getIncludeDaysOfTheMonth(Date date, Set<String> includeDates);

  /**
   * Give back the Issues.
   *
   * @return Whit the Issues.
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  List<Issue> getIssues() throws GenericEntityException;

  /**
   * The method find the logged days of the given date month.
   *
   * @param date
   *          The date.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getLoggedDaysOfTheMonth(Date date)
      throws GenericEntityException;

  /**
   * Get Piwik build property value by key.
   *
   * @param key
   *          The porperty key.
   * @return The property value.
   */
  String getPiwikPorperty(String key);

  /**
   * Give back the Projects.
   *
   * @return whit Projects.
   */
  List<String> getProjectsId();

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

  /**
   * Send a email through the Jira to the given FEEDBACK_EMAIL_TO address. The address come form the
   * jttp_build.properties. If no mail address was set by build the method only log the mail. If the
   * method not find setted mail server and default from mail address, the email not will be send
   * just logged.
   *
   * @param mailSubject
   *          The subject of the mail.
   * @param mailBody
   *          The body of the mail.
   */
  void sendEmail(String mailSubject, String mailBody);

  /**
   * Give back the all worklogs spent time between the two date.
   *
   * @param startSummary
   *          The start date.
   * @param finishSummary
   *          The finish date.
   * @param issueIds
   *          The filtered issues ids. If null or empty then don't make filtered summary.
   * @return The summary spent time in seconds.
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  long summary(Date startSummary, Date finishSummary, List<Pattern> issueIds)
      throws GenericEntityException;

}
