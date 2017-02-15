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
package org.everit.jira.core.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.everit.jira.core.SupportManager;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.core.util.WorklogUtil;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Implementation of {@link SupportManager}.
 */
public class SupportComponent implements SupportManager {

  private TimeTrackingConfiguration timeTrackingConfiguration;

  public SupportComponent(final TimeTrackingConfiguration timeTrackingConfiguration) {
    this.timeTrackingConfiguration = timeTrackingConfiguration;
  }

  @Override
  public List<MissingsWorklogsDTO> getDates(final DateTimeServer from,
      final DateTimeServer to, final boolean workingHour, final boolean checkNonWorking,
      final TimeTrackerGlobalSettings settings)
      throws GenericEntityException {
    List<MissingsWorklogsDTO> datesWhereNoWorklog = new ArrayList<MissingsWorklogsDTO>();
    DateTimeServer fromDate = from;
    DateTimeServer toDate = to;
    Set<DateTime> excludeDatesAsSet = settings.getExcludeDates();
    Set<DateTime> includeDatesAsSet = settings.getIncludeDates();
    while (!fromDate.getUserTimeZone().isAfter(toDate.getUserTimeZone())) {
      if (TimetrackerUtil.containsSetTheSameDay(excludeDatesAsSet,
          fromDate.getUserTimeZone())) {
        fromDate =
            DateTimeServer.getInstanceBasedOnUserTimeZone(fromDate.getUserTimeZone().plusDays(1));
        continue;
      }
      // check includes - not check weekend
      // check weekend - pass
      if (!TimetrackerUtil.containsSetTheSameDay(includeDatesAsSet,
          fromDate.getUserTimeZone())
          && ((fromDate.getUserTimeZone().getDayOfWeek() == DateTimeConstants.SUNDAY)
              || (fromDate.getUserTimeZone().getDayOfWeek() == DateTimeConstants.SATURDAY))) {
        fromDate =
            DateTimeServer.getInstanceBasedOnUserTimeZone(fromDate.getUserTimeZone().plusDays(1));
        continue;
      }
      // check worklog. if no worklog set result else ++ scanedDate
      DecimalFormat decimalFormat = new DecimalFormat("#.#");

      if (workingHour) {
        // fromDate = DateTimeConverterUtil.setDateToDayStart(fromDate);
        double missingsTime =
            isContainsEnoughWorklog(fromDate.getUserTimeZone().toDate(),
                checkNonWorking, settings.getNonWorkingIssuePatterns());
        if (missingsTime > 0) {
          missingsTime = missingsTime / DateTimeConverterUtil.SECONDS_PER_MINUTE
              / DateTimeConverterUtil.MINUTES_PER_HOUR;
          // BigDecimal bd =
          // new BigDecimal(Double.toString(missingsTime)).setScale(2, RoundingMode.HALF_EVEN);
          // missingsTime = bd.doubleValue();
          datesWhereNoWorklog
              .add(new MissingsWorklogsDTO(fromDate.getUserTimeZone().toDate(),
                  decimalFormat.format(missingsTime)));
        }
      } else {
        if (!TimetrackerUtil.isContainsWorklog(fromDate.getUserTimeZone().toDate())) {
          datesWhereNoWorklog
              .add(new MissingsWorklogsDTO(fromDate.getUserTimeZone().toDate(),
                  decimalFormat.format(timeTrackingConfiguration.getHoursPerDay().doubleValue())));
        }
      }
      fromDate =
          DateTimeServer.getInstanceBasedOnUserTimeZone(fromDate.getUserTimeZone().plusDays(1));
    }
    Collections.reverse(datesWhereNoWorklog);
    return datesWhereNoWorklog;
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
   *          The date what have to check. Set to the start of the day and in system Time Zone.
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
    Calendar startDate = Calendar.getInstance();
    startDate.setTime(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);
    double workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
    double expectedTimeSpent = workHoursPerDay * DateTimeConverterUtil.SECONDS_PER_MINUTE
        * DateTimeConverterUtil.MINUTES_PER_HOUR;

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user, startDate.getTimeInMillis(),
            endDate.getTimeInMillis());
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

  private void removeNonWorkingIssues(final List<GenericValue> worklogGVList,
      final List<Pattern> nonWorkingIssuePatterns) {
    List<GenericValue> worklogsCopy = new ArrayList<>(worklogGVList);
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
  public long summary(final Date startSummary, final Date finishSummary,
      final List<Pattern> issuePatterns) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user,
            startSummary.getTime(), finishSummary.getTime());

    List<GenericValue> worklogs;
    // worklog query
    worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("IssueWorklogView", exprList);
    List<GenericValue> worklogsCopy = new ArrayList<>();
    worklogsCopy.addAll(worklogs);

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
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
          permissionManager,
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
}
