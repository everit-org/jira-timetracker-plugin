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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.everit.jira.querydsl.schema.QAppUser;
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
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.exception.JTRPException;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

/**
 * Abstract implementation of {@link QuerydslCallable}. Provide source (from), joins and filter
 * condition to report queries.
 *
 * @param <T>
 *          Type of the return value.
 */
public abstract class AbstractReportQuery<T> {

  protected BooleanExpression expressionFalse;

  protected BooleanExpression expressionTrue;

  protected final QJiraissue qIssue;

  protected final QIssuestatus qIssuestatus;

  protected final QIssuetype qIssuetype;

  protected final QPriority qPriority;

  protected final QProject qProject;

  protected final QResolution qResolution;

  protected final QWorklog qWorklog;

  protected final ReportSearchParam reportSearchParam;

  /**
   * Simple constructor.
   *
   * @param reportSearchParam
   *          the {@link ReportSearchParam} object, that contains parameters to filter condition.
   *
   * @throws JTRPException
   *           if {@link ReportSearchParam#projectIds} is empty.
   */
  protected AbstractReportQuery(final ReportSearchParam reportSearchParam) {
    this.reportSearchParam = reportSearchParam;
    if (reportSearchParam.projectIds.isEmpty()) {
      throw new JTRPException("jtrp.plugin.no_browsable_project_ids");
    }
    qIssue = new QJiraissue("issue");
    qProject = new QProject("project");
    qIssuetype = new QIssuetype("issuetype");
    qIssuestatus = new QIssuestatus("issuestatus");
    qWorklog = new QWorklog("worklog");
    qPriority = new QPriority("priority");
    qResolution = new QResolution("resolution");
    expressionTrue = Expressions.ONE.eq(Expressions.ONE);
    expressionFalse = Expressions.ONE.ne(Expressions.ONE);
  }

  /**
   * Append base from and joins to the given query.
   *
   * @param query
   *          the {@link SQLQuery}.
   */
  protected void appendBaseFromAndJoin(final SQLQuery<?> query) {
    query.from(qWorklog)
        .join(qIssue).on(qWorklog.issueid.eq(qIssue.id))
        .join(qProject).on(qIssue.project.eq(qProject.id))
        .join(qIssuetype).on(qIssue.issuetype.eq(qIssuetype.id))
        .join(qIssuestatus).on(qIssue.issuestatus.eq(qIssuestatus.id))
        .join(qPriority).on(qIssue.priority.eq(qPriority.id))
        .leftJoin(qResolution).on(qIssue.resolution.eq(qResolution.id));
  }

  /**
   * Append base filter condition to the given query.
   *
   * @param query
   *          the {@link SQLQuery}.
   */
  protected void appendBaseWhere(final SQLQuery<?> query) {
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

    query.where(where);
  }

  /**
   * Append query range to query. Set offset and limit.
   *
   * @param query
   *          the {@link SQLQuery}.
   */
  protected void appendQueryRange(final SQLQuery<?> query) {
    if (reportSearchParam.offset != null) {
      query.offset(reportSearchParam.offset);
    }

    if (reportSearchParam.limit != null) {
      query.limit(reportSearchParam.limit);
    }
  }

  /**
   * Build count query.
   */
  public QuerydslCallable<Long> buildCountQuery() {
    if (reportSearchParam.issueKeys == null) {
      return new QuerydslCallable<Long>() {
        @Override
        public Long call(final Connection connection, final Configuration configuration)
            throws SQLException {
          return 0L;
        }
      };
    }
    return getCountQuery();
  }

