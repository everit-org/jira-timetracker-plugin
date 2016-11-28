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
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.RemainingEstimateType;
import org.everit.jira.core.SupportManager;
import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.dto.WorklogParameter;
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
import org.everit.jira.timetracker.plugin.util.ExceptionUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.timetracker.plugin.util.TimeAutoCompleteUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The timetracker web action support class.
 */
public class JiraTimetrackerWebAction extends JiraWebActionSupport {

  /**
   * HTTP parameters.
   */
  public static final class Parameter {

    public static final String ACTION_FLAG = "actionFlag";

    public static final String ACTION_WORKLOG_ID = "actionWorklogId";

    public static final String DATE = "date";

    public static final String DAY_BACK = "dayBack";

    public static final String DAY_NEXT = "dayNext";

    public static final String EDIT_ALL = "editAll";

    public static final String IS_SHOW_MOVE_ALL_NO_PERMISSION = "isShowMoveAllNoPermission";

    public static final String LW_CHGDATE = "lw_chgdate";

    public static final String LW_SAVE = "lw_save";

    public static final String MESSAGE = "message";

    public static final String MESSAGE_PARAMETER = "messageParameter";

    public static final String TODAY = "today";

    public static final String WORKLOG_VALUES_JSON = "worklogValuesJson";

    public static final String WORKLOGS_IDS = "worklogsIds";

  }

  /**
   * Keys for properties.
   */
  public static final class PropertiesKey {

    public static final String INVALID_DURATION_TIME = "plugin.invalid_durationTime";

    public static final String INVALID_START_TIME = "plugin.invalid_startTime";

    public static final String MISSING_ISSUE = "plugin.missing_issue";

    public static final String PLUGIN_INVALID_END_TIME = "plugin.invalid_endTime";

    public static final String PLUGIN_INVALID_TIME_INTERVAL = "plugin.invalid_timeInterval";
  }

  private static final String ACTION_DELETE = "delete";

  private static final String ACTION_EDIT = "edit";

  private static final String ACTION_EDIT_ALL = "editAll";

