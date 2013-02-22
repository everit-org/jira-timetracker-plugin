package org.everit.jira.timetracker.plugin;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.issue.Issue;

/**
 * The JiraTimetrackerPlugin interface.
 */
public interface JiraTimetrackerPlugin {

    /**
     * Create a worklog whit the given parameters.
     * 
     * @param issueId
     *            The ID of the Issue.
     * @param comment
     *            The note of the worklog.
     * @param date
     *            The date of the worklog. (yyyy-MM-dd)
     * @param startTime
     *            The start time of the worklog. (kk:mm)
     * @param timeSpent
     *            The spent time in the worklog (Jira format : 1h 30m)
     * @return {@link ActionResult} Success if the worklog created and Fail if not.
     */
    ActionResult createWorklog(String issueId, String comment, String date, String startTime, String timeSpent);

    /**
     * Delete the worklog whit the given worklog ID.
     * 
     * @param worklogId
     *            The ID of the worklog.
     * @return {@link ActionResult} Success if the worklog deleted and Fail if not.
     */
    ActionResult deleteWorklog(Long worklogId);

    /**
     * Edit an existing worklog whit the given parameters.
     * 
     * @param worklogId
     *            The ID of the worklog.
     * @param issueId
     *            The worklog Issue.
     * @param comment
     *            The worklog note.
     * @param time
     *            When start the worklog. (kk:mm)
     * @param timeSpent
     *            The spent time in the worklog (Jira format : 1h 30m)
     * @return {@link ActionResult} Success if the worklog edited and Fail if not.
     */
    ActionResult editWorklog(Long worklogId, String issueId, String comment, String time, String timeSpent);

    /**
     * Give back the Issues.
     * 
     * @return Whit the Issues.
     * @throws GenericEntityException
     *             GenericEntityException.
     */
    List<Issue> getIssues() throws GenericEntityException;

    /**
     * Give back the Projects.
     * 
     * @return whit Projects.
     * @throws GenericEntityException
     *             GenericEntityException.
     */
    List<String> getProjectsId() throws GenericEntityException;

    /**
     * Give back the Worklog by ID.
     * 
     * @param worklogId
     *            The ID of the worklog.
     * @return The result {@link EveritWorklog}.
     * @throws ParseException
     *             If cannot parse the worklog date.
     */
    EveritWorklog getWorklog(Long worklogId) throws ParseException;

    /**
     * Give back the days all worklog.
     * 
     * @param date
     *            The date.
     * @return The list of the date all worklogs.
     * @throws GenericEntityException
     *             GenericEntityException .
     * @throws ParseException
     *             When can't parse the worklog date.
     */
    List<EveritWorklog> getWorklogs(Date date) throws GenericEntityException, ParseException;

    /**
     * Give back the biggest end time of the date after worklogs method. Or give back 08:00.
     * 
     * @param worklogs
     *            The worklogs.
     * @return The last end time.
     * @throws ParseException
     *             When can't parse the worklog date.
     */
    String lastEndTime(List<EveritWorklog> worklogs) throws ParseException;

    /**
     * Give back the all worklogs spent time between the two date.
     * 
     * @param startSummary
     *            The start date.
     * @param finishSummary
     *            The finish date.
     * @return The summary spent time in Jira format (1h 30m)
     * @throws GenericEntityException
     *             GenericEntityException.
     */
    String summary(Date startSummary, Date finishSummary) throws GenericEntityException;
}
