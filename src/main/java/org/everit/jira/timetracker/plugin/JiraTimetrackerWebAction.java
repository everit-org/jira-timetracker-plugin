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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The timetracker web action support class.
 */
public class JiraTimetrackerWebAction extends JiraWebActionSupport {

  /**
   * The default worklog ID.
   */
  private static final Long DEFAULT_WORKLOG_ID = Long.valueOf(0);

  private static final String INVALID_DURATION_TIME = "plugin.invalid_durationTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";

  /**
   * The JiraTimetrackerWebAction logger.
   */
  private static Logger log = Logger.getLogger(JiraTimetrackerWebAction.class);

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWebAction.class);

  private static final String MISSING_ISSUE = "plugin.missing_issue";

  private static final String PARAM_DATE = "date";

  private static final String PARAM_ISSUESELECT = "issueSelect";

  private static final String PARAM_STARTTIME = "startTime";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final String VERSION_SPLITTER = "\\.";

  private String pluginVersion;

  private String baseUrl;

  private String userId;

  private String avatarURL = "";

  /**
   * The worklog comment.
   */
  private String comment = "";

  /**
   * The worklog comment.
   */
  private String commentForActions = "";

  private String contextPath;

  /**
   * The copied worklog id.
   */
  private Long copiedWorklogId = DEFAULT_WORKLOG_ID;

  /**
   * The date.
   */
  private Date date = null;
  /**
   * The formated date.
   */
  private String dateFormated = "";

  /**
   * The summary of day.
   */
  private String dayFilteredSummary = "";

  /**
   * The summary of day.
   */
  private String daySummary = "";

  private String debugMessage = "";
  /**
   * The deleted worklog id.
   */
  private Long deletedWorklogId = DEFAULT_WORKLOG_ID;

  /**
   * The worklog duration.
   */
  private String durationTime = "";
  /**
   * The all edit worklogs ids.
   */
  private String editAllIds = "";

  /**
   * The edited worklog id.
   */
  private Long editedWorklogId = DEFAULT_WORKLOG_ID;

  /**
   * The worklog end time.
   */
  private String endTime = "";

  /**
   * The endTime input field changer buttons value.
   */
  private int endTimeChange;

  /**
   * List of the exclude days of the date variable current months.
   */
  private List<String> excludeDays = new ArrayList<String>();
  /**
   * The calendar show actual Date Or Last Worklog Date.
   */
  private boolean isActualDate;

  /**
   * The calendar highlights coloring function is active or not.
   */
  private boolean isColoring;

  private boolean isDurationSelected = false;

  /**
   * The WebAction is edit a worklog or not.
   */
  private boolean isEdit = false;

  /**
   * The WebAction is edit all worklog or not.
   */
  private boolean isEditAll = false;

  /**
   * The calendar isPopup.
   */
  private int isPopup;

  /**
   * The issue key.
   */
  private String issueKey = "";

  /**
   * The issues.
   */
  private transient List<Issue> issues = new ArrayList<Issue>();

  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesRegex;

  /**
   * The jira main version.
   */
  private int jiraMainVersion;
  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private transient JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * List of the logged days of the date variable current months.
   */
  private List<String> loggedDays = new ArrayList<String>();
  /**
   * The message.
   */
  private String message = "";
  /**
   * The message parameter.
   */
  private String messageParameter = "";
  /**
   * The summary of month.
   */
  private String monthFilteredSummary = "";
  /**
   * The summary of month.
   */
  private String monthSummary = "";
  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;
  /**
   * The selected User for get Worklogs.
   */
  private String selectedUser = "";
  /**
   * The worklog start time.
   */
  private String startTime = "";

  /**
   *
   */
  // private Date datePCalendar = new Date();
  /**
   * The startTime input field changer buttons value.
   */
  private int startTimeChange;

  /**
   * The spent time in Jira time format (1h 20m).
   */
  private String timeSpent = "";

  private transient ApplicationUser userPickerObject;
  /**
   * The summary of week.
   */
  private String weekFilteredSummary = "";

  /**
   * The summary of week.
   */
  private String weekSummary = "";

  /**
   * The worklogs.
   */
  private List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();

  /**
   * The ids of the woklogs.
   */
  private List<Long> worklogsIds = new ArrayList<Long>();

  /**
   * Simple constructor.
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   */
  public JiraTimetrackerWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  /**
   * Put the worklogs id into a array.
   *
   * @param worklogsParam
   *          The worklogs.
   * @return The array of the ids.
   */
  private List<Long> copyWorklogIdsToArray(final List<EveritWorklog> worklogsParam) {
    final List<Long> worklogIds = new ArrayList<Long>();
    for (final EveritWorklog worklog : worklogsParam) {
      worklogIds.add(worklog.getWorklogId());
    }
    log.warn("JTWA log: copyWorklogIdsToArray: worklogIds size: "
        + worklogIds.size());
    return worklogIds;
  }

  /**
   * Handle the date change.
   *
   * @throws ParseException
   *           When can't parse date.
   */
  public void dateSwitcherAction() throws ParseException {
    final String[] dayBackValue = getHttpRequest().getParameterValues("dayBack");
    final String[] dayNextValue = getHttpRequest().getParameterValues("dayNext");
    final String[] weekBackValue = getHttpRequest().getParameterValues("weekBack");
    final String[] weekNextValue = getHttpRequest().getParameterValues("weekNext");
    final String[] monthBackValue = getHttpRequest().getParameterValues("monthBack");
    final String[] monthNextVaule = getHttpRequest().getParameterValues("monthNext");

    final Calendar tempCal = Calendar.getInstance();
    date = DateTimeConverterUtil.stringToDate(dateFormated);
    tempCal.setTime(date);
    if (dayNextValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else if (dayBackValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else if (monthNextVaule != null) {
      tempCal.add(Calendar.MONTH, 1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else if (monthBackValue != null) {
      tempCal.add(Calendar.MONTH, -1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else if (weekNextValue != null) {
      tempCal.add(Calendar.WEEK_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else if (weekBackValue != null) {
      tempCal.add(Calendar.WEEK_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else {
      parseDateParam();
    }
  }

  @Override
  public String doDefault() throws ParseException {

    final boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    normalizeContextPath();

    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());

    getJiraVersionFromBuildUtilsInfo();

    loadPluginSettingAndParseResult();
    // Just the here have to use the plugin actualDateOrLastWorklogDate setting
    if (setDateAndDateFormated().equals(ERROR)) {
      return ERROR;
    }

    excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(dateFormated);
    try {
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(selectedUser, date);
    } catch (final GenericEntityException e1) {
      // Not return with error. Log the error and set a message to inform the user.
      // The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling", e1);
      message = "plugin.calendar.logged.coloring.fail";
    }
    if ((deletedWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(deletedWorklogId)) {
      final ActionResult deleteResult = jiraTimetrackerPlugin.deleteWorklog(deletedWorklogId);
      if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
        message = deleteResult.getMessage();
        return ERROR;
      }
    }
    try {
      projectsId = jiraTimetrackerPlugin.getProjectsId();
      loadWorklogsAndMakeSummary();
    } catch (final Exception e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
    endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    try {
      handleEditAllIdsAndEditedWorklogId();
    } catch (final ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }
    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    final boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }

    normalizeContextPath();

    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.setUserSessionBaseUrl(getHttpRequest().getSession());
    userId = JiraTimetrackerAnalytics.setUserSessionUserId(getHttpRequest().getSession());

    getJiraVersionFromBuildUtilsInfo();

    loadPluginSettingAndParseResult();

    message = "";
    messageParameter = "";

    setSelectedUserFromParam();
    dateSwitcherAction();

    try {
      excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(dateFormated);
      loadWorklogsAndMakeSummary();
      projectsId = jiraTimetrackerPlugin.getProjectsId();
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }

    setFieldsValue();
    // if not edit and not submit than just a simple date change
    final String result = handleDateChangeAction();
    if (result != null) {
      return result;
    }

    selectedUser = "";
    userPickerObject = null;
    // edit all save before the input fields validate
    if (getHttpRequest().getParameter("editallsave") != null) {
      return editAllAction();
    } else if (getHttpRequest().getParameter("edit") != null) {
      return editAction();
    }

    final String validateInputFieldsResult = validateInputFields();
    if (!validateInputFieldsResult.equals(SUCCESS)) {
      return INPUT;
    }

    final String[] startTimeValue = getHttpRequest().getParameterValues(PARAM_STARTTIME);

    final ActionResult createResult = jiraTimetrackerPlugin.createWorklog(
        issueKey, commentForActions, dateFormated, startTimeValue[0], timeSpent);
    if (createResult.getStatus() == ActionResultStatus.FAIL) {
      message = createResult.getMessage();
      messageParameter = createResult.getMessageParameter();
      return INPUT;
    }
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
      isDurationSelected = false;
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Edit the worklog and handle the problems.
   *
   * @return String which will be passed to the WebAction.
   */
  public String editAction() {
    final String[] startTimeValue = getHttpRequest().getParameterValues(PARAM_STARTTIME);
    startTime = startTimeValue[0];
    final String validateInputFieldsResult = validateInputFields();
    if (validateInputFieldsResult.equals(ERROR)) {
      isEdit = true;
      return ERROR;
    }
    final ActionResult updateResult = jiraTimetrackerPlugin.editWorklog(
        editedWorklogId, issueKey, commentForActions, dateFormated,
        startTimeValue[0], timeSpent);
    if (updateResult.getStatus() == ActionResultStatus.FAIL) {
      message = updateResult.getMessage();
      isEdit = true;
      return ERROR;
    }
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    editedWorklogId = DEFAULT_WORKLOG_ID;
    isDurationSelected = false;
    return SUCCESS;
  }

  /**
   * The edit all function save action. Save the worklogs in the given date. The worklogs come form
   * the editAllIds, the date from the dateFormated.
   *
   * @return SUCCESS if the save was success else FAIL.
   * @throws ParseException
   *           If cannot parse date or time.
   */
  public String editAllAction() throws ParseException {
    // parse the editAllIds
    final List<Long> editWorklogIds = new ArrayList<Long>();
    String editAllIdsCopy = editAllIds;
    editAllIdsCopy = editAllIdsCopy.replace("[", "");
    editAllIdsCopy = editAllIdsCopy.replace("]", "");
    editAllIdsCopy = editAllIdsCopy.replace(" ", "");
    final String[] editIds = editAllIdsCopy.split(",");
    for (final String editId : editIds) {
      editWorklogIds.add(Long.valueOf(editId));
    }
    // edit the worklogs!
    // TODO what if result is a fail?????? what if just one fail?
    // ActionResult editResult;
    for (final Long editWorklogId : editWorklogIds) {
      final EveritWorklog editWorklog = jiraTimetrackerPlugin
          .getWorklog(editWorklogId);
      // editResult =
      jiraTimetrackerPlugin.editWorklog(editWorklog
          .getWorklogId(), editWorklog.getIssue(), editWorklog
              .getBody(),
          dateFormated, editWorklog.getStartTime(),
          DateTimeConverterUtil.stringTimeToString(editWorklog
              .getDuration()));
    }
    // set editAllIds to default and list worklogs
    try {
      loadWorklogsAndMakeSummary();
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    editAllIds = "";
    return SUCCESS;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getComment() {
    return comment;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Long getCopiedWorklogId() {
    return copiedWorklogId;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public String getDateFormated() {
    return dateFormated;
  }

  public String getDayFilteredSummary() {
    return dayFilteredSummary;
  }

  public String getDaySummary() {
    return daySummary;
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public Long getDeletedWorklogId() {
    return deletedWorklogId;
  }

  public String getDurationTime() {
    return durationTime;
  }

  public String getEditAllIds() {
    return editAllIds;
  }

  public Long getEditedWorklogId() {
    return editedWorklogId;
  }

  public String getEndTime() {
    return endTime;
  }

  public int getEndTimeChange() {
    return endTimeChange;
  }

  public List<String> getExcludeDays() {
    return excludeDays;
  }

  public boolean getIsColoring() {
    return isColoring;
  }

  public boolean getIsDurationSelected() {
    return isDurationSelected;
  }

  public boolean getIsEdit() {
    return isEdit;
  }

  public boolean getIsEditAll() {
    return isEditAll;
  }

  public int getIsPopup() {
    return isPopup;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public List<Pattern> getIssuesRegex() {
    return issuesRegex;
  }

  public int getJiraMainVersion() {
    return jiraMainVersion;
  }

  public JiraTimetrackerPlugin getJiraTimetrackerPlugin() {
    return jiraTimetrackerPlugin;
  }

  private void getJiraVersionFromBuildUtilsInfo() {
    final BuildUtilsInfo component = ComponentAccessor.getComponent(BuildUtilsInfo.class);
    final String version = component.getVersion();
    final String[] versionSplit = version.split(VERSION_SPLITTER);
    jiraMainVersion = Integer.parseInt(versionSplit[0]);
  }

  public List<String> getLoggedDays() {
    return loggedDays;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageParameter() {
    return messageParameter;
  }

  public String getMonthFilteredSummary() {
    return monthFilteredSummary;
  }

  public String getMonthSummary() {
    return monthSummary;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getSelectedeUser() {
    return selectedUser;
  }

  public String getStartTime() {
    return startTime;
  }

  public int getStartTimeChange() {
    return startTimeChange;
  }

  public String getUserId() {
    return userId;
  }

  public ApplicationUser getUserPickerObject() {
    return userPickerObject;
  }

  public String getWeekFilteredSummary() {
    return weekFilteredSummary;
  }

  public String getWeekSummary() {
    return weekSummary;
  }

  public List<EveritWorklog> getWorklogs() {
    return worklogs;
  }

  public List<Long> getWorklogsIds() {
    return worklogsIds;
  }

  /**
   * Date change action handler.
   *
   * @return null if the current action is not a Date change action
   */
  private String handleDateChangeAction() {
    if ((getHttpRequest().getParameter("edit") == null)
        && (getHttpRequest().getParameter("submit") == null)
        && (getHttpRequest().getParameter("editallsave") == null)) {
      try {
        handleEditAllIdsAndEditedWorklogId();
      } catch (final ParseException e) {
        LOGGER.error("Error when try parse the worklog.", e);
        return ERROR;
      }
      setUserPickerObjectBasedOnSelectedUser();
      return SUCCESS;
    }
    return null;
  }

  private String handleDuration() {
    final String[] startTimeValue = getHttpRequest().getParameterValues(PARAM_STARTTIME);
    final String durationTimeValue = getHttpRequest().getParameterValues("durationTime")[0];
    Date startDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTime(startTimeValue[0]);
    } catch (final ParseException e) {
      message = INVALID_START_TIME;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(durationTimeValue)) {
      if (!DateTimeConverterUtil.isValidJiraTime(durationTimeValue)) {
        message = INVALID_DURATION_TIME;
        return INPUT;
      } else {
        timeSpent = durationTimeValue;
        final int seconds = DateTimeConverterUtil.jiraDurationToSeconds(durationTimeValue);
        final Date endTime = DateUtils.addSeconds(startDateTime, seconds);
        if (!DateUtils.isSameDay(startDateTime, endTime)) {
          message = INVALID_DURATION_TIME;
          return INPUT;
        }
      }
    } else {
      final String result = handleValidDuration(startDateTime);
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }

  /**
   * Handle the editAllIds and the editedWorklogIds variable values. If the values different from
   * the default, then make the necessary settings.
   *
   * @throws ParseException
   *           If can't parse the editWorklog date.
   */
  private void handleEditAllIdsAndEditedWorklogId() throws ParseException {
    if (!"".equals(editAllIds)) {
      isEditAll = true;
    }
    if ((editedWorklogId != null)
        && !DEFAULT_WORKLOG_ID.equals(editedWorklogId)) {
      isEdit = true;
      EveritWorklog editWorklog;
      editWorklog = jiraTimetrackerPlugin.getWorklog(editedWorklogId);
      issueKey = editWorklog.getIssue();
      comment = editWorklog.getBody();
      startTime = editWorklog.getStartTime();
      endTime = editWorklog.getEndTime();
      durationTime = editWorklog.getDuration();
    }

    if ((copiedWorklogId != null)
        && !DEFAULT_WORKLOG_ID.equals(copiedWorklogId)) {
      // isEdit = true;
      EveritWorklog editWorklog;
      editWorklog = jiraTimetrackerPlugin.getWorklog(copiedWorklogId);
      issueKey = editWorklog.getIssue();
      comment = editWorklog.getBody();
      // startTime = editWorklog.getStartTime();
      // endTime = editWorklog.getEndTime();
      // durationTime = editWorklog.getDuration();
    }
  }

  private String handleEndTime() {
    final String[] startTimeValue = getHttpRequest().getParameterValues(PARAM_STARTTIME);
    final String[] endTimeValue = getHttpRequest().getParameterValues("endTime");
    if (!DateTimeConverterUtil.isValidTime(endTimeValue[0])) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }
    Date startDateTime;
    Date endDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(startTimeValue[0]);
      endDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(endTimeValue[0]);
    } catch (final ParseException e) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }

    final long seconds = (endDateTime.getTime() - startDateTime.getTime())
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    if (seconds > 0) {
      timeSpent = DateTimeConverterUtil.secondConvertToString(seconds);
    } else {
      message = "plugin.invalid_timeInterval";
      return INPUT;
    }
    return SUCCESS;
  }

  private String handleValidDuration(final Date startDateTime) {
    final String[] durationTimeValue = getHttpRequest().getParameterValues("durationTime");
    Date durationDateTime;
    try {
      durationDateTime = DateTimeConverterUtil
          .stringTimeToDateTimeGMT(durationTimeValue[0]);
    } catch (final ParseException e) {
      message = INVALID_DURATION_TIME;
      return INPUT;
    }

    final long seconds = durationDateTime.getTime()
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    timeSpent = DateTimeConverterUtil.secondConvertToString(seconds);

    // check the duration time to not exceed the present day
    final Date endTime = DateUtils.addSeconds(startDateTime, (int) seconds);
    if (!DateUtils.isSameDay(startDateTime, endTime)) {
      message = INVALID_DURATION_TIME;
      return INPUT;
    }
    return SUCCESS;
  }

  private void loadPluginSettingAndParseResult() {
    final PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();
    isPopup = pluginSettingsValues.isCalendarPopup();
    isActualDate = pluginSettingsValues.isActualDate();
    issuesRegex = pluginSettingsValues.getFilteredSummaryIssues();
    startTimeChange = pluginSettingsValues.getStartTimeChange();
    endTimeChange = pluginSettingsValues.getEndTimeChange();
    isColoring = pluginSettingsValues.isColoring();
  }

  /**
   * Set worklogs list, the worklogsIds list and make Summary.
   *
   * @throws GenericEntityException
   *           If GenericEntity Exception.
   * @throws ParseException
   *           If getWorklogs can't parse date.
   * @throws SQLException
   *           Cannot get the worklogs
   * @throws DataAccessException
   *           Cannot get the worklogs
   */
  private void loadWorklogsAndMakeSummary() throws GenericEntityException,
      ParseException, DataAccessException, SQLException {
    try {
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(selectedUser, date);
    } catch (final GenericEntityException e1) {
      // Not return whit error. Log the error and set a message to
      // inform the user. The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling",
          e1);
      message = "plugin.calendar.logged.coloring.fail";
    }
    worklogs = jiraTimetrackerPlugin.getWorklogs(selectedUser, date, null);
    log.warn("JTWA log: loadWorklogsAndMakeSummary: worklogs size: "
        + worklogs.size());
    worklogsIds = copyWorklogIdsToArray(worklogs);
    makeSummary();
  }

  /**
   * Make summary today, this week and this month.
   *
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  public void makeSummary() throws GenericEntityException {
    final ApplicationProperties applicationProperties = ComponentAccessor
        .getApplicationProperties();
    final boolean useISO8601 = applicationProperties
        .getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);

    Calendar startCalendar = Calendar.getInstance();
    if (useISO8601) {
      startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
    }
    startCalendar.setTime(date);
    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
    startCalendar.set(Calendar.MINUTE, 0);
    startCalendar.set(Calendar.SECOND, 0);
    startCalendar.set(Calendar.MILLISECOND, 0);
    final Calendar originalStartcalendar = (Calendar) startCalendar.clone();
    Date start = startCalendar.getTime();

    Calendar endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);

    Date end = endCalendar.getTime();
    daySummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      dayFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    while (startCalendar.get(Calendar.DAY_OF_WEEK) != startCalendar.getFirstDayOfWeek()) {
      startCalendar.add(Calendar.DATE, -1); // Substract 1 day until first day of week.
    }
    start = startCalendar.getTime();
    endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DATE, DateTimeConverterUtil.DAYS_PER_WEEK);
    end = endCalendar.getTime();
    weekSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      weekFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    startCalendar.set(Calendar.DAY_OF_MONTH, 1);
    start = startCalendar.getTime();

    endCalendar = (Calendar) originalStartcalendar.clone();
    endCalendar.set(Calendar.DAY_OF_MONTH,
        endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
    end = endCalendar.getTime();

    monthSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end, null);
    if ((issuesRegex != null) && !issuesRegex.isEmpty()) {
      monthFilteredSummary = jiraTimetrackerPlugin.summary(selectedUser, start, end,
          issuesRegex);
    }
  }

  private void normalizeContextPath() {
    final String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parseDateParam() throws ParseException {
    final String[] requestDateArray = getHttpRequest().getParameterValues(PARAM_DATE);
    if (requestDateArray != null) {
      final String requestDate = getHttpRequest().getParameterValues(PARAM_DATE)[0];
      if (!"".equals(requestDate)) {
        dateFormated = requestDate;
      }
      date = DateTimeConverterUtil.stringToDate(dateFormated);
    } else if ((dateFormated == null) || "".equals(dateFormated)) {
      date = new Date();
      dateFormated = DateTimeConverterUtil.dateToString(date);
    } else {
      date = DateTimeConverterUtil.stringToDate(dateFormated);
    }
  }

  /**
   * The readObject method for the transient variable.
   *
   * @param in
   *          The ObjectInputStream.
   * @throws IOException
   *           IOException.
   * @throws ClassNotFoundException
   *           ClassNotFoundException.
   */
  private void readObject(final ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    issues = new ArrayList<Issue>();
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setColoring(final boolean isColoring) {
    this.isColoring = isColoring;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }

  public void setCopiedWorklogId(final Long copiedWorklogId) {
    this.copiedWorklogId = copiedWorklogId;
  }

  public void setDate(final Date date) {
    this.date = (Date) date.clone();
  }

  private String setDateAndDateFormated() {
    if ("".equals(dateFormated)) {
      if (isActualDate) {
        date = Calendar.getInstance().getTime();
        dateFormated = DateTimeConverterUtil.dateToString(date);
      } else {
        try {
          date = jiraTimetrackerPlugin.firstMissingWorklogsDate(selectedUser);
          dateFormated = DateTimeConverterUtil.dateToString(date);
        } catch (final GenericEntityException e) {
          LOGGER.error("Error when try set the plugin date.", e);
          return ERROR;
        }
      }
    } else {
      try {
        date = DateTimeConverterUtil.stringToDate(dateFormated);
      } catch (final ParseException e) {
        return ERROR;
      }
    }
    return SUCCESS;
  }

  public void setDateFormated(final String dateFormated) {
    this.dateFormated = dateFormated;
  }

  public void setDayFilteredSummary(final String dayFilteredSummary) {
    this.dayFilteredSummary = dayFilteredSummary;
  }

  public void setDaySummary(final String daySummary) {
    this.daySummary = daySummary;
  }

  public void setDebugMessage(final String debugMessage) {
    this.debugMessage = debugMessage;
  }

  public void setDeletedWorklogId(final Long deletedWorklogId) {
    this.deletedWorklogId = deletedWorklogId;
  }

  public void setDurationTime(final String durationTime) {
    this.durationTime = durationTime;
  }

  public void setEdit(final boolean edit) {
    isEdit = edit;
  }

  public void setEditAll(final boolean isEditAll) {
    this.isEditAll = isEditAll;
  }

  public void setEditAllIds(final String editAllIds) {
    this.editAllIds = editAllIds;
  }

  public void setEditedWorklogId(final Long editedWorklogId) {
    this.editedWorklogId = editedWorklogId;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setEndTimeChange(final int endTimeChange) {
    this.endTimeChange = endTimeChange;
  }

  public void setExcludeDays(final List<String> excludeDays) {
    this.excludeDays = excludeDays;
  }

  /**
   * Set the read values to the input fields back.
   */
  private String setFieldsValue() {
    final String[] issueSelectValue = getHttpRequest().getParameterValues(PARAM_ISSUESELECT);
    final String[] endTimeValue = getHttpRequest().getParameterValues("endTime");
    final String[] durationTimeValue = getHttpRequest().getParameterValues("durationTime");
    // String[] startTimeValue = getHttpRequest().getParameterValues("startTime");
    final String[] commentsValue = getHttpRequest().getParameterValues("comments");
    final String[] endOrDurationValue = getHttpRequest().getParameterValues("endOrDuration");

    if ((endOrDurationValue != null) && "duration".equals(endOrDurationValue[0])) {
      isDurationSelected = true;
    }

    if (issueSelectValue != null) {
      issueKey = issueSelectValue[0];
    }

    try {
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
    } catch (final ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }

    if (endTimeValue != null) {
      endTime = endTimeValue[0];
    } else {
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    }

    if (durationTimeValue != null) {
      durationTime = durationTimeValue[0];
    }
    if (commentsValue != null) {
      comment = commentsValue[0];
      commentForActions = commentsValue[0];
      if (comment != null) {
        comment = comment.replace("\"", "\\\"");
        comment = comment.replace("\r", "\\r");
        comment = comment.replace("\n", "\\n");
      } else {
        comment = "";
      }
    }
    return null;
  }

  public void setIsDurationSelected(final boolean isDurationSelected) {
    this.isDurationSelected = isDurationSelected;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setIssues(final List<Issue> issues) {
    this.issues = issues;
  }

  public void setIssuesRegex(final List<Pattern> issuesRegex) {
    this.issuesRegex = issuesRegex;
  }

  public void setJiraMainVersion(final int jiraMainVersion) {
    this.jiraMainVersion = jiraMainVersion;
  }

  public void setJiraTimetrackerPlugin(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  public void setLoggedDays(final List<String> loggedDays) {
    this.loggedDays = loggedDays;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setMessageParameter(final String messageParameter) {
    this.messageParameter = messageParameter;
  }

  public void setMonthFilteredSummary(final String monthFilteredSummary) {
    this.monthFilteredSummary = monthFilteredSummary;
  }

  public void setMonthSummary(final String monthSummary) {
    this.monthSummary = monthSummary;
  }

  public void setPluginVersion(final String pluginVersion) {
    this.pluginVersion = pluginVersion;
  }

  public void setPopup(final int isPopup) {
    this.isPopup = isPopup;
  }

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setSelectedeUser(final String selectedeUser) {
    selectedUser = selectedeUser;
  }

  private void setSelectedUserFromParam() {
    final String[] selectedUserValue = getHttpRequest().getParameterValues("selectedUser");
    if (selectedUserValue != null) {
      selectedUser = selectedUserValue[0];
      log.info("We set selectedUSer " + selectedUser);
    } else {
      log.info("We set selectedUSer to empty");
      selectedUser = "";
    }
    LOGGER.info("The selectedUser value: " + selectedUser);
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

  public void setStartTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setUserPickerObject(final ApplicationUser userPickerObject) {
    this.userPickerObject = userPickerObject;
  }

  private void setUserPickerObjectBasedOnSelectedUser() {

    if ((selectedUser != null) && !"".equals(selectedUser)) {
      userPickerObject = ComponentAccessor.getUserUtil().getUserByName(selectedUser);
      final AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
      setAvatarURL(avatarService.getAvatarURL(
          ComponentAccessor.getJiraAuthenticationContext().getUser(),
          userPickerObject, Avatar.Size.SMALL).toString());
    } else {
      userPickerObject = null;
    }
  }

  public void setWeekFilteredSummary(final String weekFilteredSummary) {
    this.weekFilteredSummary = weekFilteredSummary;
  }

  public void setWeekSummary(final String weekSummary) {
    this.weekSummary = weekSummary;
  }

  public void setWorklogs(final List<EveritWorklog> worklogs) {
    this.worklogs = worklogs;
  }

  public void setWorklogsIds(final List<Long> worklogsIds) {
    this.worklogsIds = worklogsIds;
  }

  /**
   * Check the startTime, endTime or durationTime fields values.
   *
   * @return If the values valid the return SUCCESS else return INPUT.
   */
  public String validateInputFields() {
    final String[] startTimeValue = getHttpRequest().getParameterValues(PARAM_STARTTIME);
    final String[] endOrDurationValue = getHttpRequest().getParameterValues("endOrDuration");
    final String[] issueSelectValue = getHttpRequest().getParameterValues(PARAM_ISSUESELECT);

    // if (commentsValue[0] == null) {
    // return INPUT;
    // }
    if ((issueSelectValue == null) || (issueSelectValue[0] == null)) {
      message = MISSING_ISSUE;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(startTimeValue[0])) {
      message = INVALID_START_TIME;
      return INPUT;
    }
    if ("duration".equals(endOrDurationValue[0])) {
      final String result = handleDuration();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    } else {
      final String result = handleEndTime();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }
}
