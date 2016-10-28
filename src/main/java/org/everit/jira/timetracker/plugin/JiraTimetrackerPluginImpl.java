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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.NoEstimateUsageChangedEvent;
import org.everit.jira.analytics.event.NonWorkingUsageEvent;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.core.util.WorklogUtil;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.mail.queue.SingleMailQueueItem;

/**
 * The implementation of the {@link JiraTimetrackerPlugin}.
 */
public class JiraTimetrackerPluginImpl implements JiraTimetrackerPlugin, InitializingBean,
    DisposableBean, Serializable {

  private static final int DEFAULT_CHECK_TIME_IN_MINUTES = 1200;

  private static final String FEEDBACK_EMAIL_DEFAULT_VALUE = "${jttp.feedback.email}";

  private static final String FEEDBACK_EMAIL_TO = "FEEDBACK_EMAIL_TO";

  public static final int FIFTEEN_MINUTES = 15;

  public static final int FIVE_MINUTES = 5;

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerPluginImpl.class);

  private static final int MINUTES_IN_HOUR = 60;

  /**
   * A day in minutes.
   */
  private static final int ONE_DAY_IN_MINUTES = 1440;

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  public static final int TEN_MINUTES = 10;

  public static final int THIRTY_MINUTES = 30;

  public static final int TWENTY_MINUTES = 20;

  public static final String UNKNOW_USER_NAME = "UNKNOW_USER_NAME";

  private AnalyticsSender analyticsSender;

  private String feedBackEmailTo;

  /**
   * The issues Estimated Time Checker Future.
   */
  private ScheduledFuture<?> issueEstimatedTimeCheckerFuture;

  private Map<String, String> piwikPorpeties;

  /**
   * The plugin Scheduled Executor Service.
   */
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(1);

  private TimetrackerSettingsHelper settingsHelper;

  /**
   * Time tracking configuration.
   */
  private TimeTrackingConfiguration timeTrackingConfiguration;

  /**
   * Default constructor.
   */
  public JiraTimetrackerPluginImpl(
      final TimeTrackingConfiguration timeTrackingConfiguration,
      final AnalyticsSender analyticsSender,
      final TimetrackerSettingsHelper settingsHelper) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    loadJttpBuildProperties();

    final Runnable issueEstimatedTimeChecker = new IssueEstimatedTimeChecker(
        settingsHelper);

    issueEstimatedTimeCheckerFuture = scheduledExecutorService
        .scheduleAtFixedRate(issueEstimatedTimeChecker,
            calculateInitialDelay(),
            ONE_DAY_IN_MINUTES, TimeUnit.MINUTES);

    sendNonEstAndNonWorkAnaliticsEvent();
  }

  private long calculateInitialDelay() {
    // DEFAULT 20:00
    Calendar now = Calendar.getInstance();
    long hours = now.get(Calendar.HOUR_OF_DAY);
    long minutes = now.get(Calendar.MINUTE);
    long initialDelay =
        DEFAULT_CHECK_TIME_IN_MINUTES - ((hours * MINUTES_IN_HOUR) + minutes);
    if (initialDelay < 0) {
      initialDelay = initialDelay + ONE_DAY_IN_MINUTES;
    }
    return initialDelay;
  }

  @Override
  public void destroy() throws Exception {
    scheduledExecutorService.shutdown();
    issueEstimatedTimeCheckerFuture.cancel(true);
  }

  @Override
  public List<MissingsWorklogsDTO> getDates(final String selectedUser, final Date from,
      final Date to,
      final boolean workingHour, final boolean checkNonWorking,
      final TimeTrackerGlobalSettings settings)
          throws GenericEntityException {
    List<MissingsWorklogsDTO> datesWhereNoWorklog = new ArrayList<>();
    Calendar fromDate = Calendar.getInstance();
    fromDate.setTime(from);
    Calendar toDate = Calendar.getInstance();
    toDate.setTime(to);
    while (!fromDate.after(toDate)) {
      String currentDateString = DateTimeConverterUtil.dateToFixFormatString(fromDate.getTime());
      if (settings.getExcludeDatesAsSet().contains(currentDateString)) {
        fromDate.add(Calendar.DATE, 1);
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!settings.getIncludeDatesAsSet().contains(currentDateString)
          && ((fromDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
              || (fromDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))) {
        fromDate.add(Calendar.DATE, 1);
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      DecimalFormat decimalFormat = new DecimalFormat("#.#");

      if (workingHour) {
        double missingsTime = isContainsEnoughWorklog(fromDate.getTime(),
            checkNonWorking, settings.getNonWorkingIssuePatterns());
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
        if (!TimetrackerUtil.isContainsWorklog(fromDate.getTime())) {
          datesWhereNoWorklog.add(new MissingsWorklogsDTO((Date) fromDate.getTime().clone(),
              decimalFormat.format(timeTrackingConfiguration.getHoursPerDay().doubleValue())));
        }
      }

      fromDate.add(Calendar.DATE, 1);

    }
    Collections.reverse(datesWhereNoWorklog);
    return datesWhereNoWorklog;
  }

  private String getFromMail() {
    if (ComponentAccessor.getMailServerManager().isDefaultSMTPMailServerDefined()) {
      return ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer().getDefaultFrom();
    }
    return null;
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
      final boolean checkNonWorking, final List<Pattern> nonWorkingIssuePatterns)
          throws GenericEntityException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar startDate = DateTimeConverterUtil.setDateToDayStart(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);
    double workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    double expectedTimeSpent = workHoursPerDay * DateTimeConverterUtil.SECONDS_PER_MINUTE
        * DateTimeConverterUtil.MINUTES_PER_HOUR;

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user, startDate, endDate);
    List<GenericValue> worklogGVList =
        ComponentAccessor.getOfBizDelegator().findByAnd("IssueWorklogView", exprList);
    if ((worklogGVList == null) || worklogGVList.isEmpty()) {
      return expectedTimeSpent;
    }
    if (checkNonWorking) {
      removeNonWorkingIssues(worklogGVList, nonWorkingIssuePatterns);
    }
    long timeSpent = 0;
    for (GenericValue worklog : worklogGVList) {
      timeSpent += worklog.getLong("timeworked").longValue();
    }
    double missingsTime = expectedTimeSpent - timeSpent;
    return missingsTime;
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

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void removeNonWorkingIssues(final List<GenericValue> worklogGVList,
      final List<Pattern> nonWorkingIssuePatterns) {
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

  private void sendNonEstAndNonWorkAnaliticsEvent() {
    TimeTrackerGlobalSettings loadGlobalSettings = settingsHelper.loadGlobalSettings();
    List<Pattern> tempIssuePatterns = loadGlobalSettings.getIssuePatterns();
    List<Pattern> tempSummaryFilter = loadGlobalSettings.getNonWorkingIssuePatterns();
    String pluginId = loadGlobalSettings.getPluginUUID();

    NoEstimateUsageChangedEvent analyticsEvent =
        new NoEstimateUsageChangedEvent(pluginId, tempIssuePatterns, UNKNOW_USER_NAME);
    analyticsSender.send(analyticsEvent);
    NonWorkingUsageEvent nonWorkingUsageEvent =
        new NonWorkingUsageEvent(pluginId,
            tempSummaryFilter.isEmpty(), UNKNOW_USER_NAME);
    analyticsSender.send(nonWorkingUsageEvent);
  }

  @Override
  public long summary(final Date startSummary,
      final Date finishSummary,
      final List<Pattern> issuePatterns) throws GenericEntityException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar start = Calendar.getInstance();
    start.setTime(startSummary);
    Calendar finish = Calendar.getInstance();
    finish.setTime(finishSummary);

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user,
            start, finish);

    List<GenericValue> worklogs;
    // worklog query
    worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("IssueWorklogView", exprList);
    List<GenericValue> worklogsCopy = new ArrayList<>();
    worklogsCopy.addAll(worklogs);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    ProjectRoleManager projectRoleManager =
        ComponentAccessor.getComponent(ProjectRoleManager.class);
    // if we have non-estimated issues
    for (GenericValue worklog : worklogsCopy) {
      Long issueId = worklog.getLong("issue");
      MutableIssue issue = issueManager.getIssueObject(issueId);
      if ((issuePatterns != null) && !issuePatterns.isEmpty()) {
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
      boolean hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(user,
          issueManager,
          groupManager,
          projectRoleManager,
          worklog);
      if (!hasWorklogVisibility) {
        worklogs.remove(worklog);
      }
    }
    long timeSpent = 0;
    for (GenericValue worklog : worklogs) {
      timeSpent += worklog.getLong("timeworked").longValue();
    }
    return timeSpent;
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
