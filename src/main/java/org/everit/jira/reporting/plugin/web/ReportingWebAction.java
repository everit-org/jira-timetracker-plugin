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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.CreateReportEvent;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.exception.JTRPException;
import org.everit.jira.reporting.plugin.export.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.dto.ReportingSettingsValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.gson.Gson;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {

  private static final String HTTP_PARAM_COLLAPSED_DETAILS_MODULE = "collapsedDetailsModule";

  private static final String HTTP_PARAM_COLLAPSED_SUMMARY_MODULE = "collapsedSummaryModule";

  private static final String HTTP_PARAM_FILTER_CONDITION_JSON = "filterConditionJson";

  private static final String HTTP_PARAM_SELECTED_ACTIVE_TAB = "selectedActiveTab";

  private static final String HTTP_PARAM_SELECTED_MORE_JSON = "selectedMoreJson";

  private static final String HTTP_PARAM_SELECTED_WORKLOG_DETAILS_COLUMNS =
      "selectedWorklogDetailsColumns";

  /**
   * The Issue Collector jttp_build.porperties key.
   */
  private static final String ISSUE_COLLECTOR_SRC = "ISSUE_COLLECTOR_SRC";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private AnalyticsDTO analyticsDTO;

  private AnalyticsSender analyticsSender;

  private boolean collapsedDetailsModule = false;

  private boolean collapsedSummaryModule = false;

  private String contextPath;

  private Class<DateTimeConverterUtil> dateConverterUtil = DateTimeConverterUtil.class;

  private DurationFormatter durationFormatter = new DurationFormatter();

  private FilterCondition filterCondition;

  private String filterConditionJson = "";

  private Gson gson;

  private String issueCollectorSrc;

  private IssueSummaryReportDTO issueSummaryReport = new IssueSummaryReportDTO();

  /**
   * The message.
   */
  private String message = "";

  private List<String> notBrowsableProjectKeys = Collections.emptyList();

  private int pageSizeLimit;

  private ProjectSummaryReportDTO projectSummaryReport = new ProjectSummaryReportDTO();

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private String selectedActiveTab = "tabs-project";

  private List<String> selectedMore = Collections.emptyList();

  private List<String> selectedWorklogDetailsColumns = Collections.emptyList();

  private PluginSettingsFactory settingsFactory;

  private UserSummaryReportDTO userSummaryReport = new UserSummaryReportDTO();

  private List<String> worklogDetailsAllColumns = WorklogDetailsColumns.ALL_COLUMNS;

  private WorklogDetailsReportDTO worklogDetailsReport = new WorklogDetailsReportDTO();

  /**
   * Simple constructor.
   */
  public ReportingWebAction(final ReportingPlugin reportingPlugin,
      final PluginSettingsFactory settingsFactory, final AnalyticsSender analyticsSender) {
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(this.reportingPlugin);
    gson = new Gson();
    this.settingsFactory = settingsFactory;
    this.analyticsSender = analyticsSender;
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

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(settingsFactory,
        PiwikPropertiesUtil.PIWIK_REPORTING_SITEID);

    loadPageSizeLimit();

    loadIssueCollectorSrc();

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

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(settingsFactory,
        PiwikPropertiesUtil.PIWIK_REPORTING_SITEID);

    HttpServletRequest httpRequest = getHttpRequest();

    loadPageSizeLimit();

    loadIssueCollectorSrc();

    morePickerParse(httpRequest);

    normalizeContextPath();

    setParametersActiveTab(httpRequest);

    ConvertedSearchParam convertedSearchParam = null;
    filterConditionJson = httpRequest.getParameter(HTTP_PARAM_FILTER_CONDITION_JSON);
    String selectedWorklogDetailsColumnsJson =
        httpRequest.getParameter(HTTP_PARAM_SELECTED_WORKLOG_DETAILS_COLUMNS);
    String[] selectedWorklogDetailsColumnsArray =
        gson.fromJson(selectedWorklogDetailsColumnsJson, String[].class);
    selectedWorklogDetailsColumns = Arrays.asList(selectedWorklogDetailsColumnsArray);

    try {
      filterCondition = ConverterUtil.convertJsonToFilterCondition(filterConditionJson);
      convertedSearchParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    try {
      worklogDetailsReport =
          reportingPlugin.getWorklogDetailsReport(convertedSearchParam.reportSearchParam);

      projectSummaryReport =
          reportingPlugin.getProjectSummaryReport(convertedSearchParam.reportSearchParam);

      issueSummaryReport =
          reportingPlugin.getIssueSummaryReport(convertedSearchParam.reportSearchParam);

      userSummaryReport =
          reportingPlugin.getUserSummaryReport(convertedSearchParam.reportSearchParam);

      notBrowsableProjectKeys = convertedSearchParam.notBrowsableProjectKeys;
    } catch (JTRPException e) {
      message = e.getMessage();
      return INPUT;
    }

    String pluginUUID =
        JiraTimetrackerAnalytics.getPluginUUID(settingsFactory.createGlobalSettings());
    CreateReportEvent analyticsEvent = new CreateReportEvent(pluginUUID, filterCondition,
        selectedWorklogDetailsColumns, selectedActiveTab);
    analyticsSender.send(analyticsEvent);

    return SUCCESS;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
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

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public IssueSummaryReportDTO getIssueSummaryReport() {
    return issueSummaryReport;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getNotBrowsableProjectKeys() {
    return notBrowsableProjectKeys;
  }

  public int getPageSizeLimit() {
    return pageSizeLimit;
  }

  public ProjectSummaryReportDTO getProjectSummaryReport() {
    return projectSummaryReport;
  }

  public String getSelectedActiveTab() {
    return selectedActiveTab;
  }

  public List<String> getSelectedMore() {
    return selectedMore;
  }

  public List<String> getSelectedWorklogDetailsColumns() {
    return selectedWorklogDetailsColumns;
  }

  public UserSummaryReportDTO getUserSummaryReport() {
    return userSummaryReport;
  }

  public List<String> getWorklogDetailsAllColumns() {
    return worklogDetailsAllColumns;
  }

  public WorklogDetailsReportDTO getWorklogDetailsReport() {
    return worklogDetailsReport;
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

  public boolean isCollapsedDetailsModule() {
    return collapsedDetailsModule;
  }

  public boolean isCollapsedSummaryModule() {
    return collapsedSummaryModule;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(ISSUE_COLLECTOR_SRC);
  }

  private void loadPageSizeLimit() {
    ReportingSettingsValues loadReportingSettings = reportingPlugin.loadReportingSettings();
    pageSizeLimit = loadReportingSettings.pageSize;
  }

  private void morePickerParse(final HttpServletRequest httpRequest) {
    String selectedMoreJson = httpRequest.getParameter(HTTP_PARAM_SELECTED_MORE_JSON);
    if (selectedMoreJson != null) {
      String[] selectedMore = gson.fromJson(selectedMoreJson, String[].class);
      this.selectedMore = Arrays.asList(selectedMore);
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

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  private void setParametersActiveTab(final HttpServletRequest httpRequest) {
    String selectedActiveTab = httpRequest.getParameter(HTTP_PARAM_SELECTED_ACTIVE_TAB);
    if (selectedActiveTab != null) {
      this.selectedActiveTab = selectedActiveTab;
    }

    String collapsedDetailsModuleVal =
        httpRequest.getParameter(HTTP_PARAM_COLLAPSED_DETAILS_MODULE);
    collapsedDetailsModule = Boolean.parseBoolean(collapsedDetailsModuleVal);

    String collapsedSummaryModuleVal =
        httpRequest.getParameter(HTTP_PARAM_COLLAPSED_SUMMARY_MODULE);
    collapsedSummaryModule = Boolean.parseBoolean(collapsedSummaryModuleVal);
  }

  public void setSelectedMore(final List<String> selectedMore) {
    this.selectedMore = selectedMore;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
