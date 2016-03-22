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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.schema.QCustomfield;
import org.everit.jira.querydsl.schema.QCustomfieldvalue;
import org.everit.jira.querydsl.schema.QIssuelink;
import org.everit.jira.querydsl.schema.QIssuelinktype;
import org.everit.jira.querydsl.schema.QIssuestatus;
import org.everit.jira.querydsl.schema.QIssuetype;
import org.everit.jira.querydsl.schema.QJiraissue;
import org.everit.jira.querydsl.schema.QLabel;
import org.everit.jira.querydsl.schema.QNodeassociation;
import org.everit.jira.querydsl.schema.QPriority;
import org.everit.jira.querydsl.schema.QProject;
import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.querydsl.schema.QResolution;
import org.everit.jira.querydsl.schema.QWorklog;
import org.everit.jira.querydsl.support.QuerydslCallable;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

public class WorklogDetailsQuery implements QuerydslCallable<List<WorklogDetailsDTO>> {

  private final WorklogDetailsSearchParam worklogDetailsSearchParam;

  public WorklogDetailsQuery(final WorklogDetailsSearchParam worklogDetailsSearchParam) {
    this.worklogDetailsSearchParam = worklogDetailsSearchParam;
  }

  @Override
  public List<WorklogDetailsDTO> call(final Connection connection,
      final Configuration configuration) throws SQLException {
    QJiraissue qIssue = new QJiraissue("issue");
    QProject qProject = new QProject("project");
    QIssuetype qIssuetype = new QIssuetype("issuetype");
    QIssuestatus qIssuestatus = new QIssuestatus("issuestatus");
    QWorklog qWorklog = new QWorklog("worklog");
    QPriority qPriority = new QPriority("priority");
    QResolution qResolution = new QResolution("resolution");
    QIssuelinktype qEpicIssuelinktype = new QIssuelinktype("epic_issuelinktype");
    QIssuelink qEpicIssuelink = new QIssuelink("epic_issuelink");
    QCustomfieldvalue qCustomfieldValue = new QCustomfieldvalue("customfieldvalue");
    QCustomfield qCustomfield = new QCustomfield("customfield");
    QLabel qLabel = new QLabel("label");

    BooleanExpression where = Expressions.TRUE;
    where = filterToProjectIds(qProject, where);
    where = filterToIssueTypeIds(qIssuetype, where);
    where = filterToIssueIds(qIssue, where);
    where = filterToAffectedVersions(qIssue, where);
    where = filterToFixedVersions(qIssue, where);
    where = filterToIssueAssignees(qIssue, where);
    where = filterToIssueComponents(qIssue, where);
    where = filterToIssueCreatedDate(qIssue, where);
    where = filterToIssueReporters(qIssue, where);
    where = filterToEpicIssueIds(qEpicIssuelinktype, qEpicIssuelink, where);
    where = filterToIssueEpicName(qCustomfieldValue, qCustomfield, where);
    where = filterToIssuePriorityIds(qPriority, where);
    where = filterToIssueResolution(qResolution, qIssue, where);
    where = filterToIssueStatusIds(qIssuestatus, where);
    where = filterToIssueLabels(qLabel, where);
    where = filterToWorklogAuhtors(qWorklog, where);
    where = filterToWorklogStartDate(qWorklog, where);
    where = filterToWorklogEndDate(qWorklog, where);

    List<WorklogDetailsDTO> result = new SQLQuery<WorklogDetailsDTO>(connection, configuration)
        .select(createWroklogDetailsProjection(qIssue, qProject, qIssuetype, qIssuestatus, qWorklog,
            qPriority, qResolution))
        .from(qWorklog)
        .join(qIssue).on(qWorklog.issueid.eq(qIssue.id))
        .join(qProject).on(qIssue.project.eq(qProject.id))
        .join(qIssuetype).on(qIssue.issuetype.eq(qIssuetype.id))
        .join(qIssuestatus).on(qIssue.issuestatus.eq(qIssuestatus.id))
        .join(qPriority).on(qIssue.priority.eq(qPriority.id))
        .leftJoin(qResolution).on(qIssue.resolution.eq(qResolution.id))
        .leftJoin(qLabel).on(qIssue.id.eq(qLabel.issue))
        .leftJoin(qEpicIssuelink).on(qIssue.id.eq(qEpicIssuelink.source))
        .leftJoin(qEpicIssuelinktype).on(qEpicIssuelink.linktype.eq(qEpicIssuelinktype.id))
        .leftJoin(qCustomfieldValue).on(qIssue.id.eq(qCustomfieldValue.issue))
        .leftJoin(qCustomfield).on(qCustomfieldValue.customfield.eq(qCustomfield.id))
        .groupBy(qWorklog.id,
            qIssue.id,
            qProject.id,
            qIssuetype.id,
            qIssuestatus.id,
            qPriority.id,
            qResolution.id)
        .where(where)
        .fetch();

    extendResult(connection, configuration, result);

    return result;
  }

