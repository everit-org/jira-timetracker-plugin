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

import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.CalendarSettingsValues;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The settings page.
 */
public class JiraTimetrackerSettingsWebAction extends JiraWebActionSupport {

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerSettingsWebAction.class);
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
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
  /**
   * The calendar is popup, inLine or both.
   */
  private int isPopup;
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

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  /**
   * The startTime.
   */
  private String startTime;

  public JiraTimetrackerSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl("/secure/Dashboard.jspa");
      return getRedirect(NONE);
    }
    normalizeContextPath();
    loadPluginSettingAndParseResult();
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
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl("/secure/Dashboard.jspa");
      return getRedirect(NONE);
    }
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }

    if (request.getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(request);
      if (parseResult != null) {
        return parseResult;
      }
      savePluginSettings();
      setReturnUrl("/secure/JiraTimetrackerWebAction!default.jspa");
      return getRedirect(INPUT);
    }

    return SUCCESS;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getEndTime() {
    return endTime;
  }

  public boolean getIsActualDate() {
    return isActualDate;
  }

  public boolean getIsColoring() {
    return isColoring;
  }

  public int getIsPopup() {
    return isPopup;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageParameter() {
    return messageParameter;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getStartTime() {
    return startTime;
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    isPopup = pluginSettingsValues.isCalendarPopup();
    isActualDate = pluginSettingsValues.isActualDate();
    issuesPatterns = pluginSettingsValues.getFilteredSummaryIssues();
    collectorIssuePatterns = pluginSettingsValues.getCollectorIssues();
    excludeDates = pluginSettingsValues.getExcludeDates();
    includeDates = pluginSettingsValues.getIncludeDates();
    startTime = Integer.toString(pluginSettingsValues.getStartTimeChange());
    endTime = Integer.toString(pluginSettingsValues.getEndTimeChange());
    isColoring = pluginSettingsValues.isColoring();
  }

  private void normalizeContextPath() {
    String path = request.getContextPath();
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
    String[] popupOrInlineValue = request.getParameterValues("popupOrInline");
    String[] startTimeValue = request.getParameterValues("startTime");
    String[] endTimeValue = request.getParameterValues("endTime");

    if ("popup".equals(popupOrInlineValue[0])) {
      isPopup = JiraTimetrackerUtil.POPUP_CALENDAR_CODE;
    } else if ("inline".equals(popupOrInlineValue[0])) {
      isPopup = JiraTimetrackerUtil.INLINE_CALENDAR_CODE;
    } else {
      isPopup = JiraTimetrackerUtil.BOTH_TYPE_CALENDAR_CODE;
    }
    String[] currentOrLastValue = request.getParameterValues("currentOrLast");
    isActualDate = "current".equals(currentOrLastValue[0]);

    String[] isColoringValue = request.getParameterValues("isColoring");
    isColoring = (isColoringValue != null);

    try {
      if (jiraTimetrackerPlugin.validateTimeChange(startTimeValue[0])) {
        startTime = startTimeValue[0];
      } else {
        message = "plugin.setting.start.time.change.wrong";
      }
    } catch (NumberFormatException e) {
      message = "plugin.settings.time.format";
      messageParameter = startTimeValue[0];
    }
    try {
      if (jiraTimetrackerPlugin.validateTimeChange(endTimeValue[0])) {
        endTime = endTimeValue[0];
      } else {
        message = "plugin.setting.end.time.change.wrong";
      }
    } catch (NumberFormatException e) {
      message = "plugin.settings.time.format";
      messageParameter = endTimeValue[0];
    }
    if (!"".equals(message)) {
      return SUCCESS;
    }
    return null;
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    PluginSettingsValues pluginSettingValues = new PluginSettingsValues(
        new CalendarSettingsValues(isPopup, isActualDate, excludeDates,
            includeDates, isColoring),
        issuesPatterns,
        collectorIssuePatterns, Integer.parseInt(startTime),
        Integer.parseInt(endTime));
    jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
  }

  public void setColoring(final boolean isColoring) {
    this.isColoring = isColoring;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
    isActualDate = actualDateOrLastWorklogDate;
  }

  public void setIsPopup(final int isPopup) {
    this.isPopup = isPopup;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMessageParameter(final String messageParameter) {
    this.messageParameter = messageParameter;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

}
