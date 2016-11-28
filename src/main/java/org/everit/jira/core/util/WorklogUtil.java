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
package org.everit.jira.core.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Utility class for worklogs.
 */
public final class WorklogUtil {

  private static List<Long> createProjects(final ApplicationUser loggedInUser) {
    Collection<Project> projects = ComponentAccessor.getPermissionManager()
        .getProjects(Permissions.BROWSE, loggedInUser);

    List<Long> projectList = new ArrayList<>();
    for (Project project : projects) {
      projectList.add(project.getId());
    }
    return projectList;
  }

  /**
   * Creates worklog query expression list without permission check.
   *
   * @param startDate
   *          the start date of the worklog.
   * @param endDate
   *          the end date of the worklog.
   * @param userKey
   *          the user key for author.
   * @return the expression list.
   */
  public static List<EntityCondition> createWorklogQueryExprList(final Calendar startDate,
      final Calendar endDate,
      final String userKey) {
    // TODO remove calendar? set to long?
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

  /**
   * Creates worklog query expression list with permission check.
   *
   * @param user
   *          the user who's the author in worklog.
   * @param startDate
   *          the start date of the worklog.
   * @param endDate
   *          the end date of the worklog.
   * @return the expression list.
   */
  public static List<EntityCondition> createWorklogQueryExprListWithPermissionCheck(
      final ApplicationUser user,
      final Calendar startDate, final Calendar endDate) {

    String userKey = user.getKey();

    return WorklogUtil.createWorklogQueryExprListWithPermissionCheck(userKey, user,
        startDate,
        endDate);
  }

  /**
   * Creates worklog query expression list with permission check.
   *
   * @param selectedUser
   *          the selected user key who's the author in worklog.
   * @param loggedInUser
   *          the logged user.
   * @param startDate
   *          the start date of the worklog.
   * @param endDate
   *          the end date of the worklog.
   * @return the expression list.
   */
  public static List<EntityCondition> createWorklogQueryExprListWithPermissionCheck(
      final String selectedUser,
      final ApplicationUser loggedInUser,
      final Calendar startDate, final Calendar endDate) {
    // TODO remove calendar? set to long?
    String userKey = ((selectedUser == null) || "".equals(selectedUser))
        ? loggedInUser.getKey() : selectedUser;

    List<Long> projects = WorklogUtil.createProjects(loggedInUser);

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprList(startDate, endDate, userKey);

    EntityExpr projectExpr = new EntityExpr("project", EntityOperator.EQUALS, null);
    if (!projects.isEmpty()) {
      projectExpr = new EntityExpr("project", EntityOperator.IN, projects);
    }
    exprList.add(projectExpr);
    return exprList;
  }

  /**
   * Check has worklog visibility permission for the user.
   *
   * @param loggedInUser
   *          the logged user.
   * @param issueManager
   *          the {@link IssueManager} instance.
   * @param groupManager
   *          the {@link GroupManager} instance.
   * @param projectRoleManager
   *          the {@link ProjectRoleManager} instance.
   * @param worklogGv
   *          the {@link GenericValue} for the worklog.
   * @return true if has worklog visibility, otherwise false.
   */
  public static boolean hasWorklogVisibility(final ApplicationUser loggedInUser,
      final IssueManager issueManager, final GroupManager groupManager,
      final ProjectRoleManager projectRoleManager, final GenericValue worklogGv) {
    Collection<String> loggedUserGroupNames = groupManager.getGroupNamesForUser(loggedInUser);
    Long roleLevelId = worklogGv.getLong("rolelevel");
    String groupLevel = worklogGv.getString("grouplevel");
    Long issueId = worklogGv.getLong("issue");
    MutableIssue issue = issueManager.getIssueObject(issueId);
    boolean hasWorklogVisibility = true;
    if ((roleLevelId != null)) {
      hasWorklogVisibility = false;
      Collection<ProjectRole> projectRoles =
          projectRoleManager.getProjectRoles(loggedInUser, issue.getProjectObject());
      for (ProjectRole projectRole : projectRoles) {
        if (projectRole.getId().equals(roleLevelId)) {
          hasWorklogVisibility = true;
        }
      }
    } else if ((groupLevel != null) && !loggedUserGroupNames.contains(groupLevel)) {
      hasWorklogVisibility = false;
    }
    return hasWorklogVisibility;
  }

  private WorklogUtil() {
  }

}
