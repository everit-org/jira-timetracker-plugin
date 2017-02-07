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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.SearcherValue;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.PickerComponentDTO;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.reporting.plugin.dto.PickerVersionDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.settings.TimeTrackerSettingsHelper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.DefaultSearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.ParseResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.google.gson.Gson;

/**
 * Utility class to helps converts Object to Objects.
 */
public final class ConverterUtil {

  private static final String KEY_MISSING_JQL = "jtrp.plugin.missing.jql";

  private static final String KEY_WRONG_DATES = "plugin.wrong.dates";

  private static final String KEY_WRONG_JQL = "jtrp.plugin.wrong.jql";

  private static final String NO_BROWSE_PERMISSION = "jtrp.plugin.no.browse.permission";

  public static final String VALUE_NEGATIVE_ONE = "-1";

  private static void appendIssueAffectedVersions(final ReportSearchParam reportSearchParam,
      final List<String> issueAffectedVersions) {
    ArrayList<String> affectedVersions = new ArrayList<>();
    for (String affectedVersion : issueAffectedVersions) {
      if (TimetrackerUtil.getI18nText(PickerVersionDTO.NO_VERSION).equals(affectedVersion)) {
        reportSearchParam.selectNoAffectedVersionIssue(true);
      } else {
        affectedVersions.add(affectedVersion);
      }
    }
    reportSearchParam.issueAffectedVersions(affectedVersions);
  }

  private static void appendIssueAssignees(final ReportSearchParam reportSearchParam,
      final List<String> issueAssignees) {
    ArrayList<String> assignees = new ArrayList<>();
    for (String assignee : issueAssignees) {
      if (TimetrackerUtil.getI18nText(PickerUserDTO.UNASSIGNED_USER_NAME).equals(assignee)) {
        reportSearchParam.selectUnassgined(true);
      } else if (TimetrackerUtil.getI18nText(PickerUserDTO.CURRENT_USER_NAME)
          .equals(assignee)) {
        assignees.add(TimetrackerUtil.getLoggedUserName());
      } else {
        assignees.add(assignee);
      }
    }
    reportSearchParam.issueAssignees(assignees);
  }

  private static void appendIssueComponents(final ReportSearchParam reportSearchParam,
      final List<String> issueComponents) {
    ArrayList<String> components = new ArrayList<>();
    for (String component : issueComponents) {
      if (TimetrackerUtil.getI18nText(PickerComponentDTO.NO_COMPONENT).equals(component)) {
        reportSearchParam.selectNoComponentIssue(true);
      } else {
        components.add(component);
      }
    }
    reportSearchParam.issueComponents(components);
  }

  private static void appendIssueFixedVersions(final ReportSearchParam reportSearchParam,
      final List<String> issueFixedVersions) {
    ArrayList<String> fixedVersions = new ArrayList<>();
    for (String fixedVersion : issueFixedVersions) {
      if (TimetrackerUtil.getI18nText(PickerVersionDTO.NO_VERSION).equals(fixedVersion)) {
        reportSearchParam.selectNoFixedVersionIssue(true);
      } else if (TimetrackerUtil.getI18nText(PickerVersionDTO.RELEASED_VERSION)
          .equals(fixedVersion)) {
        reportSearchParam.selectReleasedFixVersion(true);
      } else if (TimetrackerUtil.getI18nText(PickerVersionDTO.UNRELEASED_VERSION)
          .equals(fixedVersion)) {
        reportSearchParam.selectUnreleasedFixVersion(true);
      } else {
        fixedVersions.add(fixedVersion);
      }
    }
    reportSearchParam.issueFixedVersions(fixedVersions);
  }

  private static void appendIssueReportes(final ReportSearchParam reportSearchParam,
      final List<String> issueReporters) {
    ArrayList<String> reporters = new ArrayList<>();
    for (String reporter : issueReporters) {
      if (PickerUserDTO.CURRENT_USER_NAME.equals(reporter)) {
        reporters.add(TimetrackerUtil.getLoggedUserName());
      } else {
        reporters.add(reporter);
      }
    }
    reportSearchParam.issueReporters(reporters);
  }

