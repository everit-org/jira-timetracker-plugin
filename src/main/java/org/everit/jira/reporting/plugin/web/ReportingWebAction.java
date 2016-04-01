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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
   * The created picker date.
   */
  private Date dateCreated = null;

  /**
   * The created date formated.
   */
  private String dateCreatedFormated = "";

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

  private List<String> selectedAffectedVersions;

  private List<String> selectedAssignes;

  private List<String> selectedComponents;

  private String selectedEpicName = "";

  private List<String> selectedFixVersions;

  private List<String> selectedGroups;

  private List<String> selectedLabels;

  private List<String> selectedPriorities;

  private List<String> selectedProjects;

  private List<String> selectedReportes;

  private List<String> selectedResolutions;

  private List<String> selectedStatus;

  private List<String> selectedTypes;

  private List<String> selectedUsers;

  public ReportingWebAction(final ReportingPlugin reportingPlugin) {
    this.reportingPlugin = reportingPlugin;
  }

  private void affectedVersionPickerParse() {
    String[] selectedAffectedVersionsValues =
        getHttpRequest().getParameterValues("affectedVersionPicker");
    if (selectedAffectedVersionsValues != null) {
      selectedAffectedVersions = Arrays.asList(selectedAffectedVersionsValues);
    } else {
      selectedAffectedVersions = new ArrayList<String>();
    }
  }

  private void assignePickerParse() {
    String[] selectedAssignesValues = getHttpRequest().getParameterValues("assignePicker");
    if (selectedAssignesValues != null) {
      selectedAssignes = Arrays.asList(selectedAssignesValues);
    } else {
      selectedAssignes = new ArrayList<String>();
    }
  }

  private void componentsPickerParse() {
    String[] selectedComponentsValues =
        getHttpRequest().getParameterValues("componentPicker");
    if (selectedComponentsValues != null) {
      selectedComponents = Arrays.asList(selectedComponentsValues);
    } else {
      selectedComponents = new ArrayList<String>();
    }
  }

  private void createdDatePickerParse() throws IllegalArgumentException {
    String createdPateParam = getHttpRequest().getParameter("createdPicker");
    if ((createdPateParam != null) && !"".equals(createdPateParam)) {
      dateCreatedFormated = createdPateParam;
      try {
        dateCreated = DateTimeConverterUtil.stringToDate(createdPateParam);
      } catch (ParseException e) {
        throw new IllegalArgumentException(INVALID_START_TIME);
      }
    } else {
      dateCreatedFormated = "";
      dateCreated = null;
    }
  }

  @Override
  public String doDefault() throws ParseException {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    // TODO ReportingCondition check!
    initPickersValues();
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
      pickersParsers();
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

  private void epicNamePickerParse() throws IllegalArgumentException {
    String epicNamePickerParam = getHttpRequest().getParameter("epicNamePicker");
    if (epicNamePickerParam != null) {
      selectedEpicName = epicNamePickerParam;
    } else {
      selectedEpicName = "";
    }
  }

  private void fixVersionPickerParse() {
    String[] selectedFixVersionsValues =
        getHttpRequest().getParameterValues("fixVersionPicker");
    if (selectedFixVersionsValues != null) {
      selectedFixVersions = Arrays.asList(selectedFixVersionsValues);
    } else {
      selectedFixVersions = new ArrayList<String>();
    }
  }

  public String getContextPath() {
    return contextPath;
  }

  public Date getDateCreated() {
    return (Date) dateCreated.clone();
  }

  public String getDateCreatedFormated() {
    return dateCreatedFormated;
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

  public List<String> getSelectedAffectedVersions() {
    return selectedAffectedVersions;
  }

  public List<String> getSelectedAssignes() {
    return selectedAssignes;
  }

  public List<String> getSelectedComponents() {
    return selectedComponents;
  }

  public String getSelectedEpicName() {
    return selectedEpicName;
  }

  public List<String> getSelectedFixVersions() {
    return selectedFixVersions;
  }

  public List<String> getSelectedGroups() {
    return selectedGroups;
  }

  public List<String> getSelectedLabels() {
    return selectedLabels;
  }

  public List<String> getSelectedPriorities() {
    return selectedPriorities;
  }

  public List<String> getSelectedProjects() {
    return selectedProjects;
  }

  public List<String> getSelectedReportes() {
    return selectedReportes;
  }

  public List<String> getSelectedResolutions() {
    return selectedResolutions;
  }

  public List<String> getSelectedStatus() {
    return selectedStatus;
  }

  public List<String> getSelectedTypes() {
    return selectedTypes;
  }

  public List<String> getSelectedUsers() {
    return selectedUsers;
  }

  private void groupPickerParse() {
    String[] selectedGroupsValues = getHttpRequest().getParameterValues("groupPicker");
    if (selectedGroupsValues != null) {
      selectedGroups = Arrays.asList(selectedGroupsValues);
    } else {
      selectedGroups = new ArrayList<String>();
    }
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

  private void initPickersValues() {
    selectedProjects = new ArrayList<String>();
    selectedStatus = new ArrayList<String>();
    selectedTypes = new ArrayList<String>();
    selectedPriorities = new ArrayList<String>();
    selectedResolutions = new ArrayList<String>();
    selectedGroups = new ArrayList<String>();
    selectedUsers = new ArrayList<String>();
    selectedAssignes = new ArrayList<String>();
    selectedReportes = new ArrayList<String>();
    selectedAffectedVersions = new ArrayList<String>();
    selectedFixVersions = new ArrayList<String>();
    selectedComponents = new ArrayList<String>();
    selectedLabels = new ArrayList<String>();
    selectedEpicName = "";

    dateCreatedFormated = "";
    dateCreated = null;
  }

  private void labelPickerParse() {
    String[] selectedLabelsValues =
        getHttpRequest().getParameterValues("labelPicker");
    if (selectedLabelsValues != null) {
      selectedLabels = Arrays.asList(selectedLabelsValues);
    } else {
      selectedLabels = new ArrayList<String>();
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

  private void pickersParsers() {
    projectPickerParse();
    statusPickerParse();
    typePickerParse();
    priorityPickerParse();
    resolutionPickerParse();
    groupPickerParse();
    userPickerParse();
    assignePickerParse();
    reporterPickerParse();
    affectedVersionPickerParse();
    fixVersionPickerParse();
    componentsPickerParse();
    labelPickerParse();
    createdDatePickerParse();
    epicNamePickerParse();
  }

  private void priorityPickerParse() throws IllegalArgumentException {
    String[] selectedPrioritiesValues = getHttpRequest().getParameterValues("priorityPicker");
    if (selectedPrioritiesValues != null) {
      selectedPriorities = Arrays.asList(selectedPrioritiesValues);
    } else {
      selectedPriorities = new ArrayList<String>();
    }
  }

  private void projectPickerParse() throws IllegalArgumentException {
    String[] selectedProjectsValues = getHttpRequest().getParameterValues("projectPicker");
    if (selectedProjectsValues != null) {
      selectedProjects = Arrays.asList(selectedProjectsValues);
    } else {
      selectedProjects = new ArrayList<String>();
    }
  }

  private void reporterPickerParse() {
    String[] selectedReportesValues = getHttpRequest().getParameterValues("reporterPicker");
    if (selectedReportesValues != null) {
      selectedReportes = Arrays.asList(selectedReportesValues);
    } else {
      selectedReportes = new ArrayList<String>();
    }
  }

  private void resolutionPickerParse() {
    String[] selectedResolutionsValues = getHttpRequest().getParameterValues("resolutionPicker");
    if (selectedResolutionsValues != null) {
      selectedResolutions = Arrays.asList(selectedResolutionsValues);
    } else {
      selectedResolutions = new ArrayList<String>();
    }
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setDateCreated(final Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public void setDateCreatedFormated(final String dateCreatedFormated) {
    this.dateCreatedFormated = dateCreatedFormated;
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

  public void setSelectedAffectedVersions(final List<String> selectedAffectedVersions) {
    this.selectedAffectedVersions = selectedAffectedVersions;
  }

  public void setSelectedAssignes(final List<String> selectedAssignes) {
    this.selectedAssignes = selectedAssignes;
  }

  public void setSelectedComponents(final List<String> selectedComponents) {
    this.selectedComponents = selectedComponents;
  }

  public void setSelectedEpicName(final String selectedEpicName) {
    this.selectedEpicName = selectedEpicName;
  }

  public void setSelectedFixVersions(final List<String> selectedFixVersions) {
    this.selectedFixVersions = selectedFixVersions;
  }

  public void setSelectedGroups(final List<String> selectedGroups) {
    this.selectedGroups = selectedGroups;
  }

  public void setSelectedLabels(final List<String> selectedLabels) {
    this.selectedLabels = selectedLabels;
  }

  public void setSelectedPriorities(final List<String> selectedPriorities) {
    this.selectedPriorities = selectedPriorities;
  }

  public void setSelectedProjects(final List<String> selectedProjects) {
    this.selectedProjects = selectedProjects;
  }

  public void setSelectedReportes(final List<String> selectedReportes) {
    this.selectedReportes = selectedReportes;
  }

  public void setSelectedResolutions(final List<String> selectedResolutions) {
    this.selectedResolutions = selectedResolutions;
  }

  public void setSelectedStatus(final List<String> selectedStatus) {
    this.selectedStatus = selectedStatus;
  }

  public void setSelectedTypes(final List<String> selectedTypes) {
    this.selectedTypes = selectedTypes;
  }

  public void setSelectedUsers(final List<String> selectedUsers) {
    this.selectedUsers = selectedUsers;
  }

  private void statusPickerParse() {
    String[] selectedStatusValues = getHttpRequest().getParameterValues("statusPicker");
    if (selectedStatusValues != null) {
      selectedStatus = Arrays.asList(selectedStatusValues);
    } else {
      selectedStatus = new ArrayList<String>();
    }
  }

  private void typePickerParse() {
    String[] typePickerValues = getHttpRequest().getParameterValues("typePicker");
    if (typePickerValues != null) {
      selectedTypes = Arrays.asList(typePickerValues);
    } else {
      selectedTypes = new ArrayList<String>();
    }
  }

  private void userPickerParse() {
    String[] selectedUsersValues = getHttpRequest().getParameterValues("userPicker");
    if (selectedUsersValues != null) {
      selectedUsers = Arrays.asList(selectedUsersValues);
    } else {
      selectedUsers = new ArrayList<String>();
    }
  }

}
