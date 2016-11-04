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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.ReportingGlobalSettings;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class ReportingAdminSettingsWebAction extends JiraWebActionSupport {

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private List<String> browseGroups;

  /**
   * The first day of the week.
   */
  private String contextPath;

  private boolean feedBackSendAviable;

  private String issueCollectorSrc;

  /**
   * The message.
   */
  private String message = "";

  private List<String> reportingGroups;

  private TimetrackerSettingsHelper settingsHelper;

  public ReportingAdminSettingsWebAction(
      final TimetrackerSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  private void checkMailServer() {
    feedBackSendAviable = ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined();
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    loadIssueCollectorSrc();
    normalizeContextPath();
    checkMailServer();

    loadPluginSettingAndParseResult();

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
    checkMailServer();

    loadPluginSettingAndParseResult();

    if (getHttpRequest().getParameter("savesettings") != null) {
      String parseResult = parseSaveSettings(getHttpRequest());
      if (parseResult != null) {
        return parseResult;
      }
      savePluginSettings();
      setReturnUrl("/secure/ReportingWebAction!default.jspa");
      return getRedirect(INPUT);
    }
    setReturnUrl("/secure/admin/JiraTimetrackerReportingSettingsWebAction!default.jspa");
    return getRedirect(INPUT);
  }

  public List<String> getBrowseGroups() {
    return browseGroups;
  }

  public String getContextPath() {
    return contextPath;
  }

  public boolean getFeedBackSendAviable() {
    return feedBackSendAviable;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getReportingGroups() {
    return reportingGroups;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    ReportingGlobalSettings loadReportingGlobalSettings =
        settingsHelper.loadReportingGlobalSettings();
    reportingGroups = loadReportingGlobalSettings.getReportingGroups();
    browseGroups = loadReportingGlobalSettings.getBrowseGroups();
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parseBrowseGroups(final String[] browseGroupsValue) {
    if (browseGroupsValue == null) {
      browseGroups = new ArrayList<>();
    } else {
      browseGroups = Arrays.asList(browseGroupsValue);
    }
  }

  private void parseReportingGroups(final String[] reportingGroupsValue) {
    if (reportingGroupsValue == null) {
      reportingGroups = new ArrayList<>();
    } else {
      reportingGroups = Arrays.asList(reportingGroupsValue);
    }
  }

  /**
   * Parse the request after the save button was clicked. Set the variables.
   *
   * @param request
   *          The HttpServletRequest.
   */
  public String parseSaveSettings(final HttpServletRequest request) {
    String[] reportingGroupSelectValue = request.getParameterValues("reportingGroupSelect");
    String[] browseGroupSelectValue = request.getParameterValues("browseGroupSelect");
    parseReportingGroups(reportingGroupSelectValue);
    parseBrowseGroups(browseGroupSelectValue);
    return null;
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    ReportingGlobalSettings reportingGlobalSettings =
        new ReportingGlobalSettings().browseGroups(browseGroups).reportingGroups(reportingGroups);
    settingsHelper.saveReportingGlobalSettings(reportingGlobalSettings);
  }

  public void setBrowseGroups(final List<String> browseGroups) {
    this.browseGroups = browseGroups;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setFeedBackSendAviable(final boolean feedBackSendAviable) {
    this.feedBackSendAviable = feedBackSendAviable;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setReportingGroups(final List<String> reportingGroups) {
    this.reportingGroups = reportingGroups;
  }
}
