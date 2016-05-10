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
package org.everit.jira.reporting.plugin.dto;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * Representation of the worklog details query result.
 */
public class WorklogDetailsDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String ISSUE_ASSIGNEE = "issueAssignee";

    public static final String ISSUE_AVATAR_ID = "issueAvatarId";

    public static final String ISSUE_CREATED = "issueCreated";

    public static final String ISSUE_ID = "issueId";

    public static final String ISSUE_KEY = "issueKey";

    public static final String ISSUE_REPORTER = "issueReporter";

    public static final String ISSUE_STATUS_P_NAME = "issueStatusName";

    public static final String ISSUE_SUMMARY = "issueSummary";

    public static final String ISSUE_TIME_ESTIMATE = "issueRemainingEstimate";

    public static final String ISSUE_TIME_ORIGINAL_ESTIMATE = "issueOriginalEstimate";

    public static final String ISSUE_TYPE_ICON_URL = "issueTypeIconUrl";

    public static final String ISSUE_TYPE_NAME = "issueTypeName";

    public static final String ISSUE_UPDATED = "issueUpdated";

    public static final String PRIORITY_ICON_URL = "priorityIconUrl";

    public static final String PRIORITY_NAME = "priorityName";

    public static final String PROJECT_DESCRIPTION = "projectDescription";

    public static final String PROJECT_KEY = "projectKey";

    public static final String PROJECT_NAME = "projectName";

    public static final String RESOLUTION_NAME = "resolutionName";

    public static final String WORKLOG_BODY = "worklogBody";

    public static final String WORKLOG_CREATED = "worklogCreated";

    public static final String WORKLOG_START_DATE = "worklogStartDate";

    public static final String WORKLOG_TIME_WORKED = "worklogTimeWorked";

    public static final String WORKLOG_UPDATED = "worklogUpdated";

    public static final String WORKLOG_USER = "worklogUser";

    private AliasNames() {
    }
  }

  private List<String> issueAffectedVersions = Collections.emptyList();

  private String issueAssignee;
  private Long issueAvatarId;

  private List<String> issueComponents = Collections.emptyList();

  private Timestamp issueCreated;

  private List<String> issueFixedVersions = Collections.emptyList();

  private Long issueId;

  private String issueKey;

  private Long issueOriginalEstimate;

  private Long issueRemainingEstimate;

  private String issueReporter;

  private String issueStatusName;

  private String issueSummary;

  private String issueTypeIconUrl;

  private String issueTypeName;

  private Timestamp issueUpdated;

  private String priorityIconUrl;

  private String priorityName;

  private String projectDescription;

  private String projectKey;

  private String projectName;

  private String resolutionName;

  private String worklogBody;

  private Timestamp worklogCreated;

  private Timestamp worklogStartDate;

  private Long worklogTimeWorked;

  private Timestamp worklogUpdated;

  private String worklogUser;

  public List<String> getIssueAffectedVersions() {
    return issueAffectedVersions;
  }

  public String getIssueAssignee() {
    return issueAssignee;
  }

  public Long getIssueAvatarId() {
    return issueAvatarId;
  }

  public List<String> getIssueComponents() {
    return issueComponents;
  }

  /**
   * Gets issue created date.
   */
  public Timestamp getIssueCreated() {
    if (issueCreated == null) {
      return null;
    }
    return (Timestamp) issueCreated.clone();
  }

  public List<String> getIssueFixedVersions() {
    return issueFixedVersions;
  }

  public Long getIssueId() {
    return issueId;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public Long getIssueOriginalEstimate() {
    return issueOriginalEstimate;
  }

  public Long getIssueRemainingEstimate() {
    return issueRemainingEstimate;
  }

  public String getIssueReporter() {
    return issueReporter;
  }

  public String getIssueStatusName() {
    return issueStatusName;
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

  /**
   * Gets issue updated date.
   */
  public Timestamp getIssueUpdated() {
    if (issueUpdated == null) {
      return null;
    }
    return (Timestamp) issueUpdated.clone();
  }

  public String getPriorityIconUrl() {
    return priorityIconUrl;
  }

  public String getPriorityName() {
    return priorityName;
  }

  public String getProjectDescription() {
    return projectDescription;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getResolutionName() {
    return resolutionName;
  }

  public String getWorklogBody() {
    return worklogBody;
  }

  /**
   * Gets worklog created date.
   */
  public Timestamp getWorklogCreated() {
    if (worklogCreated == null) {
      return null;
    }
    return (Timestamp) worklogCreated.clone();
  }

  /**
   * Gets worklog start date.
   */
  public Timestamp getWorklogStartDate() {
    if (worklogStartDate == null) {
      return null;
    }
    return (Timestamp) worklogStartDate.clone();
  }

  public Long getWorklogTimeWorked() {
    return worklogTimeWorked;
  }

  /**
   * Gets worklog updated date.
   */
  public Timestamp getWorklogUpdated() {
    if (worklogUpdated == null) {
      return null;
    }
    return (Timestamp) worklogUpdated.clone();
  }

  public String getWorklogUser() {
    return worklogUser;
  }

  public void setIssueAffectedVersions(final List<String> issueAffectedVersions) {
    this.issueAffectedVersions = issueAffectedVersions;
  }

  public void setIssueAssignee(final String issueAssignee) {
    this.issueAssignee = issueAssignee;
  }

  public void setIssueAvatarId(final Long issueAvatarId) {
    this.issueAvatarId = issueAvatarId;
  }

  public void setIssueComponents(final List<String> issueComponents) {
    this.issueComponents = issueComponents;
  }

  public void setIssueCreated(final Timestamp issueCreated) {
    this.issueCreated = issueCreated;
  }

  public void setIssueFixedVersions(final List<String> issueFixedVersions) {
    this.issueFixedVersions = issueFixedVersions;
  }

  public void setIssueId(final Long issueId) {
    this.issueId = issueId;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setIssueOriginalEstimate(final Long issueOriginalEstimate) {
    this.issueOriginalEstimate = issueOriginalEstimate;
  }

  public void setIssueRemainingEstimate(final Long issueRemainingEstimate) {
    this.issueRemainingEstimate = issueRemainingEstimate;
  }

  public void setIssueReporter(final String issueReporter) {
    this.issueReporter = issueReporter;
  }

  public void setIssueStatusName(final String issueStatusName) {
    this.issueStatusName = issueStatusName;
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

  public void setIssueUpdated(final Timestamp issueUpdated) {
    this.issueUpdated = issueUpdated;
  }

  public void setPriorityIconUrl(final String priorityIconUrl) {
    this.priorityIconUrl = priorityIconUrl;
  }

  public void setPriorityName(final String priorityName) {
    this.priorityName = priorityName;
  }

  public void setProjectDescription(final String projectDescription) {
    this.projectDescription = projectDescription;
  }

  public void setProjectKey(final String projectKey) {
    this.projectKey = projectKey;
  }

  public void setProjectName(final String projectName) {
    this.projectName = projectName;
  }

  public void setResolutionName(final String resolutionName) {
    this.resolutionName = resolutionName;
  }

  public void setWorklogBody(final String worklogBody) {
    this.worklogBody = worklogBody;
  }

  public void setWorklogCreated(final Timestamp worklogCreated) {
    this.worklogCreated = worklogCreated;
  }

  public void setWorklogStartDate(final Timestamp worklogStartDate) {
    this.worklogStartDate = worklogStartDate;
  }

  public void setWorklogTimeWorked(final Long worklogTimeWorked) {
    this.worklogTimeWorked = worklogTimeWorked;
  }

  public void setWorklogUpdated(final Timestamp worklogUpdated) {
    this.worklogUpdated = worklogUpdated;
  }

  public void setWorklogUser(final String worklogUser) {
    this.worklogUser = worklogUser;
  }

}
