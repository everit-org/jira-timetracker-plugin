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
package org.everit.jira.reporting.plugin.rest.dto;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains filter values to ReportSearchParam.
 */
@XmlRootElement
public class FilterCondition {

  @XmlElement
  public List<String> groups = Collections.emptyList();

  @XmlElement
  public List<String> issueAffectedVersions = Collections.emptyList();

  @XmlElement
  public List<String> issueAssignees = Collections.emptyList();

  @XmlElement
  public List<String> issueComponents = Collections.emptyList();

  @XmlElement
  public String issueCreateDate;

  @XmlElement
  public List<Long> issueEpicLinkIssueIds = Collections.emptyList();

  @XmlElement
  public String issueEpicName;

  @XmlElement
  public List<String> issueFixedVersions = Collections.emptyList();

  @XmlElement
  public List<String> issueKeys = Collections.emptyList();

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
  public String worklogEndDate;

  @XmlElement
  public String worklogStartDate;
}
