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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class AdminSettingsWebAction extends JiraWebActionSupport {

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AdminSettingsWebAction.class);

  private static final String NOT_RATED = "Not rated";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Check if the analytics is disable or enable.
   */
  private boolean analyticsCheck;
  /**
   * The collector issue key.
   */
  private String collectorIssueKey = "";

  /**
   * The collector issue ids.
   */
  private List<Pattern> collectorIssuePatterns;

  /**
   * The first day of the week.
   */
  private String contextPath;
  /**
   * The pluginSetting endTime value.
   */
  private int endTime;
  /**
   * The exclude dates in String format.
   */
  private String excludeDates = "";

  private boolean feedBackSendAviable;
  /**
   * The include dates in String format.
   */
  private String includeDates = "";
  /**
   * The calenar show the actualDate or the last unfilled date.
   */
  private boolean isActualDate;
  /**
   * The pluginSetting isColoring value.
   */
  private boolean isColoring;
  /**
   * The calendar is popup, inLine or both.
   */
  private int isPopup;
  /**
   * The issue key.
   */
  private String issueKey = "";
  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesPatterns;
  /**
   * The {@link JiraTimetrackerPlugin}.
   */

  private JiraTimetrackerPlugin jiraTimetrackerPlugin;
  /**
   * The settings page message parameter.
   */
  private String messageExclude = "";
  /**
   * The settings page message parameter.
   */
  private String messageInclude = "";
  /**
   * The paramater of the message.
   */
  private String messageParameterExclude = "";
  /**
   * The paramater of the message.
   */
  private String messageParameterInclude = "";

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  /**
   * The pluginSetting startTime value.
   */
  private int startTime;

  public AdminSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    checkMailServer();
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
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    checkMailServer();
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }

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
      String mailSubject = JiraTimetrackerUtil
          .createFeedbackMailSubject(JiraTimetrackerAnalytics.getPluginVersion());
      String mailBody = JiraTimetrackerUtil.createFeedbackMailBody(customerMail, rating, feedBack);
      jiraTimetrackerPlugin.sendEmail(mailSubject, mailBody);
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
    setReturnUrl("/secure/admin/JiraTimetrackerAdminSettingsWebAction!default.jspa");
    return getRedirect(INPUT);
  }

  public boolean getAnalyticsCheck() {
    return analyticsCheck;
  }

  public String getCollectorIssueKey() {
    return collectorIssueKey;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getExcludeDates() {
    return excludeDates;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  public String getIncludeDates() {
    return includeDates;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getMessageExclude() {
    return messageExclude;
  }

  public String getMessageInclude() {
    return messageInclude;
  }

  public String getMessageParameterExclude() {
    return messageParameterExclude;
  }

  public String getMessageParameterInclude() {
    return messageParameterInclude;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    isPopup = pluginSettingsValues.isCalendarPopup;
    isActualDate = pluginSettingsValues.isActualDate;
    startTime = pluginSettingsValues.startTimeChange;
    endTime = pluginSettingsValues.endTimeChange;
    isColoring = pluginSettingsValues.isColoring;
    issuesPatterns = pluginSettingsValues.filteredSummaryIssues;
    for (Pattern issueId : issuesPatterns) {
      issueKey += issueId.toString() + " ";
    }
    collectorIssuePatterns = pluginSettingsValues.collectorIssues;
    for (Pattern issuePattern : collectorIssuePatterns) {
      collectorIssueKey += issuePattern.toString() + " ";
    }
    excludeDates = pluginSettingsValues.excludeDates;
    includeDates = pluginSettingsValues.includeDates;
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

  private boolean parseExcludeDatesValue(final String excludeDatesValue) {
    boolean parseExcludeException = false;
    if (excludeDatesValue == null) {
      excludeDates = "";
    } else {
      String excludeDatesValueString = excludeDatesValue;
      if (!excludeDatesValueString.isEmpty()) {
        excludeDatesValueString = excludeDatesValueString
            .replace(" ", "").replace("\r", "").replace("\n", "");
        for (String dateString : excludeDatesValueString.split(",")) {
          try {
            DateTimeConverterUtil.stringToDate(dateString);
          } catch (ParseException e) {
            parseExcludeException = true;
            messageExclude = "plugin.parse.exception.exclude";
            if (messageParameterExclude.isEmpty()) {
              messageParameterExclude += dateString;
            } else {
              messageParameterExclude += ", " + dateString;
            }
          }
        }
      }
      excludeDates = excludeDatesValueString;
    }
    return parseExcludeException;
  }

  private boolean parseIncludeDatesValue(final String includeDatesValue) {
    boolean parseExcludeException = false;
    if (includeDatesValue == null) {
      includeDates = "";
    } else {
      String excludeDatesValueString = includeDatesValue;
      if (!excludeDatesValueString.isEmpty()) {
        excludeDatesValueString = excludeDatesValueString
            .replace(" ", "").replace("\r", "").replace("\n", "");
        for (String dateString : excludeDatesValueString.split(",")) {
          try {
            DateTimeConverterUtil.stringToDate(dateString);
          } catch (ParseException e) {
            parseExcludeException = true;
            messageExclude = "plugin.parse.exception.exclude";
            if (messageParameterExclude.isEmpty()) {
              messageParameterExclude += dateString;
            } else {
              messageParameterExclude += ", " + dateString;
            }
          }
        }
      }
      includeDates = excludeDatesValueString;
    }
    return parseExcludeException;
  }

  /**
   * Parse the request after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String[] issueSelectValue = request.getParameterValues("issueSelect");
    String[] collectorIssueSelectValue = request.getParameterValues("issueSelect_collector");
    String excludeDatesValue = request.getParameter("excludedates");
    String includeDatesValue = request.getParameter("includedates");
    String analyticsCheckValue = request.getParameter("analyticsCheck");

    if ((analyticsCheckValue != null) && "enable".equals(analyticsCheckValue)) {
      analyticsCheck = true;
    } else {
      analyticsCheck = false;
    }

    issuesPatterns = new ArrayList<Pattern>();
    if (issueSelectValue != null) {
      for (String filteredIssueKey : issueSelectValue) {
        issuesPatterns.add(Pattern.compile(filteredIssueKey));
      }
    }

    collectorIssuePatterns = new ArrayList<Pattern>();
    if (collectorIssueSelectValue != null) {
      for (String filteredIssueKey : collectorIssueSelectValue) {
        collectorIssuePatterns.add(Pattern.compile(filteredIssueKey));
      }
    }

    // Handle exclude and include date in the parse method end.
    boolean parseExcludeException = parseExcludeDatesValue(excludeDatesValue);
    boolean parseIncludeException = parseIncludeDatesValue(includeDatesValue);

    if (parseExcludeException || parseIncludeException) {
      return SUCCESS;
    }
    return null;
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    PluginSettingsValues pluginSettingValues = new PluginSettingsValues()
        .isCalendarPopup(isPopup).actualDate(isActualDate).excludeDates(excludeDates)
        .includeDates(includeDates).coloring(isColoring).filteredSummaryIssues(issuesPatterns)
        .collectorIssues(collectorIssuePatterns).startTimeChange(startTime).endTimeChange(endTime)
        .analyticsCheck(analyticsCheck);

    jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
  }

  public void setAnalyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
  }

  public void setCollectorIssueKey(final String collectorIssueKey) {
    this.collectorIssueKey = collectorIssueKey;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setExcludeDates(final String excludeDates) {
    this.excludeDates = excludeDates;
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  public void setIncludeDates(final String includeDates) {
    this.includeDates = includeDates;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setMessageExclude(final String messageExclude) {
    this.messageExclude = messageExclude;
  }

  public void setMessageInclude(final String messageInclude) {
    this.messageInclude = messageInclude;
  }

  public void setMessageParameterExclude(final String messageParameterExclude) {
    this.messageParameterExclude = messageParameterExclude;
  }

  public void setMessageParameterInclude(final String messageParameterInclude) {
    this.messageParameterInclude = messageParameterInclude;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }
}
