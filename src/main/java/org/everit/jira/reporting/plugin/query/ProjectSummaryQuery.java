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

import java.sql.Connection;
import java.util.List;

import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsSearchParam;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.Configuration;

public class ProjectSummaryQuery extends AbstractReportQuery<ProjectSummaryDTO> {

  public ProjectSummaryQuery(final WorklogDetailsSearchParam worklogDetailsSearchParam) {
    super(worklogDetailsSearchParam);
  }

  @Override
  protected Expression<?>[] createGroupBy() {
    return new Expression<?>[] {
        qProject.pkey,
        qProject.pname,
        qProject.description };
  }

  @Override
  protected QBean<ProjectSummaryDTO> createSelectProjection() {
    return Projections.bean(ProjectSummaryDTO.class,
        qProject.pkey.as(ProjectSummaryDTO.AliasNames.PROJECT_KEY),
        qProject.pname.as(ProjectSummaryDTO.AliasNames.PROJECT_NAME),
        qProject.description.as(ProjectSummaryDTO.AliasNames.PROJECT_DESCRIPTION),
        qIssue.timeoriginalestimate.sum()
            .as(ProjectSummaryDTO.AliasNames.ISSUE_TIME_ORIGINAL_ESTIMATE_SUM),
        qIssue.timeestimate.sum().as(ProjectSummaryDTO.AliasNames.ISSUE_TIME_ESTIMATE_SUM),
        qWorklog.timeworked.sum().as(ProjectSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM));
  }

  @Override
  protected void extendResult(final Connection connection, final Configuration configuration,
      final List<ProjectSummaryDTO> result) {
  }

}
