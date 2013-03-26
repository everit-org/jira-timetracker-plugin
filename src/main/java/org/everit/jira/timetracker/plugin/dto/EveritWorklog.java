package org.everit.jira.timetracker.plugin.dto;

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

import java.io.Serializable;
import java.text.ParseException;

import org.everit.jira.timetracker.plugin.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.JiraTimetrackerUtil;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;

/**
 * The Everit Worklog.
 */
public class EveritWorklog implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The worklog ID.
     */
    private Long worklogId;
    /**
     * The start Time.
     */
    private String startTime;
    /**
     * The worklog Issue key.
     */
    private String issue;
    /**
     * The worklog issue ID.
     */
    private Long issueId;
    /**
     * The milliseconds between the start time and the end time.
     */
    private long milliseconds;
    /**
     * The spent time.
     */
    private String duration;
    /**
     * The calculated end time.
     */
    private String endTime;
    /**
     * The worklog note.
     */
    private String body;
    /**
     * The issue estimated time is 0 or not.
     */
    private boolean isMoreEstimatedTime;

    /**
     * Simple constructor whit GenericValue.
     * 
     * @param worklogGv
     *            GenericValue worklog.
     * @throws ParseException
     *             If can't parse the date.
     */
    public EveritWorklog(final GenericValue worklogGv) throws ParseException {
        worklogId = worklogGv.getLong("id");
        startTime = worklogGv.getString("startdate");
        startTime = DateTimeConverterUtil.stringDateToStringTime(startTime);
        issueId = new Long(worklogGv.getString("issue"));
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        MutableIssue issueObject = issueManager.getIssueObject(issueId);
        issue = issueObject.getKey();
        isMoreEstimatedTime = JiraTimetrackerUtil.checkIssueEstimatedTime(issueObject);
        body = worklogGv.getString("body");
        body = body.replace("\"", "\\\"");
        body = body.replace("\r", "\\r");
        body = body.replace("\n", "\\n");
        long timeSpentInSec = worklogGv.getLong("timeworked").longValue();
        milliseconds = timeSpentInSec * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
        duration = DateTimeConverterUtil.secondConvertToString(timeSpentInSec);
        endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);

    }

    /**
     * Simple constructor whit Worklog.
     * 
     * @param worklog
     *            The worklog.
     * @throws ParseException
     *             If can't parse the date.
     */
    public EveritWorklog(final Worklog worklog) throws ParseException {
        worklogId = worklog.getId();
        startTime = DateTimeConverterUtil.dateTimeToString(worklog.getStartDate());
        issue = worklog.getIssue().getKey();
        body = worklog.getComment();
        body = body.replace("\"", "\\\"");
        body = body.replace("\r", "\\r");
        body = body.replace("\n", "\\n");
        long timeSpentInSec = worklog.getTimeSpent().longValue();
        milliseconds = timeSpentInSec * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
        duration = DateTimeConverterUtil.millisecondConvertToStringTime(milliseconds);
        endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);
    }

    public String getBody() {
        return body;
    }

    public String getDuration() {
        return duration;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean getIsMoreEstimatedTime() {
        return isMoreEstimatedTime;
    }

    public String getIssue() {
        return issue;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public String getStartTime() {
        return startTime;
    }

    public Long getWorklogId() {
        return worklogId;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }

    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    public void setIssue(final String issue) {
        this.issue = issue;
    }

    public void setMilliseconds(final long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public void setMoreEstimatedTime(final boolean isMoreEstimatedTime) {
        this.isMoreEstimatedTime = isMoreEstimatedTime;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    public void setWorklogId(final Long worklogId) {
        this.worklogId = worklogId;
    }

}
