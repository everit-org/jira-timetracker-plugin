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
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.SupportManager;
import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.TimetrackerCondition;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.SummaryDTO;
import org.everit.jira.timetracker.plugin.dto.WorklogValues;
import org.everit.jira.timetracker.plugin.exception.WorklogException;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The timetracker web action support class.
 */
public class JiraTimetrackerWebAction extends JiraWebActionSupport {

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String INVALID_DURATION_TIME = "plugin.invalid_durationTime";

    public static final String INVALID_START_TIME = "plugin.invalid_startTime";

    private static final String MISSING_ISSUE = "plugin.missing_issue";
  }

  /**
   * The default worklog ID.
   */
  private static final Long DEFAULT_WORKLOG_ID = Long.valueOf(0);

  private static final String FUTURE_WORKLOG_WARNING_URL_PARAMETER = "&showWarning=true";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * The JiraTimetrackerWebAction logger..
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWebAction.class);

  private static final String PARAM_DATE = "date";

  private static final String SELF_WITH_DATE_URL_FORMAT =
      "/secure/JiraTimetrackerWebAction.jspa?date=%s";

  private static final String SELF_WITH_DATE_WORKLOG_URL_FORMAT =
      "/secure/JiraTimetrackerWebAction.jspa?date=%s&worklogValuesJson=%s";

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private String actionFlag = "";

  /**
   * The edited worklog id.
   */
  private Long actionWorklogId = DEFAULT_WORKLOG_ID;

  private AnalyticsDTO analyticsDTO;

  private JiraRendererPlugin atlassianWikiRenderer;

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

  private double dailyPercent;

  /**
   * The date.
   */
  private Date date = null;

  /**
   * The formated date.
   */
  private Long dateFormatted;

  private String debugMessage = "";

  private DurationFormatter durationFormatter = new DurationFormatter();

  /**
   * The worklog duration.
   */
  private String durationTime = "";

  private String editAllIds;

  /**
   * The worklog end time.
   */
  private String endTime = "";

  /**
   * The endTime input field changer buttons value.
   */
  private int endTimeChange;

  private Set<Date> excludeDatesAsSet;

  /**
   * List of the exclude days of the date variable current months.
   */
  private List<Date> excludeDays = new ArrayList<>();

  private Set<Date> includeDatesAsSet;

  /**
   * The calendar show actual Date Or Last Worklog Date.
   */
  private boolean isActualDate;

  /**
   * The calendar highlights coloring function is active or not.
   */
  private boolean isColoring;

  private boolean isDurationSelected = false;

  private boolean isProgressDaily = true;

  private boolean isRounded;

  private boolean isShowFutureLogWarning;

  private boolean isShowIssueSummary;

  private String issueCollectorSrc;

  /**
   * The issue key.
   */
  private String issueKey = "";

  private IssueRenderContext issueRenderContext;

  /**
   * The issues.
   */
  private transient List<Issue> issues = new ArrayList<>();

  /**
   * The filtered Issues id.
   */
  private List<Pattern> issuesRegex;

  /**
   * List of the logged days of the date variable current months.
   */
  private List<String> loggedDays = new ArrayList<>();

  /**
   * The message.
   */
  private String message = "";

  /**
   * The message parameter.
   */
  private String messageParameter = "";

  private List<Long> parsedEditAllIds = Collections.emptyList();

  private PluginCondition pluginCondition;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  private TimetrackerSettingsHelper settingsHelper;

  /**
   * The worklog start time.
   */
  private String startTime = "";

  /**
   * The startTime input field changer buttons value.
   */
  private int startTimeChange;

  private SummaryDTO summaryDTO;

  private transient SupportManager supportManager;

  /**
   * The spent time in Jira time format (1h 20m).
   */
  private String timeSpent = "";

  private TimetrackerManager timetrackerManager;

  private TimetrackerCondition timetrackingCondition;

  private TimeTrackingConfiguration timeTrackingConfiguration;

  private EVWorklogManager worklogManager;

  /**
   * The worklogs.
   */
  private List<EveritWorklog> worklogs = new ArrayList<>();

  /**
   * The ids of the woklogs.
   */
  private List<Long> worklogsIds = new ArrayList<>();

  private long worklogsSizeWithoutPermissionChecks;

  private WorklogValues worklogValue;

  private String worklogValuesJson;

  /**
   * Simple constructor.
   */
  public JiraTimetrackerWebAction(
      final SupportManager supportManager,
      final TimetrackerManager timetrackerManager,
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final TimetrackerSettingsHelper settingsHelper,
      final EVWorklogManager worklogManager) {
    this.timetrackerManager = timetrackerManager;
    this.supportManager = supportManager;
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.worklogManager = worklogManager;
    timetrackingCondition = new TimetrackerCondition(settingsHelper);
    pluginCondition = new PluginCondition(settingsHelper);
    issueRenderContext = new IssueRenderContext(null);
    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    this.settingsHelper = settingsHelper;
    atlassianWikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
  }

  private String checkConditions() {
    boolean isUserLogged = TimetrackerUtil.isUserLogged();
    if (!isUserLogged) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!timetrackingCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    if (!pluginCondition.shouldDisplay(getLoggedInApplicationUser(), null)) {
      setReturnUrl(JIRA_HOME_URL);
      return getRedirect(NONE);
    }
    return null;
  }

  /**
   * Put the worklogs id into a array.
   *
   * @param worklogsParam
   *          The worklogs.
   * @return The array of the ids.
   */
  private List<Long> copyWorklogIdsToArray(final List<EveritWorklog> worklogsParam) {
    List<Long> worklogIds = new ArrayList<>();
    for (EveritWorklog worklog : worklogsParam) {
      if (worklog.isDeleteOwnWorklogs() && worklog.isEditOwnWorklogs()) {
        worklogIds.add(worklog.getWorklogId());
      }
    }
    return worklogIds;
  }

  private String createOrCopyAction() {
    String validateInputFieldsResult = validateInputFields();
    if (validateInputFieldsResult.equals(INPUT)) {
      return INPUT;
    }
    String result = createWorklog();
    if (SUCCESS.equals(result)) {
      if ((actionWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(actionWorklogId)
          && "copy".equals(actionFlag)) {
        actionFlag = "";
        return redirectWidthDateFormattedParameterOnlyAndShowWarning(result);
      }
      if (decideToShowWarning()) {
        return redirectWithDateAndWorklogParams(result, FUTURE_WORKLOG_WARNING_URL_PARAMETER);
      } else {
        return redirectWithDateAndWorklogParams(result, "");
      }
    }
    return result;

  }

  private String createWorklog() {
    try {
      worklogManager.createWorklog(issueKey, commentForActions, date, startTime, timeSpent);
    } catch (WorklogException e) {
      message = e.getMessage();
      messageParameter = e.messageParameter;
      return INPUT;
    }

    try {
      loadWorklogsAndMakeSummary();
      startTime = timetrackerManager.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
      isDurationSelected = false;
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Handle the date change.
   *
   * @throws ParseException
   *           When can't parse date.
   */
  public void dateSwitcherAction() throws ParseException {
    String dayBackValue = getHttpRequest().getParameter("dayBack");
    String dayNextValue = getHttpRequest().getParameter("dayNext");
    String todayValue = getHttpRequest().getParameter("today");

    parseDateParam();

    Calendar tempCal = Calendar.getInstance();
    tempCal.setTime(date);
    if (dayNextValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormatted = date.getTime();
    } else if (dayBackValue != null) {
      tempCal.add(Calendar.DAY_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormatted = date.getTime();
    } else if (todayValue != null) {
      date = new Date();
      dateFormatted = date.getTime();
    }
  }

  private boolean decideToShowWarning() {
    try {
      Date startDate = DateTimeConverterUtil.stringToDateAndTime(date, startTime);
      if (isShowFutureLogWarning && new Date().before(startDate)) {
        return true;
      }
    } catch (ParseException e) {
      LOGGER.error("parse failed", e);
    }
    return false;
  }

  private String deleteWorklog() {
    if ("delete".equals(actionFlag) && (actionWorklogId != null)
        && !DEFAULT_WORKLOG_ID.equals(actionWorklogId)) {
      try {
        worklogManager.deleteWorklog(actionWorklogId);
      } catch (WorklogException e) {
        message = e.getMessage();
        messageParameter = e.messageParameter;
        return INPUT;
      }
      actionFlag = "";
      return SUCCESS;
    }
    return null;
  }

  @Override
  public String doDefault() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID, settingsHelper);

    normalizeContextPath();
    loadIssueCollectorSrc();

    loadPluginSettingAndParseResult();

    if (isActualDate) {
      date = Calendar.getInstance().getTime();
      dateFormatted = date.getTime();
    } else {
      date = timetrackerManager.firstMissingWorklogsDate(excludeDatesAsSet, includeDatesAsSet);
      dateFormatted = date.getTime();
    }

    excludeDays = timetrackerManager.getExcludeDaysOfTheMonth(date, excludeDatesAsSet);
    projectsId = supportManager.getProjectsId();

    loggedDays = timetrackerManager.getLoggedDaysOfTheMonth(date);

    try {
      loadWorklogsAndMakeSummary();
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    setFieldsValue();
    return INPUT;
  }

  @Override
  public String doExecute() throws ParseException {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID, settingsHelper);

    normalizeContextPath();
    loadIssueCollectorSrc();

    loadPluginSettingAndParseResult();

    dateSwitcherAction();
    parseActionParams();
    parsedEditAllIds = parseEditAllIds(getHttpRequest().getParameter("editAll"));
    parseEditAllAction();

    excludeDays = timetrackerManager.getExcludeDaysOfTheMonth(date, excludeDatesAsSet);
    projectsId = supportManager.getProjectsId();

    String deleteResult = deleteWorklog();
    if (deleteResult != null) {
      try {
        loadWorklogsAndMakeSummary();
      } catch (ParseException | DataAccessException e) {
        LOGGER.error("Error when try set the plugin variables.", e);
        return ERROR;
      }
      if (SUCCESS.equals(deleteResult)) {
        return redirectWithDateFormattedParameterOnly(deleteResult, "");
      } else {
        return deleteResult;
      }
    }

    try {
      loadWorklogsAndMakeSummary();
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    setFieldsValue();
    String result = handleDateChangeAction();
    if (result != null) {
      return result;
    }

    return handleSaveActions();

  }

  /**
   * Edit the worklog and handle the problems.
   *
   * @return String which will be passed to the WebAction.
   */
  public String editAction() {
    String validateInputFieldsResult = validateInputFields();
    if (validateInputFieldsResult.equals(INPUT)) {
      return INPUT;
    }

    try {
      worklogManager.editWorklog(actionWorklogId, issueKey, commentForActions, date, startTime,
          timeSpent);
    } catch (WorklogException e) {
      message = e.getMessage();
      messageParameter = e.messageParameter;
      return INPUT;
    }

    try {
      loadWorklogsAndMakeSummary();
      startTime = timetrackerManager.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      comment = "";
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    actionWorklogId = DEFAULT_WORKLOG_ID;
    actionFlag = "";
    isDurationSelected = false;
    return SUCCESS;
  }

  /**
   * The edit all function save action. Save the worklogs in the given date. The worklogs come form
   * the editAllIds, the date from the {@code dateFormatted}.
   *
   * @return SUCCESS if the save was success else FAIL.
   * @throws ParseException
   *           If cannot parse date or time.
   */
  public String editAllAction() throws ParseException {
    // parse the editAllIds
    List<Long> editWorklogIds = parseEditAllIds(getHttpRequest().getParameter("editAll"));
    // edit the worklogs!
    for (Long editWorklogId : editWorklogIds) {
      EveritWorklog editWorklog = worklogManager.getWorklog(editWorklogId);
      worklogManager.editWorklog(editWorklog.getWorklogId(),
          editWorklog.getIssue(),
          editWorklog.getBody(),
          date,
          editWorklog.getStartTime(),
          DateTimeConverterUtil.stringTimeToString(editWorklog.getDuration()));
    }
    // set editAllIds to default and list worklogs
    try {
      loadWorklogsAndMakeSummary();
      startTime = timetrackerManager.lastEndTime(worklogs);
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      return ERROR;
    }
    return SUCCESS;
  }

  public String getActionFlag() {
    return actionFlag;
  }

  public Long getActionWorklogId() {
    return actionWorklogId;
  }

  public AnalyticsDTO getAnalyticsDTO() {
    return analyticsDTO;
  }

  public JiraRendererPlugin getAtlassianWikiRenderer() {
    return atlassianWikiRenderer;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getComment() {
    return comment;
  }

  public String getContextPath() {
    return contextPath;
  }

  public double getDailyPercent() {
    return dailyPercent;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public Long getDateFormatted() {
    return dateFormatted;
  }

  @Override
  public DateTimeFormatter getDateTimeFormatter() {
    return super.getDateTimeFormatter().withStyle(DateTimeStyle.DATE).withSystemZone();
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public String getDurationTime() {
    return durationTime;
  }

  public String getEditAllIds() {
    return editAllIds;
  }

  public String getEndTime() {
    return endTime;
  }

  public int getEndTimeChange() {
    return endTimeChange;
  }

  /**
   * Get exclude dates as the original date format.
   */
  public List<String> getExcludeDays() {
    List<String> excludeDaysAsString = new ArrayList<>(excludeDays.size());
    for (Date excludeDate : excludeDays) {
      excludeDaysAsString.add(DateTimeConverterUtil.dateToFixFormatString(excludeDate));
    }
    return excludeDaysAsString;
  }

  public boolean getIsColoring() {
    return isColoring;
  }

  public boolean getIsDurationSelected() {
    return isDurationSelected;
  }

  public boolean getIsProgressDaily() {
    return isProgressDaily;
  }

  public boolean getIsRounded() {
    return isRounded;
  }

  public boolean getIsShowIssueSummary() {
    return isShowIssueSummary;
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public boolean getIssueRegexIsNotEmpty() {
    return (issuesRegex != null) && !issuesRegex.isEmpty();
  }

  public IssueRenderContext getIssueRenderContext() {
    return issueRenderContext;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public List<Pattern> getIssuesRegex() {
    return issuesRegex;
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

  public List<Long> getParsedEditAllIds() {
    return parsedEditAllIds;
  }

  public List<String> getProjectsId() {
    return projectsId;
  }

  public String getStartTime() {
    return startTime;
  }

  public int getStartTimeChange() {
    return startTimeChange;
  }

  public SummaryDTO getSummaryDTO() {
    return summaryDTO;
  }

  public List<EveritWorklog> getWorklogs() {
    return worklogs;
  }

  public List<Long> getWorklogsIds() {
    return worklogsIds;
  }

  public long getWorklogsSizeWithoutPermissionChecks() {
    return worklogsSizeWithoutPermissionChecks;
  }

  /**
   * Date change action handler.
   *
   * @return null if the current action is not a Date change action
   */
  private String handleDateChangeAction() {
    if (getHttpRequest().getParameter("lw_save") == null) {
      try {
        handleInputWorklogId();
        handleEditAllIds();
      } catch (ParseException e) {
        LOGGER.error("Error when try parse the worklog.", e);
        return ERROR;
      }
      return SUCCESS;
    }
    return null;
  }

  private String handleDuration() {
    Date startDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTime(startTime);
    } catch (ParseException e) {
      message = PropertiesKey.INVALID_START_TIME;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(durationTime)) {
      if (!DateTimeConverterUtil.isValidJiraTime(durationTime)) {
        message = PropertiesKey.INVALID_DURATION_TIME;
        return INPUT;
      } else {
        timeSpent = durationTime;
        int seconds = DateTimeConverterUtil.jiraDurationToSeconds(durationTime);
        Date endTime = DateUtils.addSeconds(startDateTime, seconds);
        if (!DateUtils.isSameDay(startDateTime, endTime)) {
          message = PropertiesKey.INVALID_DURATION_TIME;
          return INPUT;
        }
      }
    } else {
      String result = handleValidDuration(startDateTime);
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }

  private void handleEditAllIds() {
    String editAllValue = getHttpRequest().getParameter("editAll");
    if (editAllValue != null) {
      editAllIds = editAllValue;
    }
  }

  private String handleEndTime() {
    if (!DateTimeConverterUtil.isValidTime(endTime)) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }
    Date startDateTime;
    Date endDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(startTime);
      endDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(endTime);
    } catch (ParseException e) {
      message = "plugin.invalid_endTime";
      return INPUT;
    }

    long seconds = (endDateTime.getTime() - startDateTime.getTime())
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    if (seconds > 0) {
      timeSpent = durationFormatter.exactDuration(seconds);
    } else {
      message = "plugin.invalid_timeInterval";
      return INPUT;
    }
    return SUCCESS;
  }

  /**
   * Handle the editAllIds, the editedWorklogIds and the copiedWorklogId variable values. If the
   * values different from the default, then make the necessary settings.
   *
   * @throws ParseException
   *           If can't parse the editWorklog date.
   */
  private void handleInputWorklogId() throws ParseException {
    if ((actionWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(actionWorklogId)) {
      EveritWorklog editWorklog = worklogManager.getWorklog(actionWorklogId);
      if ("edit".equals(actionFlag)) {
        startTime = editWorklog.getStartTime();
        endTime = editWorklog.getEndTime();
        durationTime = editWorklog.getDuration();
      }
      comment = editWorklog.getBody();
      issueKey = editWorklog.getIssue();
      comment = comment.replace("\"", "\\\"");
      comment = comment.replace("\r", "\\r");
      comment = comment.replace("\n", "\\n");
    }
  }

  private String handleSaveActions() throws ParseException {
    String result;
    if ((getHttpRequest().getParameter("lw_save") != null) && "editAll".equals(actionFlag)) {
      result = editAllAction();
    } else if ((getHttpRequest().getParameter("lw_save") != null) && "edit".equals(actionFlag)) {
      result = editAction();
    } else {
      result = createOrCopyAction();
    }
    if (SUCCESS.equals(result)) {
      result = redirectWidthDateFormattedParameterOnlyAndShowWarning(result);
    }
    return result;
  }

  private String handleValidDuration(final Date startDateTime) {
    Date durationDateTime;
    try {
      durationDateTime = DateTimeConverterUtil
          .stringTimeToDateTimeGMT(durationTime);
    } catch (ParseException e) {
      message = PropertiesKey.INVALID_DURATION_TIME;
      return INPUT;
    }

    long seconds = durationDateTime.getTime()
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    timeSpent = durationFormatter.exactDuration(seconds);

    // check the duration time to not exceed the present day
    Date endTime = DateUtils.addSeconds(startDateTime, (int) seconds);
    if (!DateUtils.isSameDay(startDateTime, endTime)) {
      message = PropertiesKey.INVALID_DURATION_TIME;
      return INPUT;
    }
    return SUCCESS;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void loadPluginSettingAndParseResult() {
    TimeTrackerUserSettings userSettings = settingsHelper.loadUserSettings();
    TimeTrackerGlobalSettings globalSettings = settingsHelper.loadGlobalSettings();

    isProgressDaily = userSettings.getisProgressIndicatordaily();
    isActualDate = userSettings.getActualDate();
    issuesRegex = globalSettings.getNonWorkingIssuePatterns();
    startTimeChange = userSettings.getStartTimeChange();
    endTimeChange = userSettings.getEndTimeChange();
    isColoring = userSettings.getColoring();
    isRounded = userSettings.getIsRounded();
    isShowFutureLogWarning = userSettings.getIsShowFutureLogWarning();
    isShowIssueSummary = userSettings.getIsShowIssueSummary();
    excludeDatesAsSet = globalSettings.getExcludeDates();
    includeDatesAsSet = globalSettings.getIncludeDates();
  }

  /**
   * Set worklogs list, the worklogsIds list and make Summary.
   *
   * @throws ParseException
   *           If getWorklogs can't parse date.
   * @throws DataAccessException
   *           Cannot get the worklogs
   */
  private void loadWorklogsAndMakeSummary() throws ParseException, DataAccessException {
    loggedDays = timetrackerManager.getLoggedDaysOfTheMonth(date);

    worklogs = worklogManager.getWorklogs(null, date, null);
    worklogsIds = copyWorklogIdsToArray(worklogs);
    worklogsSizeWithoutPermissionChecks =
        worklogManager.countWorklogsWithoutPermissionChecks(date, null);

    summaryDTO = new SummaryDTO.SummaryDTOBuilder(
        timeTrackingConfiguration, timetrackerManager, supportManager, date, excludeDatesAsSet,
        includeDatesAsSet, issuesRegex)
            .createSummaryDTO();
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  private void parseActionParams() {
    String actionWorklogIdValue = getHttpRequest().getParameter("actionWorklogId");
    String actionFlagValue = getHttpRequest().getParameter("actionFlag");
    if ((actionWorklogIdValue != null) && !"".equals(actionWorklogIdValue)) {
      actionWorklogId = Long.valueOf(actionWorklogIdValue);
    }
    if (actionFlagValue != null) {
      actionFlag = actionFlagValue;
    }
  }

  private void parseDateParam() throws ParseException {
    String dateFromParam = getHttpRequest().getParameter(PARAM_DATE);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFormatted = Long.valueOf(dateFromParam);
      date = new Date(dateFormatted);
    } else {
      if (isActualDate) {
        date = Calendar.getInstance().getTime();
        dateFormatted = date.getTime();
      } else {
        date = timetrackerManager.firstMissingWorklogsDate(excludeDatesAsSet, includeDatesAsSet);
        dateFormatted = date.getTime();
      }
    }

  }

  private void parseEditAllAction() {
    if (getHttpRequest().getParameter("lw_chgdate") != null) {
      String worklogsIdsValues = getHttpRequest().getParameter("worklogsIds");
      if ((worklogsIdsValues != null) && !"".equals(worklogsIdsValues)) {
        editAllIds = worklogsIdsValues;
        actionFlag = "editAll";
        parsedEditAllIds = parseEditAllIds(editAllIds);
      }
    }
  }

  /**
   * Parses the {@link #editAllIds} string to a list of {@code Long} values.
   */
  public List<Long> parseEditAllIds(final String editAllValues) {
    List<Long> editWorklogIds = new ArrayList<>();
    if ((editAllValues != null) && !"$editAllIds".equals(editAllValues)) {
      String editAllIdsCopy = editAllValues;
      editAllIdsCopy = editAllIdsCopy.replace("[", "");
      editAllIdsCopy = editAllIdsCopy.replace("]", "");
      editAllIdsCopy = editAllIdsCopy.replace(" ", "");
      if (editAllIdsCopy.trim().equals("")) {
        return Collections.emptyList();
      }
      String[] editIds = editAllIdsCopy.split(",");
      for (String editId : editIds) {
        editWorklogIds.add(Long.valueOf(editId));
      }
      return editWorklogIds;
    }
    return Collections.emptyList();
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
    issues = new ArrayList<>();
  }

  private String redirectWidthDateFormattedParameterOnlyAndShowWarning(final String result) {
    String res;
    if (decideToShowWarning()) {
      res =
          redirectWithDateFormattedParameterOnly(result, FUTURE_WORKLOG_WARNING_URL_PARAMETER);
    } else {
      res = redirectWithDateFormattedParameterOnly(result, "");
    }
    return res;
  }

  private String redirectWithDateAndWorklogParams(final String action,
      final String warningUrlParameter) {
    worklogValue.setComment("");
    String returnJson = TimetrackerUtil.convertWorklogValuesToJson(worklogValue);
    setReturnUrl(
        String.format(SELF_WITH_DATE_WORKLOG_URL_FORMAT,
            dateFormatted,
            TimetrackerUtil.urlEndcodeHandleException(returnJson)) + warningUrlParameter);
    return getRedirect(action);
  }

  private String redirectWithDateFormattedParameterOnly(final String action,
      final String warningUrlParameter) {
    setReturnUrl(
        String.format(SELF_WITH_DATE_URL_FORMAT,
            dateFormatted) + warningUrlParameter);
    return getRedirect(action);
  }

  /**
   * Decide render or not the update information bar.
   *
   * @return true if bar should be render
   */
  public boolean renderUpdateNotifier() {
    return new UpdateNotifier(settingsHelper).isShowUpdater();

  }

  public void setActionFlag(final String actionFlag) {
    this.actionFlag = actionFlag;
  }

  public void setActionWorklogId(final Long actionWorklogId) {
    this.actionWorklogId = actionWorklogId;
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
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

  public void setDate(final Date date) {
    this.date = (Date) date.clone();
  }

  public void setDateFormatted(final Long dateFormatted) {
    this.dateFormatted = dateFormatted;
  }

  public void setDebugMessage(final String debugMessage) {
    this.debugMessage = debugMessage;
  }

  public void setDurationTime(final String durationTime) {
    this.durationTime = durationTime;
  }

  public void setEditAllIds(final String editAllIds) {
    this.editAllIds = editAllIds;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setEndTimeChange(final int endTimeChange) {
    this.endTimeChange = endTimeChange;
  }

  /**
   * Set the read values to the input fields back.
   */
  private String setFieldsValue() {
    worklogValuesJson = getHttpRequest().getParameter("worklogValuesJson");
    if ((worklogValuesJson != null) && !"".equals(worklogValuesJson)) {
      worklogValue = TimetrackerUtil.convertJsonToWorklogValues(worklogValuesJson);
      isDurationSelected = worklogValue.isDuration();
      issueKey = worklogValue.getIssueKey();
      if (worklogValue.getEndTime() != null) {
        endTime = worklogValue.getEndTime();
      } else {
        endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      }
      durationTime = worklogValue.getDurationTime();
      if (worklogValue.getComment() != null) {
        commentForActions = worklogValue.getComment();
        comment = worklogValue.getComment();
        comment = comment.replace("\"", "\\\"");
        comment = comment.replace("\r", "\\r");
        comment = comment.replace("\n", "\\n");
      } else {
        comment = "";
      }
    } else {
      issueKey = "";
      endTime = DateTimeConverterUtil.dateTimeToString(new Date());
      durationTime = "";
      isDurationSelected = false;
      comment = "";
    }

    try {
      startTime = timetrackerManager.lastEndTime(worklogs);
    } catch (ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }

    return null;
  }

  public void setIsDurationSelected(final boolean isDurationSelected) {
    this.isDurationSelected = isDurationSelected;
  }

  public void setIsProgressDaily(final boolean isProgressDaily) {
    this.isProgressDaily = isProgressDaily;
  }

  public void setIsRounded(final boolean isRounded) {
    this.isRounded = isRounded;
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

  public void setLoggedDays(final List<String> loggedDays) {
    this.loggedDays = loggedDays;
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

  public void setStartTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
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
    if (issueKey == null) {
      message = PropertiesKey.MISSING_ISSUE;
      return INPUT;
    }
    String startTimeValue = worklogValue.getStartTime();
    if (!DateTimeConverterUtil.isValidTime(startTimeValue)) {
      message = PropertiesKey.INVALID_START_TIME;
      return INPUT;
    }
    startTime = startTimeValue;
    if (isDurationSelected) {
      String result = handleDuration();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    } else {
      String result = handleEndTime();
      if (!result.equals(SUCCESS)) {
        return result;
      }
    }
    return SUCCESS;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

}
