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
 * Representation of the project summary query result.
 */
public class ProjectSummaryDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String ISSUE_TIME_ESTIMATE_SUM = "issuesReaminingTimeSum";

    public static final String ISSUE_TIME_ORIGINAL_ESTIMATE_SUM = "issuesOrginalEstimatedSum";

    public static final String PROJECT_KEY = "projectKey";

    public static final String PROJECT_NAME = "projectName";

    public static final String WORKLOGGED_TIME_SUM = "workloggedTimeSum";

    private AliasNames() {
    }
  }

  private long expectedTotal;

  private long issuesOrginalEstimatedSum;

  private long issuesReaminingTimeSum;

  private String projectKey;

  private String projectName;

  private long workloggedTimeSum;

  public long getExpectedTotal() {
    expectedTotal = getWorkloggedTimeSum() + getIssuesReaminingTimeSum();
    return expectedTotal;
  }

  public long getIssuesOrginalEstimatedSum() {
    return issuesOrginalEstimatedSum;
  }

  public long getIssuesReaminingTimeSum() {
    return issuesReaminingTimeSum;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public String getProjectName() {
    return projectName;
  }

  public long getWorkloggedTimeSum() {
    return workloggedTimeSum;
  }

  public void setIssuesOrginalEstimatedSum(final long issuesOrginalEstimatedSum) {
    this.issuesOrginalEstimatedSum = issuesOrginalEstimatedSum;
  }

  public void setIssuesReaminingTimeSum(final long issuesReaminingTimeSum) {
    this.issuesReaminingTimeSum = issuesReaminingTimeSum;
  }

  public void setProjectKey(final String projectKey) {
    this.projectKey = projectKey;
  }

  public void setProjectName(final String projectName) {
    this.projectName = projectName;
  }

  public void setWorkloggedTimeSum(final long issuesTimespentSum) {
    workloggedTimeSum = issuesTimespentSum;
  }
}