  private ConcurrentSkipListSet<Long> collectIssueIds(final List<WorklogDetailsDTO> result) {
    ConcurrentSkipListSet<Long> foundedIssueIds = new ConcurrentSkipListSet<>();
    for (WorklogDetailsDTO worklogDetailsDTO : result) {
      foundedIssueIds.add(worklogDetailsDTO.issueId);
    }
    return foundedIssueIds;
  }

  private StringExpression createIssueKeyExpression(final QJiraissue qIssue,
      final QProject qProject) {
    StringExpression issueKey = qProject.pkey.concat("-").concat(qIssue.issuenum.stringValue());
    return issueKey;
  }

  private QBean<WorklogDetailsDTO> createWroklogDetailsProjection(final QJiraissue qIssue,
      final QProject qProject, final QIssuetype qIssuetype, final QIssuestatus qIssuestatus,
      final QWorklog qWorklog, final QPriority qPriority, final QResolution qResolution) {
    StringExpression issueKey = createIssueKeyExpression(qIssue, qProject);
    return Projections.fields(WorklogDetailsDTO.class,
        qProject.pkey.as(WorklogDetailsDTO.AliasNames.PROJECT_KEY),
        issueKey.as(WorklogDetailsDTO.AliasNames.ISSUE_KEY),
        qIssue.summary.as(WorklogDetailsDTO.AliasNames.ISSUE_SUMMARY),
        qIssue.id.as(WorklogDetailsDTO.AliasNames.ISSUE_ID),
        qIssuetype.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_TYPE_NAME),
        qIssuestatus.pname.as(WorklogDetailsDTO.AliasNames.ISSUE_STATUS_NAME),
        qIssue.assignee.as(WorklogDetailsDTO.AliasNames.ISSUE_ASSIGNE),
        qIssue.timeoriginalestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_ORIGINAL_ESTIMATE),
        qIssue.timeestimate.as(WorklogDetailsDTO.AliasNames.ISSUE_REMAINING_ESTIMATE),
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

