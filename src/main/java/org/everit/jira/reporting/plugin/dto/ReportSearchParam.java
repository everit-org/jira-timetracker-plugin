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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of report search param.
 */
@XmlRootElement
public class ReportSearchParam {

  @XmlElement
  public List<String> issueAffectedVersions = Collections.emptyList();

  @XmlElement
  public List<String> issueAssignees = Collections.emptyList();

  @XmlElement
  public List<String> issueComponents = Collections.emptyList();

  @XmlElement
  public Date issueCreateDate;

  @XmlElement
  public List<Long> issueEpicLinkIssueIds = Collections.emptyList();

  @XmlElement
  public String issueEpicName;

  @XmlElement
  public List<String> issueFixedVersions = Collections.emptyList();

  @XmlElement
  public List<Long> issueIds = Collections.emptyList();

  @XmlElement
  public List<String> issuePriorityIds = Collections.emptyList();

  @XmlElement
  public List<String> issueReporters = Collections.emptyList();

  @XmlElement
  public List<String> issueResolutionIds = Collections.emptyList();

  @XmlElement
  public List<String> issueStatusIds = Collections.emptyList();

  @XmlElement
  public List<String> issueTypeIds = Collections.emptyList();

  @XmlElement
  public List<String> labels = Collections.emptyList();

  @XmlElement
  public Long limit;

  @XmlElement
  public Long offset;

  @XmlElement
  public List<Long> projectIds = Collections.emptyList();

  @XmlElement
  public boolean selectNoAffectedVersionIssue = false;

  @XmlElement
  public boolean selectNoComponentIssue = false;

  @XmlElement
  public boolean selectNoFixedVersionIssue = false;

  @XmlElement
  public boolean selectReleasedFixVersion = false;

  @XmlElement
  public boolean selectUnassgined = false;

  @XmlElement
  public boolean selectUnreleasedFixVersion = false;

  @XmlElement
  public boolean selectUnresolvedResolution = false;

  @XmlElement
  public List<String> users = Collections.emptyList();

  @XmlElement
  public Date worklogEndDate;

  @XmlElement
  public Date worklogStartDate;

  public ReportSearchParam issueAffectedVersions(final List<String> issueAffectedVersions) {
    this.issueAffectedVersions = issueAffectedVersions;
    return this;
  }

  public ReportSearchParam issueAssignees(final List<String> issueAssignees) {
    this.issueAssignees = issueAssignees;
    return this;
  }

  public ReportSearchParam issueComponents(final List<String> issueComponents) {
    this.issueComponents = issueComponents;
    return this;
  }

  public ReportSearchParam issueCreateDate(final Date issueCreateDate) {
    this.issueCreateDate = issueCreateDate;
    return this;
  }

  public ReportSearchParam issueEpicLinkIssueIds(final List<Long> issueEpicLinkIssueIds) {
    this.issueEpicLinkIssueIds = issueEpicLinkIssueIds;
    return this;
  }

  public ReportSearchParam issueEpicName(final String issueEpicName) {
    this.issueEpicName = issueEpicName;
    return this;
  }

  public ReportSearchParam issueFixedVersions(final List<String> issueFixedVersions) {
    this.issueFixedVersions = issueFixedVersions;
    return this;
  }

  public ReportSearchParam issueIds(final List<Long> issueIds) {
    this.issueIds = issueIds;
    return this;
  }

  public ReportSearchParam issuePriorityIds(final List<String> issuePriorityIds) {
    this.issuePriorityIds = issuePriorityIds;
    return this;
  }

  public ReportSearchParam issueReporters(final List<String> issueReporters) {
    this.issueReporters = issueReporters;
    return this;
  }

  public ReportSearchParam issueResolutionIds(final List<String> issueResolutionIds) {
    this.issueResolutionIds = issueResolutionIds;
    return this;
  }

  public ReportSearchParam issueStatusIds(final List<String> issueStatusIds) {
    this.issueStatusIds = issueStatusIds;
    return this;
  }

  public ReportSearchParam issueTypeIds(final List<String> issueTypeIds) {
    this.issueTypeIds = issueTypeIds;
    return this;
  }

  public ReportSearchParam labels(final List<String> labels) {
    this.labels = labels;
    return this;
  }

  public ReportSearchParam limit(final Long limit) {
    this.limit = limit;
    return this;
  }

  public ReportSearchParam offset(final Long offset) {
    this.offset = offset;
    return this;
  }

  public ReportSearchParam projectIds(final List<Long> projectIds) {
    this.projectIds = projectIds;
    return this;
  }

  public ReportSearchParam selectNoAffectedVersionIssue() {
    selectNoAffectedVersionIssue = true;
    return this;
  }

  public ReportSearchParam selectNoComponentIssue() {
    selectNoComponentIssue = true;
    return this;
  }

  public ReportSearchParam selectNoFixedVersionIssue() {
    selectNoFixedVersionIssue = true;
    return this;
  }

  public ReportSearchParam selectReleasedFixVersion() {
    selectReleasedFixVersion = true;
    return this;
  }

  public ReportSearchParam selectUnassgined() {
    selectUnassgined = true;
    return this;
  }

  public ReportSearchParam selectUnreleasedFixVersion() {
    selectUnreleasedFixVersion = true;
    return this;
  }

  public ReportSearchParam selectUnresolvedResolution() {
    selectUnresolvedResolution = true;
    return this;
  }

  public ReportSearchParam users(final List<String> users) {
    this.users = users;
    return this;
  }

  public ReportSearchParam worklogEndDate(final Date worklogEndDate) {
    this.worklogEndDate = worklogEndDate;
    return this;
  }

  public ReportSearchParam worklogStartDate(final Date worklogStartDate) {
    this.worklogStartDate = worklogStartDate;
    return this;
  }

}
