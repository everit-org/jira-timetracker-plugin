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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.everit.jira.reporting.plugin.util.ConverterUtil;

/**
 * Contains filter values to ReportSearchParam.
 */
@XmlRootElement
public class FilterCondition {

  @XmlElement
  private List<Long> filter = Collections.emptyList();

  @XmlElement
  private List<String> groups =
      new ArrayList<String>(Arrays.asList(ConverterUtil.VALUE_NEGATIVE_ONE));

  @XmlElement
  private List<String> issueAffectedVersions = Collections.emptyList();

  @XmlElement
  private List<String> issueAssignees = Collections.emptyList();

  @XmlElement
  private List<String> issueComponents = Collections.emptyList();

  @XmlElement
  private Long issueCreateDate;

  @XmlElement
  private List<Long> issueEpicLinkIssueIds = Collections.emptyList();

  @XmlElement
  private String issueEpicName = "";

  @XmlElement
  private List<String> issueFixedVersions = Collections.emptyList();

  @XmlElement
  private List<String> issueKeys = Collections.emptyList();

  @XmlElement
  private List<String> issuePriorityIds = Collections.emptyList();

  @XmlElement
  private List<String> issueReporters = Collections.emptyList();

  @XmlElement
  private List<String> issueResolutionIds = Collections.emptyList();

  @XmlElement
  private List<String> issueStatusIds = Collections.emptyList();

  @XmlElement
  private List<String> issueTypeIds = Collections.emptyList();

  @XmlElement
  private List<String> labels = Collections.emptyList();

  @XmlElement
  private Long limit;

  @XmlElement
  private Long offset;

  @XmlElement
  private List<Long> projectIds = Collections.emptyList();

  @XmlElement
  private String searcherValue = ConverterUtil.DEFAULT_SEARCHER_VALUE;

  @XmlElement
  private List<String> users = Collections.emptyList();

  @XmlElement
  private Long worklogEndDate;

  @XmlElement
  private Long worklogStartDate;

  public List<Long> getFilter() {
    return filter;
  }

  public List<String> getGroups() {
    return groups;
  }

  public List<String> getIssueAffectedVersions() {
    return issueAffectedVersions;
  }

  public List<String> getIssueAssignees() {
    return issueAssignees;
  }

  public List<String> getIssueComponents() {
    return issueComponents;
  }

  public Long getIssueCreateDate() {
    return issueCreateDate;
  }

  public List<Long> getIssueEpicLinkIssueIds() {
    return issueEpicLinkIssueIds;
  }

  public String getIssueEpicName() {
    return issueEpicName;
  }

  public List<String> getIssueFixedVersions() {
    return issueFixedVersions;
  }

  public List<String> getIssueKeys() {
    return issueKeys;
  }

  public List<String> getIssuePriorityIds() {
    return issuePriorityIds;
  }

  public List<String> getIssueReporters() {
    return issueReporters;
  }

  public List<String> getIssueResolutionIds() {
    return issueResolutionIds;
  }

  public List<String> getIssueStatusIds() {
    return issueStatusIds;
  }

  public List<String> getIssueTypeIds() {
    return issueTypeIds;
  }

  public List<String> getLabels() {
    return labels;
  }

  public Long getLimit() {
    return limit;
  }

  public Long getOffset() {
    return offset;
  }

  public List<Long> getProjectIds() {
    return projectIds;
  }

  public String getSearcherValue() {
    return searcherValue;
  }

  public List<String> getUsers() {
    return users;
  }

  public Long getWorklogEndDate() {
    return worklogEndDate;
  }

  public Long getWorklogStartDate() {
    return worklogStartDate;
  }

  public void setFilter(final List<Long> filter) {
    this.filter = filter;
  }

  public void setGroups(final List<String> groups) {
    this.groups = groups;
  }

  public void setIssueAffectedVersions(final List<String> issueAffectedVersions) {
    this.issueAffectedVersions = issueAffectedVersions;
  }

  public void setIssueAssignees(final List<String> issueAssignees) {
    this.issueAssignees = issueAssignees;
  }

  public void setIssueComponents(final List<String> issueComponents) {
    this.issueComponents = issueComponents;
  }

  public void setIssueCreateDate(final Long issueCreateDate) {
    this.issueCreateDate = issueCreateDate;
  }

  public void setIssueEpicLinkIssueIds(final List<Long> issueEpicLinkIssueIds) {
    this.issueEpicLinkIssueIds = issueEpicLinkIssueIds;
  }

  public void setIssueEpicName(final String issueEpicName) {
    this.issueEpicName = issueEpicName;
  }

  public void setIssueFixedVersions(final List<String> issueFixedVersions) {
    this.issueFixedVersions = issueFixedVersions;
  }

  public void setIssueKeys(final List<String> issueKeys) {
    this.issueKeys = issueKeys;
  }

  public void setIssuePriorityIds(final List<String> issuePriorityIds) {
    this.issuePriorityIds = issuePriorityIds;
  }

  public void setIssueReporters(final List<String> issueReporters) {
    this.issueReporters = issueReporters;
  }

  public void setIssueResolutionIds(final List<String> issueResolutionIds) {
    this.issueResolutionIds = issueResolutionIds;
  }

  public void setIssueStatusIds(final List<String> issueStatusIds) {
    this.issueStatusIds = issueStatusIds;
  }

  public void setIssueTypeIds(final List<String> issueTypeIds) {
    this.issueTypeIds = issueTypeIds;
  }

  public void setLabels(final List<String> labels) {
    this.labels = labels;
  }

  public void setLimit(final Long limit) {
    this.limit = limit;
  }

  public void setOffset(final Long offset) {
    this.offset = offset;
  }

  public void setProjectIds(final List<Long> projectIds) {
    this.projectIds = projectIds;
  }

  public void setSearcherValue(final String searcherValue) {
    this.searcherValue = searcherValue;
  }

  public void setUsers(final List<String> users) {
    this.users = users;
  }

  public void setWorklogEndDate(final Long worklogEndDate) {
    this.worklogEndDate = worklogEndDate;
  }

  public void setWorklogStartDate(final Long worklogStartDate) {
    this.worklogStartDate = worklogStartDate;
  }
}