  private void extendResult(final Connection connection, final Configuration configuration,
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
      Long issueId = worklogDetailsDTO.issueId;

      worklogDetailsDTO.issueComponents = issueComponents.getOrDefault(issueId, emptyList);

      worklogDetailsDTO.issueAffectedVersion =
          issueAffectedVersions.getOrDefault(issueId, emptyList);

      worklogDetailsDTO.issueFixedVersions = issueFixedVersions.getOrDefault(issueId, emptyList);
    }
  }

  private BooleanExpression filterToAffectedVersions(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationAffectedVersion =
        new QNodeassociation("nodeassociationaffectedversion");
    QProjectversion qProjectversion = new QProjectversion("projectversion_affectedversion");

    BooleanExpression existsAffectedVersions = Expressions.FALSE;
    boolean filterToAffectedVersions = false;
    if (!worklogDetailsSearchParam.issueAffectedVersions.isEmpty()) {
      existsAffectedVersions = SQLExpressions.select(qNodeassociationAffectedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationAffectedVersion)
          .on(qNodeassociationAffectedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationAffectedVersion.associationType.eq(IssueRelationConstants.VERSION)
              .and(qNodeassociationAffectedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationAffectedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationAffectedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.vname.in(worklogDetailsSearchParam.issueAffectedVersions)))
          .exists();
      filterToAffectedVersions = true;
    }

    BooleanExpression notExistsNoAffectedVersions = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectNoAffectedVersionIssue) {
      notExistsNoAffectedVersions =
          SQLExpressions.select(qNodeassociationAffectedVersion.sourceNodeId)
              .from(qProjectversion)
              .join(qNodeassociationAffectedVersion)
              .on(qNodeassociationAffectedVersion.sinkNodeId.eq(qProjectversion.id))
              .where(
                  qNodeassociationAffectedVersion.associationType.eq(IssueRelationConstants.VERSION)
                      .and(qNodeassociationAffectedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
                      .and(qNodeassociationAffectedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
                      .and(qNodeassociationAffectedVersion.sourceNodeId.eq(qIssue.id)))
              .notExists();
      filterToAffectedVersions = true;
    }

    if (filterToAffectedVersions) {
      return where.and(existsAffectedVersions.or(notExistsNoAffectedVersions));
    }
    return where;
  }

  private BooleanExpression filterToEpicIssueIds(final QIssuelinktype qIssuelinktype,
      final QIssuelink qIssuelink, final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issueEpicLinkIssueIds.isEmpty()) {
      return where.and(qIssuelink.source.in(worklogDetailsSearchParam.issueEpicLinkIssueIds)
          .and(qIssuelinktype.linkname.eq("Epic-Story Link")));
    }
    return where;
  }

  private BooleanExpression filterToFixedVersions(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationFixedVersion =
        new QNodeassociation("nodeassociationfixedversion");
    QProjectversion qProjectversion = new QProjectversion("projectversion_fixedversion");

    BooleanExpression existsVersions = Expressions.FALSE;
    boolean filterToFixedVersions = false;
    if (!worklogDetailsSearchParam.issueFixedVersions.isEmpty()) {
      existsVersions = SQLExpressions.select(qNodeassociationFixedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationFixedVersion)
          .on(qNodeassociationFixedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationFixedVersion.associationType.eq(IssueRelationConstants.FIX_VERSION)
              .and(qNodeassociationFixedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationFixedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationFixedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.vname.in(worklogDetailsSearchParam.issueFixedVersions)))
          .exists();
      filterToFixedVersions = true;
    }

    BooleanExpression notExistsNoVersions = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectNoFixedVersionIssue) {
      notExistsNoVersions = SQLExpressions.select(qNodeassociationFixedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationFixedVersion)
          .on(qNodeassociationFixedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationFixedVersion.associationType.eq(IssueRelationConstants.FIX_VERSION)
              .and(qNodeassociationFixedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationFixedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationFixedVersion.sourceNodeId.eq(qIssue.id)))
          .notExists();
      filterToFixedVersions = true;
    }

    BooleanExpression releasedVersionExpression = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectReleasedFixVersion) {
      releasedVersionExpression = SQLExpressions
          .select(qNodeassociationFixedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationFixedVersion)
          .on(qNodeassociationFixedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationFixedVersion.associationType.eq(IssueRelationConstants.FIX_VERSION)
              .and(qNodeassociationFixedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationFixedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationFixedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.released.toLowerCase().eq("true")))
          .exists();
      filterToFixedVersions = true;
    }

    BooleanExpression unreleasedVersionExpression = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectUnreleasedFixVersion) {
      unreleasedVersionExpression = SQLExpressions
          .select(qNodeassociationFixedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationFixedVersion)
          .on(qNodeassociationFixedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationFixedVersion.associationType.eq(IssueRelationConstants.FIX_VERSION)
              .and(qNodeassociationFixedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationFixedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationFixedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.released.isNull()))
          .exists();
      filterToFixedVersions = true;
    }

    if (filterToFixedVersions) {
      return where.and(existsVersions
          .or(notExistsNoVersions)
          .or(releasedVersionExpression)
          .or(unreleasedVersionExpression));
    }
    return where;
  }

  private BooleanExpression filterToIssueAssignees(final QJiraissue qIssue,
      final BooleanExpression where) {
    boolean filterToIssueAssignees = false;
    BooleanExpression assignedExpressions = Expressions.FALSE;
    if (!worklogDetailsSearchParam.issueAssignees.isEmpty()) {
      assignedExpressions = qIssue.assignee.in(worklogDetailsSearchParam.issueAssignees);
      filterToIssueAssignees = true;
    }

    BooleanExpression unassignedExpressions = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectUnassgined) {
      unassignedExpressions = qIssue.assignee.isNull();
      filterToIssueAssignees = true;
    }

    if (filterToIssueAssignees) {
      return where.and(assignedExpressions.or(unassignedExpressions));
    }
    return where;
  }

  private BooleanExpression filterToIssueComponents(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationComponents =
        new QNodeassociation("nodeassociationcomponents");
    QComponent qComponent = new QComponent("component");

    BooleanExpression existsComponents = Expressions.FALSE;
    boolean filterToIssueComponent = false;
    if (!worklogDetailsSearchParam.issueComponents.isEmpty()) {
      existsComponents = SQLExpressions.select(qNodeassociationComponents.sourceNodeId)
          .from(qComponent)
          .join(qNodeassociationComponents)
          .on(qNodeassociationComponents.sinkNodeId.eq(qComponent.id))
          .where(qNodeassociationComponents.associationType.eq(IssueRelationConstants.COMPONENT)
              .and(qNodeassociationComponents.sinkNodeEntity.eq(Entity.Name.COMPONENT))
              .and(qNodeassociationComponents.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationComponents.sourceNodeId.eq(qIssue.id))
              .and(qComponent.cname.in(worklogDetailsSearchParam.issueComponents)))
          .exists();
      filterToIssueComponent = true;
    }

    BooleanExpression notExistsNoComponents = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectNoComponentIssue) {
      notExistsNoComponents = SQLExpressions.select(qNodeassociationComponents.sourceNodeId)
          .from(qComponent)
          .join(qNodeassociationComponents)
          .on(qNodeassociationComponents.sinkNodeId.eq(qComponent.id))
          .where(qNodeassociationComponents.associationType.eq(IssueRelationConstants.COMPONENT)
              .and(qNodeassociationComponents.sinkNodeEntity.eq(Entity.Name.COMPONENT))
              .and(qNodeassociationComponents.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationComponents.sourceNodeId.eq(qIssue.id)))
          .notExists();
      filterToIssueComponent = true;
    }

    if (filterToIssueComponent) {
      return where.and(existsComponents.or(notExistsNoComponents));
    }
    return where;
  }

  private BooleanExpression filterToIssueCreatedDate(final QJiraissue qIssue,
      final BooleanExpression where) {
    if (worklogDetailsSearchParam.issueCreateDate != null) {
      return where
          .and(qIssue.created.eq(
              new Timestamp(worklogDetailsSearchParam.issueCreateDate
                  .getTime())));
    }
    return where;
  }

  private BooleanExpression filterToIssueEpicName(final QCustomfieldvalue qCustomfieldValue,
      final QCustomfield qCustomfield, final BooleanExpression where) {
    if (worklogDetailsSearchParam.issueEpicName != null) {
      return where.and(qCustomfieldValue.stringvalue.toLowerCase()
          .like(worklogDetailsSearchParam.issueEpicName.toLowerCase() + "%")
          .and(qCustomfield.cfname.eq("Epic Name")));
    }
    return where;
  }

  private BooleanExpression filterToIssueIds(final QJiraissue qIssue,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issueIds.isEmpty()) {
      return where.and(qIssue.id.in(worklogDetailsSearchParam.issueIds));
    }
    return where;
  }

  private BooleanExpression filterToIssueLabels(final QLabel qLabel,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.labels.isEmpty()) {
      return where.and(qLabel.label.in(worklogDetailsSearchParam.labels));
    }
    return where;
  }

  private BooleanExpression filterToIssuePriorityIds(final QPriority qPriority,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issuePriorityIds.isEmpty()) {
      return where.and(qPriority.id.in(worklogDetailsSearchParam.issuePriorityIds));
    }
    return where;
  }

  private BooleanExpression filterToIssueReporters(final QJiraissue qIssue,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issueReporters.isEmpty()) {
      return where.and(qIssue.reporter.in(worklogDetailsSearchParam.issueReporters));
    }
    return where;
  }

  private BooleanExpression filterToIssueResolution(final QResolution qResolution,
      final QJiraissue qIssue, final BooleanExpression where) {
    boolean filterToIssueResolution = false;

    BooleanExpression resolutionIssuesExpression = Expressions.FALSE;
    if (!worklogDetailsSearchParam.issueResolutionIds.isEmpty()) {
      resolutionIssuesExpression = qResolution.id.in(worklogDetailsSearchParam.issueResolutionIds);
      filterToIssueResolution = true;
    }

    BooleanExpression unresolvedResolutionExpression = Expressions.FALSE;
    if (worklogDetailsSearchParam.selectUnresolvedResolution) {
      unresolvedResolutionExpression = qIssue.resolution.isNull();
      filterToIssueResolution = true;
    }

    if (filterToIssueResolution) {
      return where.and(resolutionIssuesExpression.or(unresolvedResolutionExpression));
    }
    return where;
  }

  private BooleanExpression filterToIssueStatusIds(final QIssuestatus qIssuestatus,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issueStatusIds.isEmpty()) {
      return where.and(qIssuestatus.id.in(worklogDetailsSearchParam.issueStatusIds));
    }
    return where;
  }

  private BooleanExpression filterToIssueTypeIds(final QIssuetype qIssuetype,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.issueTypeIds.isEmpty()) {
      return where.and(qIssuetype.id.in(worklogDetailsSearchParam.issueTypeIds));
    }
    return where;
  }

  private BooleanExpression filterToProjectIds(final QProject qProject,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.projectIds.isEmpty()) {
      return where.and(qProject.id.in(worklogDetailsSearchParam.projectIds));
    }
    return where;
  }

  private BooleanExpression filterToWorklogAuhtors(final QWorklog qWorklog,
      final BooleanExpression where) {
    if (!worklogDetailsSearchParam.users.isEmpty()) {
      return where.and(qWorklog.author.in(worklogDetailsSearchParam.users));
    }
    return where;
  }

  private BooleanExpression filterToWorklogEndDate(final QWorklog qWorklog,
      final BooleanExpression where) {
    if (worklogDetailsSearchParam.worklogEndDate != null) {
      return where.and(qWorklog.startdate.lt(
          new Timestamp(worklogDetailsSearchParam.worklogEndDate
              .getTime())));
    }
    return where;
  }

  private BooleanExpression filterToWorklogStartDate(final QWorklog qWorklog,
      final BooleanExpression where) {
    if (worklogDetailsSearchParam.worklogStartDate != null) {
      return where.and(qWorklog.startdate.goe(
          new Timestamp(worklogDetailsSearchParam.worklogStartDate
              .getTime())));
    }
    return where;
  }

  private Map<Long, List<String>> selectAffectedVersions(final Connection connection,
      final Configuration configuration, final Set<Long> issueIds) {
    QJiraissue qIssue = new QJiraissue("nodeassocitation_issue");
    QNodeassociation qNodeassociation =
        new QNodeassociation("nodeassocitation_affected_version");
    QProjectversion qProjectversion = new QProjectversion("nodeassocitation_version");

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
    QJiraissue qIssue = new QJiraissue("nodeassocitation_issue");
    QNodeassociation qNodeassociation =
        new QNodeassociation("nodeassocitation_affected_version");
    QComponent qComponent = new QComponent("nodeassocitation_component");

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
    QJiraissue qIssue = new QJiraissue("nodeassocitation_issue");
    QNodeassociation qNodeassociation =
        new QNodeassociation("nodeassocitation_fixed_version");
    QProjectversion qProjectversion = new QProjectversion("nodeassocitation_version");

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
