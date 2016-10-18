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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.NoEstimateUsageChangedEvent;
import org.everit.jira.analytics.event.NonWorkingUsageEvent;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class AdminSettingsWebAction extends JiraWebActionSupport {

  private static final String FREQUENT_FEEDBACK = "jttp.plugin.frequent.feedback";

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

  private transient AnalyticsSender analyticsSender;

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
   * The exclude dates in String format.
   */
  private String excludeDates = "";

  private boolean feedBackSendAviable;

  /**
   * The include dates in String format.
   */
  private String includeDates = "";

  private String issueCollectorSrc;

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

  private TimeTrackerGlobalSettings loadGlobalSettings;

  /**
   * The message.
   */
  private String message = "";

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

  private List<String> pluginGroups;

  private String pluginId;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  private TimetrackerSettingsHelper settingsHelper;

  private List<String> timetrackerGroups;

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param analyticsSender
   *          The {@link AnalyticsSender}.
   */
  public AdminSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin, final AnalyticsSender analyticsSender,
      final TimetrackerSettingsHelper settingsHelper) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
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
    loadIssueCollectorSrc();
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
    loadIssueCollectorSrc();
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
      String feedbacktResult = parseFeedback();
      if (feedbacktResult != null) {
        return feedbacktResult;
      }
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

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getMessage() {
    return message;
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

  public List<String> getPluginGroups() {
    return pluginGroups;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public List<String> getTimetrackerGroups() {
    return timetrackerGroups;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    loadGlobalSettings = settingsHelper.loadGlobalSettings();
    pluginId = loadGlobalSettings.getPluginUUID();
    issuesPatterns = loadGlobalSettings.getNonWorkingIssuePatterns();
    for (Pattern issueId : issuesPatterns) {
      issueKey += issueId.toString() + " ";
    }
    collectorIssuePatterns = loadGlobalSettings.getIssuePatterns();
    for (Pattern issuePattern : collectorIssuePatterns) {
      collectorIssueKey += issuePattern.toString() + " ";
    }
    excludeDates = loadGlobalSettings.getExcludeDatesAsString();
    includeDates = loadGlobalSettings.getIncludeDatesAsString();
    analyticsCheck = loadGlobalSettings.getAnalyticsCheck();
    pluginGroups = loadGlobalSettings.getPluginGroups();
    timetrackerGroups = loadGlobalSettings.getTimetrackerGroups();
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
      String validExvcludeDates = "";
      if (!excludeDatesValueString.isEmpty()) {
        excludeDatesValueString = excludeDatesValueString
            .replace(" ", "").replace("\r", "").replace("\n", "");
        for (String dateString : excludeDatesValueString.split(",")) {
          try {
            Date validDate = DateTimeConverterUtil.fixFormatStringToDate(dateString);
            String dateToFixFormatString = DateTimeConverterUtil.dateToFixFormatString(validDate);
            validExvcludeDates += dateToFixFormatString + ", ";
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
      excludeDates = validExvcludeDates;
    }
    return parseExcludeException;
  }

  private String parseFeedback() {
    if (JiraTimetrackerUtil.loadAndCheckFeedBackTimeStampFromSession(getHttpSession())) {
      String feedBackValue = getHttpRequest().getParameter("feedbackinput");
      String ratingValue = getHttpRequest().getParameter("rating");
      String customerMail =
          JiraTimetrackerUtil.getCheckCustomerMail(getHttpRequest().getParameter("customerMail"));
      String feedBack = "";
      String rating = NOT_RATED;
      if (feedBackValue != null) {
        feedBack = feedBackValue.trim();
      }
      if (ratingValue != null) {
        rating = ratingValue;
      }
      String mailSubject = JiraTimetrackerUtil
          .createFeedbackMailSubject(JiraTimetrackerAnalytics.getPluginVersion());
      String mailBody =
          JiraTimetrackerUtil.createFeedbackMailBody(customerMail, rating, feedBack);
      jiraTimetrackerPlugin.sendEmail(mailSubject, mailBody);
      JiraTimetrackerUtil.saveFeedBackTimeStampToSession(getHttpSession());
    } else {
      message = FREQUENT_FEEDBACK;
      return SUCCESS;
    }
    return null;
  }

  private boolean parseIncludeDatesValue(final String includeDatesValue) {
    boolean parseIncludeDateException = false;
    if (includeDatesValue == null) {
      includeDates = "";
    } else {
      String includeDatesValueString = includeDatesValue;
      String validIncludeDates = "";
      if (!includeDatesValueString.isEmpty()) {
        includeDatesValueString = includeDatesValueString
            .replace(" ", "").replace("\r", "").replace("\n", "");
        for (String dateString : includeDatesValueString.split(",")) {
          try {
            Date validDate = DateTimeConverterUtil.fixFormatStringToDate(dateString);
            String dateToFixFormatString = DateTimeConverterUtil.dateToFixFormatString(validDate);
            validIncludeDates += dateToFixFormatString + ", ";
          } catch (ParseException e) {
            parseIncludeDateException = true;
            messageInclude = "plugin.parse.exception.include";
            if (messageParameterInclude.isEmpty()) {
              messageParameterInclude += dateString;
            } else {
              messageParameterInclude += ", " + dateString;
            }
          }
        }
      }
      includeDates = validIncludeDates;
    }
    return parseIncludeDateException;
  }

  private void parsePluginGroups(final String[] pluginGroupsvalue) {
    if (pluginGroupsvalue == null) {
      pluginGroups = new ArrayList<>();
    } else {
      pluginGroups = Arrays.asList(pluginGroupsvalue);
    }
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
    String[] pluginGroupsvalue = request.getParameterValues("pluginGroupSelect");
    String[] timetrackerGroupsValue = request.getParameterValues("timetrackerGroupSelect");
    parsePluginGroups(pluginGroupsvalue);
    parseTimetrackerGroups(timetrackerGroupsValue);

    if ((analyticsCheckValue != null) && "enable".equals(analyticsCheckValue)) {
      analyticsCheck = true;
    } else {
      analyticsCheck = false;
    }

    issuesPatterns = new ArrayList<>();
    if (issueSelectValue != null) {
      for (String filteredIssueKey : issueSelectValue) {
        issuesPatterns.add(Pattern.compile(filteredIssueKey));
      }
    }

    collectorIssuePatterns = new ArrayList<>();
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

  private void parseTimetrackerGroups(final String[] timetrackerGroupsValue) {
    if (timetrackerGroupsValue == null) {
      timetrackerGroups = new ArrayList<>();
    } else {
      timetrackerGroups = Arrays.asList(timetrackerGroupsValue);
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
  public void savePluginSettings() {
    TimeTrackerGlobalSettings globalSettings = new TimeTrackerGlobalSettings();
    globalSettings.excludeDates(excludeDates)
        .includeDates(includeDates)
        .filteredSummaryIssues(issuesPatterns)
        .collectorIssues(collectorIssuePatterns)
        .analyticsCheck(analyticsCheck)
        .pluginGroups(pluginGroups)
        .timetrackerGroups(timetrackerGroups);
    settingsHelper.saveGlobalSettings(globalSettings);
    sendNonEstAndNonWorkAnaliticsEvent();
  }

  private void sendNonEstAndNonWorkAnaliticsEvent() {
    NoEstimateUsageChangedEvent analyticsEvent =
        new NoEstimateUsageChangedEvent(pluginId, collectorIssuePatterns);
    analyticsSender.send(analyticsEvent);
    NonWorkingUsageEvent nonWorkingUsageEvent =
        new NonWorkingUsageEvent(pluginId, (issuesPatterns == null) || issuesPatterns.isEmpty());
    analyticsSender.send(nonWorkingUsageEvent);
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

  public void setMessage(final String message) {
    this.message = message;
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

  public void setPluginGroups(final List<String> pluginGroups) {
    this.pluginGroups = pluginGroups;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setTimetrackerGroups(final List<String> timetrackerGroups) {
    this.timetrackerGroups = timetrackerGroups;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
