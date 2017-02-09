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
package org.everit.jira.reporting.plugin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.PagingDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.query.IssueSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.ProjectSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.UserSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.WorklogDetailsReportQueryBuilder;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * The implementation of the {@link ReportingPlugin}.
 */
public class ReportingPluginImpl implements ReportingPlugin, InitializingBean,
    DisposableBean, Serializable {

  /**
   * The plugin reporting settings groups that have browse user permission.
   */

  /**
   * Serial Version UID.
   */
  private static final long serialVersionUID = -3872710932298672883L;

  private JiraAuthenticationContext authenticationContext;

  private IssueManager issueManager;

  private PermissionManager permissionManager;

  private QuerydslSupport querydslSupport;

  /**
   * Default constructor.
   */
  public ReportingPluginImpl() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    permissionManager = ComponentAccessor.getPermissionManager();
    authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    issueManager = ComponentAccessor.getIssueManager();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  private PagingDTO createPagingDTO(final Long offset, final Long limit, final Long count) {
    Integer maxPageNumber = null;
    Integer actPageNumber = 1;
    Long start = count == 0 ? 0L : 1L;
    Long end = count;

    if ((limit != null) && (limit != 0)) {
      maxPageNumber = (int) Math.ceil(count / limit.doubleValue());
      // if no result show first page.
      maxPageNumber = maxPageNumber == 0 ? Integer.valueOf(1) : maxPageNumber;

      if ((offset != null) && (offset != 0)) {
        actPageNumber = (int) Math.ceil(offset / limit.doubleValue()) + 1;
        start = offset + 1;
      }
      end = (start + limit) - 1;
    }
    end = count < end ? count : end;

    return new PagingDTO()
        .actPageNumber(actPageNumber)
        .end(end)
        .start(start)
        .maxPageNumber(maxPageNumber);
  }

  @Override
  public void destroy() throws Exception {
  }

  @Override
  public IssueSummaryReportDTO getIssueSummaryReport(final ReportSearchParam reportSearchParam) {
    IssueSummaryReportQueryBuilder issueSummaryQueryBuilder =
        new IssueSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<IssueSummaryDTO>> issueSummaryQuery = issueSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> issueSummaryCountQuery = issueSummaryQueryBuilder.buildCountQuery();

    List<IssueSummaryDTO> issueSummaries = querydslSupport.execute(issueSummaryQuery);

    Long issueSummaryCount = querydslSupport.execute(issueSummaryCountQuery);

    PagingDTO paging = createPagingDTO(reportSearchParam.offset,
        reportSearchParam.limit,
        issueSummaryCount);

    return new IssueSummaryReportDTO()
        .issueSummaries(issueSummaries)
        .issueSummaryCount(issueSummaryCount)
        .paging(paging);
  }

  @Override
  public ProjectSummaryReportDTO getProjectSummaryReport(
      final ReportSearchParam reportSearchParam) {
    ProjectSummaryReportQueryBuilder projectSummaryQueryBuilder =
        new ProjectSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<ProjectSummaryDTO>> projectSummaryQuery = projectSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> projectSummaryCountQuery = projectSummaryQueryBuilder.buildCountQuery();

    List<ProjectSummaryDTO> projectSummaries = querydslSupport.execute(projectSummaryQuery);

    Long projectSummaryCount = querydslSupport.execute(projectSummaryCountQuery);

    PagingDTO paging = createPagingDTO(reportSearchParam.offset,
        reportSearchParam.limit,
        projectSummaryCount);

    return new ProjectSummaryReportDTO()
        .paging(paging)
        .projectSummaries(projectSummaries)
        .projectSummaryCount(projectSummaryCount);
  }

  @Override
  public UserSummaryReportDTO getUserSummaryReport(final ReportSearchParam reportSearchParam) {
    UserSummaryReportQueryBuilder userSummaryQueryBuilder =
        new UserSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<UserSummaryDTO>> userSummaryQuery = userSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> userSummaryCountQuery = userSummaryQueryBuilder.buildCountQuery();

    List<UserSummaryDTO> userSummaries = querydslSupport.execute(userSummaryQuery);

    Long userSummaryCount = querydslSupport.execute(userSummaryCountQuery);

    PagingDTO paging = createPagingDTO(reportSearchParam.offset,
        reportSearchParam.limit,
        userSummaryCount);

    return new UserSummaryReportDTO()
        .paging(paging)
        .userSummaries(userSummaries)
        .userSummaryCount(userSummaryCount);
  }

  @Override
  public WorklogDetailsReportDTO getWorklogDetailsReport(
      final ReportSearchParam reportSearchParam, final OrderBy orderBy) {
    WorklogDetailsReportQueryBuilder worklogDetailsReportQueryBuilder =
        new WorklogDetailsReportQueryBuilder(reportSearchParam, orderBy);

    QuerydslCallable<List<WorklogDetailsDTO>> worklogDetailsQuery = worklogDetailsReportQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> worklogDetailsCountQuery =
        worklogDetailsReportQueryBuilder.buildCountQuery();

    QuerydslCallable<Long> grandTotalQuery =
        worklogDetailsReportQueryBuilder.buildGrandTotalQuery();

    List<WorklogDetailsDTO> worklogDetails = querydslSupport.execute(worklogDetailsQuery);
    for (Iterator<WorklogDetailsDTO> iterator = worklogDetails.iterator(); iterator.hasNext();) {
      WorklogDetailsDTO worklogDetail = iterator.next();
      ApplicationUser user = authenticationContext.getUser();
      MutableIssue issue = issueManager.getIssueObject(worklogDetail.getIssueKey());
      if (!permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue, user)) {
        iterator.remove();
        continue;
      }
      worklogDetail.setIssueCreated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getIssueCreated()));
      worklogDetail.setIssueUpdated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getIssueUpdated()));
      worklogDetail.setWorklogCreated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getWorklogCreated()));
      worklogDetail.setWorklogStartDate(
          DateTimeConverterUtil
              .addTimeZoneToTimestamp(worklogDetail.getWorklogStartDate()));
      worklogDetail.setWorklogUpdated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getWorklogUpdated()));
    }

    Long worklogDetailsCount = querydslSupport.execute(worklogDetailsCountQuery);

    Long grandTotal = querydslSupport.execute(grandTotalQuery);

    PagingDTO paging = createPagingDTO(reportSearchParam.offset,
        reportSearchParam.limit,
        worklogDetailsCount);

    return new WorklogDetailsReportDTO()
        .worklogDetails(worklogDetails)
        .worklogDetailsCount(worklogDetailsCount)
        .grandTotal(grandTotal)
        .paging(paging);
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
