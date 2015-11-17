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
package org.everit.jira.timetracker.plugin;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ChartData;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;

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

  private static final String NOT_RATED = "Not rated";

  private static final String PARAM_DATEFROM = "dateFrom";

  private static final String PARAM_DATETO = "dateTo";

  private static final String PARAM_USERPICKER = "userPicker";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String WRONG_DATES = "plugin.wrong.dates";

  private boolean analyticsCheck;

  private String avatarURL = "";

  private String baseUrl;

  private List<ChartData> chartDataList;

  private String contextPath;

  private String currentUser = "";

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

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * The message.
   */
  private String message = "";

  private String piwikHost;

  private String piwikSiteId;

  private String pluginVersion;

  private String userId;

  private transient ApplicationUser userPickerObject;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   */
  public JiraTimetrackerChartWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
  }

  /**
   * Set dateFrom and dateFromFormated default value.
   */
  private void dateFromDefaultInit() {
    Calendar calendarFrom = Calendar.getInstance();
    calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
    dateFrom = calendarFrom.getTime();
    dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
  }

  /**
   * Set dateTo and dateToFormated default value.
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
    jiraTimetrackerPlugin.loadPluginSettings();

    setPiwikProperties();
    loadPluginSettingAndParseResult();
    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }
    chartDataList = null;

    setLoggedUserToCurrentUser();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException, GenericEntityException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    normalizeContextPath();
    checkMailServer();
    jiraTimetrackerPlugin.loadPluginSettings();

    setPiwikProperties();
    loadPluginSettingAndParseResult();
    setDefaultDates();
    setLoggedUserToCurrentUser();

    if (parseFeedback()) {
      return INPUT;
    }

    Calendar startDate = null;
    Calendar lastDate = null;
    try {
      setCurrentUserFromParam();
      setUserPickerObjectBasedOnCurrentUser();
      startDate = getStartDate();
      lastDate = getLastDate();
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

  public boolean getAnalyticsCheck() {
    return analyticsCheck;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public List<ChartData> getChartDataList() {
    return chartDataList;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getCurrentUserEmail() {
    return currentUser;
  }

  public String getDateFromFormated() {
    return dateFromFormated;
  }

  public String getDateToFormated() {
    return dateToFormated;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  private Calendar getLastDate() throws IllegalArgumentException {
    String dateToParam = getHttpRequest().getParameter(PARAM_DATETO);
    if ((dateToParam != null) && !"".equals(dateToParam)) {
      dateToFormated = dateToParam;
    } else {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    Calendar lastDate = Calendar.getInstance();
    try {
      lastDate.setTime(DateTimeConverterUtil.stringToDate(dateToParam));
    } catch (ParseException e) {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    return lastDate;
  }

  public String getMessage() {
    return message;
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

  private Calendar getStartDate() throws IllegalArgumentException {
    String dateFromParam = getHttpRequest().getParameter(PARAM_DATEFROM);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFromFormated = dateFromParam;
    } else {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    Calendar startDate = Calendar.getInstance();
    try {
      startDate.setTime(DateTimeConverterUtil.stringToDate(dateFromParam));
    } catch (ParseException e) {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    return startDate;
  }

  public String getUserId() {
    return userId;
  }

  public ApplicationUser getUserPickerObject() {
    return userPickerObject;
  }

  private void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    analyticsCheck = pluginSettingsValues.getAnalyticsCheckChange();
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private boolean parseFeedback() {
    if (getHttpRequest().getParameter("sendfeedback") != null) {
      String feedBackValue = getHttpRequest().getParameter("feedbackinput");
      String ratingValue = getHttpRequest().getParameter("rating");
      String customerMail = getHttpRequest().getParameter("customerMail");
      String feedBack = "";
      String rating = NOT_RATED;
      if (feedBackValue != null) {
        feedBack = feedBackValue;
      }
      if (ratingValue != null) {
        rating = ratingValue;
      }
      jiraTimetrackerPlugin.sendFeedBackEmail(feedBack, JiraTimetrackerAnalytics.getPluginVersion(),
          rating, customerMail);
      return true;
    }
    return false;
  }

  public void setAnalyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
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
    if (selectedUser != null) {
      currentUser = selectedUser;
    } else {
      throw new IllegalArgumentException(INVALID_USER_PICKER);
    }
    if ("".equals(currentUser)) {
      JiraAuthenticationContext authenticationContext =
          ComponentAccessor.getJiraAuthenticationContext();
      currentUser = authenticationContext.getUser().getKey();
    }
  }

  public void setDateFromFormated(final String dateFromFormated) {
    this.dateFromFormated = dateFromFormated;
  }

  public void setDateToFormated(final String dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  private void setDefaultDates() {
    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  private void setLoggedUserToCurrentUser() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    currentUser = authenticationContext.getUser().getKey();
    setUserPickerObjectBasedOnCurrentUser();
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setPiwikHost(final String piwikHost) {
    this.piwikHost = piwikHost;
  }

  private void setPiwikProperties() {
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());

    piwikHost =
        jiraTimetrackerPlugin.getPiwikPorperty(JiraTimetrackerPiwikPropertiesUtil.PIWIK_HOST);
    piwikSiteId = jiraTimetrackerPlugin
        .getPiwikPorperty(JiraTimetrackerPiwikPropertiesUtil.PIWIK_CHART_SITEID);
  }

  public void setPiwikSiteId(final String piwikSiteId) {
    this.piwikSiteId = piwikSiteId;
  }

  public void setPluginVersion(final String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setUserPickerObject(final ApplicationUser userPickerObject) {
    this.userPickerObject = userPickerObject;
  }

  private void setUserPickerObjectBasedOnCurrentUser() {
    if (!"".equals(currentUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByKey(currentUser);
      AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getUser(),
          userPickerObject, Avatar.Size.SMALL).toString());
    } else {
      userPickerObject = null;
    }
  }
}
