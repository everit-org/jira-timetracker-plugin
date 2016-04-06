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
package org.everit.jira.timetracker.plugin.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Missing worklogs page.
 */
public class JiraTimetrackerWorklogsWebAction extends JiraWebActionSupport {

  private static final String FREQUENT_FEEDBACK = "jttp.plugin.frequent.feedback";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWorklogsWebAction.class);

  private static final String NOT_RATED = "Not rated";

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

  private List<String> allDatesWhereNoWorklog;

  private boolean analyticsCheck;

  private String baseUrl;

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

  private boolean feedBackSendAviable;

  private String installedPluginId;

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

  private String piwikHost;

  private String piwikSiteId;

  private String pluginVersion;

  private List<String> showDatesWhereNoWorklog;

  /**
   * The message parameter.
   */
  private String statisticsMessageParameter = "0";

  private String userId;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   */
  public JiraTimetrackerWorklogsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
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

    normalizeContextPath();
    checkMailServer();

    loadPluginSettingAndParseResult();
    setPiwikProperties();
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
      List<Date> dateswhereNoWorklogDate = jiraTimetrackerPlugin
          .getDates("", dateFrom, dateTo, checkHours,
              checkNonWorkingIssues);
      allDatesWhereNoWorklog = new ArrayList<String>();
      for (Date date : dateswhereNoWorklogDate) {
        allDatesWhereNoWorklog.add(DateTimeConverterUtil
            .dateToString(date));
      }
      statisticsMessageParameter = Integer
          .toString(allDatesWhereNoWorklog.size());
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

    normalizeContextPath();
    checkMailServer();

    loadPluginSettingAndParseResult();
    setPiwikProperties();

    message = "";
    messageParameter = "";
    statisticsMessageParameter = "0";
    allDatesWhereNoWorklog = new ArrayList<String>();
    showDatesWhereNoWorklog = new ArrayList<String>();
    String searchValue = getHttpRequest().getParameter("search");
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
    try {
      // TODO not simple "" for selectedUser. Use user picker
      List<Date> dateswhereNoWorklogDate = jiraTimetrackerPlugin
          .getDates("", dateFrom, dateTo, checkHours,
              checkNonWorkingIssues);
      for (Date date : dateswhereNoWorklogDate) {
        allDatesWhereNoWorklog.add(DateTimeConverterUtil
            .dateToString(date));
      }
      statisticsMessageParameter = Integer
          .toString(allDatesWhereNoWorklog.size());
    } catch (GenericEntityException e) {
      LOGGER.error("Error when try to run the query.", e);
      return ERROR;
    }
    // check the page changer buttons
    numberOfPages = countNumberOfPages();
    pageChangeAction();
    setShowDatesListByActualPage(actualPage);

    parseFeedBack();

    return SUCCESS;
  }

  public int getActualPage() {
    return actualPage;
  }

  public boolean getAnalyticsCheck() {
    return analyticsCheck;
  }

  public String getBaseUrl() {
    return baseUrl;
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

  public List<String> getDateswhereNoWorklog() {
    return allDatesWhereNoWorklog;
  }

  public String getDateToFormated() {
    return dateToFormated;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  public String getInstalledPluginId() {
    return installedPluginId;
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

  public String getPiwikHost() {
    return piwikHost;
  }

  public String getPiwikSiteId() {
    return piwikSiteId;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public List<String> getShowDatesWhereNoWorklog() {
    return showDatesWhereNoWorklog;
  }

  public String getStatisticsMessageParameter() {
    return statisticsMessageParameter;
  }

  public String getUserId() {
    return userId;
  }

  private void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    analyticsCheck = pluginSettingsValues.analyticsCheck;
    installedPluginId = pluginSettingsValues.pluginUUID;
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
    if ((dayBackValue != null) && (actualPage > 1)) {
      actualPage--;
    }
    if ((dayNextValue != null) && (actualPage < numberOfPages)) {
      actualPage++;
    }

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

  private void parseFeedBack() {
    if (getHttpRequest().getParameter("sendfeedback") != null) {
      if (JiraTimetrackerUtil.loadAndCheckFeedBackTimeStampFromSession(getHttpSession())) {
        String feedBackValue = getHttpRequest().getParameter("feedbackinput");
        String ratingValue = getHttpRequest().getParameter("rating");
        String customerMail =
            JiraTimetrackerUtil.getCheckCustomerMail(getHttpRequest().getParameter("customerMail"));
        String feedBack = "";
        String rating = NOT_RATED;
        if (feedBackValue != null) {
          feedBack = feedBackValue;
        }
        if (ratingValue != null) {
          rating = ratingValue.trim();
        }
        String mailSubject = JiraTimetrackerUtil
            .createFeedbackMailSubject(JiraTimetrackerAnalytics.getPluginVersion());
        String mailBody =
            JiraTimetrackerUtil.createFeedbackMailBody(customerMail, rating, feedBack);
        jiraTimetrackerPlugin.sendEmail(mailSubject, mailBody);
        JiraTimetrackerUtil.saveFeedBackTimeStampToSession(getHttpSession());
      } else {
        message = FREQUENT_FEEDBACK;
      }
    }
  }

  public void setActualPage(final int actualPage) {
    this.actualPage = actualPage;
  }

  public void setAnalyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
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

  public void setDateswhereNoWorklog(final List<String> dateswhereNoWorklog) {
    allDatesWhereNoWorklog = dateswhereNoWorklog;
  }

  public void setDateToFormated(final String dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  public void setInstalledPluginId(final String installedPluginId) {
    this.installedPluginId = installedPluginId;
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

  public void setPiwikHost(final String piwikHost) {
    this.piwikHost = piwikHost;
  }

  private void setPiwikProperties() {
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());

    piwikHost =
        jiraTimetrackerPlugin.getPiwikPorperty(PiwikPropertiesUtil.PIWIK_HOST);
    piwikSiteId = jiraTimetrackerPlugin
        .getPiwikPorperty(PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID);
  }

  public void setPiwikSiteId(final String piwikSiteId) {
    this.piwikSiteId = piwikSiteId;
  }

  public void setPluginVersion(final String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }

  /**
   * Set the showDatesWhereNoWorklog by the actual page.
   *
   * @param actualPageParam
   *          The sub list of allDatesWhereNoWorklog.
   */
  private void setShowDatesListByActualPage(final int actualPageParam) {
    int from = (actualPageParam - 1) * ROW_COUNT;
    int to = actualPageParam * ROW_COUNT;
    if ((actualPageParam == 1) && (allDatesWhereNoWorklog.size() < ROW_COUNT)) {
      to = allDatesWhereNoWorklog.size();
    }
    if ((actualPageParam == numberOfPages)
        && ((allDatesWhereNoWorklog.size() % ROW_COUNT) != 0)) {
      to = from + (allDatesWhereNoWorklog.size() % ROW_COUNT);
    }
    showDatesWhereNoWorklog = allDatesWhereNoWorklog.subList(from, to);
  }

  public void setShowDatesWhereNoWorklog(
      final List<String> showDatesWhereNoWorklog) {
    this.showDatesWhereNoWorklog = showDatesWhereNoWorklog;
  }

  public void setStatisticsMessageParameter(
      final String statisticsMessageParameter) {
    this.statisticsMessageParameter = statisticsMessageParameter;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

}
