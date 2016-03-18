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

import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The first day of the week.
   */
  private String contextPath;

  /**
   * The message.
   */
  private String message = "";

  private ReportingPlugin reportingPlugin;

  public ReportingWebAction(final ReportingPlugin reportingPlugin) {
    this.reportingPlugin = reportingPlugin;
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    // TODO ReportingCondition check!
    normalizeContextPath();

    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    // TODO ReportingCondition check!
    normalizeContextPath();
    // TODO delete this .getClass() call
    reportingPlugin.getClass();
    return SUCCESS;
  }

  public String getContextPath() {
    return contextPath;
  }

  public String getMessage() {
    return message;
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

}
