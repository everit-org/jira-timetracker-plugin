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
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.AnalyticsStatusChangedEvent;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.EveritWorklogComparator;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The implementation of the {@link JiraTimetrackerPlugin}.
 */
public class JiraTimetrackerPluginImpl implements JiraTimetrackerPlugin, InitializingBean,
    DisposableBean, Serializable {

  private static final int DATE_LENGTH = 7;

  private static final String DATE_PARSE = "plugin.date_parse";

  private static final int DEFAULT_CHECK_TIME_IN_MINUTES = 1200;

  private static final String FEEDBACK_EMAIL_DEFAULT_VALUE = "${jttp.feedback.email}";

  private static final String FEEDBACK_EMAIL_TO = "FEEDBACK_EMAIL_TO";

  private static final int FIFTEEN_MINUTES = 15;

  private static final int FIVE_MINUTES = 5;

  private static final String INVALID_ISSUE = "plugin.invalid_issue";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerPluginImpl.class);

  private static final int MINUTES_IN_HOUR = 60;

  private static final String NOPERMISSION_CREATE_WORKLOG = "jttp.nopermission.worklog.create";

  private static final String NOPERMISSION_DELETE_WORKLOG = "jttp.nopermission.worklog.delete";

  private static final String NOPERMISSION_ISSUE = "plugin.nopermission_issue";

  private static final String NOPERMISSION_UPDATE_WORKLOG = "jttp.nopermission.worklog.update";

  /**
   * A day in minutes.
   */
  private static final int ONE_DAY_IN_MINUTES = 1440;

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private static final int TEN_MINUTES = 10;

  private static final int THIRTY_MINUTES = 30;

  private static final int TWENTY_MINUTES = 20;

  private static final String WORKLOG_CREATE_FAIL = "plugin.worklog.create.fail";

  /**
   * The parsed analytics check value.
   */
  private boolean analyticsCheckValue;

  private AnalyticsSender analyticsSender;

  /**
   * The collector issues ids.
   */
  private List<Pattern> collectorIssuePatterns;

  /**
   * The collector issues ids.
   */
  private List<Pattern> defaultNonEstimedIssuePatterns = new ArrayList<>();

  /**
   * The summary filter issues ids.
   */
  private final List<Pattern> defaultNonWorkingIssueIds = new ArrayList<>();

  /**
   * The parsed exclude dates.
   */
  private Set<String> excludeDatesSet = new HashSet<>();

  /**
   * The exclude dates from the properties file.
   */
  private String excludeDatesString;

  private String feedBackEmailTo;

  /**
   * The plugin global setting form the settingsFactory.
   */
  private PluginSettings globalSettings;

  /**
   * The parsed include dates.
   */
  private Set<String> includeDatesSet = new HashSet<>();

  /**
   * The include dates from the properties file.
   */
  private String includeDatesString;

  /**
   * The issue check time in minutes.
   */
  private long issueCheckTimeInMinutes;

  /**
   * The issues Estimated Time Checker Future.
   */
  private ScheduledFuture<?> issueEstimatedTimeCheckerFuture;

  /**
   * The summary filter issues ids.
   */
  private List<Pattern> nonWorkingIssuePatterns;

  private Map<String, String> piwikPorpeties;

  private List<String> pluginGroups;

  /**
   * The plugin setting form the settingsFactory.
   */
  private PluginSettings pluginSettings;

  /**
   * The plugin setting values.
   */
  private PluginSettingsValues pluginSettingsValues;

  /**
   * The plugin universal unique identifier generated the first run of the
   * {@link InitializingBean#afterPropertiesSet()} method. Stored in the jira global settings.
   */
  private String pluginUUID;

  /**
   * The plugin Scheduled Executor Service.
   */
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(1);

  /**
   * The PluginSettingsFactory.
   */
  private final PluginSettingsFactory settingsFactory;

  private List<String> timetrackerGroups;

  /**
   * Time tracking configuration.
   */
  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public JiraTimetrackerPluginImpl(final PluginSettingsFactory settingsFactory,
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final AnalyticsSender analyticsSender) {
    this.settingsFactory = settingsFactory;
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.analyticsSender = analyticsSender;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    generatePluginUUID();

    loadJttpBuildProperties();

    setDefaultVariablesValue();

    final Runnable issueEstimatedTimeChecker = new IssueEstimatedTimeChecker(
        this);

    issueEstimatedTimeCheckerFuture = scheduledExecutorService
        .scheduleAtFixedRate(issueEstimatedTimeChecker,
            calculateInitialDelay(issueCheckTimeInMinutes),
            ONE_DAY_IN_MINUTES, TimeUnit.MINUTES);
  }

  private long calculateInitialDelay(final long time) {
    Calendar now = Calendar.getInstance();
    long hours = now.get(Calendar.HOUR_OF_DAY);
    long minutes = now.get(Calendar.MINUTE);
    long initialDelay = time - ((hours * MINUTES_IN_HOUR) + minutes);
    if (initialDelay < 0) {
      initialDelay = initialDelay + ONE_DAY_IN_MINUTES;
    }
    return initialDelay;
  }

  public int countRealWorkDaysInWeek(final List<String> weekDaysAsString) {
    int exludeDates = CollectionUtils.countMatches(excludeDatesSet, new Predicate() {

      @Override
      public boolean evaluate(final Object object) {
        return weekDaysAsString.contains(object);
      }
    });
    int includeDates = CollectionUtils.countMatches(includeDatesSet, new Predicate() {

      @Override
      public boolean evaluate(final Object object) {
        return weekDaysAsString.contains(object);
      }
    });
    return (7 - exludeDates) + includeDates;
  }

  private List<Long> createProjects(final ApplicationUser loggedInUser) {
    Collection<Project> projects = ComponentAccessor.getPermissionManager()
        .getProjects(Permissions.BROWSE, loggedInUser);

    List<Long> projectList = new ArrayList<>();
    for (Project project : projects) {
      projectList.add(project.getId());
    }
    return projectList;
  }

  @Override
  public ActionResult createWorklog(final String issueId,
      final String comment, final Date date,
      final String startTime, final String timeSpent) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    MutableIssue issue = issueManager.getIssueObject(issueId);
    if (issue == null) {
      return new ActionResult(ActionResultStatus.FAIL,
          INVALID_ISSUE, issueId);
    }
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue,
        user)) {
      return new ActionResult(ActionResultStatus.FAIL,
          NOPERMISSION_ISSUE, issueId);
    }
    Date startDate;
    try {
      startDate = DateTimeConverterUtil.stringToDateAndTime(date, startTime);
    } catch (ParseException e) {
      return new ActionResult(ActionResultStatus.FAIL,
          DATE_PARSE, date + " " + startTime);
    }

    WorklogNewEstimateInputParameters params = WorklogInputParametersImpl
        .issue(issue).startDate(startDate).timeSpent(timeSpent)
        .comment(comment).buildNewEstimate();
    WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
    if (!worklogService.hasPermissionToCreate(serviceContext, issue, true)) {
      return new ActionResult(ActionResultStatus.FAIL,
          NOPERMISSION_CREATE_WORKLOG, issueId);
    }
    WorklogResult worklogResult = worklogService.validateCreate(
        serviceContext, params);
    if (worklogResult == null) {
      return new ActionResult(ActionResultStatus.FAIL,
          WORKLOG_CREATE_FAIL);
    }
    Worklog createdWorklog = worklogService.createAndAutoAdjustRemainingEstimate(serviceContext,
        worklogResult, true);
    if (createdWorklog == null) {
      return new ActionResult(ActionResultStatus.FAIL,
          WORKLOG_CREATE_FAIL);
    }

    return new ActionResult(ActionResultStatus.SUCCESS,
        "plugin.worklog.create.success");
  }

  private List<EntityCondition> createWorklogQueryExprList(final ApplicationUser user,
      final Calendar startDate, final Calendar endDate) {

    String userKey = user.getKey();

    EntityExpr startExpr = new EntityExpr("startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
            startDate.getTimeInMillis()));
    EntityExpr endExpr = new EntityExpr("startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));
    EntityExpr userExpr = new EntityExpr("author", EntityOperator.EQUALS,
        userKey);

    List<EntityCondition> exprList = new ArrayList<>();
    exprList.add(userExpr);
    exprList.add(startExpr);
    exprList.add(endExpr);
    return exprList;
  }

  private List<EntityCondition> createWorklogQueryExprListWithPermissionCheck(
      final String selectedUser,
      final ApplicationUser loggedInUser,
      final Calendar startDate, final Calendar endDate) {

    String userKey = ((selectedUser == null) || "".equals(selectedUser))
        ? loggedInUser.getKey() : selectedUser;

    List<Long> projects = createProjects(loggedInUser);

    EntityExpr startExpr = new EntityExpr("startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
            startDate.getTimeInMillis()));
    EntityExpr endExpr = new EntityExpr("startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));
    EntityExpr userExpr = new EntityExpr("author", EntityOperator.EQUALS,
        userKey);
    List<EntityCondition> exprList = new ArrayList<>();
    exprList.add(userExpr);
    exprList.add(startExpr);
    exprList.add(endExpr);

    if (!projects.isEmpty()) {
      EntityExpr projectExpr = new EntityExpr("project", EntityOperator.IN, projects);
      exprList.add(projectExpr);
    }
    return exprList;
  }

  @Override
  public ActionResult deleteWorklog(final Long id) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
    WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
    WorklogManager worklogManager = ComponentAccessor.getWorklogManager();
    Worklog worklog = worklogManager.getById(id);
    if (!worklogService.hasPermissionToDelete(serviceContext, worklog)) {
      return new ActionResult(ActionResultStatus.FAIL,
          NOPERMISSION_DELETE_WORKLOG, worklog.getIssue().getKey());
    }
    WorklogResult deleteWorklogResult = worklogService.validateDelete(
        serviceContext, id);
    if (deleteWorklogResult == null) {
      return new ActionResult(ActionResultStatus.FAIL,
          "plugin.worklog.delete.fail", id.toString());
    }
    worklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext,
        deleteWorklogResult, true);
    return new ActionResult(ActionResultStatus.SUCCESS,
        "plugin.worklog.delete.success", id.toString());
  }

  @Override
  public void destroy() throws Exception {
    scheduledExecutorService.shutdown();
    issueEstimatedTimeCheckerFuture.cancel(true);
  }

  @Override
  public ActionResult editWorklog(final Long id, final String issueId,
      final String comment, final Date date, final String time,
      final String timeSpent) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(user);

    WorklogManager worklogManager = ComponentAccessor.getWorklogManager();
    Worklog worklog = worklogManager.getById(id);
    IssueManager issueManager = ComponentAccessor.getIssueManager();
    MutableIssue issue = issueManager.getIssueObject(issueId);
    if (issue == null) {
      return new ActionResult(ActionResultStatus.FAIL,
          "plugin.invalide_issue", issueId);
    }
    if (!worklog.getIssue().getKey().equals(issueId)) {
      PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
      if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue,
          user)) {
        return new ActionResult(ActionResultStatus.FAIL,
            NOPERMISSION_ISSUE, issueId);
      }
      ActionResult deleteResult = deleteWorklog(id);
      if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
        return deleteResult;
      }
      ActionResult createResult = createWorklog(issueId, comment,
          date, time, timeSpent);
      if (createResult.getStatus() == ActionResultStatus.FAIL) {
        return createResult;
      }
    } else {
      Date dateCreate;
      try {
        dateCreate = DateTimeConverterUtil
            .stringToDateAndTime(date, time);
      } catch (ParseException e) {
        return new ActionResult(ActionResultStatus.FAIL,
            DATE_PARSE + date + " " + time);
      }
      WorklogInputParameters params = WorklogInputParametersImpl
          .issue(issue).startDate(dateCreate).timeSpent(timeSpent)
          .comment(comment).worklogId(id).issue(issue).build();
      WorklogService worklogService = ComponentAccessor.getComponent(WorklogService.class);
      if (!worklogService.hasPermissionToUpdate(serviceContext, worklog)) {
        return new ActionResult(ActionResultStatus.FAIL,
            NOPERMISSION_UPDATE_WORKLOG, issueId);
      }
      WorklogResult worklogResult = worklogService.validateUpdate(
          serviceContext, params);
      if (worklogResult == null) {
        return new ActionResult(ActionResultStatus.FAIL,
            "plugin.worklog.update.fail");
      }

      worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext,
          worklogResult, true);

    }
    return new ActionResult(ActionResultStatus.SUCCESS,
        "plugin.worklog.update.success");

  }

  @Override
  public Date firstMissingWorklogsDate() throws GenericEntityException {
    Calendar scannedDate = Calendar.getInstance();
    // one week
    scannedDate.set(Calendar.DAY_OF_YEAR,
        scannedDate.get(Calendar.DAY_OF_YEAR) - DateTimeConverterUtil.DAYS_PER_WEEK);
    for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
      // convert date to String
      Date scanedDateDate = scannedDate.getTime();
      String scanedDateString = DateTimeConverterUtil.dateToFixFormatString(scanedDateDate);
      // check excludse - pass
      if (excludeDatesSet.contains(scanedDateString)) {
        scannedDate.set(Calendar.DAY_OF_YEAR, scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!includeDatesSet.contains(scanedDateString)
          && ((scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
              || (scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))) {
        scannedDate.set(Calendar.DAY_OF_YEAR,
            scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      boolean isDateContainsWorklog = isContainsWorklog(scanedDateDate);
      if (!isDateContainsWorklog) {
        return scanedDateDate;
      } else {
        scannedDate.set(Calendar.DAY_OF_YEAR,
            scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
      }
    }
    // if we find everything all right then return with the current date
    return scannedDate.getTime();
  }

  private void generatePluginUUID() {
    globalSettings = settingsFactory.createGlobalSettings();
    setPluginUUID();
    if ((pluginUUID == null) || pluginUUID.isEmpty()) {
      pluginUUID = UUID.randomUUID().toString();
      globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
          + GlobalSettingsKey.JTTP_PLUGIN_UUID,
          pluginUUID);
    }
  }

  @Override
  public List<Pattern> getCollectorIssuePatterns() {
    if (collectorIssuePatterns == null) {
      collectorIssuePatterns = defaultNonEstimedIssuePatterns;
    }
    return collectorIssuePatterns;
  }

  @Override
  public List<MissingsWorklogsDTO> getDates(final String selectedUser, final Date from,
      final Date to,
      final boolean workingHour, final boolean checkNonWorking)
      throws GenericEntityException {
    List<MissingsWorklogsDTO> datesWhereNoWorklog = new ArrayList<>();
    Calendar fromDate = Calendar.getInstance();
    fromDate.setTime(from);
    Calendar toDate = Calendar.getInstance();
    toDate.setTime(to);
    while (!fromDate.after(toDate)) {
      String currentDateString = DateTimeConverterUtil.dateToFixFormatString(fromDate.getTime());
      if (excludeDatesSet.contains(currentDateString)) {
        fromDate.add(Calendar.DATE, 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!includeDatesSet.contains(currentDateString)
          && ((fromDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
              || (fromDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))) {
        fromDate.add(Calendar.DATE, 1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      DecimalFormat decimalFormat = new DecimalFormat("#.#");

      if (workingHour) {
        double missingsTime = isContainsEnoughWorklog(fromDate.getTime(),
            checkNonWorking);
        if (missingsTime > 0) {
          missingsTime = missingsTime / DateTimeConverterUtil.SECONDS_PER_MINUTE
              / DateTimeConverterUtil.MINUTES_PER_HOUR;
          // BigDecimal bd =
          // new BigDecimal(Double.toString(missingsTime)).setScale(2, RoundingMode.HALF_EVEN);
          // missingsTime = bd.doubleValue();
          datesWhereNoWorklog
              .add(new MissingsWorklogsDTO((Date) fromDate.getTime().clone(),
                  decimalFormat.format(missingsTime)));
        }
      } else {
        if (!isContainsWorklog(fromDate.getTime())) {
          datesWhereNoWorklog.add(new MissingsWorklogsDTO((Date) fromDate.getTime().clone(),
              decimalFormat.format(timeTrackingConfiguration.getHoursPerDay().doubleValue())));
        }
      }

      fromDate.add(Calendar.DATE, 1);

    }
    Collections.reverse(datesWhereNoWorklog);
    return datesWhereNoWorklog;
  }

  private int getEndTimeChange() {
    int endTimeChange = FIVE_MINUTES;

    Object endTimeChangeObj =
        pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE);
    if (endTimeChangeObj != null) {
      try {
        endTimeChange = Integer.parseInt(endTimeChangeObj.toString());
        if (!validateTimeChange(Integer.toString(endTimeChange))) {
          endTimeChange = FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated endTime change value. Set the default value (1).", e);
      }
    }
    return endTimeChange;
  }

  @Override
  public List<String> getExcludeDaysOfTheMonth(final Date date) {
    String fixDate = DateTimeConverterUtil.dateToFixFormatString(date);
    List<String> resultexcludeDays = new ArrayList<>();
    for (String exludeDate : excludeDatesSet) {
      // TODO this if not handle the 2013-4-04 date..... this is wrong or
      // not? .... think about it.
      if (exludeDate.startsWith(fixDate.substring(0, DATE_LENGTH))) {
        resultexcludeDays.add(exludeDate.substring(exludeDate.length() - 2));
      }
    }

    return resultexcludeDays;
  }

  private String getFromMail() {
    if (ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined()) {
      return ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer().getDefaultFrom();
    }
    return null;
  }

  @Override
  public List<Issue> getIssues() throws GenericEntityException {
    // List<GenericValue> issuesGV = null;
    // issuesGV = ComponentAccessor.getOfBizDelegator().findAll("Issue");
    List<Issue> issues = new ArrayList<>();
    // for (GenericValue issueGV : issuesGV) {
    // issues.add(IssueImpl.getIssueObject(issueGV));
    // }
    return issues;
  }

  @Override
  public List<String> getLoggedDaysOfTheMonth(final Date date)
      throws GenericEntityException {
    List<String> resultDays = new ArrayList<>();
    int dayOfMonth = 1;
    Calendar startCalendar = Calendar.getInstance();
    startCalendar.setTime(date);
    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    Date start = startCalendar.getTime();

    while (dayOfMonth <= DateTimeConverterUtil.LAST_DAY_OF_MONTH) {
      if (isContainsWorklog(start)) {
        resultDays.add(Integer.toString(dayOfMonth));
      }
      startCalendar.set(Calendar.DAY_OF_MONTH, ++dayOfMonth);
      start = startCalendar.getTime();
    }

    return resultDays;
  }

  @Override
  public String getPiwikPorperty(final String key) {
    return piwikPorpeties.get(key);
  }

  @Override
  public List<String> getProjectsId() {
    List<String> projectsId = new ArrayList<>();
    List<GenericValue> projectsGV = ComponentAccessor.getOfBizDelegator().findAll("Project");
    for (GenericValue project : projectsGV) {
      projectsId.add(project.getString("id"));
    }
    return projectsId;
  }

  private int getStartTimeChange() {
    int startTimeChange = FIVE_MINUTES;

    Object startTimeChangeObj =
        pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE);
    if (startTimeChangeObj != null) {
      try {
        startTimeChange = Integer.parseInt(startTimeChangeObj.toString());
        if (!validateTimeChange(Integer.toString(startTimeChange))) {
          startTimeChange = FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated startTime change value. Set the default value (1).", e);
      }
    }
    return startTimeChange;
  }

  @Override
  public EveritWorklog getWorklog(final Long worklogId) throws ParseException {
    WorklogManager worklogManager = ComponentAccessor.getWorklogManager();
    Worklog worklog = worklogManager.getById(worklogId);
    return new EveritWorklog(worklog);
  }

  @Override
  public List<EveritWorklog> getWorklogs(final String selectedUser, final Date date,
      final Date finalDate)
      throws ParseException {
    Calendar startDate = DateTimeConverterUtil.setDateToDayStart(date);
    Calendar endDate = (Calendar) startDate.clone();
    if (finalDate == null) {
      endDate.add(Calendar.DAY_OF_MONTH, 1);
    } else {
      endDate = DateTimeConverterUtil.setDateToDayStart(finalDate);
      endDate.add(Calendar.DAY_OF_MONTH, 1);
    }

    List<EveritWorklog> worklogs = new ArrayList<>();

    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser loggedInUser = authenticationContext.getUser();

    String userKey;
    if ((selectedUser == null) || "".equals(selectedUser)) {
      userKey = loggedInUser.getKey();
    } else {
      userKey = ComponentAccessor.getUserUtil().getUserByName(selectedUser).getKey();
    }

    List<EntityCondition> exprList = createWorklogQueryExprListWithPermissionCheck(userKey,
        loggedInUser, startDate, endDate);

    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator()
        .findByAnd("IssueWorklogView", exprList);

    for (GenericValue worklogGv : worklogGVList) {
      EveritWorklog worklog = new EveritWorklog(worklogGv);
      worklogs.add(worklog);
    }

    Collections.sort(worklogs, EveritWorklogComparator.INSTANCE);
    return worklogs;
  }

  /**
   * Check the given date is contains enough worklog. The worklog spent time have to be equal or
   * greater then 8 hours.
   *
   * @param date
   *          The date what have to check.
   * @param checkNonWorking
   *          Exclude or not the non-working issues.
   * @return The number of missings hours.
   * @throws GenericEntityException
   *           If GenericEntity Exception.
   */
  private double isContainsEnoughWorklog(final Date date,
      final boolean checkNonWorking) throws GenericEntityException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar startDate = DateTimeConverterUtil.setDateToDayStart(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);
    double workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    double expectedTimeSpent = workHoursPerDay * DateTimeConverterUtil.SECONDS_PER_MINUTE
        * DateTimeConverterUtil.MINUTES_PER_HOUR;

    List<EntityCondition> exprList = createWorklogQueryExprList(user, startDate, endDate);
    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator().findByAnd("Worklog",
        exprList);
    if ((worklogGVList == null) || worklogGVList.isEmpty()) {
      return expectedTimeSpent;
    }
    if (checkNonWorking) {
      removeNonWorkingIssues(worklogGVList);
    }
    long timeSpent = 0;
    for (GenericValue worklog : worklogGVList) {
      timeSpent += worklog.getLong("timeworked").longValue();
    }
    double missingsTime = expectedTimeSpent - timeSpent;
    return missingsTime;
  }

  /**
   * Check the given date, the user have worklogs or not.
   *
   * @param date
   *          The date what have to check.
   * @return If The user have worklogs the given date then true, esle false.
   * @throws GenericEntityException
   *           GenericEntity Exception.
   */
  private boolean isContainsWorklog(final Date date)
      throws GenericEntityException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar startDate = DateTimeConverterUtil.setDateToDayStart(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);

    List<EntityCondition> exprList = createWorklogQueryExprList(user, startDate,
        endDate);

    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator().findByAnd("Worklog",
        exprList);

    return !((worklogGVList == null) || worklogGVList.isEmpty());
  }

  @Override
  public String lastEndTime(final List<EveritWorklog> worklogs)
      throws ParseException {
    if ((worklogs == null) || (worklogs.size() == 0)) {
      return "08:00";
    }
    String endTime = worklogs.get(0).getEndTime();
    for (int i = 1; i < worklogs.size(); i++) {
      Date first = DateTimeConverterUtil.stringTimeToDateTime(worklogs
          .get(i - 1).getEndTime());
      Date second = DateTimeConverterUtil.stringTimeToDateTime(worklogs
          .get(i).getEndTime());
      if (first.compareTo(second) == 1) {
        endTime = worklogs.get(i - 1).getEndTime();
      } else {
        endTime = worklogs.get(i).getEndTime();
      }
    }
    return endTime;
  }

  private void loadJttpBuildProperties() {
    Properties properties = PropertiesUtil.getJttpBuildProperties();

    feedBackEmailTo = properties.getProperty(FEEDBACK_EMAIL_TO);

    piwikPorpeties = new HashMap<>();
    piwikPorpeties.put(PiwikPropertiesUtil.PIWIK_HOST,
        properties.getProperty(PiwikPropertiesUtil.PIWIK_HOST));
    piwikPorpeties.put(PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID,
        properties.getProperty(PiwikPropertiesUtil.PIWIK_TIMETRACKER_SITEID));
    piwikPorpeties.put(PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID,
        properties.getProperty(PiwikPropertiesUtil.PIWIK_WORKLOGS_SITEID));
    piwikPorpeties.put(PiwikPropertiesUtil.PIWIK_CHART_SITEID,
        properties.getProperty(PiwikPropertiesUtil.PIWIK_CHART_SITEID));
    piwikPorpeties.put(PiwikPropertiesUtil.PIWIK_TABLE_SITEID,
        properties.getProperty(PiwikPropertiesUtil.PIWIK_TABLE_SITEID));
  }

  @Override
  public PluginSettingsValues loadPluginSettings() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    globalSettings = settingsFactory.createGlobalSettings();
    setNonWorkingIssuePatterns();
    setCollectorIssuePatterns();
    setExcludeDates();
    setIncludeDates();
    setTimetrackerGroups();
    setPluginGroups();
    analyticsCheckValue = JiraTimetrackerAnalytics.getAnalyticsCheck(globalSettings);
    setPluginUUID();

    pluginSettings = settingsFactory.createSettingsForKey(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX + user.getName());

    // the default is the Daily Progress Indicator
    Boolean isProgressIndicatorDaily = true;
    if ("false"
        .equals(pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_PROGRESS_INDICATOR))) {
      isProgressIndicatorDaily = false;
    }

    // the default is the Actual Date
    Boolean isActualDate = true;
    if ("false".equals(pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE))) {
      isActualDate = false;
    }

    // the default coloring is TRUE
    Boolean isColoring = true;
    if ("false".equals(pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_COLORIG))) {
      isColoring = false;
    }

    // SET startTime Change the default value is 5
    int startTimeChange = getStartTimeChange();
    // SET endtTime Change the default value is 5
    int endTimeChange = getEndTimeChange();
    // Here set the other values
    pluginSettingsValues = new PluginSettingsValues()
        .isProgressIndicatordaily(isProgressIndicatorDaily)
        .actualDate(isActualDate)
        .excludeDates(excludeDatesString)
        .includeDates(includeDatesString)
        .coloring(isColoring)
        .filteredSummaryIssues(nonWorkingIssuePatterns)
        .collectorIssues(collectorIssuePatterns)
        .startTimeChange(startTimeChange)
        .endTimeChange(endTimeChange)
        .analyticsCheck(analyticsCheckValue)
        .pluginUUID(pluginUUID)
        .pluginGroups(pluginGroups)
        .timetrackingGroups(timetrackerGroups);
    return pluginSettingsValues;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void removeNonWorkingIssues(final List<GenericValue> worklogGVList) {
    List<GenericValue> worklogsCopy = new ArrayList<>(worklogGVList);
    // if we have non-estimated issues

    // TODO FIXME summaryFilteredIssuePatterns rename nonworking
    // pattern
    if ((nonWorkingIssuePatterns != null) && !nonWorkingIssuePatterns.isEmpty()) {
      IssueManager issueManager = ComponentAccessor.getIssueManager();
      for (GenericValue worklog : worklogsCopy) {
        Long issueId = worklog.getLong("issue");
        MutableIssue issue = issueManager.getIssueObject(issueId);
        for (Pattern issuePattern : nonWorkingIssuePatterns) {
          boolean issueMatches = issuePattern.matcher(issue.getKey()).matches();
          // if match not count in summary
          if (issueMatches) {
            worklogGVList.remove(worklog);
            break;
          }
        }
      }
    }
  }

  @Override
  public void savePluginSettings(
      final PluginSettingsValues pluginSettingsParameters) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    pluginSettings = settingsFactory
        .createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + user.getName());
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_PROGRESS_INDICATOR,
        pluginSettingsParameters.isProgressIndicatorDaily.toString());
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE,
        pluginSettingsParameters.isActualDate.toString());
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_COLORIG,
        pluginSettingsParameters.isColoring.toString());
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE,
        Integer.toString(pluginSettingsParameters.startTimeChange));
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE,
        Integer.toString(pluginSettingsParameters.endTimeChange));

    globalSettings = settingsFactory.createGlobalSettings();
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS,
        pluginSettingsParameters.filteredSummaryIssues);
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES,
        pluginSettingsParameters.collectorIssues);
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES,
        pluginSettingsParameters.excludeDates);
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_INCLUDE_DATES,
        pluginSettingsParameters.includeDates);

    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_PLUGIN_PERMISSION,
        pluginSettingsParameters.pluginGroups);
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_TIMETRACKER_PERMISSION,
        pluginSettingsParameters.timetrackingGroups);

    Object analyticsCheckObj = globalSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_ANALYTICS_CHECK_CHANGE);
    boolean analyticsCheck = analyticsCheckObj == null
        ? true
        : Boolean.parseBoolean(analyticsCheckObj.toString());

    if (analyticsCheck != pluginSettingsParameters.analyticsCheck) {
      setPluginUUID();
      analyticsSender.send(new AnalyticsStatusChangedEvent(pluginUUID,
          pluginSettingsParameters.analyticsCheck));
    }
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_ANALYTICS_CHECK_CHANGE,
        Boolean.toString(pluginSettingsParameters.analyticsCheck));
  }

  @Override
  public void sendEmail(final String mailSubject, final String mailBody) {
    String defaultFrom = getFromMail();
    if (!FEEDBACK_EMAIL_DEFAULT_VALUE.equals(feedBackEmailTo) && (defaultFrom != null)) {
      Email email = new Email(feedBackEmailTo);
      email.setFrom(defaultFrom);
      email.setSubject(mailSubject);
      email.setBody(mailBody);
      SingleMailQueueItem singleMailQueueItem = new SingleMailQueueItem(email);
      singleMailQueueItem.setMailThreader(null);
      ComponentAccessor.getMailQueue().addItem(singleMailQueueItem);
    } else {
      LOGGER.error(
          "Feedback not sent, beacause To mail address is not defined. \n"
              + "The message: \n" + mailBody);
    }
  }

  private void setCollectorIssuePatterns() {
    List<String> tempIssuePatternList = (List<String>) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES);
    if (tempIssuePatternList != null) {
      // add collector issues
      collectorIssuePatterns = new ArrayList<>();
      for (String tempIssuePattern : tempIssuePatternList) {
        collectorIssuePatterns.add(Pattern.compile(tempIssuePattern));
      }
    } else {
      collectorIssuePatterns = defaultNonEstimedIssuePatterns;
    }
  }

  /**
   * Set the default values of the important variables.
   */
  private void setDefaultVariablesValue() {
    // DEFAULT 20:00
    issueCheckTimeInMinutes = DEFAULT_CHECK_TIME_IN_MINUTES;
    // Default exclude and include dates set are empty. No DATA!!
    // Default: no non working issue. we simple use the empty list
    // defaultNonWorkingIssueIds = new ArrayList<Long>();
    // The default non estimted issues regex. All issue non estimeted.
    defaultNonEstimedIssuePatterns = new ArrayList<>();
    defaultNonEstimedIssuePatterns.add(Pattern.compile(".*"));
  }

  private void setExcludeDates() {
    String tempSpecialDates = (String) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES);
    excludeDatesSet = new HashSet<>();
    excludeDatesString = "";
    if (tempSpecialDates != null) {
      excludeDatesString = tempSpecialDates;
      for (String excludeDate : excludeDatesString.split(",")) {
        excludeDatesSet.add(excludeDate);
      }
    }
  }

  private void setIncludeDates() {
    String tempSpecialDates = (String) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_INCLUDE_DATES);
    if (tempSpecialDates != null) {
      includeDatesString = tempSpecialDates;
      includeDatesSet = new HashSet<>();
      for (String includeDate : includeDatesString.split(",")) {
        includeDatesSet.add(includeDate);
      }
    } else {
      // Default Empty
      includeDatesSet = new HashSet<>();
      includeDatesString = "";
    }
  }

  private void setNonWorkingIssuePatterns() {
    List<String> tempIssuePatternList = (List<String>) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS);
    if (tempIssuePatternList != null) {
      // add non working issues
      nonWorkingIssuePatterns = new ArrayList<>();
      for (String tempIssuePattern : tempIssuePatternList) {
        nonWorkingIssuePatterns.add(Pattern.compile(tempIssuePattern));
      }
    } else {
      // default! from properties load default issues!!
      nonWorkingIssuePatterns = defaultNonWorkingIssueIds;
    }
  }

  private void setPluginGroups() {
    List<String> pluginGroupsNames = (List<String>) globalSettings
        .get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_PLUGIN_PERMISSION);
    pluginGroups = new ArrayList<>();
    if (pluginGroupsNames != null) {
      pluginGroups = pluginGroupsNames;
    }
  }

  private void setPluginUUID() {
    pluginUUID = JiraTimetrackerAnalytics.getPluginUUID(globalSettings);
  }

  private void setTimetrackerGroups() {
    List<String> timetrackerGroupsNames = (List<String>) globalSettings
        .get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_TIMETRACKER_PERMISSION);
    timetrackerGroups = new ArrayList<>();
    if (timetrackerGroupsNames != null) {
      timetrackerGroups = timetrackerGroupsNames;
    }
  }

  @Override
  public long summary(final Date startSummary,
      final Date finishSummary,
      final List<Pattern> issuePatterns) throws GenericEntityException {
    // TODO JIRAPLUGIN-348 refactor
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar start = Calendar.getInstance();
    start.setTime(startSummary);
    Calendar finish = Calendar.getInstance();
    finish.setTime(finishSummary);

    List<EntityCondition> exprList = createWorklogQueryExprList(user,
        start, finish);

    List<GenericValue> worklogs;
    // worklog query
    worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("Worklog",
        exprList);
    List<GenericValue> worklogsCopy = new ArrayList<>();
    worklogsCopy.addAll(worklogs);
    // if we have non-estimated issues
    if ((issuePatterns != null) && !issuePatterns.isEmpty()) {
      for (GenericValue worklog : worklogsCopy) {
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Long issueId = worklog.getLong("issue");
        MutableIssue issue = issueManager.getIssueObject(issueId);
        for (Pattern issuePattern : issuePatterns) {
          boolean issueMatches = issuePattern.matcher(issue.getKey())
              .matches();
          // if match not count in summary
          if (issueMatches) {
            worklogs.remove(worklog);
            break;
          }
        }
      }
    }
    long timeSpent = 0;
    for (GenericValue worklog : worklogs) {
      timeSpent += worklog.getLong("timeworked").longValue();
    }
    return timeSpent;
  }

  @Override
  public String summaryToGui(final Date startSummary, final Date finishSummary,
      final List<Pattern> issueIds)
      throws GenericEntityException {
    // TODO JIRAPLUGIN-348 this is works for the daily progress indicator
    long timeSpent = summary(startSummary, finishSummary, issueIds);
    return new DurationFormatter().exactDuration(timeSpent);
  }

  @Override
  public boolean validateTimeChange(final String changeValue)
      throws NumberFormatException {
    int changeValueInt = Integer.parseInt(changeValue);

    switch (changeValueInt) {
      case FIVE_MINUTES:
        return true;
      case TEN_MINUTES:
        return true;
      case FIFTEEN_MINUTES:
        return true;
      case TWENTY_MINUTES:
        return true;
      case THIRTY_MINUTES:
        return true;
      default:
        return false;
    }

  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
