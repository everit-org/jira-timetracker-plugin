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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.schema.QJiraissue;
import org.everit.jira.querydsl.schema.QNodeassociation;
import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.query.util.QueryUtil;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Queries for worklog details report.
 */
public class WorklogDetailsReportQueryBuilder extends AbstractReportQuery<WorklogDetailsDTO> {

  private SimpleExpression<String> issueAssigneeExpression;

  private StringExpression issueKey;

  private SimpleExpression<String> issueReporterExpression;

  private OrderBy orderBy;

  private HashMap<String, Expression<?>> orderByMap;

  private SimpleExpression<String> worklogAuthorExpression;

  /**
   * Simple constructor.
   *
   * @param reportSearchParam
   *          the {@link ReportSearchParam} object, that contains parameters to filter condition.
   * @param orderBy
   *          the {@link OrderBy} object.
   */
  public WorklogDetailsReportQueryBuilder(final ReportSearchParam reportSearchParam,
      final OrderBy orderBy) {
    super(reportSearchParam);

    if (orderBy != null) {
      this.orderBy = orderBy;
    } else {
      this.orderBy = OrderBy.DEFAULT;
    }

    createExpressions();
    createOrderByMap();
  }

  private ConcurrentSkipListSet<Long> collectIssueIds(final List<WorklogDetailsDTO> result) {
    ConcurrentSkipListSet<Long> issueIds = new ConcurrentSkipListSet<>();
    for (WorklogDetailsDTO worklogDetailsDTO : result) {
      issueIds.add(worklogDetailsDTO.getIssueId());
    }
    return issueIds;
  }

  private void createExpressions() {
    issueKey = QueryUtil.createIssueKeyExpression(qIssue, qProject);

    issueAssigneeExpression = new CaseBuilder()
        .when(QueryUtil.selectDisplayNameForUserExist(qIssue.assignee))
        .then(QueryUtil.selectDisplayNameForUser(qIssue.assignee))
        .otherwise(qIssue.assignee)
        .as(WorklogDetailsDTO.AliasNames.ISSUE_ASSIGNEE);

    issueReporterExpression = new CaseBuilder()
        .when(QueryUtil.selectDisplayNameForUserExist(qIssue.reporter))
        .then(QueryUtil.selectDisplayNameForUser(qIssue.reporter))
        .otherwise(qIssue.reporter)
        .as(WorklogDetailsDTO.AliasNames.ISSUE_REPORTER);

    worklogAuthorExpression = new CaseBuilder()
        .when(QueryUtil.selectDisplayNameForUserExist(qWorklog.author))
        .then(QueryUtil.selectDisplayNameForUser(qWorklog.author))
        .otherwise(qWorklog.author)
        .as(WorklogDetailsDTO.AliasNames.WORKLOG_USER);
  }

  private void createOrderByMap() {
    orderByMap = new HashMap<>();
    orderByMap.put(WorklogDetailsColumns.ASSIGNEE, issueAssigneeExpression);
    orderByMap.put(WorklogDetailsColumns.CREATED, qIssue.created);
    orderByMap.put(WorklogDetailsColumns.ESTIMATED, qIssue.timeoriginalestimate);
    orderByMap.put(WorklogDetailsColumns.ISSUE_KEY, issueKey);
    orderByMap.put(WorklogDetailsColumns.ISSUE_SUMMARY, qIssue.summary);
    orderByMap.put(WorklogDetailsColumns.PRIORITY, qPriority.sequence);
    orderByMap.put(WorklogDetailsColumns.PROJECT, qProject.pname);
    orderByMap.put(WorklogDetailsColumns.REMAINING, qIssue.timeestimate);
    orderByMap.put(WorklogDetailsColumns.REPORTER, issueReporterExpression);
    orderByMap.put(WorklogDetailsColumns.RESOLUTION, qResolution.sequence);
    orderByMap.put(WorklogDetailsColumns.START_TIME, qWorklog.startdate);
    orderByMap.put(WorklogDetailsColumns.STATUS, qIssuestatus.sequence);
    orderByMap.put(WorklogDetailsColumns.TIME_SPENT, qWorklog.timeworked);
    orderByMap.put(WorklogDetailsColumns.TYPE, qIssuetype.sequence);
    orderByMap.put(WorklogDetailsColumns.UPDATED, qIssue.updated);
    orderByMap.put(WorklogDetailsColumns.USER, worklogAuthorExpression);
    orderByMap.put(WorklogDetailsColumns.WORKLOG_CREATED, qWorklog.created);
    orderByMap.put(WorklogDetailsColumns.WORKLOG_UPDATED, qWorklog.updated);
  }

