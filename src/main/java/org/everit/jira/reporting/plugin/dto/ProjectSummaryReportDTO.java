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
import java.util.List;

public class ProjectSummaryReportDTO {

  private PagingDTO paging = new PagingDTO();

  private List<ProjectSummaryDTO> projectSummaries = Collections.emptyList();

  private Long projectSummaryCount = 0L;

  public PagingDTO getPaging() {
    return paging;
  }

  public List<ProjectSummaryDTO> getProjectSummaries() {
    return projectSummaries;
  }

  public Long getProjectSummaryCount() {
    return projectSummaryCount;
  }

  public ProjectSummaryReportDTO paging(final PagingDTO paging) {
    this.paging = paging;
    return this;
  }

  public ProjectSummaryReportDTO projectSummaries(final List<ProjectSummaryDTO> projectSummaries) {
    this.projectSummaries = projectSummaries;
    return this;
  }

  public ProjectSummaryReportDTO projectSummaryCount(final Long projectSummaryCount) {
    this.projectSummaryCount = projectSummaryCount;
    return this;
  }

}
