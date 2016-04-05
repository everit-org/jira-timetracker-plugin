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

import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.query.util.QueryUtil;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Queries for user summary report.
 */
public class UserSummaryReportQuery extends AbstractReportQuery {

  public UserSummaryReportQuery(final ReportSearchParam reportSearchParam) {
    super(reportSearchParam);
  }

  /**
   * Build count user summary query.
   */
  public QuerydslCallable<Long> buildCountQuery() {
    return new QuerydslCallable<Long>() {
      @Override
      public Long call(final Connection connection, final Configuration configuration)
          throws SQLException {
        NumberPath<Long> authorCountPath = Expressions.numberPath(Long.class,
            new PathMetadata(null, "authorCount", PathType.VARIABLE));

        SQLQuery<Long> fromQuery = new SQLQuery<Long>(connection, configuration)
            .select(qWorklog.author.count().as(authorCountPath));

        appendBaseFromAndJoin(fromQuery);
        appendBaseWhere(fromQuery);
        fromQuery.groupBy(qWorklog.author);

        SQLQuery<Long> query = new SQLQuery<Long>(connection, configuration)
            .select(authorCountPath.count())
            .from(fromQuery.as("fromCount"));

        return query.fetchOne();
      }
    };
  }

  /**
   * Build user summary query.
   */
  public QuerydslCallable<List<UserSummaryDTO>> buildQuery() {
    return new QuerydslCallable<List<UserSummaryDTO>>() {

      @Override
      public List<UserSummaryDTO> call(final Connection connection,
          final Configuration configuration) throws SQLException {
        SQLQuery<UserSummaryDTO> query = new SQLQuery<UserSummaryDTO>(connection, configuration)
            .select(createQuerySelectProjection());

        appendBaseFromAndJoin(query);
        appendBaseWhere(query);
        appendQueryRange(query);

        query.groupBy(createQueryGroupBy());

        return query.fetch();
      }
    };
  }

  private Expression<?>[] createQueryGroupBy() {
    return new Expression<?>[] { qCwdUser.displayName,
        qWorklog.author };
  }

  private QBean<UserSummaryDTO> createQuerySelectProjection() {
    StringExpression userExpression = QueryUtil.createUserExpression(qCwdUser, qWorklog);

    return Projections.bean(UserSummaryDTO.class,
        userExpression.as(UserSummaryDTO.AliasNames.USER_DISPLAY_NAME),
        qWorklog.timeworked.sum().as(UserSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM));
  }

}
