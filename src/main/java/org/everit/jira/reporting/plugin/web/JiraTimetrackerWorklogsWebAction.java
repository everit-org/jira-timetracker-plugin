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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.SupportManager;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.dto.MissingsPageingDTO;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.ExceptionUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.joda.time.DateTime;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Missing worklogs page.
 */
public class JiraTimetrackerWorklogsWebAction extends JiraWebActionSupport {

  /**
   * HTTP parameters.
   */
  public static final class Parameter {

    public static final String ACTUAL_PAGE = "actualPage";

    public static final String DATE_FROM_FORMATED = "dateFromFormated";

    public static final String DATE_TO_FORMATED = "dateToFormated";

    public static final String DATEFROM = "dateFromMil";

    public static final String DATETO = "dateToMil";

    public static final String HOUR = "hour";

    public static final String NONWORKING = "nonworking";

    public static final String PAGE_BACK = "pageBack";

    public static final String PAGE_NEXT = "pageNext";

    public static final String PAGING = "paging";

  }

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String PLUGIN_WRONG_DATES = "plugin.wrong.dates";
  }

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWorklogsWebAction.class);

  /**
   * The number of rows in the dates table.
   */
  private static final int ROW_COUNT = 20;

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The actual page.
   */
  private int actualPage;

  private List<MissingsWorklogsDTO> allDatesWhereNoWorklog = new ArrayList<MissingsWorklogsDTO>();

  private AnalyticsDTO analyticsDTO;

  /**
   * The report check the worklogs time spent is equal or greater than 8 hours.
   */
  public boolean checkHours = false;

  /**
   * If check the worklogs spent time, then exclude the non working issues, or not.
   */
  public boolean checkNonWorkingIssues = false;

  private String contextPath;

  private DateTimeServer dateFrom;

  /**
   * The formated date.
   */
  private Long dateFromFormated;

  private DateTimeServer dateTo;

  /**
   * The formated date.
   */
  private Long dateToFormated;

  private String issueCollectorSrc;

  /**
   * The message.
   */
  private String message = "";

  /**
   * The message parameter.
   */
  private String messageParameter = "";

  /**
   * The number of pages.
   */
  private int numberOfPages;

  private MissingsPageingDTO paging = new MissingsPageingDTO();

  private PluginCondition pluginCondition;

  private ReportingCondition reportingCondition;

  private TimeTrackerSettingsHelper settingsHelper;

  private List<MissingsWorklogsDTO> showDatesWhereNoWorklog = new ArrayList<MissingsWorklogsDTO>();

  private String stacktrace = "";

  private SupportManager supportManager;

  /**
   * Simple constructor.
   */
  public JiraTimetrackerWorklogsWebAction(
      final SupportManager supportManager,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.supportManager = supportManager;
    this.settingsHelper = settingsHelper;
    reportingCondition = new ReportingCondition(settingsHelper);
    pluginCondition = new PluginCondition(settingsHelper);
  }

  private void afterAction() {
    numberOfPages = countNumberOfPages();
    pageChangeAction();
    setShowDatesListByActualPage(actualPage);
  }

  private void beforeAction() {
    normalizeContextPath();
    loadIssueCollectorSrc();

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID, settingsHelper);

    parseDateParams();
  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    return null;
  }

  /**
   * Count how much page need to show the dates.
   *
   * @return Number of pages.
   */
  private int countNumberOfPages() {
    int numberOfPages = 0;
    numberOfPages = allDatesWhereNoWorklog.size() / ROW_COUNT;
    if ((allDatesWhereNoWorklog.size() % ROW_COUNT) != 0) {
      numberOfPages++;
    }
    return numberOfPages;
  }

  /**
   * Set dateFrom and dateFromFormated default value.
   */
  private void dateFromDefaultInit() {
    DateTime date = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
    date = date.minusMonths(1);
    dateFromFormated = DateTimeConverterUtil.convertDateTimeToDate(date).getTime();
  }

  /**
   * Set dateTo and dateToFormated default values.
   */
  private void dateToDefaultInit() {
    DateTime date = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
    dateToFormated = DateTimeConverterUtil.convertDateTimeToDate(date).getTime();
  }

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    beforeAction();

    TimeTrackerGlobalSettings globalSettings = settingsHelper.loadGlobalSettings();
    try {
      // TODO not simple "" for selectedUser. Use user picker
      // Default check box parameter false, false
      allDatesWhereNoWorklog = supportManager
          .getDates(dateFrom, dateTo, checkHours,
              checkNonWorkingIssues, globalSettings);
    } catch (GenericEntityException e) {
      LOGGER.error("Error when try to run the query.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    afterAction();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    beforeAction();

    String searchActionResult = parseParams();
    if (searchActionResult != null) {
      return searchActionResult;
    }

    TimeTrackerGlobalSettings globalSettings = settingsHelper.loadGlobalSettings();
    try {
      // TODO not simple "" for selectedUser. Use user picker
      allDatesWhereNoWorklog = supportManager
          .getDates(dateFrom, dateTo, checkHours,
              checkNonWorkingIssues, globalSettings);
    } catch (GenericEntityException e) {
      LOGGER.error("Error when try to run the query.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    afterAction();

    return SUCCESS;
  }

  public int getActualPage() {
    return actualPage;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public boolean getCheckHours() {
    return checkHours;
  }

  public boolean getCheckNonWorkingIssues() {
    return checkNonWorkingIssues;
  }

  public String getContextPath() {
    return contextPath;
  }

  public DateTimeFormatter getDateFormatInUserTimeZOne() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(TimetrackerUtil.getLoggedUserTimeZone().toTimeZone());
  }

  public Long getDateFromFormated() {
    return dateFromFormated;
  }

  public List<MissingsWorklogsDTO> getDateswhereNoWorklog() {
    return allDatesWhereNoWorklog;
  }

  @Override
  public DateTimeFormatter getDateTimeFormatter() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE).withSystemZone();
  }

  public Long getDateToFormated() {
    return dateToFormated;
  }

  public String getEndDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(TimetrackerUtil.getLoggedUserTimeZone().toTimeZone())
        .format(new Date(dateToFormated));
  }

  public String getFromDateInJSDatePickerFormat() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE_PICKER)
        .withZone(TimetrackerUtil.getLoggedUserTimeZone().toTimeZone())
        .format(new Date(dateFromFormated));
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageParameter() {
    return messageParameter;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public MissingsPageingDTO getPaging() {
    return paging;
  }

  public List<MissingsWorklogsDTO> getShowDatesWhereNoWorklog() {
    return showDatesWhereNoWorklog;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  /**
   * Handle the page changer action.
   */
  public void pageChangeAction() {
    actualPage = 1;

    String dayBackValue = getHttpRequest().getParameter(Parameter.PAGE_BACK);
    String dayNextValue = getHttpRequest().getParameter(Parameter.PAGE_NEXT);
    String paging = getHttpRequest().getParameter(Parameter.PAGING);
    if ((dayBackValue != null) && (actualPage > 1)) {
      actualPage--;
    }
    if ((dayNextValue != null) && (actualPage < numberOfPages)) {
      actualPage++;
    }
    if (paging != null) {
      actualPage = Integer.parseInt(paging);
    }
  }

  private void parseCheckboxParam() {
    String hourValue = getHttpRequest().getParameter(Parameter.HOUR);
    String nonworkingValue = getHttpRequest().getParameter(Parameter.NONWORKING);
    if (hourValue != null) {
      checkHours = true;
    }
    if (nonworkingValue != null) {
      checkNonWorkingIssues = true;
    }
  }

  private void parseDateParams() {
    String requestDateFrom = getHttpRequest().getParameter(Parameter.DATEFROM);
    if (requestDateFrom != null) {
      dateFromFormated = Long.valueOf(requestDateFrom);
    } else if (dateFromFormated == null) {
      // TODO check this if else
      dateFromDefaultInit();
    }
    dateFrom = DateTimeServer.getInstanceBasedOnUserTimeZone(dateFromFormated);

    String requestDateTo = getHttpRequest().getParameter(Parameter.DATETO);
    if (requestDateTo != null) {
      dateToFormated = Long.valueOf(requestDateTo);
    } else if (dateToFormated == null) {
      // TODO check this if else
      dateToDefaultInit();
    }
    dateTo = DateTimeServer.getInstanceBasedOnUserTimeZone(dateToFormated);
  }

  private void parsePagingParams() {
    String requestDateFromFormated = getHttpRequest().getParameter(Parameter.DATE_FROM_FORMATED);
    if (requestDateFromFormated != null) {
      dateFromFormated = Long.valueOf(requestDateFromFormated);
    }
    String requestDateToFormated = getHttpRequest().getParameter(Parameter.DATE_TO_FORMATED);
    if (requestDateToFormated != null) {
      dateToFormated = Long.valueOf(requestDateToFormated);
    }
    actualPage = Integer.parseInt(getHttpRequest().getParameter(Parameter.ACTUAL_PAGE));
  }

  private String parseParams() throws ParseException {
    String searchValue = getHttpRequest().getParameter("search");
    // set actual page default! we start the new query with the first page
    actualPage = 1;
    if (searchValue != null) {
      if (dateFrom.getUserTimeZone().compareTo(dateTo.getUserTimeZone()) >= 0) {
        message = PropertiesKey.PLUGIN_WRONG_DATES;
        return INPUT;
      }
    } else {
      parsePagingParams();
      dateFrom = DateTimeServer.getInstanceBasedOnUserTimeZone(dateFromFormated);
      dateTo = DateTimeServer.getInstanceBasedOnUserTimeZone(dateToFormated);
    }
    parseCheckboxParam();
    return null;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Decide render or not the update information bar.
   *
   * @return true if bar should be render
   */
  public boolean renderUpdateNotifier() {
    return new UpdateNotifier(settingsHelper).isShowUpdater();
  }

  /**
   * Set the showDatesWhereNoWorklog by the actual page.
   *
   * @param actualPageParam
   *          The sub list of allDatesWhereNoWorklog.
   */
  private void setShowDatesListByActualPage(final int actualPageParam) {
    if (allDatesWhereNoWorklog.size() > 0) { // if the result is 0, use dafault paging
      int from = (actualPageParam - 1) * ROW_COUNT;
      int to = actualPageParam * ROW_COUNT;
      if ((actualPageParam == 1) && (allDatesWhereNoWorklog.size() < ROW_COUNT)) {
        to = allDatesWhereNoWorklog.size();
      }
      if ((actualPageParam == numberOfPages)
          && ((allDatesWhereNoWorklog.size() % ROW_COUNT) != 0)) {
        to = from + (allDatesWhereNoWorklog.size() % ROW_COUNT);
      }
      paging = paging.start(from + 1).end(to).resultSize(allDatesWhereNoWorklog.size())
          .actPageNumber(actualPageParam).maxPageNumber(numberOfPages);
      showDatesWhereNoWorklog = allDatesWhereNoWorklog.subList(from, to);
    }
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
