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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;
import org.everit.jira.timetracker.plugin.PluginCondition;
import org.everit.jira.timetracker.plugin.TimetrackerCondition;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.everit.jira.timetracker.plugin.dto.WorklogValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The timetracker web action support class.
 */
public class JiraTimetrackerWebAction extends JiraWebActionSupport {

  private static final int DAYS_IN_WEEK = 7;

  /**
   * The default worklog ID.
   */
  private static final Long DEFAULT_WORKLOG_ID = Long.valueOf(0);

  private static final String FUTURE_WORKLOG_WARNING_URL_PARAMETER = "&showWarning=true";

  private static final int HUNDRED = 100;

  private static final String INVALID_DURATION_TIME = "plugin.invalid_durationTime";

  private static final String INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String JIRA_HOME_URL = "/secure/Dashboard.jspa";
  /**
   * The JiraTimetrackerWebAction logger..
   */
  private static final Logger LOGGER = Logger
      .getLogger(JiraTimetrackerWebAction.class);

  private static final String MISSING_ISSUE = "plugin.missing_issue";

  private static final String PARAM_DATE = "date";

  private static final int SECOND_INHOUR = 3600;

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

  /**
   * The summary of day.
   */
  private String dayFilteredSummary = "";

  private long dayFilteredSummaryInSecond;

  private String daySumIndustryFormatted;

  /**
   * The summary of day.
   */
  private String daySummary = "";

  private long daySummaryInSeconds;

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

  /**
   * List of the exclude days of the date variable current months.
   */
  private List<String> excludeDays = new ArrayList<>();

  private double expectedWorkSecondsInDay;

  private double expectedWorkSecondsInMonth;

  private double expectedWorkSecondsInWeek;

  private String hoursPerDayFormatted;

  private String installedPluginId;

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
   * The {@link JiraTimetrackerPlugin}.
   */
  private transient JiraTimetrackerPlugin jiraTimetrackerPlugin;

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

  /**
   * The summary of month.
   */
  private String monthFilteredSummary = "";

  private long monthFilteredSummaryInSecond;

  /**
   * The summary of month.
   */
  private String monthSummary = "";

  private long monthSummaryInSecounds;

  private List<Long> parsedEditAllIds = Collections.emptyList();

  private PluginCondition pluginCondition;

  private final PluginSettingsFactory pluginSettingsFactory;

  /**
   * The IDs of the projects.
   */
  private List<String> projectsId;

  /**
   * The worklog start time.
   */
  private String startTime = "";

  /**
   * The startTime input field changer buttons value.
   */
  private int startTimeChange;

  /**
   * The spent time in Jira time format (1h 20m).
   */
  private String timeSpent = "";

  private TimetrackerCondition timetrackingCondition;

  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * The summary of week.
   */
  private String weekFilteredSummary = "";

  private long weekFilteredSummaryInSecond;

  /**
   * The summary of week.
   */
  private String weekSummary = "";