  private static final String FUTURE_WORKLOG_WARNING_URL_PARAMETER = "&showWarning=true";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * The JiraTimetrackerWebAction logger..
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWebAction.class);

  private static final String SELF_WITH_DATE_MESSAGES_URL_FORMAT =
      "/secure/JiraTimetrackerWebAction.jspa?date=%s&message=%s&messageParameter=%s";

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
  private Long actionWorklogId = null;

  private AnalyticsDTO analyticsDTO;

  private final JiraRendererPlugin atlassianWikiRenderer;

  private String contextPath;

  /**
   * The date.
   */
  private Date date = null;

  /**
   * The formated date.
   */
  private Long dateFormatted;

  private boolean defaultCommand = false;

  private DurationFormatter durationFormatter = new DurationFormatter();

  private String editAllIds;

  private Date endDateTime;

  /**
   * List of the exclude days of the date variable current months.
   */
  private List<Date> excludeDays = new ArrayList<>();

  private TimeTrackerGlobalSettings globalSettings;

  private String issueCollectorSrc;

  private final IssueRenderContext issueRenderContext;

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

  private final PluginCondition pluginCondition;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  private final TimetrackerSettingsHelper settingsHelper;

  private boolean showMoveAllNoPermission = false;

  private String stacktrace = "";

  private SummaryDTO summaryDTO;

  private final SupportManager supportManager;

  /**
   * The spent time in Jira time format (1h 20m).
   */
  private String timeSpent = "";

  private final TimetrackerManager timetrackerManager;

  private final TimetrackerCondition timetrackingCondition;

  private final TimeTrackingConfiguration timeTrackingConfiguration;

  private TimeTrackerUserSettings userSettings;

  private final EVWorklogManager worklogManager;

  /**
   * The worklogs.
   */
  private List<EveritWorklog> worklogs = new ArrayList<>();

  /**
   * The ids of the woklogs.
   */
  private List<Long> worklogsIds = new ArrayList<>();

  private long worklogsSizeWithoutPermissionChecks;

  private WorklogValues worklogValues;

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

  private void afterActions() {
    normalizeContextPath();

    analyticsDTO = JiraTimetrackerAnalytics
        .getAnalyticsDTO(PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID, settingsHelper);

    excludeDays =
        timetrackerManager.getExcludeDaysOfTheMonth(date, globalSettings.getExcludeDates());

    projectsId = supportManager.getProjectsId();

    worklogsSizeWithoutPermissionChecks =
        worklogManager.countWorklogsWithoutPermissionChecks(date, null);

    summaryDTO = new SummaryDTO.SummaryDTOBuilder(
        timeTrackingConfiguration, timetrackerManager, supportManager, date,
        globalSettings.getExcludeDates(),
        globalSettings.getIncludeDates(), globalSettings.getNonWorkingIssuePatterns())
            .createSummaryDTO();
  }

  private void beforeActions() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);

    userSettings = settingsHelper.loadUserSettings();
    globalSettings = settingsHelper.loadGlobalSettings();

    parseDateParam();

    boolean dayBack = getHttpRequest().getParameter(Parameter.DAY_BACK) != null;
    boolean dayNext = getHttpRequest().getParameter(Parameter.DAY_NEXT) != null;
    boolean today = getHttpRequest().getParameter(Parameter.TODAY) != null;
    if (dayBack || dayNext || today) {
      parseDateParamAfterDateSwitcher(dayBack, dayNext, today);
    }

    String actionWorklogIdValue = getHttpRequest().getParameter(Parameter.ACTION_WORKLOG_ID);
    if ((actionWorklogIdValue != null) && !"".equals(actionWorklogIdValue)
        && !"$actionWorklogId".equals(actionWorklogIdValue)) {
      actionWorklogId = Long.valueOf(actionWorklogIdValue);
    }
    String actionFlagValue = getHttpRequest().getParameter(Parameter.ACTION_FLAG);
    if (actionFlagValue != null) {
      actionFlag = actionFlagValue;
    }

    parseMessageParam();

    showMoveAllNoPermission =
        Boolean.valueOf(getHttpRequest().getParameter(Parameter.IS_SHOW_MOVE_ALL_NO_PERMISSION));
  }

  private String calculateTimeSpentForDuration(final String startTime, final String durationTime) {
    Date startDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTime(startTime);
    } catch (IllegalArgumentException e) {
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
        endDateTime = DateUtils.addSeconds(startDateTime, seconds);
        if (!DateUtils.isSameDay(startDateTime, endDateTime)) {
          message = PropertiesKey.INVALID_DURATION_TIME;
          return INPUT;
        }
      }
    } else {
      Date durationDateTime;
      try {
        durationDateTime = DateTimeConverterUtil
            .stringTimeToDateTimeWithFixFormat(worklogValues.getDurationTime());
      } catch (ParseException e) {
        message = PropertiesKey.INVALID_DURATION_TIME;
        return INPUT;
      }

      long seconds = durationDateTime.getTime()
          / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
      timeSpent = durationFormatter.exactDuration(seconds);

      // check the duration time to not exceed the present day
      endDateTime = DateUtils.addSeconds(startDateTime, (int) seconds);
      if (!DateUtils.isSameDay(startDateTime, endDateTime)) {
        message = PropertiesKey.INVALID_DURATION_TIME;
        return INPUT;
      }
      return SUCCESS;
    }
    return SUCCESS;
  }

  private String calculateTimeSpentForEndTime(final String startTime, final String endTime) {
    if (!DateTimeConverterUtil.isValidTime(endTime)) {
      message = PropertiesKey.PLUGIN_INVALID_END_TIME;
      return INPUT;
    }
    Date startDateTime;
    try {
      startDateTime = DateTimeConverterUtil.stringTimeToDateTime(startTime);
      endDateTime = DateTimeConverterUtil.stringTimeToDateTime(endTime);
    } catch (IllegalArgumentException e) {
      message = PropertiesKey.PLUGIN_INVALID_END_TIME;
      return INPUT;
    }

    long seconds = (endDateTime.getTime() - startDateTime.getTime())
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    if (seconds > 0) {
      timeSpent = durationFormatter.exactDuration(seconds);
    } else {
      message = PropertiesKey.PLUGIN_INVALID_TIME_INTERVAL;
      return INPUT;
    }
    return SUCCESS;
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
   * @param worklogs
   *          The worklogs.
   * @return The array of the ids.
   */
  private List<Long> copyWorklogIdsToArray(final List<EveritWorklog> worklogs) {
    List<Long> worklogIds = new ArrayList<>();
    for (EveritWorklog worklog : worklogs) {
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
    String result = createWorklog(worklogValues.getIssueKey(), worklogValues.getCommentForActions(),
        date, worklogValues.getStartTime(), timeSpent);
    if (SUCCESS.equals(result)) {
      if ((actionWorklogId != null) && "copy".equals(actionFlag)) {
        actionFlag = "";
        return redirectWithDateFormattedParameterOnly(result,
            decideToShowWarningUrl());
      }
      try {
        loadWorklogs();
        worklogValues.setStartTime(timetrackerManager.lastEndTime(worklogs));
      } catch (ParseException | DataAccessException e) {
        LOGGER.error("Error when try set the plugin variables.", e);
        stacktrace = ExceptionUtil.getStacktrace(e);
        return ERROR;
      }
      worklogValues.setEndTime(DateTimeConverterUtil.dateTimeToString(new Date()));
      worklogValues.setComment("");

      return redirectWithDateAndWorklogParams(result, decideToShowWarningUrl());
    }
    return result;
  }

  private String createWorklog(final String issueKey, final String commentForActions,
      final Date date, final String startTime, final String timeSpent) {
    try {
      RemainingEstimateType remainingEstimateType =
          RemainingEstimateType.valueOf(worklogValues.getRemainingEstimateType());
      String optinalValue = "";
      if (RemainingEstimateType.NEW.equals(remainingEstimateType)) {
        optinalValue = worklogValues.getNewEstimate();
      } else if (RemainingEstimateType.MANUAL.equals(remainingEstimateType)) {
        optinalValue = worklogValues.getAdjustmentAmount();
      }
      WorklogParameter worklogParameter = new WorklogParameter(issueKey,
          commentForActions,
          date,
          startTime,
          timeSpent,
          optinalValue,
          remainingEstimateType);
      worklogManager.createWorklog(worklogParameter);
    } catch (WorklogException e) {
      message = e.getMessage();
      messageParameter = e.messageParameter;
      return INPUT;
    }
    return SUCCESS;
  }

  private String decideToShowWarningUrl() {
    if (endDateTime != null) {
      try {
        Date worklogEndDate = DateTimeConverterUtil.stringToDateAndTime(date, endDateTime);
        if (userSettings.isShowFutureLogWarning() && new Date().before(worklogEndDate)) {
          return FUTURE_WORKLOG_WARNING_URL_PARAMETER;
        }
      } catch (IllegalArgumentException e) {
        LOGGER.error("parse failed", e);
      }
    }
    return "";
  }

  private String deleteWorklog() {
    try {
      String type = getHttpRequest().getParameter("remainingEstimateTypeForDelete");
      String optionalValue = null;
      RemainingEstimateType remainingEstimateType = RemainingEstimateType.valueOf(type);
      if (RemainingEstimateType.NEW.equals(remainingEstimateType)) {
        optionalValue = getHttpRequest().getParameter("newEstimateForDelete");
      } else if (RemainingEstimateType.MANUAL.equals(remainingEstimateType)) {
        optionalValue = getHttpRequest().getParameter("adjustmentAmountForDelete");
      }
      worklogManager.deleteWorklog(actionWorklogId, optionalValue, remainingEstimateType);
    } catch (WorklogException e) {
      message = e.getMessage();
      messageParameter = e.messageParameter;
      return INPUT;
    }
    return redirectWithDateFormattedParameterOnly(SUCCESS, "");
  }

  @Override
  public String doDefault() {
    defaultCommand = true;
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    beforeActions();

    try {
      loadWorklogs();
      parseWorklogValues();
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    afterActions();
    return INPUT;
  }

  @Override
  public String doExecute() {
    String checkConditionsResult = checkConditions();
    if (checkConditionsResult != null) {
      return checkConditionsResult;
    }

    beforeActions();

    parsedEditAllIds = parseEditAllIds(getHttpRequest().getParameter(Parameter.EDIT_ALL));

    try {
      loadWorklogs();
      parseWorklogValues();
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    String result = handleAction();

    // redirected or error result.
    if (NONE.equals(result) || ERROR.equals(result)) {
      return result;
    }

    // success or input result
    loggedDays = timetrackerManager.getLoggedDaysOfTheMonth(date);

    try {
      loadWorklogs();
    } catch (ParseException | DataAccessException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }

    afterActions();
    return result;
  }

  private String editAction() {
    String validateInputFieldsResult = validateInputFields();
    if (validateInputFieldsResult.equals(INPUT)) {
      return INPUT;
    }

    try {
      RemainingEstimateType remainingEstimateType =
          RemainingEstimateType.valueOf(worklogValues.getRemainingEstimateType());
      String optinalValue = "";
      if (RemainingEstimateType.NEW.equals(remainingEstimateType)) {
        optinalValue = worklogValues.getNewEstimate();
      } else if (RemainingEstimateType.MANUAL.equals(remainingEstimateType)) {
        optinalValue = worklogValues.getAdjustmentAmount();
      }
      WorklogParameter worklogParameter = new WorklogParameter(worklogValues.getIssueKey(),
          worklogValues.getCommentForActions(),
          date,
          worklogValues.getStartTime(),
          timeSpent,
          optinalValue,
          remainingEstimateType);
      worklogManager.editWorklog(actionWorklogId, worklogParameter);
    } catch (WorklogException e) {
      message = e.getMessage();
      messageParameter = e.messageParameter;
      return INPUT;
    }
    return redirectWithDateFormattedParameterOnly(SUCCESS, decideToShowWarningUrl());
  }

  private String editAllAction() {
    // parse the editAllIds
    List<Long> editWorklogIds = parseEditAllIds(getHttpRequest().getParameter(Parameter.EDIT_ALL));
    try {
      // edit the worklogs!
      for (Long editWorklogId : editWorklogIds) {
        try {
          EveritWorklog editWorklog = worklogManager.getWorklog(editWorklogId);
          WorklogParameter worklogParameter = new WorklogParameter(editWorklog.getIssue(),
              editWorklog.getBody(),
              date,
              editWorklog.getStartTime(),
              DateTimeConverterUtil.stringTimeToString(editWorklog.getDuration()),
              "",
              RemainingEstimateType.AUTO);
          worklogManager.editWorklog(editWorklog.getWorklogId(), worklogParameter);
          endDateTime = DateTimeConverterUtil.stringTimeToDateTime(editWorklog.getEndTime());
        } catch (WorklogException e) {
          message = e.getMessage();
          messageParameter = e.messageParameter;
          return redirectWithDateFormattedAndMessagesParameter(INPUT, decideToShowWarningUrl());
        }
      }
    } catch (ParseException | DataAccessException | IllegalArgumentException e) {
      LOGGER.error("Error when try set the plugin variables.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }
    return redirectWithDateFormattedParameterOnly(SUCCESS, decideToShowWarningUrl());
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

  public String getContextPath() {
    return contextPath;
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

  public String getEditAllIds() {
    return editAllIds;
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

  public List<String> getGenerateAutoCompleteList() {
    return TimeAutoCompleteUtil.generateAutoCompleteList();
  }

  public String getIssueCollectorSrc() {
    return issueCollectorSrc;
  }

  public boolean getIssueRegexIsNotEmpty() {
    List<Pattern> nonWorkingIssuePatterns = globalSettings.getNonWorkingIssuePatterns();
    return !nonWorkingIssuePatterns.isEmpty();
  }

  public IssueRenderContext getIssueRenderContext() {
    return issueRenderContext;
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

  public String getStacktrace() {
    return stacktrace;
  }

  public SummaryDTO getSummaryDTO() {
    return summaryDTO;
  }

  public TimeTrackerUserSettings getUserSettings() {
    return userSettings;
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

  public WorklogValues getWorklogValues() {
    return worklogValues;
  }

  private String handleAction() {
    String result = NONE;
    if (getHttpRequest().getParameter(Parameter.LW_CHGDATE) != null) {
      result = moveAllAction();
    } else if (ACTION_DELETE.equals(actionFlag) && (actionWorklogId != null)) {
      result = deleteWorklog();
    } else if (getHttpRequest().getParameter(Parameter.LW_SAVE) == null) {
      result = notSaveAction();
    } else if (ACTION_EDIT_ALL.equals(actionFlag)) {
      result = editAllAction();
    } else if (ACTION_EDIT.equals(actionFlag)) {
      result = editAction();
    } else {
      result = createOrCopyAction();
    }
    return result;
  }

  public boolean isDefaultCommand() {
    return defaultCommand;
  }

  public boolean isShowMoveAllNoPermission() {
    return showMoveAllNoPermission;
  }

  private void loadWorklogs()
      throws ParseException, DataAccessException {
    worklogs = worklogManager.getWorklogs(null, date, null);
    worklogsIds = copyWorklogIdsToArray(worklogs);
  }

  private String moveAllAction() {
    String worklogsIdsValues = getHttpRequest().getParameter(Parameter.WORKLOGS_IDS);
    if ((worklogsIdsValues != null) && !"".equals(worklogsIdsValues)) {
      editAllIds = worklogsIdsValues;
      actionFlag = ACTION_EDIT_ALL;
      parsedEditAllIds = parseEditAllIds(editAllIds);
      if (parsedEditAllIds.size() != worklogs.size()) {
        showMoveAllNoPermission = true;
      }
    }
    return SUCCESS;
  }

  private void normalizeContextPath() {
    String path = getHttpRequest().getContextPath();
    if ((path.length() > 0) && "/".equals(path.substring(path.length() - 1))) {
      contextPath = path.substring(0, path.length() - 1);
    } else {
      contextPath = path;
    }
  }

  /**
   * Date change action handler.
   *
   * @return null if the current action is not a Date change action
   */
  private String notSaveAction() {
    try {
      if ((actionWorklogId != null)) {
        EveritWorklog editWorklog;
        try {
          editWorklog = worklogManager.getWorklog(actionWorklogId);
        } catch (WorklogException e) {
          message = e.getMessage();
          messageParameter = e.messageParameter;
          return redirectWithDateFormattedAndMessagesParameter(INPUT, decideToShowWarningUrl());
        }
        if (ACTION_EDIT.equals(actionFlag)) {
          worklogValues.setStartTime(editWorklog.getStartTime());
          worklogValues.setEndTime(editWorklog.getEndTime());
          worklogValues.setDurationTime(editWorklog.getDuration());
        }
        String comment = editWorklog.getBody();
        worklogValues.setIssueKey(editWorklog.getIssue());
        comment = comment.replace("\"", "\\\"");
        comment = comment.replace("\r", "\\r");
        comment = comment.replace("\n", "\\n");
        worklogValues.setComment(comment);
      }

      String editAllValue = getHttpRequest().getParameter(Parameter.EDIT_ALL);
      if (editAllValue != null) {
        editAllIds = editAllValue;
      }
    } catch (ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      stacktrace = ExceptionUtil.getStacktrace(e);
      return ERROR;
    }
    return SUCCESS;
  }

  private void parseDateParam() {
    String dateFromParam = getHttpRequest().getParameter(Parameter.DATE);
    if ((dateFromParam != null) && !"".equals(dateFromParam)) {
      dateFormatted = Long.valueOf(dateFromParam);
      date = new Date(dateFormatted);
    } else {
      if (userSettings.isActualDate()) {
        date = Calendar.getInstance().getTime();
        dateFormatted = date.getTime();
      } else {
        date = timetrackerManager.firstMissingWorklogsDate(globalSettings.getExcludeDates(),
            globalSettings.getIncludeDates());
        dateFormatted = date.getTime();
      }
    }

  }

  private void parseDateParamAfterDateSwitcher(final boolean dayBack, final boolean dayNext,
      final boolean today) {
    Calendar tempCal = Calendar.getInstance();
    tempCal.setTime(date);
    if (dayNext) {
      tempCal.add(Calendar.DAY_OF_YEAR, 1);
      date = tempCal.getTime();
      dateFormatted = date.getTime();
    } else if (dayBack) {
      tempCal.add(Calendar.DAY_OF_YEAR, -1);
      date = tempCal.getTime();
      dateFormatted = date.getTime();
    } else if (today) {
      date = new Date();
      dateFormatted = date.getTime();
    }
  }

  private List<Long> parseEditAllIds(final String editAllValues) {
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

  private void parseMessageParam() {
    String messageParam = getHttpRequest().getParameter(Parameter.MESSAGE);
    if ((messageParam != null) && !"".equals(messageParam)) {
      message = messageParam;
      String messageParameterParam = getHttpRequest().getParameter(Parameter.MESSAGE_PARAMETER);
      if ((messageParameterParam != null) && !"".equals(messageParameterParam)) {
        messageParameter = messageParameterParam;
      }
    }
  }

  private void parseWorklogValues() throws ParseException {
    String worklogValuesJson = getHttpRequest().getParameter(Parameter.WORKLOG_VALUES_JSON);
    if ((worklogValuesJson != null) && !"".equals(worklogValuesJson)) {
      worklogValues = TimetrackerUtil.convertJsonToWorklogValues(worklogValuesJson);
    } else {
      worklogValues = new WorklogValues();
      worklogValues.setRemainingEstimateType(RemainingEstimateType.AUTO.name());
      worklogValues.setEndTime(DateTimeConverterUtil.dateTimeToString(new Date()));
      worklogValues.setStartTime(timetrackerManager.lastEndTime(worklogs));
      worklogValues.setIsDuration(userSettings.isActiveFieldDuration());
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
  }

  private String redirectWithDateAndWorklogParams(final String action,
      final String warningUrlParameter) {
    String returnJson = TimetrackerUtil.convertWorklogValuesToJson(worklogValues);
    setReturnUrl(
        String.format(SELF_WITH_DATE_WORKLOG_URL_FORMAT,
            dateFormatted,
            TimetrackerUtil.urlEndcodeHandleException(returnJson)) + warningUrlParameter);
    return getRedirect(action);
  }

  private String redirectWithDateFormattedAndMessagesParameter(final String action,
      final String warningUrlParameter) {
    setReturnUrl(
        String.format(SELF_WITH_DATE_MESSAGES_URL_FORMAT,
            dateFormatted, message, messageParameter) + warningUrlParameter);
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

  private String validateInputFields() {
    if (worklogValues.getIssueKey() == null) {
      message = PropertiesKey.MISSING_ISSUE;
      return INPUT;
    }
    String startTimeValue = worklogValues.getStartTime();
    if (!DateTimeConverterUtil.isValidTime(startTimeValue)) {
      message = PropertiesKey.INVALID_START_TIME;
      return INPUT;
    }
    if (worklogValues.isDuration()) {
      String result = calculateTimeSpentForDuration(worklogValues.getStartTime(),
          worklogValues.getDurationTime());
      if (!result.equals(SUCCESS)) {
        return result;
      }
    } else {
      String result =
          calculateTimeSpentForEndTime(worklogValues.getStartTime(), worklogValues.getEndTime());
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