  private static void appendIssueResolution(final ReportSearchParam reportSearchParam,
      final List<String> issueResolutionIds) {
    ArrayList<String> resolutionIds = new ArrayList<>();
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
    Map<Long, String> projectIdProjectKeys = new HashMap<>();
    PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getUser();
    Collection<Project> projects =
        permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user);
    List<Long> allBrowsableProjectIds = new ArrayList<>();
    for (Project project : projects) {
      Long projectId = project.getId();
      allBrowsableProjectIds.add(projectId);
      projectIdProjectKeys.put(projectId, project.getKey());
    }

    List<Long> notBrowsableProjectIds = new ArrayList<>();

    if (projectIds.isEmpty()) {
      reportSearchParam.projectIds(allBrowsableProjectIds);
    } else {
      notBrowsableProjectIds = new ArrayList<>(projectIds);
      notBrowsableProjectIds.removeAll(allBrowsableProjectIds);

      List<Long> browsableProjectIds = new ArrayList<>(projectIds);
      browsableProjectIds.removeAll(notBrowsableProjectIds);

      reportSearchParam.projectIds(browsableProjectIds);
    }

    List<String> notBrowsableProjectKeys = new ArrayList<>();
    for (Long projectId : notBrowsableProjectIds) {
      String projectKey = projectIdProjectKeys.get(projectId);
      notBrowsableProjectKeys.add(projectKey == null ? projectId.toString() : projectKey);
    }
    return notBrowsableProjectKeys;
  }

  private static void collectUsersFromParams(final FilterCondition filterCondition,
      final ReportSearchParam reportSearchParam, final TimeTrackerSettingsHelper settingsHelper) {
    List<String> users = new ArrayList<>(filterCondition.getUsers());
    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser user = jiraAuthenticationContext.getUser();
    if (!PermissionUtil.hasBrowseUserPermission(user, settingsHelper)) {
      if ((users.size() == 1) && (users.contains(PickerUserDTO.CURRENT_USER_NAME)
          || users.contains(TimetrackerUtil.getLoggedUserName()))) {
        if (users.remove(PickerUserDTO.CURRENT_USER_NAME)) {
          users.add(TimetrackerUtil.getLoggedUserName());
        }
      } else {
        throw new IllegalArgumentException(NO_BROWSE_PERMISSION);
      }
    } else {
      if (!users.isEmpty() && users.contains(PickerUserDTO.NONE_USER_NAME)) {
        users = ConverterUtil.queryUsersInGroup(filterCondition.getGroups(), reportSearchParam);
      } else if (users.remove(PickerUserDTO.CURRENT_USER_NAME)) {
        users.add(TimetrackerUtil.getLoggedUserName());
      }
    }
    reportSearchParam.users(users);
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
      final FilterCondition filterCondition, final TimeTrackerSettingsHelper settingsHelper) {
    if (filterCondition == null) {
      throw new NullPointerException("filterCondition parameter is null");
    }

    // TODO issueCreated?
    DateTimeServer worklogEndDate =
        DateTimeServer.getInstanceBasedOnUserTimeZone(filterCondition.getWorklogEndDate());
    worklogEndDate =
        DateTimeServer.getInstanceBasedOnUserTimeZone(worklogEndDate.getUserTimeZone().plusDays(1));

    // DateTime worklogEndDate = new DateTime(filterCondition.getWorklogEndDate());
    // worklogEndDate = DateTimeConverterUtil.setDateToDayStart(worklogEndDate);
    // worklogEndDate = worklogEndDate.plusDays(1);
    // TODO check DTS?
    // worklogEndDate = DateTimeConverterUtil.convertDateZoneToUserTimeZone(worklogEndDate);

    DateTimeServer worklogStartDate =
        DateTimeServer.getInstanceBasedOnUserTimeZone(filterCondition.getWorklogStartDate());
    // worklogStartDate = DateTimeConverterUtil.setDateToDayStart(worklogStartDate);
    // TODO check DTS?
    // worklogStartDate = DateTimeConverterUtil.convertDateZoneToUserTimeZone(worklogStartDate);

    ReportSearchParam reportSearchParam = new ReportSearchParam();
    List<String> searchParamIssueKeys;
    List<String> notBrowsableProjectKeys;

    if (SearcherValue.FILTER.lowerCaseValue
        .equals(filterCondition.getSearcherValue())) {
      try {
        searchParamIssueKeys = ConverterUtil.getIssueKeysFromFilterSearcerValue(filterCondition);
        notBrowsableProjectKeys =
            ConverterUtil.appendProjectIds(reportSearchParam, new ArrayList<Long>());
      } catch (SearchException | JqlParseException e) {
        throw new IllegalArgumentException(KEY_WRONG_JQL);
      }
    } else {
      searchParamIssueKeys = filterCondition.getIssueKeys();

      ConverterUtil.setBasicSearcherValuesParams(filterCondition, reportSearchParam);

      notBrowsableProjectKeys =
          ConverterUtil.appendProjectIds(reportSearchParam, filterCondition.getProjectIds());
    }
    reportSearchParam.worklogEndDate(worklogEndDate.getSystemTimeZoneDayStartDate())
        .worklogStartDate(worklogStartDate.getSystemTimeZoneDayStartDate())
        .issueKeys(searchParamIssueKeys);

    if (!reportSearchParam.worklogStartDate.before(reportSearchParam.worklogEndDate)) {
      throw new IllegalArgumentException(KEY_WRONG_DATES);
    }
    ConverterUtil.collectUsersFromParams(filterCondition, reportSearchParam, settingsHelper);

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
   * Convert {@link FilterCondition} class to json string.
   *
   * @param filterCondition
   *          the {@link FilterCondition} object. Cannot be <code>null</code>.
   *
   * @return the json string.
   *
   * @throws NullPointerException
   *           if filterCondition parameter is <code>null</code>.
   */
  public static String convertFilterConditionToJson(final FilterCondition filterCondition) {
    if (filterCondition == null) {
      throw new NullPointerException("EMPTY_FILTER");
    }
    return new Gson().toJson(filterCondition);
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

  /**
   * Convert a {@link Pattern} list to string list.
   */
  public static List<String> convertPatternsToString(final List<Pattern> patterns) {
    List<String> convertedPatterns = new ArrayList<>(patterns.size());
    for (Pattern pattern : patterns) {
      convertedPatterns.add(pattern.pattern());
    }
    return convertedPatterns;
  }

  /**
   * Convert string to {@link OrderBy} object.
   *
   * @param orderBy
   *          the string that representation orderby. Example jtrp_col_issueKey-ASC.
   * @return the {@link OrderBy} object. If orderby string null or invalid return the
   *         {@link OrderBy#DEFAULT}.
   */
  public static OrderBy convertToOrderBy(final String orderBy) {
    if (orderBy == null) {
      return OrderBy.DEFAULT;
    }

    String[] splitOrderBy = orderBy.split("-");
    if (splitOrderBy.length != 2) {
      return OrderBy.DEFAULT;
    }

    String order = splitOrderBy[1];
    return new OrderBy()
        .columnName(splitOrderBy[0])
        .order(order)
        .asc("ASC".equals(order));
  }

  private static List<String> getIssueKeysFromFilterSearcerValue(
      final FilterCondition filterCondition) throws SearchException, JqlParseException {
    List<String> searchParamIssueKeys;
    DefaultSearchRequestService defaultSearchRequestService =
        ComponentAccessor.getComponentOfType(DefaultSearchRequestService.class);
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    User loggedInUser = authenticationContext.getLoggedInUser();
    JiraServiceContext serviceContext = new JiraServiceContextImpl(loggedInUser);
    if (filterCondition.getFilter().isEmpty()) {
      throw new IllegalArgumentException(KEY_MISSING_JQL);
    }
    SearchRequest filter =
        defaultSearchRequestService.getFilter(serviceContext, filterCondition.getFilter().get(0));
    if (filter == null) {
      throw new IllegalArgumentException(KEY_WRONG_JQL);
    }
    searchParamIssueKeys = ConverterUtil.getIssuesKeyByJQL(filter.getQuery().getQueryString());
    return searchParamIssueKeys;
  }

  // private static Date getDate(final Long date) {
  // if (date == null) {
  // return null;
  // }
  // return new Date(date);
  // }

  private static List<String> getIssuesKeyByJQL(final String jql)
      throws SearchException,
      JqlParseException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    User loggedInUser = authenticationContext.getLoggedInUser();
    List<String> issuesKeys = new ArrayList<>();
    SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
    ParseResult parseResult = searchService.parseQuery(loggedInUser, jql);
    if (parseResult.isValid()) {
      SearchResults results = searchService.search(loggedInUser,
          parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
      List<Issue> issues = results.getIssues();
      for (Issue issue : issues) {
        issuesKeys.add(issue.getKey());
      }
    } else {
      throw new JqlParseException(null, parseResult.getErrors().toString());
    }
    return issuesKeys;
  }

  private static ArrayList<String> getUserNamesFromGroup(final List<String> groupNames) {
    ArrayList<String> userNames = new ArrayList<>();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    for (String groupName : groupNames) {
      Collection<String> userNamesInGroup = groupManager.getUserNamesInGroup(groupName);
      for (String userName : userNamesInGroup) {
        userNames.add(userName.toLowerCase(Locale.getDefault()));
      }
    }
    return userNames;
  }

  private static List<String> queryUsersInGroup(final List<String> groups,
      final ReportSearchParam reportSearchParam) {
    if (groups.isEmpty()) {
      return Collections.emptyList();
    }
    ArrayList<String> users = ConverterUtil.getUserNamesFromGroup(groups);
    if (users.isEmpty()) {
      reportSearchParam.groupsHasNoMembers(true);
    }
    return users;
  }

  private static void setBasicSearcherValuesParams(final FilterCondition filterCondition,
      final ReportSearchParam reportSearchParam) {
    Date issueCreated = null;
    if (filterCondition.getIssueCreateDate() != null) {
      DateTimeServer issueCreatedDateTimeServer =
          DateTimeServer.getInstanceBasedOnUserTimeZone(filterCondition.getIssueCreateDate());
      issueCreated = issueCreatedDateTimeServer.getSystemTimeZoneDate();
    }
    reportSearchParam.issueCreateDate(issueCreated)
        .issueEpicLinkIssueIds(filterCondition.getIssueEpicLinkIssueIds())
        .issuePriorityIds(filterCondition.getIssuePriorityIds())
        .issueStatusIds(filterCondition.getIssueStatusIds())
        .issueTypeIds(filterCondition.getIssueTypeIds())
        .labels(filterCondition.getLabels());

    ConverterUtil.appendIssueAssignees(reportSearchParam, filterCondition.getIssueAssignees());

    ConverterUtil.appendIssueReportes(reportSearchParam, filterCondition.getIssueReporters());

    ConverterUtil.appendIssueResolution(reportSearchParam,
        filterCondition.getIssueResolutionIds());

    ConverterUtil.appendIssueAffectedVersions(reportSearchParam,
        filterCondition.getIssueAffectedVersions());

    ConverterUtil.appendIssueFixedVersions(reportSearchParam,
        filterCondition.getIssueFixedVersions());

    ConverterUtil.appendIssueComponents(reportSearchParam, filterCondition.getIssueComponents());

    String epicName = filterCondition.getIssueEpicName();
    if ((epicName != null) && !epicName.isEmpty()) {
      reportSearchParam.issueEpicName(epicName);
    }
  }

  private ConverterUtil() {
  }
}