  private QBean<WorklogDetailsDTO> createQuerySelectProjection() {
    return Projections.bean(WorklogDetailsDTO.class,
        qProject.pname.as(WorklogDetailsDTO.AliasNames.PROJECT_NAME),
        qProject.pkey.as(WorklogDetailsDTO.AliasNames.PROJECT_KEY),
        issueKey.as(WorklogDetailsDTO.AliasNames.ISSUE_KEY),
        qIssue.summary.as(WorklogDetailsDTO.AliasNames.ISSUE_SUMMARY),
        qIssue.id.as(WorklogDetailsDTO.AliasNames.ISSUE_ID),
        qIssuetype.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_TYPE_NAME),
        qIssuetype.iconurl.as(WorklogDetailsDTO.AliasNames.ISSUE_TYPE_ICON_URL),
        qIssuetype.avatar.as(WorklogDetailsDTO.AliasNames.ISSUE_AVATAR_ID),
        qIssuestatus.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_STATUS_P_NAME),
        issueAssigneeExpression,
        qIssue.timeoriginalestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_TIME_ORIGINAL_ESTIMATE),
        qIssue.timeestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_TIME_ESTIMATE),
        qWorklog.worklogbody.as(WorklogDetailsDTO.AliasNames.WORKLOG_BODY),
        qWorklog.timeworked.as(WorklogDetailsDTO.AliasNames.WORKLOG_TIME_WORKED),
        qProject.description.as(WorklogDetailsDTO.AliasNames.PROJECT_DESCRIPTION),
        qPriority.pname.as(WorklogDetailsDTO.AliasNames.PRIORITY_NAME),
        qPriority.iconurl.as(WorklogDetailsDTO.AliasNames.PRIORITY_ICON_URL),
        issueReporterExpression,
        qIssue.created.as(WorklogDetailsDTO.AliasNames.ISSUE_CREATED),
        qIssue.updated.as(WorklogDetailsDTO.AliasNames.ISSUE_UPDATED),
        qResolution.pname.as(WorklogDetailsDTO.AliasNames.RESOLUTION_NAME),
        qWorklog.startdate.as(WorklogDetailsDTO.AliasNames.WORKLOG_START_DATE),
        qWorklog.created.as(WorklogDetailsDTO.AliasNames.WORKLOG_CREATED),
        qWorklog.updated.as(WorklogDetailsDTO.AliasNames.WORKLOG_UPDATED),
        worklogAuthorExpression);
  }

  private void extendResult(final Connection connection, final Configuration configuration,
      final List<WorklogDetailsDTO> result) {
    ConcurrentSkipListSet<Long> collectIssueIds = collectIssueIds(result);

    Map<Long, List<String>> issueComponents = selectIssueComponents(connection, configuration,
        collectIssueIds);

    Map<Long, List<String>> issueFixedVersions = selectIssueFixedVersions(connection, configuration,
        collectIssueIds);

    Map<Long, List<String>> issueAffectedVersions = selectAffectedVersions(connection,
        configuration, collectIssueIds);

    for (WorklogDetailsDTO worklogDetailsDTO : result) {
      Long issueId = worklogDetailsDTO.getIssueId();

      List<String> components = issueComponents.get(issueId);
      if (components != null) {
        worklogDetailsDTO.setIssueComponents(components);
      }

      List<String> affectedVersions = issueAffectedVersions.get(issueId);
      if (affectedVersions != null) {
        worklogDetailsDTO.setIssueAffectedVersions(affectedVersions);
      }

      List<String> fixedVersions = issueFixedVersions.get(issueId);
      if (fixedVersions != null) {
        worklogDetailsDTO.setIssueFixedVersions(fixedVersions);
      }
    }

  }

  @Override
  protected QuerydslCallable<Long> getCountQuery() {
    return new QuerydslCallable<Long>() {
      @Override
      public Long call(final Connection connection, final Configuration configuration)
          throws SQLException {
        NumberPath<Long> worklogCountPath = Expressions.numberPath(Long.class,
            new PathMetadata(null, "worklogCount", PathType.VARIABLE));

        SQLQuery<Long> fromQuery = new SQLQuery<Long>(connection, configuration)
            .select(qWorklog.id.count().as(worklogCountPath));

        appendBaseFromAndJoin(fromQuery);
        appendBaseWhere(fromQuery);
        fromQuery.groupBy(qWorklog.id);

        SQLQuery<Long> query = new SQLQuery<Long>(connection, configuration)
            .select(worklogCountPath.count())
            .from(fromQuery.as("fromCount"));

        return query.fetchOne();
      }
    };
  }

  @Override
  protected QuerydslCallable<List<WorklogDetailsDTO>> getQuery() {
    return new QuerydslCallable<List<WorklogDetailsDTO>>() {

      @Override
      public List<WorklogDetailsDTO> call(final Connection connection,
          final Configuration configuration) throws SQLException {

        SQLQuery<WorklogDetailsDTO> query =
            new SQLQuery<WorklogDetailsDTO>(connection, configuration)
                .select(createQuerySelectProjection());

        appendBaseFromAndJoin(query);
        appendBaseWhere(query);
        appendQueryRange(query);

        Expression<?> expression = orderByMap.get(orderBy.columnName);
        Order order = Order.DESC;
        if (expression == null) {
          expression = orderByMap.get(OrderBy.DEFAULT.columnName);
          order = Order.ASC;
        } else {
          if (orderBy.asc) {
            order = Order.ASC;
          }
        }
        query.orderBy(new OrderSpecifier(order, expression));

        List<WorklogDetailsDTO> result = query.fetch();

        extendResult(connection, configuration, result);

        return result;
      }
    };
  }

  private Map<Long, List<String>> selectAffectedVersions(final Connection connection,
      final Configuration configuration, final Set<Long> issueIds) {
    QJiraissue qIssue = new QJiraissue("na_issue");
    QNodeassociation qNodeassociation = new QNodeassociation("nodeassocitation");
    QProjectversion qProjectversion = new QProjectversion("na_version");

    return new SQLQuery<>(connection, configuration)
        .select(qIssue.id, qProjectversion.vname)
        .from(qProjectversion)
        .join(qNodeassociation).on(qNodeassociation.sinkNodeId.eq(qProjectversion.id))
        .join(qIssue).on(qNodeassociation.sourceNodeId.eq(qIssue.id))
        .where(qNodeassociation.associationType.eq(IssueRelationConstants.VERSION)
            .and(qNodeassociation.sinkNodeEntity.eq(Entity.Name.VERSION))
            .and(qNodeassociation.sourceNodeEntity.eq(Entity.Name.ISSUE))
            .and(qIssue.id.in(issueIds)))
        .transform(GroupBy.groupBy(qIssue.id).as(GroupBy.list(qProjectversion.vname)));
  }

  private Map<Long, List<String>> selectIssueComponents(final Connection connection,
      final Configuration configuration, final Set<Long> issueIds) {
    QJiraissue qIssue = new QJiraissue("na_issue");
    QNodeassociation qNodeassociation = new QNodeassociation("nodeassocitation");
    QComponent qComponent = new QComponent("na_component");

    return new SQLQuery<>(connection, configuration)
        .select(qIssue.id, qComponent.cname)
        .from(qComponent)
        .join(qNodeassociation).on(qNodeassociation.sinkNodeId.eq(qComponent.id))
        .join(qIssue).on(qNodeassociation.sourceNodeId.eq(qIssue.id))
        .where(qNodeassociation.associationType.eq(IssueRelationConstants.COMPONENT)
            .and(qNodeassociation.sinkNodeEntity.eq(Entity.Name.COMPONENT))
            .and(qNodeassociation.sourceNodeEntity.eq(Entity.Name.ISSUE))
            .and(qIssue.id.in(issueIds)))
        .transform(GroupBy.groupBy(qIssue.id).as(GroupBy.list(qComponent.cname)));
  }

  private Map<Long, List<String>> selectIssueFixedVersions(final Connection connection,
      final Configuration configuration, final Set<Long> issueIds) {
    QJiraissue qIssue = new QJiraissue("na_issue");
    QNodeassociation qNodeassociation = new QNodeassociation("nodeassocitation");
    QProjectversion qProjectversion = new QProjectversion("na_version");

    return new SQLQuery<>(connection, configuration)
        .select(qIssue.id, qProjectversion.vname)
        .from(qProjectversion)
        .join(qNodeassociation).on(qNodeassociation.sinkNodeId.eq(qProjectversion.id))
        .join(qIssue).on(qNodeassociation.sourceNodeId.eq(qIssue.id))
        .where(qNodeassociation.associationType.eq(IssueRelationConstants.FIX_VERSION)
            .and(qNodeassociation.sinkNodeEntity.eq(Entity.Name.VERSION))
            .and(qNodeassociation.sourceNodeEntity.eq(Entity.Name.ISSUE))
            .and(qIssue.id.in(issueIds)))
        .transform(GroupBy.groupBy(qIssue.id).as(GroupBy.list(qProjectversion.vname)));
  }
}
