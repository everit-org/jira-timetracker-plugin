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
import java.util.ArrayList;
import java.util.List;

import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
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
import org.everit.jira.timetracker.plugin.dto.ReportingSettingsValues;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The implementation of the {@link ReportingPlugin}.
 */
public class ReportingPluginImpl implements ReportingPlugin, InitializingBean,
    DisposableBean, Serializable {

  private static final int DEFAULT_PAGE_SIZE = 20;

  /**
   * The plugin reporting settings is use Noworks.
   */
  private static final String JTTP_PLUGIN_REPORTING_SETTINGS_GROUPS = "reportingGroups";

  /**
   * The plugin repoting settings key prefix.
   */
  private static final String JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX = "jttp_report";

  /**
   * The plugin reporting settings is use Noworks.
   */
  private static final String JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE = "pagerSize";

  /**
   * Serial Version UID.
   */
  private static final long serialVersionUID = -3872710932298672883L;

  private int pageSize;

  private QuerydslSupport querydslSupport;

  private List<String> reportingGroups;

  /**
   * The plugin reporting setting form the settingsFactory.
   */
  private PluginSettings reportingSettings;

  /**
   * The plugin reporting setting values.
   */
  private ReportingSettingsValues reportingSettingsValues;

  /**
   * The PluginSettingsFactory.
   */
  private final PluginSettingsFactory settingsFactory;

  /**
   * Default constructor.
   */
  public ReportingPluginImpl(final PluginSettingsFactory settingsFactory) {
    this.settingsFactory = settingsFactory;
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
      final ReportSearchParam reportSearchParam) {
    WorklogDetailsReportQueryBuilder worklogDetailsReportQueryBuilder =
        new WorklogDetailsReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<WorklogDetailsDTO>> worklogDetailsQuery = worklogDetailsReportQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> worklogDetailsCountQuery =
        worklogDetailsReportQueryBuilder.buildCountQuery();

    QuerydslCallable<Long> grandTotalQuery =
        worklogDetailsReportQueryBuilder.buildGrandTotalQuery();

    List<WorklogDetailsDTO> worklogDetails = querydslSupport.execute(worklogDetailsQuery);

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

  @Override
  public ReportingSettingsValues loadReportingSettings() {
    reportingSettings = settingsFactory
        .createSettingsForKey(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX);
    setReportingGroups();
    setPageSize();
    reportingSettingsValues =
        new ReportingSettingsValues().reportingGroups(reportingGroups).pageSize(pageSize);
    return reportingSettingsValues;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  @Override
  public void saveReportingSettings(final ReportingSettingsValues reportingSettingsParameter) {
    reportingSettings = settingsFactory
        .createSettingsForKey(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX);
    reportingSettings.put(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
        + JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE,
        String.valueOf(reportingSettingsParameter.pageSize));
    reportingSettings.put(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
        + JTTP_PLUGIN_REPORTING_SETTINGS_GROUPS, reportingSettingsParameter.reportingGroups);
  }

  private void setPageSize() {
    pageSize = DEFAULT_PAGE_SIZE;
    String pageSizeValue =
        (String) reportingSettings.get(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
            + JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE);
    if (pageSizeValue != null) {
      pageSize = Integer.parseInt(pageSizeValue);
    }
  }

  private void setReportingGroups() {
    List<String> reportingGroupsNames = (List<String>) reportingSettings
        .get(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX + JTTP_PLUGIN_REPORTING_SETTINGS_GROUPS);
    reportingGroups = new ArrayList<String>();
    if (reportingGroupsNames != null) {
      reportingGroups = reportingGroupsNames;
    }
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
