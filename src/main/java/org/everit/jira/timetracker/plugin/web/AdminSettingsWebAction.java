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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.NoEstimateUsageChangedEvent;
import org.everit.jira.analytics.event.NonWorkingUsageEvent;
import org.everit.jira.core.SupportManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeZoneTypes;
import org.everit.jira.timetracker.plugin.util.ExceptionUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class AdminSettingsWebAction extends JiraWebActionSupport {

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String PLUGIN_NONESTIMATED_EMPTY_VALUE = "plugin.nonestimated.empty.value";

    public static final String PLUGIN_SETTING_DATE_EXITST_INCLUDE_EXCLUDE =
        "plugin.setting.date.exitst.include.exclude";

  }

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AdminSettingsWebAction.class);

  private static final String NON_EST_ALL = "nonEstAll";

  private static final String NON_EST_NONE = "nonEstNone";

  private static final String NON_EST_SELECTED = "nonEstSelected";

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
   * The exclude dates in UNIX long format. Sorted by natural order.
   */
  private Set<Long> excludeDates = new TreeSet<>();

  /**
   * The include dates in UNIX long format. Sorted by natural order.
   */
  private Set<Long> includeDates = new TreeSet<>();

  private String issueCollectorSrc;

  /**
   * The issue key.
   */
  private String issueKey = "";

  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesPatterns;

  private TimeTrackerGlobalSettings loadGlobalSettings;

  /**
   * The message.
   */
  private String message = "";

  private String pluginId;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  private TimeTrackerSettingsHelper settingsHelper;

  private String stacktrace = "";

  private SupportManager supportManager;

  private TimeZoneTypes timeZoneType;

  /**
   * Simple constructor.
   */
  public AdminSettingsWebAction(
      final SupportManager supportManager, final AnalyticsSender analyticsSender,
      final TimeTrackerSettingsHelper settingsHelper) {
    this.supportManager = supportManager;
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if ("doExecute".equals(getHttpRequest().getParameter("action"))) {
      return doExecute();
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    loadPluginSettingAndParseResult();

    projectsId = supportManager.getProjectsId();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    loadPluginSettingAndParseResult();
    try {
      projectsId = supportManager.getProjectsId();
    } catch (Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    if (getHttpRequest().getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(getHttpRequest());
      if (parseResult != null) {
        setReturnUrl("/secure/admin/TimetrackerAdminSettingsWebAction!default.jspa");
        return parseResult;
      }
      savePluginSettings();
    }
    setReturnUrl("/secure/admin/TimetrackerAdminSettingsWebAction!default.jspa");
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

  public Set<Long> getExcludeDates() {
    return excludeDates;
  }

  public Set<Long> getIncludeDates() {
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

  /**
   * Decide which check box is selected.
   */
  public String getNonEstSelect() {
    if (collectorIssuePatterns.isEmpty()) {
      return NON_EST_ALL;
    } else if ((collectorIssuePatterns.size() == 1)
        && collectorIssuePatterns.get(0).pattern().equals(".*")) {
      return NON_EST_NONE;
    } else {
      return NON_EST_SELECTED;
    }
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public TimeZoneTypes getTimeZoneType() {
    return timeZoneType;
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
    excludeDates = loadGlobalSettings.getExcludeDatesAsLong();
    includeDates = loadGlobalSettings.getIncludeDatesAsLong();
    analyticsCheck = loadGlobalSettings.getAnalyticsCheck();
    timeZoneType = loadGlobalSettings.getTimeZone();
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private Set<Long> parseDates(final String[] excludeDatesValue) {
    Set<Long> tempDates = new TreeSet<>();
    if (excludeDatesValue == null) {
      return tempDates;
    }
    for (String string : excludeDatesValue) {
      try {
        tempDates.add(Long.valueOf(string));
      } catch (NumberFormatException e) {
        LOGGER.warn("Failed to parse long to Date " + string);
      }
    }
    return tempDates;
  }

  private boolean parseNonEstValues(final HttpServletRequest request) {
    String nonEstSelectValue = request.getParameter("nonEstSelect");
    if (nonEstSelectValue.equals(NON_EST_SELECTED)) {
      String[] collectorIssueSelectValue = request.getParameterValues("issueSelect_collector");
      if ((collectorIssueSelectValue != null) && (collectorIssueSelectValue.length != 0)) {
        for (String filteredIssueKey : collectorIssueSelectValue) {
          collectorIssuePatterns.add(Pattern.compile(filteredIssueKey));
        }
      } else {
        message = PropertiesKey.PLUGIN_NONESTIMATED_EMPTY_VALUE;
        return true;
      }
    } else if (nonEstSelectValue.equals(NON_EST_NONE)) {
      collectorIssuePatterns.add(Pattern.compile(".*"));
    }
    return false;
  }

  /**
   * Parse the request after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String[] issueSelectValue = request.getParameterValues("issueSelect");
    String[] excludeDatesValue = request.getParameterValues("excludedates");
    String[] includeDatesValue = request.getParameterValues("includedates");
    String analyticsCheckValue = request.getParameter("analyticsCheck");
    String timeZoneValue = request.getParameter("selectTimeZone");

    if ((analyticsCheckValue != null) && "enable".equals(analyticsCheckValue)) {
      analyticsCheck = true;
    } else {
      analyticsCheck = false;
    }

    if ((timeZoneValue != null) && "selectUserTimeZone".equals(timeZoneValue)) {
      timeZoneType = TimeZoneTypes.USER;
    } else {
      timeZoneType = TimeZoneTypes.SYSTEM;
    }

    issuesPatterns = new ArrayList<>();
    if (issueSelectValue != null) {
      for (String filteredIssueKey : issueSelectValue) {
        issuesPatterns.add(Pattern.compile(filteredIssueKey));
      }
    }
    collectorIssuePatterns = new ArrayList<>();
    boolean parseNonEstException = parseNonEstValues(request);

    excludeDates = parseDates(excludeDatesValue);
    includeDates = parseDates(includeDatesValue);
    HashSet<Long> interSect = new HashSet<>(excludeDates);
    interSect.retainAll(includeDates);
    if (!interSect.isEmpty()) {
      message = PropertiesKey.PLUGIN_SETTING_DATE_EXITST_INCLUDE_EXCLUDE;
      return SUCCESS;
    }
    if (parseNonEstException) {
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
    TimeTrackerGlobalSettings globalSettings = new TimeTrackerGlobalSettings();
    globalSettings.excludeDates(excludeDates)
        .includeDates(includeDates)
        .filteredSummaryIssues(issuesPatterns)
        .collectorIssues(collectorIssuePatterns)
        .analyticsCheck(analyticsCheck)
        .timeZone(timeZoneType);
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

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
