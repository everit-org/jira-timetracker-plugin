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

import javax.servlet.http.HttpServletRequest;

import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.dto.ReportingSettingsValues;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Admin settings page.
 */
public class ReportingAdminSettingsWebAction extends JiraWebActionSupport {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private static final String FREQUENT_FEEDBACK = "jttp.plugin.frequent.feedback";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  private static final String NOT_RATED = "Not rated";

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

  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * The message.
   */
  private String message = "";

  private List<String> reportingGroups;

  private ReportingPlugin reportingPlugin;

  public ReportingAdminSettingsWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin, final ReportingPlugin reportingPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.reportingPlugin = reportingPlugin;
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
    checkMailServer();

    loadPluginSettingAndParseResult();

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
    checkMailServer();

    loadPluginSettingAndParseResult();

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

  public String getMessage() {
    return message;
  }

  public List<String> getReportingGroups() {
    return reportingGroups;
  }

  /**
   * Load the plugin settings and set the variables.
   */
  public void loadPluginSettingAndParseResult() {
    ReportingSettingsValues pluginSettingsValues = reportingPlugin
        .loadReportingSettings();
    reportingGroups = pluginSettingsValues.reportingGroups;
    browseGroups = pluginSettingsValues.browseGroups;
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
      browseGroups = new ArrayList<String>();
    } else {
      browseGroups = Arrays.asList(browseGroupsValue);
    }
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

  private void parseReportingGroups(final String[] reportingGroupsValue) {
    if (reportingGroupsValue == null) {
      reportingGroups = new ArrayList<String>();
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
    String pageSizeValue = request.getParameter("pageSizeInput");
    parseReportingGroups(reportingGroupSelectValue);
    parseBrowseGroups(browseGroupSelectValue);
    return null;
  }

  /**
   * Save the plugin settings.
   */
  public void savePluginSettings() {
    ReportingSettingsValues reportingSettingsValues =
        new ReportingSettingsValues().reportingGroups(reportingGroups).browseGroups(browseGroups);
    reportingPlugin.saveReportingSettings(reportingSettingsValues);
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
