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

import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
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
 * The Timetracker table report action support class.
 */
public class JiraTimetrackerTableWebAction extends JiraWebActionSupport {
  /**
   * EveritWorklog comparator by Date.
   */
  private static class OrderByDate implements Comparator<EveritWorklog>, Serializable {
    private static final long serialVersionUID = 2000628478189889582L;

    @Override
    public int compare(final EveritWorklog wl1, final EveritWorklog wl2) {
      return wl1.getDate().compareTo(wl2.getDate());
    }
  }

  private static final String EXCEEDED_A_YEAR = "plugin.exceeded.year";

  private static final String GET_WORKLOGS_ERROR_MESSAGE = "Error when trying to get worklogs.";

  private static final String INVALID_END_TIME = "plugin.invalid_endTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String INVALID_USER_PICKER = "plugin.user.picker.label";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerChartWebAction.class);

  private static final int MILLISEC_IN_SEC = 1000;

  private static final String PARAM_DATEFROM = "dateFrom";

  private static final String PARAM_DATETO = "dateTo";

  private static final String PARAM_USERPICKER = "userPicker";
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String WRONG_DATES = "plugin.wrong.dates";

  private String pluginVersion;

  private String baseUrl;

  private String userId;

  private String avatarURL = "";

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

  private HashMap<Integer, List<Object>> daySum = new HashMap<Integer, List<Object>>();

  private List<Pattern> issuesRegex;
  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;
  /**
   * The message.
   */
  private String message = "";

  private HashMap<Integer, List<Object>> monthSum = new HashMap<Integer, List<Object>>();

  private HashMap<Integer, List<Object>> realDaySum = new HashMap<Integer, List<Object>>();

  private HashMap<Integer, List<Object>> realMonthSum = new HashMap<Integer, List<Object>>();

  private HashMap<Integer, List<Object>> realWeekSum = new HashMap<Integer, List<Object>>();

  private transient ApplicationUser userPickerObject;

  private HashMap<Integer, List<Object>> weekSum = new HashMap<Integer, List<Object>>();

  private List<EveritWorklog> worklogs;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   */
  public JiraTimetrackerTableWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  private void addToDaySummary(final EveritWorklog worklog) {
    int dayNo = worklog.getDayNo();
    ArrayList<Object> list = new ArrayList<Object>();
    Long prevDaySum = (daySum.get(dayNo) == null) ? Long.valueOf(0)
        : (Long) daySum.get(dayNo).get(0);
    Long sumSec = prevDaySum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
    daySum.put(dayNo, list);
  }

  private void addToMonthSummary(final EveritWorklog worklog) {
    int monthNo = worklog.getMonthNo();
    ArrayList<Object> list = new ArrayList<Object>();
    Long prevMonthSum = (monthSum.get(monthNo) == null) ? Long.valueOf(0)
        : (Long) monthSum.get(monthNo).get(0);
    Long sumSec = prevMonthSum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
    monthSum.put(monthNo, list);
  }

