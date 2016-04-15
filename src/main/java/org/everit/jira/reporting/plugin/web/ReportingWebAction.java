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

import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.gson.Gson;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {

  private static final String HTTP_PARAM_FILTER_CONDITION_JSON = "filterConditionJson";

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

  private IssueSummaryReportDTO issueSummaryReport = new IssueSummaryReportDTO();

  /**
   * The message.
   */
  private String message = "";

  private ProjectSummaryReportDTO projectSummaryReport = new ProjectSummaryReportDTO();

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private List<String> selectedMore;

  private List<String> selectedWorklogDetailsColumns = new ArrayList<String>();

  private UserSummaryReportDTO userSummaryReport = new UserSummaryReportDTO();

  private WorklogDetailsReportDTO worklogDetailsReport = new WorklogDetailsReportDTO();

  /**
   * Simple constructor.
   */
  public ReportingWebAction(final ReportingPlugin reportingPlugin) {
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(this.reportingPlugin);
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

    createDurationFormatter();

    morePickerParse();
    normalizeContextPath();
    ConvertedSearchParam convertedSearchParam = null;
    filterConditionJson = getHttpRequest().getParameter(HTTP_PARAM_FILTER_CONDITION_JSON);
    String selectedWorklogDetailsColumnsJson =
        getHttpRequest().getParameter("selectedWorklogDetailsColumns");
    String[] selectedWorklogDetailsColumnsArray = new Gson()
        .fromJson(selectedWorklogDetailsColumnsJson, String[].class);
    selectedWorklogDetailsColumns = Arrays.asList(selectedWorklogDetailsColumnsArray);

    try {
      filterCondition = ConverterUtil.convertJsonToFilterCondition(filterConditionJson);
      convertedSearchParam = ConverterUtil
          .convertFilterConditionToConvertedSearchParam(filterCondition);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    worklogDetailsReport =
        reportingPlugin.getWorklogDetailsReport(convertedSearchParam.reportSearchParam);

    projectSummaryReport =
        reportingPlugin.getProjectSummaryReport(convertedSearchParam.reportSearchParam);

    issueSummaryReport =
        reportingPlugin.getIssueSummaryReport(convertedSearchParam.reportSearchParam);

    userSummaryReport =
        reportingPlugin.getUserSummaryReport(convertedSearchParam.reportSearchParam);

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

  public IssueSummaryReportDTO getIssueSummaryReport() {
    return issueSummaryReport;
  }

  public String getMessage() {
    return message;
  }

  public ProjectSummaryReportDTO getProjectSummaryReport() {
    return projectSummaryReport;
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
