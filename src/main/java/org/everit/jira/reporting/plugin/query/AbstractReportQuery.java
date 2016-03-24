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
import java.util.List;

import org.everit.jira.querydsl.schema.QAppUser;
import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.schema.QCustomfield;
import org.everit.jira.querydsl.schema.QCustomfieldvalue;
import org.everit.jira.querydsl.schema.QCwdUser;
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
import org.everit.jira.reporting.plugin.dto.WorklogDetailsSearchParam;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

public abstract class AbstractReportQuery<T> implements QuerydslCallable<List<T>> {

  protected BooleanExpression expressionFalse;

  protected BooleanExpression expressionTrue;

  protected final QAppUser qAppUser;

  protected final QCwdUser qCwdUser;

  protected final QJiraissue qIssue;

  protected final QIssuestatus qIssuestatus;

  protected final QIssuetype qIssuetype;

  protected final QPriority qPriority;

  protected final QProject qProject;

  protected final QResolution qResolution;

  protected final QWorklog qWorklog;

  protected final WorklogDetailsSearchParam worklogDetailsSearchParam;

  protected AbstractReportQuery(final WorklogDetailsSearchParam worklogDetailsSearchParam) {
    this.worklogDetailsSearchParam = worklogDetailsSearchParam;
    qIssue = new QJiraissue("issue");
    qProject = new QProject("project");
    qIssuetype = new QIssuetype("issuetype");
    qIssuestatus = new QIssuestatus("issuestatus");
    qWorklog = new QWorklog("worklog");
    qPriority = new QPriority("priority");
    qResolution = new QResolution("resolution");
    qAppUser = new QAppUser("appuser");
    qCwdUser = new QCwdUser("cwd_user");
    expressionTrue = Expressions.ONE.eq(Expressions.ONE);
    expressionFalse = Expressions.ONE.ne(Expressions.ONE);
  }

  @Override
  public List<T> call(final Connection connection,
      final Configuration configuration) throws SQLException {

    BooleanExpression where = expressionTrue;
    where = filterToProjectIds(qProject, where);
    where = filterToIssueTypeIds(qIssuetype, where);
    where = filterToIssueIds(qIssue, where);
    where = filterToAffectedVersions(qIssue, where);
    where = filterToFixedVersions(qIssue, where);
    where = filterToIssueAssignees(qIssue, where);
    where = filterToIssueComponents(qIssue, where);
    where = filterToIssueEpicIssueIds(qIssue, where);
    where = filterToIssueEpicName(qIssue, where);
    where = filterToIssueCreatedDate(qIssue, where);
    where = filterToIssueReporters(qIssue, where);
    where = filterToIssuePriorityIds(qPriority, where);
    where = filterToIssueResolution(qResolution, qIssue, where);
    where = filterToIssueStatusIds(qIssuestatus, where);
    where = filterToIssueLabels(qIssue, where);
    where = filterToWorklogAuhtors(qWorklog, where);
    where = filterToWorklogStartDate(qWorklog, where);
    where = filterToWorklogEndDate(qWorklog, where);

    List<T> result = new SQLQuery<T>(connection, configuration)
        .select(createSelectProjection())
        .from(qWorklog)
        .join(qIssue).on(qWorklog.issueid.eq(qIssue.id))
        .join(qProject).on(qIssue.project.eq(qProject.id))
        .join(qIssuetype).on(qIssue.issuetype.eq(qIssuetype.id))
        .join(qIssuestatus).on(qIssue.issuestatus.eq(qIssuestatus.id))
        .join(qPriority).on(qIssue.priority.eq(qPriority.id))
        .leftJoin(qResolution).on(qIssue.resolution.eq(qResolution.id))
        .leftJoin(qAppUser).on(qAppUser.userKey.eq(qWorklog.author))
        .leftJoin(qCwdUser).on(qCwdUser.lowerUserName.eq(qAppUser.lowerUserName))
        .where(where)
        .groupBy(createGroupBy())
        .fetch();

    extendResult(connection, configuration, result);

    return result;
  }

  protected abstract Expression<?>[] createGroupBy();

  protected StringExpression createIssueKeyExpression(final QJiraissue qIssue,
      final QProject qProject) {
    StringExpression issueKey = qProject.pkey.concat("-").concat(qIssue.issuenum.stringValue());
    return issueKey;
  }