  /**
   * Build grand total query.
   */
  public QuerydslCallable<Long> buildGrandTotalQuery() {
    return new QuerydslCallable<Long>() {
      @Override
      public Long call(final Connection connection, final Configuration configuration)
          throws SQLException {
        if (reportSearchParam.issueKeys == null) {
          return 0L;
        }
        NumberPath<Long> worklogTimeSumPath = Expressions.numberPath(Long.class,
            new PathMetadata(null, "worklogTimeSum", PathType.VARIABLE));

        SQLQuery<Long> fromQuery = new SQLQuery<Long>(connection, configuration)
            .select(qWorklog.timeworked.sum().as(worklogTimeSumPath));

        appendBaseFromAndJoin(fromQuery);
        appendBaseWhere(fromQuery);
        fromQuery.groupBy(qWorklog.id);

        SQLQuery<Long> query = new SQLQuery<Long>(connection, configuration)
            .select(worklogTimeSumPath.sum())
            .from(fromQuery.as("fromSum"));

        Long grandTotal = query.fetchOne();
        return grandTotal == null ? Long.valueOf(0L) : grandTotal;
      }
    };
  }

  /**
   * Build query.
   */
  public QuerydslCallable<List<T>> buildQuery() {
    if (reportSearchParam.issueKeys == null) {
      return new QuerydslCallable<List<T>>() {
        @Override
        public List<T> call(final Connection connection, final Configuration configuration)
            throws SQLException {
          return Collections.emptyList();
        }
      };
    }
    return getQuery();
  }

