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
import java.util.Calendar;
import java.util.Date;

import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Repoting page web action.
 */
public class ReportingWebAction extends JiraWebActionSupport {
  private static final String INVALID_END_TIME = "plugin.invalid_endTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  private static final String PARAM_DATEFROM = "dateFrom";

  private static final String PARAM_DATETO = "dateTo";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String WRONG_DATES = "plugin.wrong.dates";

  /**
   * The first day of the week.
   */
  private String contextPath;

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
    initDatesIfNecessary();

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

    Calendar startDate = null;
    Calendar lastDate = null;
    try {
      startDate = parseDateFrom();
      lastDate = parseDateTo();
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
      return INPUT;
    }

    if (startDate.after(lastDate)) {
      message = WRONG_DATES;
      return INPUT;
    }

    return SUCCESS;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Date getDateFrom() {
    return (Date) dateFrom.clone();
  }

  public String getDateFromFormated() {
    return dateFromFormated;
  }

  public Date getDateTo() {
    return (Date) dateTo.clone();
  }

  public String getDateToFormated() {
    return dateToFormated;
  }

  public String getMessage() {
    return message;
  }

  private void initDatesIfNecessary() {
    if ("".equals(dateFromFormated)) {
      Calendar calendarFrom = Calendar.getInstance();
      calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
      dateFrom = calendarFrom.getTime();
      dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
    }
    if ("".equals(dateToFormated)) {
      Calendar calendarTo = Calendar.getInstance();
      dateTo = calendarTo.getTime();
      dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
    }
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private Calendar parseDateFrom() throws IllegalArgumentException {
    String dateFromParam = getHttpRequest().getParameter(PARAM_DATEFROM);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFromFormated = dateFromParam;
    } else {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    Calendar parsedCalendarFrom = Calendar.getInstance();
    try {
      dateFrom = DateTimeConverterUtil.stringToDate(dateFromParam);
      parsedCalendarFrom.setTime(dateFrom);
    } catch (ParseException e) {
      throw new IllegalArgumentException(INVALID_START_TIME);
    }
    return parsedCalendarFrom;
  }

  private Calendar parseDateTo() throws IllegalArgumentException {
    String dateToParam = getHttpRequest().getParameter(PARAM_DATETO);
    if ((dateToParam != null) && !"".equals(dateToParam)) {
      dateToFormated = dateToParam;
    } else {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    Calendar parsedCalendarTo = Calendar.getInstance();
    try {
      dateTo = DateTimeConverterUtil.stringToDate(dateToParam);
      parsedCalendarTo.setTime(dateTo);
    } catch (ParseException e) {
      throw new IllegalArgumentException(INVALID_END_TIME);
    }
    return parsedCalendarTo;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setDateFrom(final Date dateFrom) {
    this.dateFrom = dateFrom;
  }

  public void setDateFromFormated(final String dateFromFormated) {
    this.dateFromFormated = dateFromFormated;
  }

  public void setDateTo(final Date dateTo) {
    this.dateTo = dateTo;
  }

  public void setDateToFormated(final String dateToFormated) {
    this.dateToFormated = dateToFormated;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

}
