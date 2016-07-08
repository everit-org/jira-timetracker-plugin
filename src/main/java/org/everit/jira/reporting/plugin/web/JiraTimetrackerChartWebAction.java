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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.reporting.plugin.ReportingCondition;
import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.util.PermissionUtil;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.dto.ChartData;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.TimetrackerReportsSessionData;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Timetracker chart report action support class.
 */
public class JiraTimetrackerChartWebAction extends JiraWebActionSupport {

  private static final String GET_WORKLOGS_ERROR_MESSAGE = "Error when trying to get worklogs.";

  private static final String INVALID_END_TIME = "plugin.invalid_endTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String INVALID_USER_PICKER = "plugin.user.picker.label";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerChartWebAction.class);

  private static final String PARAM_DATEFROM = "dateFromMil";

  private static final String PARAM_DATETO = "dateToMil";

  private static final String PARAM_USERPICKER = "selectedUser";

  private static final String SELF_WITH_DATE_AND_USER_URL_FORMAT =
      "/secure/JiraTimetrackerChartWebAction.jspa"
          + "?dateFromMil=%s"
          + "&dateToMil=%s"
          + "&selectedUser=%s"
          + "&search";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String SESSION_KEY = "jttpChartStore";

  private static final String WRONG_DATES = "plugin.wrong.dates";

  private AnalyticsDTO analyticsDTO;

  private String avatarURL = "";

  private List<ChartData> chartDataList;

  private String contextPath;

  private String currentUser = "";

  /**
   * The formated date.
   */
  private Long dateFromFormated;

  /**
   * The formated date.
   */
  private Long dateToFormated;

  public boolean hasBrowseUsersPermission = true;

  private String issueCollectorSrc;

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * The message.
   */
  private String message = "";

  private PluginCondition pluginCondition;

  private final PluginSettingsFactory pluginSettingsFactory;

  private ReportingCondition reportingCondition;

  private ReportingPlugin reportingPlugin;

