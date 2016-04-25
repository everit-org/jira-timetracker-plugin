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

/**
 * Representation of the issue summary query result.
 */
public class IssueSummaryDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String ASSIGNEE = "assignee";

    public static final String ISSUE_KEY = "issueKey";

    public static final String ISSUE_ORIGINAL_ESTIMATE_SUM = "orginalEstimatedSum";

    public static final String ISSUE_SUMMARY = "issueSummary";

    public static final String ISSUE_TIME_ESTIMATE_SUM = "reaminingTimeSum";

    public static final String ISSUE_TYPE_ICON_URL = "issueTypeIconUrl";

    public static final String ISSUE_TYPE_NAME = "issueTypeName";

    public static final String PRIORITY_ICON_URL = "priorityIconUrl";

    public static final String PRIORITY_NAME = "priorityName";

    public static final String STATUS_NAME = "statusName";

    public static final String WORKLOGGED_TIME_SUM = "workloggedTimeSum";

    private AliasNames() {
    }
  }

  private String assignee;

  private long expected;

  private String issueKey;

  private String issueSummary;

  private String issueTypeIconUrl;

  private String issueTypeName;

  private long orginalEstimatedSum;

  private String priorityIconUrl;

  private String priorityName;

  private long reaminingTimeSum;

  private String statusName;

  private long workloggedTimeSum;

  public String getAssignee() {
    return assignee;
  }

  public long getExpected() {
    expected = workloggedTimeSum + reaminingTimeSum;
    return expected;
  }

  public String getIssueKey() {
    return issueKey;
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

  public long getOrginalEstimatedSum() {
    return orginalEstimatedSum;
  }

  public String getPriorityIconUrl() {
    return priorityIconUrl;
  }

  public String getPriorityName() {
    return priorityName;
  }

  public long getReaminingTimeSum() {
    return reaminingTimeSum;
  }

  public String getStatusName() {
    return statusName;
  }

  public long getWorkloggedTimeSum() {
    return workloggedTimeSum;
  }

  public void setAssignee(final String assignee) {
    this.assignee = assignee;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
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

  public void setOrginalEstimatedSum(final long orginalEstimatedSum) {
    this.orginalEstimatedSum = orginalEstimatedSum;
  }

  public void setPriorityIconUrl(final String priorityIconUrl) {
    this.priorityIconUrl = priorityIconUrl;
  }

  public void setPriorityName(final String priorityName) {
    this.priorityName = priorityName;
  }

  public void setReaminingTimeSum(final long reaminingTimeSum) {
    this.reaminingTimeSum = reaminingTimeSum;
  }

  public void setStatusName(final String statusName) {
    this.statusName = statusName;
  }

  public void setWorkloggedTimeSum(final long timespentSum) {
    workloggedTimeSum = timespentSum;
  }

}