  protected abstract QBean<T> createSelectProjection();

  protected abstract void extendResult(final Connection connection,
      final Configuration configuration, final List<T> result);

  private BooleanExpression filterToAffectedVersions(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationAffectedVersion =
        new QNodeassociation("nodeassociationaffectedversion");
    QProjectversion qProjectversion = new QProjectversion("projectversion_affectedversion");

    BooleanExpression existsAffectedVersions = expressionFalse;
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

    BooleanExpression notExistsNoAffectedVersions = expressionFalse;
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

  private BooleanExpression filterToFixedVersions(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationFixedVersion =
        new QNodeassociation("nodeassociationfixedversion");
    QProjectversion qProjectversion = new QProjectversion("projectversion_fixedversion");

    BooleanExpression existsVersions = expressionFalse;
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

    BooleanExpression notExistsNoVersions = expressionFalse;
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

    BooleanExpression releasedVersionExpression = expressionFalse;
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

    BooleanExpression unreleasedVersionExpression = expressionFalse;
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
    BooleanExpression assignedExpressions = expressionFalse;
    if (!worklogDetailsSearchParam.issueAssignees.isEmpty()) {
      assignedExpressions = qIssue.assignee.in(worklogDetailsSearchParam.issueAssignees);
      filterToIssueAssignees = true;
    }

    BooleanExpression unassignedExpressions = expressionFalse;
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

    BooleanExpression existsComponents = expressionFalse;
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

    BooleanExpression notExistsNoComponents = expressionFalse;
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

  private BooleanExpression filterToIssueEpicIssueIds(final QJiraissue qIssue,
      final BooleanExpression where) {
    QIssuelinktype qIssuelinktype = new QIssuelinktype("epic_issuelinktype");
    QIssuelink qIssuelink = new QIssuelink("epic_issuelink");

    if (!worklogDetailsSearchParam.issueEpicLinkIssueIds.isEmpty()) {
      return where.and(SQLExpressions.select(qIssuelink.id)
          .from(qIssuelink)
          .join(qIssuelinktype).on(qIssuelink.linktype.eq(qIssuelinktype.id))
          .where(qIssuelink.source.eq(qIssue.id)
              .and(qIssuelink.source.in(worklogDetailsSearchParam.issueEpicLinkIssueIds))
              .and(qIssuelinktype.linkname.eq("Epic-Story Link")))
          .exists());
    }
    return where;
  }

  private BooleanExpression filterToIssueEpicName(final QJiraissue qIssue,
      final BooleanExpression where) {
    QCustomfieldvalue qCustomfieldValue = new QCustomfieldvalue("customfieldvalue");
    QCustomfield qCustomfield = new QCustomfield("customfield");

    if (worklogDetailsSearchParam.issueEpicName != null) {
      return where.and(SQLExpressions.select(qCustomfield.id)
          .from(qCustomfield)
          .leftJoin(qCustomfieldValue).on(qCustomfield.id.eq(qCustomfieldValue.customfield))
          .where(qCustomfieldValue.issue.eq(qIssue.id)
              .and(qCustomfieldValue.stringvalue.toLowerCase()
                  .like(worklogDetailsSearchParam.issueEpicName.toLowerCase() + "%"))
              .and(qCustomfield.cfname.eq("Epic Name")))
          .exists());
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

  private BooleanExpression filterToIssueLabels(final QJiraissue qIssue,
      final BooleanExpression where) {
    QLabel qLabel = new QLabel("label");
    if (!worklogDetailsSearchParam.labels.isEmpty()) {
      return where.and(SQLExpressions.select(qLabel.id)
          .from(qLabel)
          .where(qLabel.issue.eq(qIssue.id)
              .and(qLabel.label.in(worklogDetailsSearchParam.labels)))
          .exists());
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

    BooleanExpression resolutionIssuesExpression = expressionFalse;
    if (!worklogDetailsSearchParam.issueResolutionIds.isEmpty()) {
      resolutionIssuesExpression = qResolution.id.in(worklogDetailsSearchParam.issueResolutionIds);
      filterToIssueResolution = true;
    }

    BooleanExpression unresolvedResolutionExpression = expressionFalse;
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

}