  private transient ApplicationUser userPickerObject;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param pluginSettingsFactory
   *          the {@link PluginSettingsFactory}.
   */
  public JiraTimetrackerChartWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final ReportingPlugin reportingPlugin,
      final PluginSettingsFactory pluginSettingsFactory) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.reportingPlugin = reportingPlugin;
    reportingCondition = new ReportingCondition(this.reportingPlugin);
    this.pluginSettingsFactory = pluginSettingsFactory;
    pluginCondition = new PluginCondition(jiraTimetrackerPlugin);
  }

  private String checkConditions() {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
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

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    normalizeContextPath();
    loadIssueCollectorSrc();

    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(), reportingPlugin);

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_CHART_SITEID);

    boolean loadedFromSession = loadDataFromSession();
    initDatesIfNecessary();
    initCurrentUserIfNecessary();
    chartDataList = null;

    if (loadedFromSession) {
      setReturnUrl(getFormattedRedirectUrl());
      return getRedirect(NONE);
    } else {
      return INPUT;
    }
  }

  @Override
  public String doExecute() throws ParseException, GenericEntityException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    normalizeContextPath();
    loadIssueCollectorSrc();

    hasBrowseUsersPermission =
        PermissionUtil.hasBrowseUserPermission(getLoggedInApplicationUser(), reportingPlugin);

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_CHART_SITEID);

    Calendar startDate = null;
    Calendar lastDate = null;
    try {
      setCurrentUserFromParam();
      setUserPickerObjectBasedOnCurrentUser();
      startDate = parseDateFrom();
      lastDate = parseDateTo();
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    if (startDate.after(lastDate)) {
      message = WRONG_DATES;
      return INPUT;
    }

    List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
    try {
      worklogs.addAll(jiraTimetrackerPlugin.getWorklogs(currentUser, startDate.getTime(),
          lastDate.getTime()));
      saveDataToSession();
    } catch (DataAccessException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      return ERROR;
    } catch (SQLException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      return ERROR;
    }

    Map<String, Long> map = new HashMap<String, Long>();
    for (EveritWorklog worklog : worklogs) {
      String projectName = worklog.getIssue().split("-")[0];
      Long newValue = worklog.getMilliseconds();
      Long oldValue = map.get(projectName);
      if (oldValue == null) {
        map.put(projectName, newValue);
      } else {
        map.put(projectName, oldValue + newValue);
      }
    }
    chartDataList = new ArrayList<ChartData>();
    for (Entry<String, Long> entry : map.entrySet()) {
      chartDataList.add(new ChartData(entry.getKey(), entry.getValue()));
    }
    return SUCCESS;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  /**
   * ChartDataList JSON representation.
   *
   * @return String array of chartDataList.
   */
  @HtmlSafe
  public String getChartDataList() {
    Gson gson = new GsonBuilder().create();
    return gson.toJson(chartDataList);
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getCurrentUserEmail() {
    return currentUser;
  }

  public Long getDateFromFormated() {
    return dateFromFormated;
  }

  public Long getDateToFormated() {
    return dateToFormated;
  }

  private String getFormattedRedirectUrl() {
    String currentUserEncoded;
    try {
      currentUserEncoded = URLEncoder.encode(currentUser, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      currentUserEncoded = "";
    }
    return String.format(
        SELF_WITH_DATE_AND_USER_URL_FORMAT,
        dateFromFormated,
        dateToFormated,
        currentUserEncoded);
  }

  public boolean getHasBrowseUsersPermission() {
    return hasBrowseUsersPermission;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getMessage() {
    return message;
  }

  public ApplicationUser getUserPickerObject() {
    return userPickerObject;
  }

  private void initCurrentUserIfNecessary() {
    if ("".equals(currentUser) || !hasBrowseUsersPermission) {
      JiraAuthenticationContext authenticationContext = ComponentAccessor
          .getJiraAuthenticationContext();
      currentUser = authenticationContext.getUser().getUsername();
      setUserPickerObjectBasedOnCurrentUser();
    }
  }

  private void initDatesIfNecessary() {
    if (dateFromFormated == null) {
      Calendar calendarFrom = Calendar.getInstance();
      calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
      dateFromFormated = calendarFrom.getTimeInMillis();
    }
    if (dateToFormated == null) {
      Calendar calendarTo = Calendar.getInstance();
      dateToFormated = calendarTo.getTimeInMillis();
    }
  }

  private boolean loadDataFromSession() {
    HttpSession session = getHttpSession();
    Object data = session.getAttribute(SESSION_KEY);

    if (!(data instanceof TimetrackerReportsSessionData)) {
      return false;
    }
    TimetrackerReportsSessionData timetrackerReportsSessionData =
        (TimetrackerReportsSessionData) data;
    currentUser = timetrackerReportsSessionData.currentUser;
    dateFromFormated = timetrackerReportsSessionData.dateFrom;
    dateToFormated = timetrackerReportsSessionData.dateTo;
    return true;
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

  private Calendar parseDateFrom() throws IllegalArgumentException {
    String dateFromParam = getHttpRequest().getParameter(PARAM_DATEFROM);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFromFormated = Long.valueOf(dateFromParam);
      Calendar parsedCalendarFrom = Calendar.getInstance();
      parsedCalendarFrom.setTimeInMillis(dateFromFormated);
      return parsedCalendarFrom;
    } else {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
  }

  private Calendar parseDateTo() throws IllegalArgumentException {
    String dateToParam = getHttpRequest().getParameter(PARAM_DATETO);
    if ((dateToParam != null) && !"".equals(dateToParam)) {
      dateToFormated = Long.valueOf(dateToParam);
      Calendar parsedCalendarTo = Calendar.getInstance();
      parsedCalendarTo.setTimeInMillis(dateToFormated);
      return parsedCalendarTo;
    } else {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void saveDataToSession() {
    HttpSession session = getHttpSession();
    session.setAttribute(SESSION_KEY,
        new TimetrackerReportsSessionData().currentUser(currentUser).dateFrom(dateFromFormated)
            .dateTo(dateToFormated));
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setChartDataList(final List<ChartData> chartDataList) {
    this.chartDataList = chartDataList;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setCurrentUser(final String currentUserEmail) {
    currentUser = currentUserEmail;
  }

  private void setCurrentUserFromParam() throws IllegalArgumentException {
    String selectedUser = getHttpRequest().getParameter(PARAM_USERPICKER);
    if (selectedUser == null) {
      throw new IllegalArgumentException(INVALID_USER_PICKER);
    }
    currentUser = selectedUser;
    if ("".equals(currentUser) || !hasBrowseUsersPermission) {
      JiraAuthenticationContext authenticationContext = ComponentAccessor
          .getJiraAuthenticationContext();
      currentUser = authenticationContext.getUser().getKey();
    }
  }

  public void setDateFromFormated(final Long dateFromFormated) {
    this.dateFromFormated = dateFromFormated;
  }

  public void setDateToFormated(final Long dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  public void setHasBrowseUsersPermission(final boolean hasBrowseUsersPermission) {
    this.hasBrowseUsersPermission = hasBrowseUsersPermission;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setUserPickerObject(final ApplicationUser userPickerObject) {
    this.userPickerObject = userPickerObject;
  }

  private void setUserPickerObjectBasedOnCurrentUser() {
    if (!"".equals(currentUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByName(currentUser);
      if (userPickerObject == null) {
        throw new IllegalArgumentException(INVALID_USER_PICKER);
      }
      AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getUser(),
          userPickerObject, Avatar.Size.SMALL).toString());
    } else {
      userPickerObject = null;
    }
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
