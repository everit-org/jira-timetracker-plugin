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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.MissingsPageingDTO;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Missing worklogs page.
 */
public class JiraTimetrackerWorklogsWebAction extends JiraWebActionSupport {

  /**
   * The Issue Collector jttp_build.porperties key.
   */
  private static final String ISSUE_COLLECTOR_SRC = "ISSUE_COLLECTOR_SRC";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWorklogsWebAction.class);

  private static final String PARAM_DATEFROM = "dateFrom";

  private static final String PARAM_DATETO = "dateTo";

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

  private List<MissingsWorklogsDTO> allDatesWhereNoWorklog;

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

  /**
   * The date.
   */
  private Date dateFrom = null;

  /**
   * The formated date.
   */
  private String dateFromFormated = "";

  /**
   * The date.
   */
  private Date dateTo = null;

  /**
   * The formated date.
   */
  private String dateToFormated = "";

  private String issueCollectorSrc;

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

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

  private final PluginSettingsFactory pluginSettingsFactory;

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private List<MissingsWorklogsDTO> showDatesWhereNoWorklog;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param pluginSettingsFactory
   *          the {@link PluginSettingsFactory}.
   */
  public JiraTimetrackerWorklogsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final ReportingPlugin reportingPlugin,
      final PluginSettingsFactory pluginSettingsFactory) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(this.reportingPlugin);
    this.pluginSettingsFactory = pluginSettingsFactory;
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
    Calendar calendarFrom = Calendar.getInstance();
    calendarFrom.set(Calendar.MONTH, calendarFrom.get(Calendar.MONTH) - 1);
    dateFrom = calendarFrom.getTime();
    dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
  }

  /**
   * Set dateTo and dateToFormated default values.
   */
  private void dateToDefaultInit() {
    Calendar calendarTo = Calendar.getInstance();
    dateTo = calendarTo.getTime();
    dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
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

    normalizeContextPath();
    loadIssueCollectorSrc();

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID);

    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }
    dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
    try {
      // TODO not simple "" for selectedUser. Use user picker
      // Default check box parameter false, false
      allDatesWhereNoWorklog = jiraTimetrackerPlugin
          .getDates("", dateFrom, dateTo, checkHours,
              checkNonWorkingIssues);
    } catch (GenericEntityException e) {
      LOGGER.error("Error when try to run the query.", e);
      return ERROR;
    }
    numberOfPages = countNumberOfPages();
    actualPage = 1;
    setShowDatesListByActualPage(actualPage);
    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    // set variables default value back
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!reportingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    normalizeContextPath();
    loadIssueCollectorSrc();

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID);

    initVariables();
    parseConstantParams();
    String searchActionResult = searchAction();
    if (searchActionResult != null) {
      return searchActionResult;
    }
    try {
      // TODO not simple "" for selectedUser. Use user picker
      allDatesWhereNoWorklog = jiraTimetrackerPlugin
          .getDates("", dateFrom, dateTo, checkHours,
              checkNonWorkingIssues);
    } catch (GenericEntityException e) {
      LOGGER.error("Error when try to run the query.", e);
      return ERROR;
    }
    // check the page changer buttons
    numberOfPages = countNumberOfPages();
    pageChangeAction();
    setShowDatesListByActualPage(actualPage);

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

  public String getDateFromFormated() {
    return dateFromFormated;
  }

  public List<MissingsWorklogsDTO> getDateswhereNoWorklog() {
    return allDatesWhereNoWorklog;
  }

  public String getDateToFormated() {
    return dateToFormated;
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

  private void initVariables() {
    message = "";
    messageParameter = "";
    allDatesWhereNoWorklog = new ArrayList<MissingsWorklogsDTO>();
    showDatesWhereNoWorklog = new ArrayList<MissingsWorklogsDTO>();
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(ISSUE_COLLECTOR_SRC);
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
    String dayBackValue = getHttpRequest().getParameter("pageBack");
    String dayNextValue = getHttpRequest().getParameter("pageNext");
    String paging = getHttpRequest().getParameter("paging");
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

  private void parseConstantParams() {
    dateFromFormated = getHttpRequest().getParameter("dateFromFormated");
    dateToFormated = getHttpRequest().getParameter("dateToFormated");
    actualPage = Integer.parseInt(getHttpRequest().getParameter("actualPage"));
  }

  private boolean parseDateParams() {

    String requestDateFrom = getHttpRequest().getParameter(PARAM_DATEFROM);
    try {
      if (requestDateFrom != null) {
        if (!"".equals(requestDateFrom)) {
          dateFromFormated = requestDateFrom;
        }
        dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
      } else if ((dateFromFormated == null) || "".equals(dateFromFormated)) {
        dateFromDefaultInit();
      } else {
        dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
      }

      String requestDateTo = getHttpRequest().getParameter(PARAM_DATETO);
      if (requestDateTo != null) {
        if (!"".equals(requestDateTo)) {
          dateToFormated = requestDateTo;
        }
        dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
      } else if ((dateToFormated == null) || "".equals(dateToFormated)) {
        dateToDefaultInit();
      } else {
        dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
      }
    } catch (ParseException e) {
      message = "plugin.wrong.dates";
      return false;
    }

    dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
    dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
    return true;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private String searchAction() throws ParseException {
    String searchValue = getHttpRequest().getParameter("search");
    // TODO no search....
    // if not null then we have to change the dates and make a new query
    if (searchValue != null) {
      // set actual page default! we start the new query with the first page
      if (!parseDateParams()) {
        return INPUT;
      }
      actualPage = 1;
      if (dateFrom.compareTo(dateTo) >= 0) {
        message = "plugin.wrong.dates";
        return INPUT;
      }
      String hourValue = getHttpRequest().getParameter("hour");
      String nonworkingValue = getHttpRequest().getParameter("nonworking");
      if (hourValue != null) {
        checkHours = true;
      }
      if (nonworkingValue != null) {
        checkNonWorkingIssues = true;
      }
    } else {
      dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
      dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
    }
    return null;
  }

  public void setActualPage(final int actualPage) {
    this.actualPage = actualPage;
  }

  public void setCheckHours(final boolean checkHours) {
    this.checkHours = checkHours;
  }

  public void setCheckNonWorkingIssues(final boolean checkNonWorkingIssues) {
    this.checkNonWorkingIssues = checkNonWorkingIssues;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setDateFromFormated(final String dateFromFormated) {
    this.dateFromFormated = dateFromFormated;
  }

  public void setDateswhereNoWorklog(final List<MissingsWorklogsDTO> dateswhereNoWorklog) {
    allDatesWhereNoWorklog = dateswhereNoWorklog;
  }

  public void setDateToFormated(final String dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMessageParameter(final String messageParameter) {
    this.messageParameter = messageParameter;
  }

  public void setNumberOfPages(final int numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

  public void setPaging(final MissingsPageingDTO paging) {
    this.paging = paging;
  }

  /**
   * Set the showDatesWhereNoWorklog by the actual page.
   *
   * @param actualPageParam
   *          The sub list of allDatesWhereNoWorklog.
   */
  private void setShowDatesListByActualPage(final int actualPageParam) {
    // TODO MissingsPageingDTO - from+1, to, actual page?, max page? allDatesWhereNoWorklog.size() -
    // static replace whit that!
    // TODO ROW_COUNT based on pageington settings of the user???
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

  public void setShowDatesWhereNoWorklog(
      final List<MissingsWorklogsDTO> showDatesWhereNoWorklog) {
    this.showDatesWhereNoWorklog = showDatesWhereNoWorklog;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
