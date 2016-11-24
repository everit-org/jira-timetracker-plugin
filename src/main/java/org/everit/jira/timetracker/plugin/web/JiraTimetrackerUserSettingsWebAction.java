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

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.TimetrackerCondition;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The settings page.
 */
public class JiraTimetrackerUserSettingsWebAction extends JiraWebActionSupport {

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String PLUGIN_SETTING_DEFAULT_STARTTIME_CHANGE_WRONG =
        "plugin.setting.default.starttime.change.wrong";

    public static final String PLUGIN_SETTING_END_TIME_CHANGE_WRONG =
        "plugin.setting.end.time.change.wrong";

    public static final String PLUGIN_SETTING_START_TIME_CHANGE_WRONG =
        "plugin.setting.start.time.change.wrong";

    public static final String PLUGIN_SETTINGS_TIME_FORMAT = "plugin.settings.time.format";
  }

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private boolean activeFieldDuration;

  private AnalyticsDTO analyticsDTO;

  private String defaultStartTime;

  /**
   * The endTime.
   */
  private int endTime;

  /**
   * The calenar show the actualDate or the last unfilled date.
   */
  private boolean isActualDate;

  /**
   * The calendar highlights coloring.
   */
  private boolean isColoring;

  private boolean isRounded;

  private boolean isShowFutureLogWarning;

  private boolean isShowIssueSummary;

  private String issueCollectorSrc;

  /**
   * The message.
   */
  private String message = "";

  /**
   * The message parameter.
   */
  private String messageParameter = "";

  private PluginCondition pluginCondition;

  private boolean progressIndDaily;

  private TimetrackerSettingsHelper settingsHelper;

  /**
   * The startTime.
   */
  private int startTime;

  private TimetrackerCondition timetrackingCondition;

  /**
   * Simpe consturctor.
   *
   */
  public JiraTimetrackerUserSettingsWebAction(
      final TimetrackerSettingsHelper settingsHelper) {
    timetrackingCondition = new TimetrackerCondition(settingsHelper);
    pluginCondition = new PluginCondition(settingsHelper);
    this.settingsHelper = settingsHelper;
  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!timetrackingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
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
    loadIssueCollectorSrc();
    loadUserSettings();
    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(
        PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID, settingsHelper);
    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    loadIssueCollectorSrc();
    loadUserSettings();
    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(
        PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID, settingsHelper);

    if (getHttpRequest().getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(getHttpRequest());
      if (parseResult != null) {
        return parseResult;
      }
      saveUserSettings();
      setReturnUrl("/secure/JiraTimetrackerWebAction!default.jspa");
      return getRedirect(INPUT);
    }

    return SUCCESS;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public String getDefaultStartTime() {
    return defaultStartTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public boolean getIsActualDate() {
    return isActualDate;
  }

  public boolean getIsColoring() {
    return isColoring;
  }

  public boolean getIsRounded() {
    return isRounded;
  }

  public boolean getIsShowFutureLogWarning() {
    return isShowFutureLogWarning;
  }

  public boolean getIsShowIssueSummary() {
    return isShowIssueSummary;
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

  public boolean getProgressIndDaily() {
    return progressIndDaily;
  }

  public int getStartTime() {
    return startTime;
  }

  public boolean isActiveFieldDuration() {
    return activeFieldDuration;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadUserSettings() {
    TimeTrackerUserSettings loaduserSettings = settingsHelper.loadUserSettings();
    endTime = loaduserSettings.getEndTimeChange();
    isActualDate = loaduserSettings.isActualDate();
    isColoring = loaduserSettings.isColoring();
    isRounded = loaduserSettings.isRounded();
    progressIndDaily = loaduserSettings.isProgressIndicatordaily();
    isShowFutureLogWarning =
        loaduserSettings.isShowFutureLogWarning();
    isShowIssueSummary =
        loaduserSettings.getIsShowIssueSummary();
    startTime = loaduserSettings.getStartTimeChange();
    activeFieldDuration = loaduserSettings.isActiveFieldDuration();
    defaultStartTime = loaduserSettings.getDefaultStartTime();
  }

  private void pareseCalendarAppearanceSettings(final HttpServletRequest request) {
    isActualDate = "current".equals(request.getParameter("currentOrLast"));

    isColoring = request.getParameter("isColoring") != null;
  }

  private void parseInputFieldsSettings(final HttpServletRequest request,
      final String activeFieldParam) {
    if ("duration".equals(activeFieldParam)) {
      activeFieldDuration = true;
    } else {
      activeFieldDuration = false;
    }

    defaultStartTime = request.getParameter("defaultStartTime");
    try {
      DateTimeConverterUtil.stringTimeToDateTime(defaultStartTime);
    } catch (IllegalArgumentException e) {
      message = PropertiesKey.PLUGIN_SETTING_DEFAULT_STARTTIME_CHANGE_WRONG;
      ApplicationProperties applicationProperties =
          ComponentAccessor.getComponent(ApplicationProperties.class);
      messageParameter = applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_DATE_TIME);
    }

    String startTimeValue = request.getParameter("startTime");
    try {
      if (TimetrackerUtil.validateTimeChange(startTimeValue)) {
        startTime = Integer.parseInt(startTimeValue);
      } else {
        message = PropertiesKey.PLUGIN_SETTING_START_TIME_CHANGE_WRONG;
      }
    } catch (NumberFormatException e) {
      message = PropertiesKey.PLUGIN_SETTINGS_TIME_FORMAT;
      messageParameter = startTimeValue;
    }

    String endTimeValue = request.getParameter("endTime");
    try {
      if (TimetrackerUtil.validateTimeChange(endTimeValue)) {
        endTime = Integer.parseInt(endTimeValue);
      } else {
        message = PropertiesKey.PLUGIN_SETTING_END_TIME_CHANGE_WRONG;
      }
    } catch (NumberFormatException e) {
      message = PropertiesKey.PLUGIN_SETTINGS_TIME_FORMAT;
      messageParameter = endTimeValue;
    }
  }

  /**
   * Parse the reqest after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String activeFieldParam = request.getParameter("activeField");

    // calendar appearance
    pareseCalendarAppearanceSettings(request);

    // notifications
    isShowFutureLogWarning = request.getParameter("isShowFutureLogWarning") != null;

    // progress indicator appearance
    String popupOrInlineValue = request.getParameter("progressInd");
    if ("daily".equals(popupOrInlineValue)) {
      progressIndDaily = true;
    } else {
      progressIndDaily = false;
    }

    // input fields settings
    parseInputFieldsSettings(request, activeFieldParam);

    // worklog data appearance
    parseWorklogDataApperanceSettings(request);

    if (!"".equals(message)) {
      return SUCCESS;
    }
    return null;
  }

  private void parseWorklogDataApperanceSettings(final HttpServletRequest request) {
    String isRoundedValue = request.getParameter("isRounded");
    isRounded = (isRoundedValue != null);

    String isShowIssueSummaryValue = request.getParameter("isShowIssueSummary");
    if ("showIssueSummary".equals(isShowIssueSummaryValue)) {
      isShowIssueSummary = true;
    } else {
      isShowIssueSummary = false;
    }
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Save the plugin settings.
   */
  public void saveUserSettings() {
    TimeTrackerUserSettings timeTrackerUserSettings = new TimeTrackerUserSettings()
        .coloring(isColoring)
        .isProgressIndicatordaily(progressIndDaily)
        .actualDate(isActualDate)
        .startTimeChange(startTime)
        .endTimeChange(endTime)
        .isRounded(isRounded)
        .isShowIssueSummary(isShowIssueSummary)
        .isShowFutureLogWarning(isShowFutureLogWarning)
        .activeFieldDuration(activeFieldDuration)
        .defaultStartTime(defaultStartTime);
    settingsHelper.saveUserSettings(timeTrackerUserSettings);
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