  private void addToRealDaySummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int dayNo = worklog.getDayNo();
    ArrayList<Object> realList = new ArrayList<Object>();
    Long prevRealDaySum = (realDaySum.get(dayNo) == null) ? Long.valueOf(0)
        : (Long) realDaySum.get(dayNo).get(0);
    Long realSumSec = prevRealDaySum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
    realDaySum.put(dayNo, realList);
  }

  private void addToRealMonthSummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int monthNo = worklog.getMonthNo();
    ArrayList<Object> realList = new ArrayList<Object>();
    Long prevRealMonthSum = realMonthSum.get(monthNo) == null ? Long.valueOf(0)
        : (Long) realMonthSum.get(monthNo).get(0);
    Long realSumSec = prevRealMonthSum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
    realMonthSum.put(monthNo, realList);
  }

  private void addToRealWeekSummary(final EveritWorklog worklog, final boolean isRealWorklog) {
    int weekNo = worklog.getWeekNo();
    ArrayList<Object> realList = new ArrayList<Object>();
    Long prevRealWeekSum = realWeekSum.get(weekNo) == null ? Long.valueOf(0)
        : (Long) realWeekSum.get(weekNo).get(0);
    Long realSumSec = prevRealWeekSum;
    if (isRealWorklog) {
      realSumSec += (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    }
    realList.add(realSumSec);
    realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
    realWeekSum.put(weekNo, realList);
  }

  private void addToWeekSummary(final EveritWorklog worklog) {
    ArrayList<Object> list = new ArrayList<Object>();
    int weekNo = worklog.getWeekNo();
    Long prevWeekSum = weekSum.get(weekNo) == null ? Long.valueOf(0)
        : (Long) weekSum.get(weekNo).get(0);
    Long sumSec = prevWeekSum + (worklog.getMilliseconds() / MILLISEC_IN_SEC);
    list.add(sumSec);
    list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
    weekSum.put(weekNo, list);
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
    jiraTimetrackerPlugin.loadPluginSettings();
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());
    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }

    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    currentUser = authenticationContext.getUser().getKey();
    setUserPickerObjectBasedOnCurrentUser();

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
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());
    PluginSettingsValues pluginSettings = jiraTimetrackerPlugin.loadPluginSettings();
    setIssuesRegex(pluginSettings.getFilteredSummaryIssues());

    setDefaultDates();

    Calendar startDate = null;
    Calendar lastDate = null;
    try {
      setCurrentUserFromParam();
      setUserPickerObjectBasedOnCurrentUser();
      startDate = getStartDate();
      lastDate = getLastDate();
      validateDates(startDate, lastDate);
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    worklogs = new ArrayList<EveritWorklog>();
    try {
      worklogs.addAll(jiraTimetrackerPlugin.getWorklogs(currentUser, startDate.getTime(),
          lastDate.getTime()));
    } catch (DataAccessException | SQLException e) {
      LOGGER.error(GET_WORKLOGS_ERROR_MESSAGE, e);
      return ERROR;
    }

    Collections.sort(worklogs, new OrderByDate());

    for (EveritWorklog worklog : worklogs) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(worklog.getDate());

      boolean isRealWorklog = isRealWorklog(worklog);

      addToMonthSummary(worklog);
      addToRealMonthSummary(worklog, isRealWorklog);

      addToWeekSummary(worklog);
      addToRealWeekSummary(worklog, isRealWorklog);

      addToDaySummary(worklog);
      addToRealDaySummary(worklog, isRealWorklog);
    }

    return SUCCESS;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getBaseUrl() {
    return baseUrl;
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

  public HashMap<Integer, List<Object>> getDaySum() {
    return daySum;
  }

  public List<Pattern> getIssuesRegex() {
    return issuesRegex;
  }

  private Calendar getLastDate() throws IllegalArgumentException {
    String dateToParam = getHttpRequest().getParameterValues(PARAM_DATETO)[0];
    if (!"".equals(dateToParam)) {
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

  public HashMap<Integer, List<Object>> getMonthSum() {
    return monthSum;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public HashMap<Integer, List<Object>> getRealDaySum() {
    return realDaySum;
  }

  public HashMap<Integer, List<Object>> getRealMonthSum() {
    return realMonthSum;
  }

  public HashMap<Integer, List<Object>> getRealWeekSum() {
    return realWeekSum;
  }

  private Calendar getStartDate() throws IllegalArgumentException {
    String dateFromParam = getHttpRequest().getParameterValues(PARAM_DATEFROM)[0];
    if (!"".equals(dateFromParam)) {
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

  public HashMap<Integer, List<Object>> getWeekSum() {
    return weekSum;
  }

  public List<EveritWorklog> getWorklogs() {
    return worklogs;
  }

  private boolean isRealWorklog(final EveritWorklog worklog) {
    boolean isRealWorklog = true;
    if (issuesRegex != null) {
      for (Pattern issuePattern : issuesRegex) {
        boolean issueMatches = issuePattern.matcher(worklog.getIssue()).matches();
        // if match not count in summary
        if (issueMatches) {
          isRealWorklog = false;
          break;
        }
      }
    }
    return isRealWorklog;
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setCurrentUser(final String currentUserEmail) {
    currentUser = currentUserEmail;
  }

  private void setCurrentUserFromParam() throws IllegalArgumentException {
    if (getHttpRequest().getParameterValues(PARAM_USERPICKER) != null) {
      currentUser = getHttpRequest().getParameterValues(PARAM_USERPICKER)[0];
    } else {
      throw new IllegalArgumentException(INVALID_USER_PICKER);
    }
    if ((currentUser == null) || "".equals(currentUser)) {
      JiraAuthenticationContext authenticationContext = ComponentAccessor
          .getJiraAuthenticationContext();
      currentUser = authenticationContext.getUser().getKey();
    }
  }

  public void setDateFromFormated(final String dateFromFormated) {
    this.dateFromFormated = dateFromFormated;
  }

  public void setDateToFormated(final String dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  public void setDaySum(final HashMap<Integer, List<Object>> daySum) {
    this.daySum = daySum;
  }

  private void setDefaultDates() {
    if ("".equals(dateFromFormated)) {
      dateFromDefaultInit();
    }
    if ("".equals(dateToFormated)) {
      dateToDefaultInit();
    }
  }

  public void setIssuesRegex(final List<Pattern> issuesRegex) {
    this.issuesRegex = issuesRegex;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMonthSum(final HashMap<Integer, List<Object>> monthSum) {
    this.monthSum = monthSum;
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
    if ((currentUser != null) && !"".equals(currentUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByKey(currentUser);
      AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getUser(),
          userPickerObject, Avatar.Size.SMALL).toString());
    } else {
      userPickerObject = null;
    }
  }

  public void setWeekSum(final HashMap<Integer, List<Object>> weekSum) {
    this.weekSum = weekSum;
  }

  public void setWorklogs(final List<EveritWorklog> worklogs) {
    this.worklogs = worklogs;
  }

  private void validateDates(final Calendar startDate, final Calendar lastDate) {
    if (startDate.after(lastDate)) {
      throw new IllegalArgumentException(WRONG_DATES);
    }

    Calendar yearCheckCal = (Calendar) lastDate.clone();
    yearCheckCal.add(Calendar.YEAR, -1);
    if (startDate.before(yearCheckCal)) {
      throw new IllegalArgumentException(EXCEEDED_A_YEAR);
    }
  }
}
