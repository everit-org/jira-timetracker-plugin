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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ChartData;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.usercompatibility.UserCompatibilityHelper;
import com.atlassian.jira.usercompatibility.UserWithKey;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The Timetracker chart report action support class.
 */
public class JiraTimetrackerChartWebAction extends JiraWebActionSupport {

  private static final String WRONG_DATES = "plugin.wrong.dates";

  private static final String GET_WORKLOGS_ERROR_MESSAGE = "Error when trying to get worklogs.";

  private static final String INVALID_END_TIME = "plugin.invalid_endTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String INVALID_USER_PICKER = "plugin.user.picker.label";

  private static final String PARAM_DATETO = "dateTo";

  private static final String PARAM_DATEFROM = "dateFrom";

  private static final String PARAM_USERPICKER = "userPicker";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerChartWebAction.class);

  private String pluginVersion;

  private String baseUrl;

  private String userId;

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

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
  /**
   * The message.
   */
  private String message = "";

  private String contextPath;

  private List<ChartData> chartDataList;

  private List<User> allUsers;

  private String currentUserKey = "";

  private String avatarURL = "";

  private transient UserWithKey userPickerObject;

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

  /**
   * Set dateFrom and dateFromFormated default value.
   */
  private void dateFromDefaultInit() {
    final Calendar calendarFrom = Calendar.getInstance();
    calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
    dateFrom = calendarFrom.getTime();
    dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
  }

  /**
   * Set dateTo and dateToFormated default value.
   */
  private void dateToDefaultInit() {
    final Calendar calendarTo = Calendar.getInstance();
    dateTo = calendarTo.getTime();
    dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
  }

  @Override
  public String doDefault() throws ParseException {
    final boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    normalizeContextPath();
    jiraTimetrackerPlugin.loadPluginSettings();
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(request.getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(request.getSession());

    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }
    chartDataList = null;

    allUsers = new ArrayList<User>(UserUtils.getAllUsers());
    Collections.sort(allUsers);

    final JiraAuthenticationContext authenticationContext = ComponentManager.getInstance()
        .getJiraAuthenticationContext();
    currentUserKey = UserCompatibilityHelper.getKeyForUser(authenticationContext.getLoggedInUser());
    setUserPickerObjectBasedOnCurrentUser();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    final boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    normalizeContextPath();
    jiraTimetrackerPlugin.loadPluginSettings();
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(request.getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(request.getSession());

    setDefaultDates();

    Calendar startDate = null;
    Calendar lastDate = null;
    try {
      setCurrentUserFromParam();
      setUserPickerObjectBasedOnCurrentUser();
      startDate = getStartDate();
      lastDate = getLastDate();
    } catch (final IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    if (startDate.after(lastDate)) {
      message = WRONG_DATES;
      return INPUT;
    }

    final List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
    try {
      worklogs.addAll(jiraTimetrackerPlugin.getWorklogs(currentUserKey, startDate.getTime(),
          lastDate.getTime()));
    } catch (final DataAccessException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      message = GET_WORKLOGS_ERROR_MESSAGE;
      return ERROR;
    } catch (final SQLException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      message = GET_WORKLOGS_ERROR_MESSAGE;
      return ERROR;
    }

    final Map<String, Long> map = new HashMap<String, Long>();
    for (final EveritWorklog worklog : worklogs) {
      final String projectName = worklog.getIssue().split("-")[0];
      final Long newValue = worklog.getMilliseconds();
      final Long oldValue = map.get(projectName);
      if (oldValue == null) {
        map.put(projectName, newValue);
      } else {
        map.put(projectName, oldValue + newValue);
      }
    }
    chartDataList = new ArrayList<ChartData>();
    for (final Entry<String, Long> entry : map.entrySet()) {
      chartDataList.add(new ChartData(entry.getKey(), entry.getValue()));
    }
    return SUCCESS;
  }

  public List<User> getAllUsers() {
    return allUsers;
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
    return currentUserKey;
  }

  public String getDateFromFormated() {
    return dateFromFormated;
  }

  public String getDateToFormated() {
    return dateToFormated;
  }

  private Calendar getLastDate() throws IllegalArgumentException {
    final String dateToParam = request.getParameterValues(PARAM_DATETO)[0];
    if (!"".equals(dateToParam)) {
      dateToFormated = dateToParam;
    } else {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    final Calendar lastDate = Calendar.getInstance();
    try {
      lastDate.setTime(DateTimeConverterUtil.stringToDate(dateToParam));
    } catch (final ParseException e) {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    return lastDate;
  }

  public String getMessage() {
    return message;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  private Calendar getStartDate() throws IllegalArgumentException {
    final String dateFromParam = request.getParameterValues(PARAM_DATEFROM)[0];
    if (!"".equals(dateFromParam)) {
      dateFromFormated = dateFromParam;
    } else {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    final Calendar startDate = Calendar.getInstance();
    try {
      startDate.setTime(DateTimeConverterUtil.stringToDate(dateFromParam));
    } catch (final ParseException e) {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    return startDate;
  }

  public String getUserId() {
    return userId;
  }

  public UserWithKey getUserPickerObject() {
    return userPickerObject;
  }

  private void normalizeContextPath() {
    final String path = request.getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  public void setAllUsers(final List<User> allUsers) {
    this.allUsers = allUsers;
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
    currentUserKey = currentUserEmail;
  }

  private void setCurrentUserFromParam() throws IllegalArgumentException {
    if (request.getParameterValues(PARAM_USERPICKER) != null) {
      final User user = UserCompatibilityHelper.getUserForKey((request
          .getParameterValues(PARAM_USERPICKER)[0]));
      currentUserKey = UserCompatibilityHelper.getKeyForUser(user);
    } else {
      throw new IllegalArgumentException(INVALID_USER_PICKER);
    }
    if ((currentUserKey == null) || "".equals(currentUserKey)) {
      final JiraAuthenticationContext authenticationContext = ComponentManager.getInstance()
          .getJiraAuthenticationContext();
      currentUserKey = UserCompatibilityHelper.getKeyForUser(authenticationContext
          .getLoggedInUser());
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

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setPluginVersion(final String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setUserPickerObject(final UserWithKey userPickerObject) {
    this.userPickerObject = userPickerObject;
  }

  private void setUserPickerObjectBasedOnCurrentUser() {
    if (!"".equals(currentUserKey)) {
      final User user = UserCompatibilityHelper.getUserForKey(currentUserKey);
      userPickerObject = UserCompatibilityHelper.convertUserObject(user);
      final User loggedInUser = ComponentManager.getInstance().getJiraAuthenticationContext()
          .getLoggedInUser();
      final AvatarService avatarService = ComponentManager
          .getComponentInstanceOfType(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(loggedInUser, currentUserKey, Avatar.Size.SMALL)
          .toString());
    } else {
      userPickerObject = null;
    }
  }
}
