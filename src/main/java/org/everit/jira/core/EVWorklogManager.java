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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.exception.DataAccessException;

/**
 * The EVWorklogManager is used to retrieve, create, edit, and remove work logs in JIRA.
 */
public interface EVWorklogManager {

  /**
   * Count worklog size without permission check between start and end date for the logged user.
   *
   * @param startDate
   *          the start date.
   * @param endDate
   *          the end date.
   * @return the size of worklogs.
   */
  long countWorklogsWithoutPermissionChecks(Date startDate, Date endDate);

  /**
   * Creates a worklog based on given parameters.
   *
   * @param issueId
   *          The id of the issue.
   * @param comment
   *          The note of the worklog.
   * @param date
   *          The date of the worklog.
   * @param startTime
   *          The start time of the worklog. (hh:mm)
   * @param timeSpent
   *          The spent time in the worklog (JIRA format : 1h 30m)
   * @return {@link ActionResult} Success if the worklog created and Fail if not.
   */
  ActionResult createWorklog(String issueId, String comment, Date date,
      String startTime, String timeSpent);

  /**
   * Deletes the worklog based on worklog id.
   *
   * @param worklogId
   *          The id of the worklog.
   * @return {@link ActionResult} Success if the worklog deleted and Fail if not.
   */
  ActionResult deleteWorklog(Long worklogId);

  /**
   * Edit an existing worklog whit the given parameters.
   *
   * @param worklogId
   *          The id of the worklog.
   * @param issueId
   *          The worklog issue.
   * @param comment
   *          The worklog note.
   * @param date
   *          The date of the worklog.
   * @param time
   *          When start the worklog. (hh:mm)
   * @param timeSpent
   *          The spent time in the worklog (JIRA format : 1h 30m)
   * @return {@link ActionResult} Success if the worklog edited and Fail if not.
   */
  ActionResult editWorklog(Long worklogId, String issueId, String comment,
      Date date, String time, String timeSpent);

  /**
   * Give back the Worklog based on worklog id.
   *
   * @param worklogId
   *          The id of the worklog.
   * @return The result {@link EveritWorklog}.
   * @throws ParseException
   *           If cannot parse the worklog date.
   */
  EveritWorklog getWorklog(Long worklogId) throws ParseException;

  /**
   * Give back the days all worklog of the selectedUser. If selectedUser null or empty the actual
   * logged in user will used.
   *
   * @param startDate
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
      throws DataAccessException, SQLException, ParseException, GenericEntityException;
}
