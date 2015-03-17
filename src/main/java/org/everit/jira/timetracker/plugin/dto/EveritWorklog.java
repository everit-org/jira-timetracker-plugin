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
package org.everit.jira.timetracker.plugin.dto;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.everit.jira.timetracker.plugin.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.JiraTimetrackerUtil;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
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
   * The start Date.
   */
  private String startDate;

  private int monthNo;

  private int weekNo;

  private int dayNo;

  private Date date;
  /**
   * The worklog Issue key.
   */
  private String issue;

  /**
   * The worklog Issue Summary.
   */
  private String issueSummary;

  /**
   * The worklog Issue epic.
   */
  private String issueParent;
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
   *          GenericValue worklog.
   * @param collectorIssuePatterns
   *          The collector Issues Pattern list.
   * @throws ParseException
   *           If can't parse the date.
   */
  public EveritWorklog(final GenericValue worklogGv,
      final List<Pattern> collectorIssuePatterns) throws ParseException {
    worklogId = worklogGv.getLong("id");
    startTime = worklogGv.getString("startdate");
    date = DateTimeConverterUtil.stringToDateAndTime(startTime);
    startTime = DateTimeConverterUtil.dateTimeToString(date);
    startDate = DateTimeConverterUtil.dateToString(date);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    weekNo = calendar.get(Calendar.WEEK_OF_YEAR);
    monthNo = calendar.get(Calendar.MONTH) + 1;
    dayNo = calendar.get(Calendar.DAY_OF_YEAR);
    issueId = Long.valueOf(worklogGv.getString("issue"));
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    MutableIssue issueObject = issueManager.getIssueObject(issueId);
    issue = issueObject.getKey();
    issueSummary = issueObject.getSummary();

    if (issueObject.getParentObject() != null) {
      issueParent = issueObject.getParentObject().getKey();
    } else {
      issueParent = "";
    }
    isMoreEstimatedTime = JiraTimetrackerUtil.checkIssueEstimatedTime(
        issueObject, collectorIssuePatterns);
    body = worklogGv.getString("body");
    if (body != null) {
      body = body.replace("\"", "\\\"");
      body = body.replace("\r", "\\r");
      body = body.replace("\n", "\\n");
    } else {
      body = "";
    }
    long timeSpentInSec = worklogGv.getLong("timeworked").longValue();
    milliseconds = timeSpentInSec
        * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    duration = DateTimeConverterUtil.secondConvertToString(timeSpentInSec);
    endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);

  }

  /**
   * Simple constructor with ResultSet.
   *
   * @param rs
   *          ResultSet
   * @param collectorIssuePatterns
   *          The collector Issues Pattern list.
   * @throws ParseException
   *           Cannot parse date time
   * @throws SQLException
   *           Cannot access resultSet fields
   */
  public EveritWorklog(final ResultSet rs,
      final List<Pattern> collectorIssuePatterns) throws ParseException, SQLException {
    worklogId = rs.getLong("id");
    startTime = rs.getString("startdate");
    date = DateTimeConverterUtil.stringToDateAndTime(startTime);
    startTime = DateTimeConverterUtil.dateTimeToString(date);
    startDate = DateTimeConverterUtil.dateToString(date);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    weekNo = calendar.get(Calendar.WEEK_OF_YEAR);
    monthNo = calendar.get(Calendar.MONTH) + 1;
    dayNo = calendar.get(Calendar.DAY_OF_YEAR);
    issueId = rs.getLong("issueid");
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    MutableIssue issueObject = issueManager.getIssueObject(issueId);
    issue = issueObject.getKey();
    issueSummary = issueObject.getSummary();

    if (issueObject.getParentObject() != null) {
      issueParent = issueObject.getParentObject().getKey();
    } else {
      issueParent = "";
    }
    isMoreEstimatedTime = JiraTimetrackerUtil.checkIssueEstimatedTime(
        issueObject, collectorIssuePatterns);
    body = rs.getString("worklogbody");
    if (body != null) {
      body = body.replace("\"", "\\\"");
      body = body.replace("\r", "\\r");
      body = body.replace("\n", "\\n");
    } else {
      body = "";
    }
    long timeSpentInSec = rs.getLong("timeworked");
    milliseconds = timeSpentInSec
        * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    duration = DateTimeConverterUtil.secondConvertToString(timeSpentInSec);
    endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);

  }

  /**
   * Simple constructor whit Worklog.
   *
   * @param worklog
   *          The worklog.
   * @throws ParseException
   *           If can't parse the date.
   */
  public EveritWorklog(final Worklog worklog) throws ParseException {
    worklogId = worklog.getId();
    date = worklog.getStartDate();
    startTime = DateTimeConverterUtil.dateTimeToString(date);
    startDate = DateTimeConverterUtil.dateToString(date);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    weekNo = calendar.get(Calendar.WEEK_OF_YEAR);
    monthNo = calendar.get(Calendar.MONTH) + 1;
    dayNo = calendar.get(Calendar.DAY_OF_YEAR);
    issue = worklog.getIssue().getKey();
    issueSummary = worklog.getIssue().getSummary();
    body = worklog.getComment();
    if (body != null) {
      body = body.replace("\"", "\\\"");
      body = body.replace("\r", "\\r");
      body = body.replace("\n", "\\n");
    } else {
      body = "";
    }
    long timeSpentInSec = worklog.getTimeSpent().longValue();
    milliseconds = timeSpentInSec
        * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    duration = DateTimeConverterUtil
        .millisecondConvertToStringTime(milliseconds);
    endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);
  }

  public String getBody() {
    return body;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public int getDayNo() {
    return dayNo;
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

  public String getIssueParent() {
    return issueParent;
  }

  public String getIssueSummary() {
    return issueSummary;
  }

  public long getMilliseconds() {
    return milliseconds;
  }

  public int getMonthNo() {
    return monthNo;
  }

  public String getStartDate() {
    return startDate;
  }

  public String getStartTime() {
    return startTime;
  }

  public int getWeekNo() {
    return weekNo;
  }

  public Long getWorklogId() {
    return worklogId;
  }

  public void setBody(final String body) {
    this.body = body;
  }

  public void setDate(final Date date) {
    this.date = (Date) date.clone();
  }

  public void setDayNo(final int dayNo) {
    this.dayNo = dayNo;
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

  public void setIssueParent(final String issueParent) {
    this.issueParent = issueParent;
  }

  public void setIssueSummary(final String issueSummary) {
    this.issueSummary = issueSummary;
  }

  public void setMilliseconds(final long milliseconds) {
    this.milliseconds = milliseconds;
  }

  public void setMonthNo(final int monthNo) {
    this.monthNo = monthNo;
  }

  public void setMoreEstimatedTime(final boolean isMoreEstimatedTime) {
    this.isMoreEstimatedTime = isMoreEstimatedTime;
  }

  public void setStartDate(final String startDate) {
    this.startDate = startDate;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

  public void setWeekNo(final int weekNo) {
    this.weekNo = weekNo;
  }

  public void setWorklogId(final Long worklogId) {
    this.worklogId = worklogId;
  }

}