  private BooleanExpression filterToAffectedVersions(final QJiraissue qIssue,
      final BooleanExpression where) {
    QNodeassociation qNodeassociationAffectedVersion =
        new QNodeassociation("nodeassociationaffectedversion");
    QProjectversion qProjectversion = new QProjectversion("projectversion_affectedversion");

    BooleanExpression existsAffectedVersions = expressionFalse;
    boolean filterToAffectedVersions = false;
    if (!reportSearchParam.issueAffectedVersions.isEmpty()) {
      existsAffectedVersions = SQLExpressions.select(qNodeassociationAffectedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationAffectedVersion)
          .on(qNodeassociationAffectedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationAffectedVersion.associationType.eq(IssueRelationConstants.VERSION)
              .and(qNodeassociationAffectedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationAffectedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationAffectedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.vname.in(reportSearchParam.issueAffectedVersions)))
          .exists();
      filterToAffectedVersions = true;
    }

    BooleanExpression notExistsNoAffectedVersions = expressionFalse;
    if (reportSearchParam.selectNoAffectedVersionIssue) {
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
    if (!reportSearchParam.issueFixedVersions.isEmpty()) {
      existsVersions = SQLExpressions.select(qNodeassociationFixedVersion.sourceNodeId)
          .from(qProjectversion)
          .join(qNodeassociationFixedVersion)
          .on(qNodeassociationFixedVersion.sinkNodeId.eq(qProjectversion.id))
          .where(qNodeassociationFixedVersion.associationType.eq(IssueRelationConstants.FIX_VERSION)
              .and(qNodeassociationFixedVersion.sinkNodeEntity.eq(Entity.Name.VERSION))
              .and(qNodeassociationFixedVersion.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationFixedVersion.sourceNodeId.eq(qIssue.id))
              .and(qProjectversion.vname.in(reportSearchParam.issueFixedVersions)))
          .exists();
      filterToFixedVersions = true;
    }

    BooleanExpression notExistsNoVersions = expressionFalse;
    if (reportSearchParam.selectNoFixedVersionIssue) {
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
    if (reportSearchParam.selectReleasedFixVersion) {
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
    if (reportSearchParam.selectUnreleasedFixVersion) {
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
    QAppUser qAppUser = new QAppUser("qIssueAssigneAppUser");
    boolean filterToIssueAssignees = false;
    BooleanExpression assignedExpressions = expressionFalse;
    if (!reportSearchParam.issueAssignees.isEmpty()) {
      assignedExpressions = qIssue.assignee.in(
          SQLExpressions.select(qAppUser.userKey)
              .from(qAppUser)
              .where(qAppUser.lowerUserName.in(reportSearchParam.issueAssignees)));
      filterToIssueAssignees = true;
    }

    BooleanExpression unassignedExpressions = expressionFalse;
    if (reportSearchParam.selectUnassgined) {
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
    if (!reportSearchParam.issueComponents.isEmpty()) {
      existsComponents = SQLExpressions.select(qNodeassociationComponents.sourceNodeId)
          .from(qComponent)
          .join(qNodeassociationComponents)
          .on(qNodeassociationComponents.sinkNodeId.eq(qComponent.id))
          .where(qNodeassociationComponents.associationType.eq(IssueRelationConstants.COMPONENT)
              .and(qNodeassociationComponents.sinkNodeEntity.eq(Entity.Name.COMPONENT))
              .and(qNodeassociationComponents.sourceNodeEntity.eq(Entity.Name.ISSUE))
              .and(qNodeassociationComponents.sourceNodeId.eq(qIssue.id))
              .and(qComponent.cname.in(reportSearchParam.issueComponents)))
          .exists();
      filterToIssueComponent = true;
    }

    BooleanExpression notExistsNoComponents = expressionFalse;
    if (reportSearchParam.selectNoComponentIssue) {
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
    if (reportSearchParam.issueCreateDate != null) {
      Calendar issueCreateDateEndCalendar = Calendar.getInstance();
      issueCreateDateEndCalendar.setTime(reportSearchParam.issueCreateDate);
      issueCreateDateEndCalendar.add(Calendar.DAY_OF_MONTH, 1);
      Date issueCreateDateEndDate = issueCreateDateEndCalendar.getTime();
      return where
          .and(qIssue.created.between(
              new Timestamp(reportSearchParam.issueCreateDate.getTime()),
              new Timestamp(issueCreateDateEndDate.getTime())));
    }
    return where;
  }

  private BooleanExpression filterToIssueEpicIssueIds(final QJiraissue qIssue,
      final BooleanExpression where) {
    QIssuelinktype qIssuelinktype = new QIssuelinktype("epic_issuelinktype");
    QIssuelink qIssuelink = new QIssuelink("epic_issuelink");

    if (!reportSearchParam.issueEpicLinkIssueIds.isEmpty()) {
      return where.and(SQLExpressions.select(qIssuelink.id)
          .from(qIssuelink)
          .join(qIssuelinktype).on(qIssuelink.linktype.eq(qIssuelinktype.id))
          .where(qIssuelink.source.eq(qIssue.id)
              .and(qIssuelink.source.in(reportSearchParam.issueEpicLinkIssueIds))
              .and(qIssuelinktype.linkname.eq("Epic-Story Link")))
          .exists());
    }
    return where;
  }

  private BooleanExpression filterToIssueEpicName(final QJiraissue qIssue,
      final BooleanExpression where) {
    QCustomfieldvalue qCustomfieldValue = new QCustomfieldvalue("customfieldvalue");
    QCustomfield qCustomfield = new QCustomfield("customfield");

    if (reportSearchParam.issueEpicName != null) {
      return where.and(SQLExpressions.select(qCustomfield.id)
          .from(qCustomfield)
          .leftJoin(qCustomfieldValue).on(qCustomfield.id.eq(qCustomfieldValue.customfield))
          .where(qCustomfieldValue.issue.eq(qIssue.id)
              .and(qCustomfieldValue.stringvalue.toLowerCase()
                  .like(reportSearchParam.issueEpicName.toLowerCase(Locale.getDefault()) + "%"))
              .and(qCustomfield.cfname.eq("Epic Name")))
          .exists());
    }
    return where;
  }

  private BooleanExpression filterToIssueIds(final QJiraissue qIssue,
      final BooleanExpression where) {
    QJiraissue subQueryIssue = new QJiraissue("subQueryIssue");
    QProject subQueryProject = new QProject("subQueryProject");
    if (!reportSearchParam.issueKeys.isEmpty()) {
      BooleanExpression predicate = expressionFalse;
      for (String issueKey : reportSearchParam.issueKeys) {
        String[] split = issueKey.split("-");
        predicate = predicate.or(subQueryProject.pkey.eq(split[0])
            .and(subQueryIssue.issuenum.stringValue().eq(split[1])));
      }
      return where.and(qIssue.id.in(SQLExpressions.select(subQueryIssue.id)
          .from(subQueryIssue)
          .join(subQueryProject).on(subQueryIssue.project.eq(subQueryProject.id))
          .where(predicate)));
    }
    return where;
  }

  private BooleanExpression filterToIssueLabels(final QJiraissue qIssue,
      final BooleanExpression where) {
    QLabel qLabel = new QLabel("label");
    if (!reportSearchParam.labels.isEmpty()) {
      return where.and(SQLExpressions.select(qLabel.id)
          .from(qLabel)
          .where(qLabel.issue.eq(qIssue.id)
              .and(qLabel.label.in(reportSearchParam.labels)))
          .exists());
    }
    return where;
  }

  private BooleanExpression filterToIssuePriorityIds(final QPriority qPriority,
      final BooleanExpression where) {
    if (!reportSearchParam.issuePriorityIds.isEmpty()) {
      return where.and(qPriority.id.in(reportSearchParam.issuePriorityIds));
    }
    return where;
  }

  private BooleanExpression filterToIssueReporters(final QJiraissue qIssue,
      final BooleanExpression where) {
    QAppUser qAppUser = new QAppUser("qIssueReporterAppUser");
    if (!reportSearchParam.issueReporters.isEmpty()) {
      return where.and(qIssue.reporter.in(
          SQLExpressions.select(qAppUser.userKey)
              .from(qAppUser)
              .where(qAppUser.lowerUserName.in(reportSearchParam.issueReporters))));
    }
    return where;
  }

  private BooleanExpression filterToIssueResolution(final QResolution qResolution,
      final QJiraissue qIssue, final BooleanExpression where) {
    boolean filterToIssueResolution = false;

    BooleanExpression resolutionIssuesExpression = expressionFalse;
    if (!reportSearchParam.issueResolutionIds.isEmpty()) {
      resolutionIssuesExpression = qResolution.id.in(reportSearchParam.issueResolutionIds);
      filterToIssueResolution = true;
    }

    BooleanExpression unresolvedResolutionExpression = expressionFalse;
    if (reportSearchParam.selectUnresolvedResolution) {
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
    if (!reportSearchParam.issueStatusIds.isEmpty()) {
      return where.and(qIssuestatus.id.in(reportSearchParam.issueStatusIds));
    }
    return where;
  }

  private BooleanExpression filterToIssueTypeIds(final QIssuetype qIssuetype,
      final BooleanExpression where) {
    if (!reportSearchParam.issueTypeIds.isEmpty()) {
      return where.and(qIssuetype.id.in(reportSearchParam.issueTypeIds));
    }
    return where;
  }

  private BooleanExpression filterToProjectIds(final QProject qProject,
      final BooleanExpression where) {
    if (!reportSearchParam.projectIds.isEmpty()) {
      return where.and(qProject.id.in(reportSearchParam.projectIds));
    }
    return where;
  }

  private BooleanExpression filterToWorklogAuhtors(final QWorklog qWorklog,
      final BooleanExpression where) {
    QAppUser qAppUser = new QAppUser("qWorklogAuthor");
    if (!reportSearchParam.users.isEmpty()) {
      return where.and(qWorklog.author.in(
          SQLExpressions.select(qAppUser.userKey)
              .from(qAppUser)
              .where(qAppUser.lowerUserName.in(reportSearchParam.users))));
    }
    return where;
  }

  private BooleanExpression filterToWorklogEndDate(final QWorklog qWorklog,
      final BooleanExpression where) {
    if (reportSearchParam.worklogEndDate != null) {
      return where.and(qWorklog.startdate.lt(
          new Timestamp(reportSearchParam.worklogEndDate
              .getTime())));
    }
    return where;
  }

  private BooleanExpression filterToWorklogStartDate(final QWorklog qWorklog,
      final BooleanExpression where) {
    if (reportSearchParam.worklogStartDate != null) {
      return where.and(qWorklog.startdate.goe(
          new Timestamp(reportSearchParam.worklogStartDate
              .getTime())));
    }
    return where;
  }

  protected abstract QuerydslCallable<Long> getCountQuery();

  protected abstract QuerydslCallable<List<T>> getQuery();

}
