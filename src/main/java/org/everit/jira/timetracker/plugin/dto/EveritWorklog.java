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
import java.text.ParseException;
import java.util.Date;

import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * The Everit Worklog.
 */
public class EveritWorklog implements Serializable {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The worklog note.
   */
  private String body;

  private DateTime date;

  private int dayNo;

  private boolean deleteOwnWorklogs;

  /**
   * The spent time.
   */
  private String duration;

  private boolean editOwnWorklogs;

  /**
   * The calculated end time.
   */
  private String endTime;

  private String exactRemaining;

  /**
   * The issue closed.
   */
  private boolean isClosed = false;

  /**
   * The issue estimated time is 0 or not.
   */
  private boolean isMoreEstimatedTime;

  /**
   * The worklog Issue key.
   */
  private String issue;

  private Long issueAvatarId;

  /**
   * The worklog issue ID.
   */
  private Long issueId;

  /**
   * The worklog Issue epic.
   */
  private String issueParent = "";

  /**
   * The worklog Issue Summary.
   */
  private String issueSummary;

  private String issueTypeIconUrl;

  private String issueTypeName;

  /**
   * The milliseconds between the start time and the end time.
   */
  private long milliseconds;

  private int monthNo;

  /**
   * Remaining time on the issue.
   */
  private String roundedRemaining;

  /**
   * The start Date.
   */
  private String startDate;

  /**
   * The start Time.
   */
  private String startTime;

  private int weekNo;

  /**
   * The worklog ID.
   */
  private Long worklogId;

  /**
   * Simple constructor with GenericValue.
   *
   * @param worklogGv
   *          GenericValue worklog.
   * @throws ParseException
   *           If can't parse the date.
   * @throws IllegalArgumentException
   *           If can't parse the date.
   */
  public EveritWorklog(final GenericValue worklogGv)
      throws ParseException, IllegalArgumentException {
    worklogId = worklogGv.getLong("id");
    startTime = worklogGv.getString("startdate");
    DateTime systemDate =
        new DateTime(DateTimeConverterUtil.stringToDateAndTime(startTime).getTime());
    date = DateTimeConverterUtil.convertDateZoneToUserTimeZone(systemDate);
    startTime =
        DateTimeConverterUtil.dateTimeToString(DateTimeConverterUtil.convertDateTimeToDate(date));
    startDate =
        DateTimeConverterUtil.dateToString(DateTimeConverterUtil.convertDateTimeToDate(date));
    weekNo = date.getWeekOfWeekyear(); // TODO check
    monthNo = date.getMonthOfYear() + 1; // TODO check
    dayNo = date.getDayOfYear(); // TODO check
    issueId = Long.valueOf(worklogGv.getString("issue"));
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    MutableIssue issueObject = issueManager.getIssueObject(issueId);
    issue = issueObject.getKey();
    issueSummary = issueObject.getSummary();
    if (StatusCategory.COMPLETE
        .equals(issueObject.getStatusObject().getSimpleStatus().getStatusCategory().getKey())) {
      isClosed = true;
    }
    issueTypeName = issueObject.getIssueTypeObject().getName();
    Avatar avatar = issueObject.getIssueTypeObject().getAvatar();
    if (avatar != null) {
      issueAvatarId = avatar.getId();
    }
    issueTypeIconUrl = issueObject.getIssueTypeObject().getIconUrl();
    if (issueObject.getParentObject() != null) {
      issueParent = issueObject.getParentObject().getKey();
    }
    Long issueEstimate = issueObject.getEstimate();
    if ((issueEstimate != null) && (issueEstimate > 0)) {
      isMoreEstimatedTime = true;
    } else {
      isMoreEstimatedTime = false;
      // fix issueObject.getEstimate(); null or negative value problem.
      issueEstimate = 0L;
    }
    body = worklogGv.getString("body");
    if (body == null) {
      body = "";
    }
    DurationFormatter durationFormatter = new DurationFormatter();
    long timeSpentInSec = worklogGv.getLong("timeworked").longValue();
    milliseconds = timeSpentInSec
        * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    duration = durationFormatter.exactDuration(timeSpentInSec);
    endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);