  private long weekSummaryInSecond;

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
   *
   * @param jiraTimetrackerPlugin
   *          The {@link JiraTimetrackerPlugin}.
   * @param timeTrackingConfiguration
   *          the {@link TimeTrackingConfiguration}.
   * @param pluginSettingsFactory
   *          the {@link PluginSettingsFactory}.
   */
  public JiraTimetrackerWebAction(
      final JiraTimetrackerPlugin jiraTimetrackerPlugin,
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final PluginSettingsFactory pluginSettingsFactory) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.pluginSettingsFactory = pluginSettingsFactory;
    timetrackingCondition = new TimetrackerCondition(jiraTimetrackerPlugin);
    pluginCondition = new PluginCondition(jiraTimetrackerPlugin);
    issueRenderContext = new IssueRenderContext(null);
    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    atlassianWikiRenderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
  }

  private void calculateExpectedWorkSecondsInDay() {
    expectedWorkSecondsInDay =
        timeTrackingConfiguration.getHoursPerDay().doubleValue() * SECOND_INHOUR;
  }

  private void calculateExpectedWorkSecondsInMonth() {
    Calendar dayIndex = createNewCalendarWithWeekStart();
    dayIndex.setTime(date);
    dayIndex.set(Calendar.DAY_OF_MONTH, 1);
    Calendar monthLastDay = createNewCalendarWithWeekStart();
    monthLastDay.setTime(date);
    monthLastDay.set(Calendar.DAY_OF_MONTH,
        monthLastDay.getActualMaximum(Calendar.DAY_OF_MONTH));

    long daysInMonth =
        TimeUnit.DAYS.convert(monthLastDay.getTimeInMillis() - dayIndex.getTimeInMillis(),
            TimeUnit.MILLISECONDS) + 1;
    int excludeDtaes = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(date).size();
    int includeDtaes = jiraTimetrackerPlugin.getIncludeDaysOfTheMonth(date).size();
    int nonWorkDaysCount = 0;
    for (int i = 1; i < daysInMonth; i++) {
      int dayOfweek = dayIndex.get(Calendar.DAY_OF_WEEK);
      if ((dayOfweek == Calendar.SUNDAY) || (dayOfweek == Calendar.SATURDAY)) {
        nonWorkDaysCount++;
      }
      dayIndex.add(Calendar.DAY_OF_MONTH, 1);
    }
    long realWorkDaysInMonth = (daysInMonth + includeDtaes) - excludeDtaes - nonWorkDaysCount;
    expectedWorkSecondsInMonth = realWorkDaysInMonth * expectedWorkSecondsInDay;
  }

  private void calculateExpectedWorkSecondsInWeek() {
    List<String> weekdaysAsString = new ArrayList<>();
    Calendar dayIndex = createNewCalendarWithWeekStart();
    dayIndex.setTime(getWeekStart(date));
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    for (int i = 0; i < DAYS_IN_WEEK; i++) {
      weekdaysAsString.add(simpleDateFormat.format(dayIndex.getTime()));
      dayIndex.add(Calendar.DAY_OF_MONTH, 1);
    }
    double realWorkDaysInWeek = jiraTimetrackerPlugin.countRealWorkDaysInWeek(weekdaysAsString);
    expectedWorkSecondsInWeek =
        realWorkDaysInWeek * expectedWorkSecondsInDay;
  }

  private String checkConditions() {
    boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
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

  private double correctNoneWorkIndicatorPercent(final double realWorkIndicatorPrecent,
      final double noneWorkIndicatorPrecent) {
    double correctedNoneWorkIndicatorPercent = noneWorkIndicatorPrecent;
    double sumPercent = noneWorkIndicatorPrecent + realWorkIndicatorPrecent;
    if (sumPercent > HUNDRED) {
      correctedNoneWorkIndicatorPercent =
          noneWorkIndicatorPrecent - (sumPercent - HUNDRED);
    }
    return correctedNoneWorkIndicatorPercent;
  }

  private Calendar createNewCalendarWithWeekStart() {
    ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
    boolean useISO8601 = applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);
    Calendar c = Calendar.getInstance();
    if (useISO8601) {
      c.setFirstDayOfWeek(Calendar.MONDAY);
    }
    return c;
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
    ActionResult createResult = jiraTimetrackerPlugin.createWorklog(
        issueKey, commentForActions, date, startTime, timeSpent);
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
      ActionResult deleteResult = jiraTimetrackerPlugin.deleteWorklog(actionWorklogId);
      if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
        message = deleteResult.getMessage();
        messageParameter = deleteResult.getMessageParameter();
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

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID);

    normalizeContextPath();
    loadIssueCollectorSrc();

    loadPluginSettingAndParseResult();
    if (setDateAndDateFormatted().equals(ERROR)) {
      return ERROR;
    }

    excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(date);
    projectsId = jiraTimetrackerPlugin.getProjectsId();
    try {
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(date);
    } catch (GenericEntityException e1) {
      // Not return with error. Log the error and set a message to inform the user.
      // The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling", e1);
      message = "plugin.calendar.logged.coloring.fail";
    }
    try {
      loadWorklogsAndMakeSummary();
    } catch (Exception e) {
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

    analyticsDTO = JiraTimetrackerAnalytics.getAnalyticsDTO(pluginSettingsFactory,
        PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID);

    normalizeContextPath();
    loadIssueCollectorSrc();

    loadPluginSettingAndParseResult();

    dateSwitcherAction();
    parseActionParams();
    parsedEditAllIds = parseEditAllIds(getHttpRequest().getParameter("editAll"));
    parseEditAllAction();

    excludeDays = jiraTimetrackerPlugin.getExcludeDaysOfTheMonth(date);
    projectsId = jiraTimetrackerPlugin.getProjectsId();

    String deleteResult = deleteWorklog();
    if (deleteResult != null) {
      try {
        loadWorklogsAndMakeSummary();
      } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
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
    } catch (GenericEntityException | ParseException | DataAccessException | SQLException e) {
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
    ActionResult updateResult = jiraTimetrackerPlugin.editWorklog(
        actionWorklogId, issueKey, commentForActions, date,
        startTime, timeSpent);
    if (updateResult.getStatus() == ActionResultStatus.FAIL) {
      message = updateResult.getMessage();
      messageParameter = updateResult.getMessageParameter();
      return INPUT;
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
      EveritWorklog editWorklog = jiraTimetrackerPlugin
          .getWorklog(editWorklogId);
      jiraTimetrackerPlugin.editWorklog(editWorklog
          .getWorklogId(), editWorklog.getIssue(), editWorklog
              .getBody(),
          date, editWorklog.getStartTime(),
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

  /**
   * Calculate and if necassary correct the day none working indicator percent.
   *
   * @return The day filtered none work indicator percent.
   */
  public double getDayFilteredNonWorkIndicatorPrecent() {
    double dayFilteredRealWorkIndicatorPrecent = getDayFilteredRealWorkIndicatorPrecent();
    double dayFilteredNoneWorkIndicatorPrecent =
        ((daySummaryInSeconds - dayFilteredSummaryInSecond) / expectedWorkSecondsInDay)
            * HUNDRED;
    dayFilteredNoneWorkIndicatorPrecent = correctNoneWorkIndicatorPercent(
        dayFilteredRealWorkIndicatorPrecent, dayFilteredNoneWorkIndicatorPrecent);
    return dayFilteredNoneWorkIndicatorPrecent;
  }

  public double getDayFilteredRealWorkIndicatorPrecent() {
    return (dayFilteredSummaryInSecond / expectedWorkSecondsInDay) * HUNDRED;
  }

  public String getDayFilteredSummary() {
    return dayFilteredSummary;
  }

  public double getDayIndicatorPrecent() {
    return (daySummaryInSeconds / expectedWorkSecondsInDay) * HUNDRED;
  }

  public String getDaySumIndustryFormatted() {
    return daySumIndustryFormatted;
  }

  public String getDaySummary() {
    return daySummary;
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

  public List<String> getExcludeDays() {
    return excludeDays;
  }

  public String getFormattedExpectedWorkTimeInDay() {
    return durationFormatter.exactDuration((long) expectedWorkSecondsInDay);
  }

  public String getFormattedExpectedWorkTimeInMonth() {
    return durationFormatter.exactDuration((long) expectedWorkSecondsInMonth);
  }

  public String getFormattedExpectedWorkTimeInWeek() {
    return durationFormatter.exactDuration((long) expectedWorkSecondsInWeek);
  }

  public String getFormattedNonWorkTimeInDay() {
    return durationFormatter.exactDuration(daySummaryInSeconds - dayFilteredSummaryInSecond);
  }

  public String getFormattedNonWorkTimeInMonth() {
    return durationFormatter.exactDuration(monthSummaryInSecounds - monthFilteredSummaryInSecond);
  }

  public String getFormattedNonWorkTimeInWeek() {
    return durationFormatter.exactDuration(weekSummaryInSecond - weekFilteredSummaryInSecond);
  }

  public String getHoursPerDayFormatted() {
    return hoursPerDayFormatted;
  }

  public String getInstalledPluginId() {
    return installedPluginId;
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

  public JiraTimetrackerPlugin getJiraTimetrackerPlugin() {
    return jiraTimetrackerPlugin;
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

  /**
   * Calculate and if necassary correct the week none working indicator percent.
   *
   * @return The week filtered none work indicator percent.
   */
  public double getMonthFilteredNonWorkIndicatorPrecent() {
    double monthFilteredRealWorkIndicatorPrecent = getMonthFilteredRealWorkIndicatorPrecent();
    double monthFilteredNonWorkIndicatorPrecent =
        ((monthSummaryInSecounds - monthFilteredSummaryInSecond) / expectedWorkSecondsInMonth)
            * HUNDRED;
    monthFilteredNonWorkIndicatorPrecent = correctNoneWorkIndicatorPercent(
        monthFilteredRealWorkIndicatorPrecent, monthFilteredNonWorkIndicatorPrecent);
    return monthFilteredNonWorkIndicatorPrecent;
  }

  public double getMonthFilteredRealWorkIndicatorPrecent() {
    return (monthFilteredSummaryInSecond / expectedWorkSecondsInMonth) * HUNDRED;
  }

  public String getMonthFilteredSummary() {
    return monthFilteredSummary;
  }

  public double getMonthIndicatorPrecent() {
    return (monthSummaryInSecounds / expectedWorkSecondsInMonth) * HUNDRED;
  }

  public String getMonthSummary() {
    return monthSummary;
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

  /**
   * Calculate and if necassary correct the week none working indicator percent.
   *
   * @return The week filtered none work indicator percent.
   */
  public double getWeekFilteredNonWorkIndicatorPrecent() {
    double weekFilteredRealWorkIndicatorPrecent = getWeekFilteredRealWorkIndicatorPrecent();
    double weekFilteredNonWorkIndicatorPrecent =
        ((weekSummaryInSecond - weekFilteredSummaryInSecond) / expectedWorkSecondsInWeek)
            * HUNDRED;
    weekFilteredNonWorkIndicatorPrecent = correctNoneWorkIndicatorPercent(
        weekFilteredRealWorkIndicatorPrecent, weekFilteredNonWorkIndicatorPrecent);
    return weekFilteredNonWorkIndicatorPrecent;
  }

  public double getWeekFilteredRealWorkIndicatorPrecent() {
    return (weekFilteredSummaryInSecond / expectedWorkSecondsInWeek) * HUNDRED;
  }

  public String getWeekFilteredSummary() {
    return weekFilteredSummary;
  }

  public double getWeekIndicatorPrecent() {
    return (weekSummaryInSecond / expectedWorkSecondsInWeek) * HUNDRED;
  }

  private Date getWeekStart(final Date date) {
    Calendar c = createNewCalendarWithWeekStart();
    c.setTime(date);
    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
    c.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
    Date firstDate = c.getTime();
    return firstDate;
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
      message = INVALID_START_TIME;
      return INPUT;
    }

    if (!DateTimeConverterUtil.isValidTime(durationTime)) {
      if (!DateTimeConverterUtil.isValidJiraTime(durationTime)) {
        message = INVALID_DURATION_TIME;
        return INPUT;
      } else {
        timeSpent = durationTime;
        int seconds = DateTimeConverterUtil.jiraDurationToSeconds(durationTime);
        Date endTime = DateUtils.addSeconds(startDateTime, seconds);
        if (!DateUtils.isSameDay(startDateTime, endTime)) {
          message = INVALID_DURATION_TIME;
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
      EveritWorklog editWorklog = jiraTimetrackerPlugin.getWorklog(actionWorklogId);
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
      message = INVALID_DURATION_TIME;
      return INPUT;
    }

    long seconds = durationDateTime.getTime()
        / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
    timeSpent = durationFormatter.exactDuration(seconds);

    // check the duration time to not exceed the present day
    Date endTime = DateUtils.addSeconds(startDateTime, (int) seconds);
    if (!DateUtils.isSameDay(startDateTime, endTime)) {
      message = INVALID_DURATION_TIME;
      return INPUT;
    }
    return SUCCESS;
  }

  private void loadIssueCollectorSrc() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();
    issueCollectorSrc = properties.getProperty(PropertiesUtil.ISSUE_COLLECTOR_SRC);
  }

  private void loadPluginSettingAndParseResult() {
    PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
        .loadPluginSettings();

    isProgressDaily = pluginSettingsValues.isProgressIndicatorDaily;
    isActualDate = pluginSettingsValues.isActualDate;
    issuesRegex = pluginSettingsValues.filteredSummaryIssues;
    startTimeChange = pluginSettingsValues.startTimeChange;
    endTimeChange = pluginSettingsValues.endTimeChange;
    isColoring = pluginSettingsValues.isColoring;
    installedPluginId = pluginSettingsValues.pluginUUID;
    isRounded = pluginSettingsValues.isRounded;
    isShowFutureLogWarning = pluginSettingsValues.isShowFutureLogWarning;
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
      loggedDays = jiraTimetrackerPlugin.getLoggedDaysOfTheMonth(date);
    } catch (GenericEntityException e1) {
      // Not return whit error. Log the error and set a message to
      // inform the user. The calendar fill will missing.
      LOGGER.error(
          "Error while try to collect the logged days for the calendar color fulling",
          e1);
      message = "plugin.calendar.logged.coloring.fail";
    }
    worklogs = jiraTimetrackerPlugin.getWorklogs(null, date, null);
    worklogsIds = copyWorklogIdsToArray(worklogs);
    worklogsSizeWithoutPermissionChecks =
        jiraTimetrackerPlugin.countWorklogsWithoutPermissionChecks(date, null);

    makeSummary();
  }

  /**
   * Make summary today, this week and this month.
   *
   * @throws GenericEntityException
   *           GenericEntityException.
   */
  public void makeSummary() throws GenericEntityException {
    Calendar startCalendar = createNewCalendarWithWeekStart();
    startCalendar.setTime(date);
    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
    startCalendar.set(Calendar.MINUTE, 0);
    startCalendar.set(Calendar.SECOND, 0);
    startCalendar.set(Calendar.MILLISECOND, 0);
    Calendar originalStartcalendar = (Calendar) startCalendar.clone();
    Date start = startCalendar.getTime();

    Calendar endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);

    Date end = endCalendar.getTime();
    daySummaryInSeconds = jiraTimetrackerPlugin.summary(start, end, null);
    daySummary = durationFormatter.exactDuration(daySummaryInSeconds);
    if (getIssueRegexIsNotEmpty()) {
      dayFilteredSummaryInSecond = jiraTimetrackerPlugin.summary(start, end,
          issuesRegex);
      dayFilteredSummary = durationFormatter.exactDuration(dayFilteredSummaryInSecond);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    while (startCalendar.get(Calendar.DAY_OF_WEEK) != startCalendar.getFirstDayOfWeek()) {
      startCalendar.add(Calendar.DATE, -1); // Substract 1 day until first day of week.
    }
    start = startCalendar.getTime();
    endCalendar = (Calendar) startCalendar.clone();
    endCalendar.add(Calendar.DATE, DateTimeConverterUtil.DAYS_PER_WEEK);
    end = endCalendar.getTime();
    weekSummaryInSecond = jiraTimetrackerPlugin.summary(start, end, null);
    weekSummary = durationFormatter.exactDuration(weekSummaryInSecond);
    if (getIssueRegexIsNotEmpty()) {
      weekFilteredSummaryInSecond = jiraTimetrackerPlugin.summary(start, end,
          issuesRegex);
      weekFilteredSummary = durationFormatter.exactDuration(weekFilteredSummaryInSecond);
    }

    startCalendar = (Calendar) originalStartcalendar.clone();
    startCalendar.set(Calendar.DAY_OF_MONTH, 1);
    start = startCalendar.getTime();

    endCalendar = (Calendar) originalStartcalendar.clone();
    endCalendar.set(Calendar.DAY_OF_MONTH,
        endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    endCalendar.add(Calendar.DAY_OF_MONTH, 1);
    end = endCalendar.getTime();
    monthSummaryInSecounds = jiraTimetrackerPlugin.summary(start, end, null);
    monthSummary = durationFormatter.exactDuration(monthSummaryInSecounds);
    if (getIssueRegexIsNotEmpty()) {
      monthFilteredSummaryInSecond = jiraTimetrackerPlugin.summary(start, end,
          issuesRegex);
      monthFilteredSummary = durationFormatter.exactDuration(monthFilteredSummaryInSecond);
    }

    double hoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    hoursPerDayFormatted = durationFormatter.workHoursDayIndustryDuration();
    daySumIndustryFormatted = durationFormatter.industryDuration(daySummaryInSeconds);
    double daySumMin = daySummaryInSeconds / (double) DateTimeConverterUtil.SECONDS_PER_MINUTE;
    double daySumHour = daySumMin / DateTimeConverterUtil.MINUTES_PER_HOUR;
    dailyPercent = daySumHour / hoursPerDay;
    calculateExpectedWorkSecondsInDay();
    calculateExpectedWorkSecondsInWeek();
    calculateExpectedWorkSecondsInMonth();
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
        try {
          date = jiraTimetrackerPlugin.firstMissingWorklogsDate();
          dateFormatted = date.getTime();
        } catch (GenericEntityException e) {
          LOGGER.error("Error when try set the plugin date.", e);
        }
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
    String returnJson = JiraTimetrackerUtil.convertWorklogValuesToJson(worklogValue);
    setReturnUrl(
        String.format(SELF_WITH_DATE_WORKLOG_URL_FORMAT,
            dateFormatted,
            JiraTimetrackerUtil.urlEndcodeHandleException(returnJson)) + warningUrlParameter);
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
    return new UpdateNotifier(pluginSettingsFactory, JiraTimetrackerUtil.getLoggedUserName())
        .isShowUpdater();

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

  private String setDateAndDateFormatted() {
    String dateFromParam = getHttpRequest().getParameter(PARAM_DATE);
    if ((dateFromParam == null) || "".equals(dateFromParam)) {
      if (isActualDate) {
        date = Calendar.getInstance().getTime();
        dateFormatted = date.getTime();
      } else {
        try {
          date = jiraTimetrackerPlugin.firstMissingWorklogsDate();
          dateFormatted = date.getTime();
        } catch (GenericEntityException e) {
          LOGGER.error("Error when try set the plugin date.", e);
          return ERROR;
        }
      }
    } else {
      dateFormatted = Long.valueOf(dateFromParam);
      date = new Date(dateFormatted);
    }
    return SUCCESS;
  }

  public void setDateFormatted(final Long dateFormatted) {
    this.dateFormatted = dateFormatted;
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

  public void setExcludeDays(final List<String> excludeDays) {
    this.excludeDays = excludeDays;
  }

  /**
   * Set the read values to the input fields back.
   */
  private String setFieldsValue() {
    worklogValuesJson = getHttpRequest().getParameter("worklogValuesJson");
    if ((worklogValuesJson != null) && !"".equals(worklogValuesJson)) {
      worklogValue = JiraTimetrackerUtil.convertJsonToWorklogValues(worklogValuesJson);
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
      startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
    } catch (ParseException e) {
      LOGGER.error("Error when try parse the worklog.", e);
      return ERROR;
    }

    return null;
  }

  public void setInstalledPluginId(final String installedPluginId) {
    this.installedPluginId = installedPluginId;
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

  public void setProjectsId(final List<String> projectsId) {
    this.projectsId = projectsId;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

  public void setStartTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
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
    if (issueKey == null) {
      message = MISSING_ISSUE;
      return INPUT;
    }
    String startTimeValue = worklogValue.getStartTime();
    if (!DateTimeConverterUtil.isValidTime(startTimeValue)) {
      message = INVALID_START_TIME;
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
