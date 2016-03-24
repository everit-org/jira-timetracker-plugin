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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.schema.QJiraissue;
import org.everit.jira.querydsl.schema.QNodeassociation;
import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsSearchParam;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

public class WorklogDetailsQuery extends AbstractReportQuery<WorklogDetailsDTO> {

  public WorklogDetailsQuery(final WorklogDetailsSearchParam worklogDetailsSearchParam) {
    super(worklogDetailsSearchParam);
  }

  private ConcurrentSkipListSet<Long> collectIssueIds(final List<WorklogDetailsDTO> result) {
    ConcurrentSkipListSet<Long> issueIds = new ConcurrentSkipListSet<>();
    for (WorklogDetailsDTO worklogDetailsDTO : result) {
      issueIds.add(worklogDetailsDTO.getIssueId());
    }
    return issueIds;
  }

  @Override
  protected Expression<?>[] createGroupBy() {
    return new Expression<?>[] {};
  }

  @Override
  protected QBean<WorklogDetailsDTO> createSelectProjection() {
    StringExpression issueKey = createIssueKeyExpression(qIssue, qProject);
    return Projections.bean(WorklogDetailsDTO.class,
        qProject.pkey.as(WorklogDetailsDTO.AliasNames.PROJECT_KEY),
        issueKey.as(WorklogDetailsDTO.AliasNames.ISSUE_KEY),
        qIssue.summary.as(WorklogDetailsDTO.AliasNames.ISSUE_SUMMARY),
        qIssue.id.as(WorklogDetailsDTO.AliasNames.ISSUE_ID),
        qIssuetype.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_TYPE_NAME),
        qIssuestatus.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_STATUS_P_NAME),
        qIssue.assignee.as(WorklogDetailsDTO.AliasNames.ISSUE_ASSIGNE),
        qIssue.timeoriginalestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_TIME_ORIGINAL_ESTIMATE),
        qIssue.timeestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_TIME_ESTIMATE),
        qWorklog.worklogbody.as(WorklogDetailsDTO.AliasNames.WORKLOG_BODY),
        qWorklog.timeworked.as(WorklogDetailsDTO.AliasNames.WORKLOG_TIME_WORKED),
        qProject.description.as(WorklogDetailsDTO.AliasNames.PROJECT_DESCRIPTION),
        qPriority.pname.as(WorklogDetailsDTO.AliasNames.PRIORITY_NAME),
        qIssue.reporter.as(WorklogDetailsDTO.AliasNames.ISSUE_REPORTER),
        qIssue.created.as(WorklogDetailsDTO.AliasNames.ISSUE_CREATED),
        qIssue.updated.as(WorklogDetailsDTO.AliasNames.ISSUE_UPDATED),
        qResolution.pname.as(WorklogDetailsDTO.AliasNames.RESOLUTION_NAME),
        qWorklog.startdate.as(WorklogDetailsDTO.AliasNames.WORKLOG_START_DATE),
        qWorklog.created.as(WorklogDetailsDTO.AliasNames.WORKLOG_CREATED),
        qWorklog.updated.as(WorklogDetailsDTO.AliasNames.WORKLOG_UPDATED));
  }

  @Override
  protected void extendResult(final Connection connection, final Configuration configuration,
      final List<WorklogDetailsDTO> result) {
    ConcurrentSkipListSet<Long> collectIssueIds = collectIssueIds(result);

    Map<Long, List<String>> issueComponents = selectIssueComponents(connection, configuration,
        collectIssueIds);

    Map<Long, List<String>> issueFixedVersions = selectIssueFixedVersions(connection, configuration,
        collectIssueIds);

    Map<Long, List<String>> issueAffectedVersions = selectAffectedVersions(connection,
        configuration, collectIssueIds);

    List<String> emptyList = Collections.emptyList();
    for (WorklogDetailsDTO worklogDetailsDTO : result) {
      Long issueId = worklogDetailsDTO.getIssueId();

      worklogDetailsDTO.setIssueComponents(issueComponents.getOrDefault(issueId, emptyList));

      worklogDetailsDTO.setIssueAffectedVersion(
          issueAffectedVersions.getOrDefault(issueId, emptyList));

      worklogDetailsDTO.setIssueFixedVersions(issueFixedVersions.getOrDefault(issueId, emptyList));
    }

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
