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
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.TimetrackerCondition;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The settings page.
 */
public class JiraTimetrackerSettingsWebAction extends JiraWebActionSupport {

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerSettingsWebAction.class);

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private boolean analyticsCheck;

  private AnalyticsDTO analyticsDTO;

  /**
   * The collector issue ids.
   */
  private List<Pattern> collectorIssuePatterns;

  private String contextPath;

  /**
   * The endTime.
   */
  private String endTime;

  /**
   * The exclude dates in String format.
   */
  private String excludeDates = "";

  /**
   * The first day of the week.
   */
  private int fdow;

  /**
   * The include dates in String format.
   */
  private String includeDates = "";

  /**
   * The calenar show the actualDate or the last unfilled date.
   */
  private boolean isActualDate;

  /**
   * The calendar highlights coloring.
   */
  private boolean isColoring;

  private boolean isRounded;

  private String issueCollectorSrc;

  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesPatterns;

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

  private PluginCondition pluginCondition;

  private final PluginSettingsFactory pluginSettingsFactory;

  private boolean progressIndDaily;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  /**
   * The startTime.
   */
  private String startTime;

  private TimetrackerCondition timetrackingCondition;

  /**
   * Simpe consturctor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param pluginSettingsFactory
   *          the {@link PluginSettingsFactory}.
   */
  public JiraTimetrackerSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final PluginSettingsFactory pluginSettingsFactory) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.pluginSettingsFactory = pluginSettingsFactory;
    timetrackingCondition = new TimetrackerCondition(jiraTimetrackerPlugin);
    pluginCondition = new PluginCondition(jiraTimetrackerPlugin);
  }

  private String checkConditions() {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
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
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID);
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID);
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }

    if (getHttpRequest().getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(getHttpRequest());
      if (parseResult != null) {
        return parseResult;
      }
      savePluginSettings();
      setReturnUrl("/secure/JiraTimetrackerWebAction!default.jspa");
      return getRedirect(INPUT);
    }

    return SUCCESS;
  }

  public boolean getAnalyticsCheck() {
    return analyticsCheck;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getEndTime() {
    return endTime;
  }

  public int getFdow() {
    return fdow;
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

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getStartTime() {
    return startTime;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    progressIndDaily = pluginSettingsValues.isProgressIndicatorDaily;
    isActualDate = pluginSettingsValues.isActualDate;
    issuesPatterns = pluginSettingsValues.filteredSummaryIssues;
    collectorIssuePatterns = pluginSettingsValues.collectorIssues;
    excludeDates = pluginSettingsValues.excludeDates;
    includeDates = pluginSettingsValues.includeDates;
    startTime = Integer.toString(pluginSettingsValues.startTimeChange);
    endTime = Integer.toString(pluginSettingsValues.endTimeChange);
    isColoring = pluginSettingsValues.isColoring;
    isRounded = pluginSettingsValues.isRounded;
    analyticsCheck = pluginSettingsValues.analyticsCheck;
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
   * Parse the reqest after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String popupOrInlineValue = request.getParameter("progressInd");
    String startTimeValue = request.getParameter("startTime");
    String endTimeValue = request.getParameter("endTime");

    if ("daily".equals(popupOrInlineValue)) {
      progressIndDaily = true;
    } else {
      progressIndDaily = false;
    }

    String currentOrLastValue = request.getParameter("currentOrLast");
    isActualDate = "current".equals(currentOrLastValue);

    String isColoringValue = request.getParameter("isColoring");
    isColoring = (isColoringValue != null);

    String isRoundedValue = request.getParameter("isRounded");
    isRounded = (isRoundedValue != null);

    try {
      if (jiraTimetrackerPlugin.validateTimeChange(startTimeValue)) {
        startTime = startTimeValue;
      } else {
        message = "plugin.setting.start.time.change.wrong";
      }
    } catch (NumberFormatException e) {
      message = "plugin.settings.time.format";
      messageParameter = startTimeValue;
    }
    try {
      if (jiraTimetrackerPlugin.validateTimeChange(endTimeValue)) {
        endTime = endTimeValue;
      } else {
        message = "plugin.setting.end.time.change.wrong";
      }
    } catch (NumberFormatException e) {
      message = "plugin.settings.time.format";
      messageParameter = endTimeValue;
    }
    if (!"".equals(message)) {
      return SUCCESS;
    }
    return null;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    PluginSettingsValues pluginSettingValues = new PluginSettingsValues()
        .isProgressIndicatordaily(progressIndDaily)
        .actualDate(isActualDate)
        .excludeDates(excludeDates)
        .includeDates(includeDates).coloring(isColoring)
        .filteredSummaryIssues(issuesPatterns)
        .collectorIssues(collectorIssuePatterns)
        .startTimeChange(Integer.parseInt(startTime))
        .endTimeChange(Integer.parseInt(endTime))
        .analyticsCheck(analyticsCheck)
        .isRounded(isRounded);
    jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
  }

  public void setAnalyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setFdow(final int fdow) {
    this.fdow = fdow;
  }

  public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
    isActualDate = actualDateOrLastWorklogDate;
  }

  public void setIsColoring(final boolean isColoring) {
    this.isColoring = isColoring;
  }

  public void setIsRounded(final boolean isRounded) {
    this.isRounded = isRounded;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMessageParameter(final String messageParameter) {
    this.messageParameter = messageParameter;
  }

  public void setProgressIndDaily(final boolean progressIndDaily) {
    this.progressIndDaily = progressIndDaily;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
