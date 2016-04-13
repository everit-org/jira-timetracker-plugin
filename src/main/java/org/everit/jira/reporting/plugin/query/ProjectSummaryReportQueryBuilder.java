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
import java.sql.SQLException;
import java.util.List;

import org.everit.jira.querydsl.schema.QProject;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

/**
 * Queries for project summary report.
 */
public class ProjectSummaryReportQueryBuilder extends AbstractReportQuery {

  public ProjectSummaryReportQueryBuilder(final ReportSearchParam reportSearchParam) {
    super(reportSearchParam);
  }

  /**
   * Build count project summary query.
   */
  public QuerydslCallable<Long> buildCountQuery() {
    return new QuerydslCallable<Long>() {
      @Override
      public Long call(final Connection connection, final Configuration configuration)
          throws SQLException {
        NumberPath<Long> projectCountPath = Expressions.numberPath(Long.class,
            new PathMetadata(null, "projectCount", PathType.VARIABLE));

        SQLQuery<Long> fromQuery = new SQLQuery<Long>(connection, configuration)
            .select(qProject.id.count().as(projectCountPath));

        appendBaseFromAndJoin(fromQuery);
        appendBaseWhere(fromQuery);
        fromQuery.groupBy(qProject.id);

        SQLQuery<Long> query = new SQLQuery<Long>(connection, configuration)
            .select(projectCountPath.count())
            .from(fromQuery.as("fromCount"));

        return query.fetchOne();
      }
    };
  }

  /**
   * Build project summary query.
   */
  public QuerydslCallable<List<ProjectSummaryDTO>> buildQuery() {
    return new QuerydslCallable<List<ProjectSummaryDTO>>() {

      @Override
      public List<ProjectSummaryDTO> call(final Connection connection,
          final Configuration configuration) throws SQLException {
        NumberPath<Long> fromProjectIdPath = Expressions.numberPath(Long.class,
            new PathMetadata(null, "fromProjectId", PathType.VARIABLE));
        NumberPath<Long> timeOriginalSumPath = Expressions.numberPath(Long.class,
            new PathMetadata(null,
                ProjectSummaryDTO.AliasNames.ISSUE_TIME_ORIGINAL_ESTIMATE_SUM,
                PathType.VARIABLE));
        NumberPath<Long> timeEstimateSumPath = Expressions.numberPath(Long.class,
            new PathMetadata(null,
                ProjectSummaryDTO.AliasNames.ISSUE_TIME_ESTIMATE_SUM,
                PathType.VARIABLE));
        NumberPath<Long> workloggedSumPath = Expressions.numberPath(Long.class,
            new PathMetadata(null,
                ProjectSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM,
                PathType.VARIABLE));

        SQLQuery<Tuple> fromQuery = SQLExpressions.select(
            qProject.id.as(fromProjectIdPath),
            qIssue.timeoriginalestimate.sum().as(timeOriginalSumPath),
            qIssue.timeestimate.sum().as(timeEstimateSumPath),
            qWorklog.timeworked.sum().as(workloggedSumPath));

        appendBaseFromAndJoin(fromQuery);
        appendBaseWhere(fromQuery);
        appendQueryRange(fromQuery);
        fromQuery.groupBy(qProject.id);

        QProject qProject = new QProject("m_project");
        return new SQLQuery<ProjectSummaryDTO>(connection, configuration)
            .select(Projections.bean(ProjectSummaryDTO.class,
                qProject.pkey.as(ProjectSummaryDTO.AliasNames.PROJECT_KEY),
                qProject.pname.as(ProjectSummaryDTO.AliasNames.PROJECT_NAME),
                qProject.description.as(ProjectSummaryDTO.AliasNames.PROJECT_DESCRIPTION),
                timeOriginalSumPath,
                timeEstimateSumPath,
                workloggedSumPath))
            .from(fromQuery.as("sums"))
            .join(qProject).on(qProject.id.eq(fromProjectIdPath))
            .orderBy(qProject.pkey.asc())
            .fetch();
      }
    };
  }

}