    roundedRemaining = durationFormatter.roundedDuration(issueEstimate);
    exactRemaining = durationFormatter.exactDuration(issueEstimate);

    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    ApplicationUser loggedUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
    deleteOwnWorklogs =
        permissionManager.hasPermission(ProjectPermissions.DELETE_OWN_WORKLOGS, issueObject,
            loggedUser);
    editOwnWorklogs =
        permissionManager.hasPermission(ProjectPermissions.EDIT_OWN_WORKLOGS, issueObject,
            loggedUser);
  }

  /**
   * Simple constructor whit Worklog.
   *
   * @param worklog
   *          The worklog.
   * @throws IllegalArgumentException
   *           If can't parse the date.
   */
  public EveritWorklog(final Worklog worklog) throws IllegalArgumentException {
    worklogId = worklog.getId();
    DateTime systemDate = new DateTime(worklog.getStartDate().getTime());
    date = DateTimeConverterUtil.convertDateZoneToUserTimeZone(systemDate);
    startTime =
        DateTimeConverterUtil.dateTimeToString(DateTimeConverterUtil.convertDateTimeToDate(date));
    startDate =
        DateTimeConverterUtil.dateToString(DateTimeConverterUtil.convertDateTimeToDate(date));
    weekNo = date.getWeekOfWeekyear(); // TODO check
    monthNo = date.getMonthOfYear() + 1; // TODO check
    dayNo = date.getDayOfYear(); /// TODO check
    issue = worklog.getIssue().getKey();
    issueSummary = worklog.getIssue().getSummary();
    body = worklog.getComment();
    if (body == null) {
      body = "";
    }
    long timeSpentInSec = worklog.getTimeSpent().longValue();
    milliseconds = timeSpentInSec
        * DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    duration = DateTimeConverterUtil.dateTimeToStringWithFixFormat(new Date(milliseconds));
    endTime = DateTimeConverterUtil.countEndTime(startTime, milliseconds);
  }

  public String getBody() {
    return body;
  }

  public Date getDate() {
    return DateTimeConverterUtil.convertDateTimeToDate(date);
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

  public String getExactRemaining() {
    return exactRemaining;
  }

  public boolean getIsClosed() {
    return isClosed;
  }

  public boolean getIsMoreEstimatedTime() {
    return isMoreEstimatedTime;
  }

  public String getIssue() {
    return issue;
  }

  public Long getIssueAvatarId() {
    return issueAvatarId;
  }

  public String getIssueParent() {
    return issueParent;
  }

  public String getIssueSummary() {
    return issueSummary;
  }

  public String getIssueTypeIconUrl() {
    return issueTypeIconUrl;
  }

  public String getIssueTypeName() {
    return issueTypeName;
  }

  public long getMilliseconds() {
    return milliseconds;
  }

  public int getMonthNo() {
    return monthNo;
  }

  public String getRoundedRemaining() {
    return roundedRemaining;
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

  public boolean isDeleteOwnWorklogs() {
    return deleteOwnWorklogs;
  }

  public boolean isEditOwnWorklogs() {
    return editOwnWorklogs;
  }

  public void setBody(final String body) {
    this.body = body;
  }

  public void setDate(final Date date) {
    this.date = new DateTime(date.getTime());
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

  public void setExactRemaining(final String exactRemaining) {
    this.exactRemaining = exactRemaining;
  }

  public void setIsClosed(final boolean isClosed) {
    this.isClosed = isClosed;
  }

  public void setIssue(final String issue) {
    this.issue = issue;
  }

  public void setIssueAvatarId(final Long issueAvatarId) {
    this.issueAvatarId = issueAvatarId;
  }

  public void setIssueParent(final String issueParent) {
    this.issueParent = issueParent;
  }

  public void setIssueSummary(final String issueSummary) {
    this.issueSummary = issueSummary;
  }

  public void setIssueTypeIconUrl(final String issueTypeIconUrl) {
    this.issueTypeIconUrl = issueTypeIconUrl;
  }

  public void setIssueTypeName(final String issueTypeName) {
    this.issueTypeName = issueTypeName;
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

  public void setRoundedRemaining(final String remaining) {
    roundedRemaining = remaining;
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
