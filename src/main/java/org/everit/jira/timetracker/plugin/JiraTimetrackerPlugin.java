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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;

/**
 * The JiraTimetrackerPlugin interface.
 */
public interface JiraTimetrackerPlugin {

  /**
   * Create a worklog whit the given parameters.
   *
   * @param issueId
   *          The ID of the Issue.
   * @param comment
   *          The note of the worklog.
   * @param date
   *          The date of the worklog. (yyyy-MM-dd)
   * @param startTime
   *          The start time of the worklog. (kk:mm)
   * @param timeSpent
   *          The spent time in the worklog (Jira format : 1h 30m)
   * @return {@link ActionResult} Success if the worklog created and Fail if not.
   */
  ActionResult createWorklog(String issueId, String comment, String date,
      String startTime, String timeSpent);

  /**
   * Delete the worklog whit the given worklog ID.
   *
   * @param worklogId
   *          The ID of the worklog.
   * @return {@link ActionResult} Success if the worklog deleted and Fail if not.
   */
  ActionResult deleteWorklog(Long worklogId);

  /**
   * Edit an existing worklog whit the given parameters.
   *
   * @param worklogId
   *          The ID of the worklog.
   * @param issueId
   *          The worklog Issue.
   * @param comment
   *          The worklog note.
   * @param dateFormatted
   *          The date of the worklog (yyyy-MM-dd).
   * @param time
   *          When start the worklog. (kk:mm)
   * @param timeSpent
   *          The spent time in the worklog (Jira format : 1h 30m)
   * @return {@link ActionResult} Success if the worklog edited and Fail if not.
   */
  ActionResult editWorklog(Long worklogId, String issueId, String comment,
      String dateFormatted, String time, String timeSpent);

  /**
   * Give back the date of the first day where missing worklogs. Use the properties files includes
   * and excludes date settings.
   *
   * @param selectedUser
   *          The selected User.
   * @return The Date representation of the day.
   * @throws GenericEntityException
   *           GenericEntityException
   */
  Date firstMissingWorklogsDate(String selectedUser) throws GenericEntityException;

  /**
   * Give back the collector issue patterns. If the list will be null, then give back the propeties
   * file default collecter issues patterns list.
   */
  List<Pattern> getCollectorIssuePatterns();

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
   *
   * @return The list of the dates.
   * @throws GenericEntityException
   *           If GenericEntity Exception.
   */
  List<Date> getDates(String selectedUser, Date from, Date to, boolean workingHours,
      boolean nonWorking) throws GenericEntityException;

  /**
   * The method find the exclude date of the given date month.
   *
   * @param date
   *          The date.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getExcludeDaysOfTheMonth(String date);

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
   * @param selectedUser
   *          The selected User.
   * @param date
   *          The date.
   * @return The list of the days in String format. (Eg. ["12","15"])
   */
  List<String> getLoggedDaysOfTheMonth(String selectedUser, Date date)
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
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  List<String> getProjectsId() throws GenericEntityException;

  /**
   * Give back the Worklog by ID.
   *
   * @param worklogId
   *          The ID of the worklog.
   * @return The result {@link EveritWorklog}.
   * @throws ParseException
   *           If cannot parse the worklog date.
   */
  EveritWorklog getWorklog(Long worklogId) throws ParseException;

  /**
   * Give back the days all worklog of the selectedUser. If selectedUser null or empty the actual
   * logged in user will used.
   *
   * @param date
   *          The date.
   * @param selectedUser
   *          The selected User.
   * @return The list of the date all worklogs.
   * @throws GenericEntityException
   *           GenericEntityException .
   * @throws ParseException
   *           When can't parse the worklog date.
   */
  List<EveritWorklog> getWorklogs(String selectedUser, Date startDate, Date endDate)
      throws DataAccessException,
      SQLException,
      ParseException, GenericEntityException;

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
   * Give back the plugin settings values.
   *
   * @return {@link PluginSettingsValues} object what contains the settings.
   */
  PluginSettingsValues loadPluginSettings();

  /**
   * Set the plugin settings and save them.
   *
   * @param pluginSettingsParameter
   *          The plugin settings parameters.
   * @return {@link ActionResult} if the plugin settings was saved successful SUCCESS else FAIL.
   */
  void savePluginSettings(PluginSettingsValues pluginSettingsParameter);

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
   * @param selectedUser
   *          The seleced User.
   * @param startSummary
   *          The start date.
   * @param finishSummary
   *          The finish date.
   * @param issueIds
   *          The filtered issues ids. If null or empty then don't make filtered summary.
   * @return The summary spent time in Jira format (1h 30m)
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  String summary(String selectedUser, Date startSummary, Date finishSummary, List<Pattern> issueIds)
      throws GenericEntityException;

  /**
   * Validate the start and end time changer buttons values. The acceptable values: 1, 5, 10, 15,
   * 20, 30.
   *
   * @param changeValue
   *          The new value.
   * @return True if the value acceptable else false.
   * @throws NumberFormatException
   *           If the value can't parse to int.
   */
  boolean validateTimeChange(final String changeValue)
      throws NumberFormatException;

}
