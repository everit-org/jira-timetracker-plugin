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
package org.everit.jira.reporting.plugin.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.reporting.plugin.dto.PickerVersionDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.google.gson.Gson;

/**
 * Utility class to helps converts Object to Objects.
 */
public final class ConverterUtil {

  private static final String KEY_INVALID_END_TIME = "plugin.invalid_endTime";

  private static final String KEY_INVALID_START_TIME = "plugin.invalid_startTime";

  private static final String KEY_WRONG_DATES = "plugin.wrong.dates";

  public static final String VALUE_NEGATIVE_ONE = "-1";

  private static final String VALUE_NO_COMPONENT = "No component";

  private static void appendIssueAffectedVersions(final ReportSearchParam reportSearchParam,
      final List<String> issueAffectedVersions) {
    ArrayList<String> affectedVersions = new ArrayList<String>();
    for (String affectedVersion : issueAffectedVersions) {
      if (PickerVersionDTO.NO_VERSION.equals(affectedVersion)) {
        reportSearchParam.selectNoAffectedVersionIssue(true);
      } else {
        affectedVersions.add(affectedVersion);
      }
    }
    reportSearchParam.issueAffectedVersions(affectedVersions);
  }

  private static void appendIssueAssignees(final ReportSearchParam reportSearchParam,
      final List<String> issueAssignees) {
    ArrayList<String> assignees = new ArrayList<String>();
    for (String assignee : issueAssignees) {
      if (PickerUserDTO.UNASSIGNED_USER_NAME.equals(assignee)) {
        reportSearchParam.selectUnassgined(true);
      } else {
        assignees.add(assignee);
      }
    }
    reportSearchParam.issueAssignees(assignees);
  }

  private static void appendIssueComponents(final ReportSearchParam reportSearchParam,
      final List<String> issueComponents) {
    ArrayList<String> components = new ArrayList<String>();
    for (String component : issueComponents) {
      if (VALUE_NO_COMPONENT.equals(component)) {
        reportSearchParam.selectNoComponentIssue(true);
      } else {
        components.add(component);
      }
    }
    reportSearchParam.issueComponents(components);
  }

  private static void appendIssueFixedVersions(final ReportSearchParam reportSearchParam,
      final List<String> issueFixedVersions) {
    ArrayList<String> fixedVersions = new ArrayList<String>();
    for (String fixedVersion : issueFixedVersions) {
      if (PickerVersionDTO.NO_VERSION.equals(fixedVersion)) {
        reportSearchParam.selectNoFixedVersionIssue(true);
      } else if (PickerVersionDTO.RELEASED_VERSION.equals(fixedVersion)) {
        reportSearchParam.selectReleasedFixVersion(true);
      } else if (PickerVersionDTO.UNRELEASED_VERSION.equals(fixedVersion)) {
        reportSearchParam.selectUnreleasedFixVersion(true);
      } else {
        fixedVersions.add(fixedVersion);
      }
    }
    reportSearchParam.issueFixedVersions(fixedVersions);
  }

  private static void appendIssueResolution(final ReportSearchParam reportSearchParam,
      final List<String> issueResolutionIds) {
    ArrayList<String> resolutionIds = new ArrayList<String>();
    for (String resolutionId : issueResolutionIds) {
      if (VALUE_NEGATIVE_ONE.equals(resolutionId)) {
        reportSearchParam.selectUnresolvedResolution(true);
      } else {
        resolutionIds.add(resolutionId);
      }
    }
    reportSearchParam.issueResolutionIds(resolutionIds);
  }

  /**
   * Append browsable project ids to {@link ReportSearchParam}. Return the not browsable project
   * ids.
   */
  private static List<String> appendProjectIds(final ReportSearchParam reportSearchParam,
      final List<Long> projectIds) {
    Map<Long, String> projectIdProjectKeys = new HashMap<Long, String>();
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getUser();
    Collection<Project> projects =
        permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user);
    List<Long> allBrowsableProjectIds = new ArrayList<Long>();
    for (Project project : projects) {
      Long projectId = project.getId();
      allBrowsableProjectIds.add(projectId);
      projectIdProjectKeys.put(projectId, project.getKey());
    }

    List<Long> notBrowsableProjectIds = new ArrayList<Long>();

    if (projectIds.isEmpty()) {
      reportSearchParam.projectIds(allBrowsableProjectIds);
    } else {
      notBrowsableProjectIds = new ArrayList<Long>(projectIds);
      notBrowsableProjectIds.removeAll(allBrowsableProjectIds);

      List<Long> browsableProjectIds = new ArrayList<Long>(projectIds);
      browsableProjectIds.removeAll(notBrowsableProjectIds);

      reportSearchParam.projectIds(browsableProjectIds);
    }

