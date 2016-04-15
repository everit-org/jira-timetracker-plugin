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
package org.everit.jira.reporting.plugin.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.query.IssueSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.ProjectSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.UserSummaryReportQueryBuilder;
import org.everit.jira.reporting.plugin.query.WorklogDetailsReportQueryBuilder;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {

  private static final String HTTP_PARAM_FILTER_CONDITION_JSON = "filterConditionJson";

  /**
   * The Issue Collector jttp_build.porperties key.
   */
  private static final String ISSUE_COLLECTOR_SRC = "ISSUE_COLLECTOR_SRC";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The first day of the week.
   */
  private String contextPath;

  private Class<DateTimeConverterUtil> dateConverterUtil = DateTimeConverterUtil.class;

  private DurationFormatter durationFormatter;

  private FilterCondition filterCondition;

  private String filterConditionJson = "";

  private Long grandTotal;

  private String issueCollectorSrc;

  private List<IssueSummaryDTO> issueSummaries;

  private Long issueSummaryCount;

  /**
   * The message.
   */
  private String message = "";

  private List<ProjectSummaryDTO> projectSummaries;

  private Long projectSummaryCount;

  private QuerydslSupport querydslSupport;

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private List<String> selectedMore;

  private List<UserSummaryDTO> userSummaries;

  private Long userSummaryCount;

  private List<WorklogDetailsDTO> worklogDetails;

  private Long worklogDetailsCount;

  /**
   * Simple constructor.
   */
  public ReportingWebAction(final ReportingPlugin reportingPlugin) {
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(this.reportingPlugin);
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      // FIXME zs.cz what happens?
      throw new RuntimeException(e);
    }
  }

  private void createDurationFormatter() {
    durationFormatter = new DurationFormatter();
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    loadIssueCollectorSrc();
    createDurationFormatter();

    selectedMore = new ArrayList<String>();
    filterCondition = new FilterCondition();
    initDatesIfNecessary();

    normalizeContextPath();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    loadIssueCollectorSrc();
    createDurationFormatter();

    morePickerParse();
    normalizeContextPath();
    ConvertedSearchParam convertedSearchParam = null;
    filterConditionJson = getHttpRequest().getParameter(HTTP_PARAM_FILTER_CONDITION_JSON);
    try {
      filterCondition = ConverterUtil.convertJsonToFilterCondition(filterConditionJson);
      convertedSearchParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    reportWorklogDetails(convertedSearchParam.reportSearchParam);

    reportProjectSummary(convertedSearchParam.reportSearchParam);

    reportIssueSummary(convertedSearchParam.reportSearchParam);

    reportUserSummary(convertedSearchParam.reportSearchParam);

    return SUCCESS;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Class<DateTimeConverterUtil> getDateConverterUtil() {
    return dateConverterUtil;
  }

  public DurationFormatter getDurationFormatter() {
    return durationFormatter;
  }

  public FilterCondition getFilterCondition() {
    return filterCondition;
  }

  public String getFilterConditionJson() {
    return filterConditionJson;
  }

  public Long getGrandTotal() {
    return grandTotal;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public List<IssueSummaryDTO> getIssueSummaries() {
    return issueSummaries;
  }

  public Long getIssueSummaryCount() {
    return issueSummaryCount;
  }

  public String getMessage() {
    return message;
  }

  public List<ProjectSummaryDTO> getProjectSummaries() {
    return projectSummaries;
  }

  public Long getProjectSummaryCount() {
    return projectSummaryCount;
  }

  public List<String> getSelectedMore() {
    return selectedMore;
  }

  public List<UserSummaryDTO> getUserSummaries() {
    return userSummaries;
  }

  public Long getUserSummaryCount() {
    return userSummaryCount;
  }

  public List<WorklogDetailsDTO> getWorklogDetails() {
    return worklogDetails;
  }

  public Long getWorklogDetailsCount() {
    return worklogDetailsCount;
  }

  private void initDatesIfNecessary() {
    if ("".equals(filterCondition.getWorklogStartDate())) {
      Calendar calendarFrom = Calendar.getInstance();
      calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
      Date dateFrom = calendarFrom.getTime();
      filterCondition.setWorklogStartDate(DateTimeConverterUtil.dateToString(dateFrom));
    }
    if ("".equals(filterCondition.getWorklogEndDate())) {
      Calendar calendarTo = Calendar.getInstance();
      Date dateTo = calendarTo.getTime();
      filterCondition.setWorklogEndDate(DateTimeConverterUtil.dateToString(dateTo));
    }
  }

  private void loadIssueCollectorSrc() {
    Properties properties;
    try {
      properties = PropertiesUtil.getJttpBuildProperties();
      issueCollectorSrc = properties.getProperty(ISSUE_COLLECTOR_SRC);
    } catch (IOException e) {
      // TODO add logger?
      issueCollectorSrc = "";
    }
  }

  private void morePickerParse() {
    String[] selectedMoreValues = getHttpRequest().getParameterValues("morePicker");
    if (selectedMoreValues != null) {
      selectedMore = Arrays.asList(selectedMoreValues);
    } else {
      selectedMore = new ArrayList<String>();
    }
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void reportIssueSummary(final ReportSearchParam reportSearchParam) {
    IssueSummaryReportQueryBuilder issueSummaryQueryBuilder =
        new IssueSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<IssueSummaryDTO>> issueSummaryQuery = issueSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> issueSummaryCountQuery = issueSummaryQueryBuilder.buildCountQuery();

    issueSummaries = querydslSupport.execute(issueSummaryQuery);

    issueSummaryCount = querydslSupport.execute(issueSummaryCountQuery);
  }

  private void reportProjectSummary(final ReportSearchParam reportSearchParam) {
    ProjectSummaryReportQueryBuilder projectSummaryQueryBuilder =
        new ProjectSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<ProjectSummaryDTO>> projectSummaryQuery = projectSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> projectSummaryCountQuery = projectSummaryQueryBuilder.buildCountQuery();

    projectSummaries = querydslSupport.execute(projectSummaryQuery);

    projectSummaryCount = querydslSupport.execute(projectSummaryCountQuery);
  }

  private void reportUserSummary(final ReportSearchParam reportSearchParam) {
    UserSummaryReportQueryBuilder userSummaryQueryBuilder =
        new UserSummaryReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<UserSummaryDTO>> userSummaryQuery = userSummaryQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> userSummaryCountQuery = userSummaryQueryBuilder.buildCountQuery();

    userSummaries = querydslSupport.execute(userSummaryQuery);

    userSummaryCount = querydslSupport.execute(userSummaryCountQuery);
  }

  private void reportWorklogDetails(final ReportSearchParam reportSearchParam) {
    WorklogDetailsReportQueryBuilder worklogDetailsReportQueryBuilder =
        new WorklogDetailsReportQueryBuilder(reportSearchParam);

    QuerydslCallable<List<WorklogDetailsDTO>> worklogDetailsQuery = worklogDetailsReportQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> worklogDetailsCountQuery =
        worklogDetailsReportQueryBuilder.buildCountQuery();

    QuerydslCallable<Long> grandTotalQuery =
        worklogDetailsReportQueryBuilder.buildGrandTotalQuery();

    worklogDetails = querydslSupport.execute(worklogDetailsQuery);

    worklogDetailsCount = querydslSupport.execute(worklogDetailsCountQuery);

    grandTotal = querydslSupport.execute(grandTotalQuery);
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setSelectedMore(final List<String> selectedMore) {
    this.selectedMore = selectedMore;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
