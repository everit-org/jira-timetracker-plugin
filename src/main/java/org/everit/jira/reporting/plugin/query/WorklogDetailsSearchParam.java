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

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class WorklogDetailsSearchParam {

  public List<String> groups = Collections.emptyList();

  public List<String> issueAffectedVersions = Collections.emptyList();

  public List<String> issueAssignees = Collections.emptyList();

  public List<String> issueComponents = Collections.emptyList();

  public Date issueCreateDate;

  public List<Long> issueEpicLinkIssueIds = Collections.emptyList();

  public String issueEpicName;

  public List<String> issueFixedVersions = Collections.emptyList();

  public List<Long> issueIds = Collections.emptyList();

  public List<String> issuePriorityIds = Collections.emptyList();

  public List<String> issueReporters = Collections.emptyList();

  public List<String> issueResolutionIds = Collections.emptyList();

  public List<String> issueStatusIds = Collections.emptyList();

  public List<String> issueTypeIds = Collections.emptyList();

  public List<String> labels = Collections.emptyList();

  public List<Long> projectIds = Collections.emptyList();

  public boolean selectNoAffectedVersionIssue = false;

  public boolean selectNoComponentIssue = false;

  public boolean selectNoFixedVersionIssue = false;

  public boolean selectReleasedFixVersion = false;

  public boolean selectUnassgined = false;

  public boolean selectUnreleasedFixVersion = false;

  public boolean selectUnresolvedResolution = false;

  public List<String> users = Collections.emptyList();

  public Date worklogEndDate;

  public Date worklogStartDate;

}