    List<String> notBrowsableProjectKeys = new ArrayList<String>();
    for (Long projectId : notBrowsableProjectIds) {
      String projectKey = projectIdProjectKeys.get(projectId);
      notBrowsableProjectKeys.add(projectKey == null ? projectId.toString() : projectKey);
    }
    return notBrowsableProjectKeys;
  }

  /**
   * Convert {@link FilterCondition} object to {@link ConvertedSearchParam} object.
   *
   * @param filterCondition
   *          the {@link FilterCondition} object. Cannot be <code>null</code>.
   * @return the {@link ConvertedSearchParam} object.
   *
   * @throws NullPointerException
   *           if filterCondition is <code>null</code>.
   * @throws IllegalArgumentException
   *           if has problem in convert. Contains property key name in message.
   */
  public static ConvertedSearchParam convertFilterConditionToConvertedSearchParam(
      final FilterCondition filterCondition) {
    if (filterCondition == null) {
      throw new NullPointerException("filterCondition parameter is null");
    }

    ReportSearchParam reportSearchParam = new ReportSearchParam()
        .issueCreateDate(
            ConverterUtil.getDate(filterCondition.getIssueCreateDate(), KEY_INVALID_START_TIME))
        .issueEpicLinkIssueIds(filterCondition.getIssueEpicLinkIssueIds())
        .issuePriorityIds(filterCondition.getIssuePriorityIds())
        .issueReporters(filterCondition.getIssueReporters())
        .issueStatusIds(filterCondition.getIssueStatusIds())
        .issueTypeIds(filterCondition.getIssueTypeIds())
        .labels(filterCondition.getLabels())
        .worklogEndDate(
            ConverterUtil.getRequiredDate(filterCondition.getWorklogEndDate(),
                KEY_INVALID_END_TIME))
        .worklogStartDate(
            ConverterUtil.getRequiredDate(filterCondition.getWorklogStartDate(),
                KEY_INVALID_START_TIME))
        .issueKeys(filterCondition.getIssueKeys());

    ConverterUtil.appendIssueAssignees(reportSearchParam, filterCondition.getIssueAssignees());

    ConverterUtil.appendIssueResolution(reportSearchParam, filterCondition.getIssueResolutionIds());

    ConverterUtil.appendIssueAffectedVersions(reportSearchParam,
        filterCondition.getIssueAffectedVersions());

    ConverterUtil.appendIssueFixedVersions(reportSearchParam,
        filterCondition.getIssueFixedVersions());

    ConverterUtil.appendIssueComponents(reportSearchParam, filterCondition.getIssueComponents());

    List<String> notBrowsableProjectKeys =
        ConverterUtil.appendProjectIds(reportSearchParam, filterCondition.getProjectIds());

    if (reportSearchParam.worklogStartDate.after(reportSearchParam.worklogEndDate)) {
      throw new IllegalArgumentException(KEY_WRONG_DATES);
    }
    String epicName = filterCondition.getIssueEpicName();
    if ((epicName != null) && !epicName.isEmpty()) {
      reportSearchParam.issueEpicName(epicName);
    }

    List<String> users = filterCondition.getUsers();
    if (!users.isEmpty() && users.contains(PickerUserDTO.NONE_USER_NAME)) {
      users = ConverterUtil.getUserNamesFromGroup(filterCondition.getGroups());
    } else if (users.contains(PickerUserDTO.CURRENT_USER_NAME)) {
      users.remove(PickerUserDTO.CURRENT_USER_NAME);
      users.add(JiraTimetrackerUtil.getLoggedUserName());
    }
    reportSearchParam.users(users);

    if (filterCondition.getOffset() != null) {
      reportSearchParam.offset(filterCondition.getOffset());
    }

    if (filterCondition.getLimit() != null) {
      reportSearchParam.limit(filterCondition.getLimit());
    }

    return new ConvertedSearchParam()
        .notBrowsableProjectKeys(notBrowsableProjectKeys)
        .reportSearchParam(reportSearchParam);
  }

  /**
   * Convert json string to {@link FilterCondition} class.
   *
   * @param json
   *          the json string from which the object is to be deserialized to {@link FilterCondition}
   *          object. Cannot be <code>null</code>.
   * @return the {@link FilterCondition} object.
   *
   * @throws NullPointerException
   *           if json parameter is <code>null</code>.
   */
  public static FilterCondition convertJsonToFilterCondition(final String json) {
    if (json == null) {
      throw new NullPointerException("EMPTY_JSON");
    }
    return new Gson()
        .fromJson(json, FilterCondition.class);
  }

  private static Date getDate(final String date, final String errorMsgKey) {
    if ((date == null) || date.isEmpty()) {
      return null;
    }
    try {
      return DateTimeConverterUtil.stringToDate(date);
    } catch (ParseException e) {
      throw new IllegalArgumentException(errorMsgKey, e);
    }
  }

  private static Date getRequiredDate(final String date, final String errorMsgKey) {
    Date parsedDate = ConverterUtil.getDate(date, errorMsgKey);
    if (parsedDate == null) {
      throw new IllegalArgumentException(errorMsgKey);
    }
    return parsedDate;
  }

  private static List<String> getUserNamesFromGroup(final List<String> groupNames) {
    List<String> userNames = new ArrayList<String>();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    for (String groupName : groupNames) {
      Collection<String> userNamesInGroup = groupManager.getUserNamesInGroup(groupName);
      userNames.addAll(userNamesInGroup);
    }
    return userNames;
  }

  private ConverterUtil() {
  }
}
