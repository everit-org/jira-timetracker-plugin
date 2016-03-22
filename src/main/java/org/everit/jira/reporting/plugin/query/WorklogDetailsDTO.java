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
package org.everit.jira.reporting.plugin.query;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

public class WorklogDetailsDTO {

  public final class AliasNames {

    public static final String ISSUE_ASSIGNE = "issueAssigne";

    public static final String ISSUE_CREATED = "issueCreated";

    public static final String ISSUE_ID = "issueId";

    public static final String ISSUE_KEY = "issueKey";

    public static final String ISSUE_ORIGINAL_ESTIMATE = "issueOriginalEstimate";

    public static final String ISSUE_REMAINING_ESTIMATE = "issueRemainingEstimate";

    public static final String ISSUE_REPORTER = "issueReporter";

    public static final String ISSUE_STATUS_NAME = "issueStatusName";

    public static final String ISSUE_SUMMARY = "issueSummary";

    public static final String ISSUE_TYPE_NAME = "issueTypeName";

    public static final String ISSUE_UPDATED = "issueUpdated";

    public static final String PRIORITY_NAME = "priorityName";

    public static final String PROJECT_DESCRIPTION = "projectDescription";

    public static final String PROJECT_KEY = "projectKey";

    public static final String RESOLUTION_NAME = "resolutionName";

    public static final String WORKLOG_BODY = "worklogBody";

    public static final String WORKLOG_CREATED = "worklogCreated";

    public static final String WORKLOG_START_DATE = "worklogStartDate";

    public static final String WORKLOG_TIME_WORKED = "worklogTimeWorked";

    public static final String WORKLOG_UPDATED = "worklogUpdated";

    private AliasNames() {
    }
  }

  public List<String> issueAffectedVersion = Collections.emptyList();

  public String issueAssigne;

  public List<String> issueComponents = Collections.emptyList();

  public Timestamp issueCreated;

  public List<String> issueFixedVersions = Collections.emptyList();

  public Long issueId;

  public String issueKey;

  public Long issueOriginalEstimate;

  public Long issueRemainingEstimate;

  public String issueReporter;

  public String issueStatusName;

  public String issueSummary;

  public String issueTypeName;

  public Timestamp issueUpdated;

  public String priorityName;

  public String projectDescription;

  public String projectKey;

  public String resolutionName;

  public String worklogBody;

  public Timestamp worklogCreated;

  public Timestamp worklogStartDate;

  public Long worklogTimeWorked;

  public Timestamp worklogUpdated;

  public List<String> getIssueAffectedVersion() {
    return issueAffectedVersion;
  }

  public String getIssueAssigne() {
    return issueAssigne;
  }

  public List<String> getIssueComponents() {
    return issueComponents;
  }

  public Timestamp getIssueCreated() {
    return issueCreated;
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

  public String getIssueTypeName() {
    return issueTypeName;
  }

  public Timestamp getIssueUpdated() {
    return issueUpdated;
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

  public String getResolutionName() {
    return resolutionName;
  }

  public String getWorklogBody() {
    return worklogBody;
  }

  public Timestamp getWorklogCreated() {
    return worklogCreated;
  }

  public Timestamp getWorklogStartDate() {
    return worklogStartDate;
  }

  public Long getWorklogTimeWorked() {
    return worklogTimeWorked;
  }

  public Timestamp getWorklogUpdated() {
    return worklogUpdated;
  }

  public void setIssueAffectedVersion(final List<String> issueAffectedVersion) {
    this.issueAffectedVersion = issueAffectedVersion;
  }

  public void setIssueAssigne(final String issueAssigne) {
    this.issueAssigne = issueAssigne;
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

  public void setIssueTypeName(final String issueTypeName) {
    this.issueTypeName = issueTypeName;
  }

  public void setIssueUpdated(final Timestamp issueUpdated) {
    this.issueUpdated = issueUpdated;
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

}
