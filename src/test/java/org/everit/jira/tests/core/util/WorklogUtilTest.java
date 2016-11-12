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
package org.everit.jira.tests.core.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.jira.core.util.WorklogUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.MockProjectRoleManager.MockProjectRole;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

public class WorklogUtilTest {

  static class DummyGenericValue extends GenericValue {
    private static final long serialVersionUID = 3415063923321743460L;

    private final Map<String, Object> values;

    @SuppressWarnings("deprecation")
    public DummyGenericValue(final Map<String, Object> values) {
      super(new ModelEntity());
      this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    @Override
    public Long getLong(final String key) {
      return (Long) values.get(key);
    }

    @Override
    public String getString(final String key) {
      return (String) values.get(key);
    }
  }

  private Calendar endDate;
  private MockApplicationUser noProjectUser;
  private Calendar startDate;
  private MockApplicationUser test0User;

  private void assertEntityExpression(final EntityExpr expression, final String expectedLhs,
      final EntityOperator expectedOperator, final Object expectedRhs) {
    Assert.assertEquals(expectedLhs, expression.getLhs());
    Assert.assertEquals(expectedOperator, expectedOperator);
    Assert.assertEquals(expectedRhs, expression.getRhs());
  }

  @Before
  public void before() {
    startDate = Calendar.getInstance();
    endDate = (Calendar) startDate.clone();
    endDate.set(Calendar.YEAR, 2000);
    test0User = new MockApplicationUser("test0", "test0_username");
    noProjectUser = new MockApplicationUser("noProjectUser", "noProjectUser_username");
  }

  private DummyGenericValue createDummyGenericValue(final Long roleLevelId, final String groupLevel,
      final long issueId) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("rolelevel", roleLevelId);
    values.put("grouplevel", groupLevel);
    values.put("issue", issueId);
    return new DummyGenericValue(values);
  }

  private void initMockComponentsForCreateWorklogQueryExpr(final MockApplicationUser test0User,
      final MockApplicationUser noProjectUser,
      final List<Project> projects) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(permissionManager.getProjects(Permissions.BROWSE, test0User))
        .thenReturn(projects);
    Mockito.when(permissionManager.getProjects(Permissions.BROWSE, noProjectUser))
        .thenReturn(new ArrayList<Project>());
    mockComponentWorker.addMock(PermissionManager.class, permissionManager);

    mockComponentWorker.init();
  }

  private void initMockComponentsForHasWorklogVisibility() {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockJiraAuthenticationContext.getI18nHelper().getLocale())
        .thenReturn(new Locale("en", "US"));
    Mockito.when(mockJiraAuthenticationContext.getUser())
        .thenReturn(null);
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext);

    MockIssue mockIssue = new MockIssue(1, "KEY-12");
    IssueManager issueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(issueManager.getIssueObject(Matchers.anyLong()))
        .thenReturn(mockIssue);
    mockComponentWorker.addMock(IssueManager.class, issueManager);

    List<String> groups = new ArrayList<>(Arrays.asList("one", "group_1", "group_2"));
    GroupManager groupManager = Mockito.mock(GroupManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(groupManager.getGroupNamesForUser((ApplicationUser) Matchers.any()))
        .thenReturn(groups);
    mockComponentWorker.addMock(GroupManager.class, groupManager);

    List<ProjectRole> projectRoles = new ArrayList<>();
    projectRoles.add(new MockProjectRole(0, "p_admin", ""));
    projectRoles.add(new MockProjectRole(1, "p_user", ""));
    projectRoles.add(new MockProjectRole(2, "p_developer", ""));
    ProjectRoleManager projectRoleManager =
        Mockito.mock(ProjectRoleManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(projectRoleManager.getProjectRoles((ApplicationUser) Matchers.any(),
        (Project) Matchers.any()))
        .thenReturn(projectRoles);
    mockComponentWorker.addMock(ProjectRoleManager.class, projectRoleManager);

    mockComponentWorker.init();
  }

  @Test
  public void testCreateWorklogQueryExprListWithPermissionCheckLoggedUser() {
    List<Project> projects = new ArrayList<>();
    projects.add(new MockProject(1));
    projects.add(new MockProject(2));

    initMockComponentsForCreateWorklogQueryExpr(test0User, noProjectUser, projects);

    // has project to user.
    List<EntityCondition> test0UserConditions =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(test0User,
            startDate,
            endDate);

    EntityExpr ecAuthor = (EntityExpr) test0UserConditions.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, test0User.getKey());

    Assert.assertEquals(4, test0UserConditions.size());
    EntityExpr ecStartDateGreaterThanEqual = (EntityExpr) test0UserConditions.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    EntityExpr ecStartDateLessThan = (EntityExpr) test0UserConditions.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    EntityExpr ecProject = (EntityExpr) test0UserConditions.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.IN, new ArrayList<Long>(Arrays.asList(1L, 2L)));

    // no projects to user.
    List<EntityCondition> noProjectUserConditions =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(noProjectUser,
            startDate,
            endDate);

    ecAuthor = (EntityExpr) noProjectUserConditions.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, noProjectUser.getKey());

    Assert.assertEquals(4, noProjectUserConditions.size());
    ecStartDateGreaterThanEqual = (EntityExpr) noProjectUserConditions.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    ecStartDateLessThan = (EntityExpr) noProjectUserConditions.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    ecProject = (EntityExpr) noProjectUserConditions.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.EQUALS, null);
  }

  @Test
  public void testCreateWorklogQueryExprListWithPermissionCheckSelectedUser() {
    List<Project> projects = new ArrayList<>();
    projects.add(new MockProject(1));
    projects.add(new MockProject(2));

    initMockComponentsForCreateWorklogQueryExpr(test0User, noProjectUser, projects);

    // has project to user.
    List<EntityCondition> test0UserConditions =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck("test",
            test0User,
            startDate,
            endDate);

    EntityExpr ecAuthor = (EntityExpr) test0UserConditions.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, "test");

    Assert.assertEquals(4, test0UserConditions.size());
    EntityExpr ecStartDateGreaterThanEqual = (EntityExpr) test0UserConditions.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    EntityExpr ecStartDateLessThan = (EntityExpr) test0UserConditions.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    EntityExpr ecProject = (EntityExpr) test0UserConditions.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.IN, new ArrayList<Long>(Arrays.asList(1L, 2L)));

    // no projects to user.
    List<EntityCondition> noProjectUserConditions =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck("test",
            noProjectUser,
            startDate,
            endDate);

    ecAuthor = (EntityExpr) noProjectUserConditions.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, "test");

    Assert.assertEquals(4, noProjectUserConditions.size());
    ecStartDateGreaterThanEqual = (EntityExpr) noProjectUserConditions.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    ecStartDateLessThan = (EntityExpr) noProjectUserConditions.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    ecProject = (EntityExpr) noProjectUserConditions.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.EQUALS, null);

    // has project to user but no add selected user.
    List<EntityCondition> test0UserConditionsNullSelectedUser =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(null, test0User,
            startDate,
            endDate);

    ecAuthor = (EntityExpr) test0UserConditionsNullSelectedUser.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, test0User.getKey());

    Assert.assertEquals(4, test0UserConditionsNullSelectedUser.size());
    ecStartDateGreaterThanEqual =
        (EntityExpr) test0UserConditionsNullSelectedUser.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    ecStartDateLessThan = (EntityExpr) test0UserConditionsNullSelectedUser.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    ecProject = (EntityExpr) test0UserConditionsNullSelectedUser.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.IN, new ArrayList<Long>(Arrays.asList(1L, 2L)));

    // has project to user but no add selected user.
    List<EntityCondition> test0UserConditionsEmptySelectedUser =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck("", test0User,
            startDate,
            endDate);

    ecAuthor = (EntityExpr) test0UserConditionsEmptySelectedUser.get(0);
    assertEntityExpression(ecAuthor, "author",
        EntityOperator.EQUALS, test0User.getKey());

    Assert.assertEquals(4, test0UserConditionsEmptySelectedUser.size());
    ecStartDateGreaterThanEqual =
        (EntityExpr) test0UserConditionsEmptySelectedUser.get(1);
    assertEntityExpression(ecStartDateGreaterThanEqual, "startdate",
        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTimeInMillis()));

    ecStartDateLessThan = (EntityExpr) test0UserConditionsEmptySelectedUser.get(2);
    assertEntityExpression(ecStartDateLessThan, "startdate",
        EntityOperator.LESS_THAN, new Timestamp(endDate.getTimeInMillis()));

    ecProject = (EntityExpr) test0UserConditionsEmptySelectedUser.get(3);
    assertEntityExpression(ecProject, "project",
        EntityOperator.IN, new ArrayList<Long>(Arrays.asList(1L, 2L)));
  }

  @Test
  public void testHasWorklogVisibility() {
    initMockComponentsForHasWorklogVisibility();

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    ProjectRoleManager projectRoleManager =
        ComponentAccessor.getComponent(ProjectRoleManager.class);

    DummyGenericValue existsRoleLevelGV = createDummyGenericValue(0L, null, 1);
    boolean hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(null,
        issueManager,
        groupManager,
        projectRoleManager,
        existsRoleLevelGV);
    Assert.assertTrue(hasWorklogVisibility);

    DummyGenericValue existsGroupLevelGV = createDummyGenericValue(null, "group_1", 1);
    hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(null,
        issueManager,
        groupManager,
        projectRoleManager,
        existsGroupLevelGV);
    Assert.assertTrue(hasWorklogVisibility);

    DummyGenericValue notExistsRoleLevelGV = createDummyGenericValue(-1L, null, 1);
    hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(null,
        issueManager,
        groupManager,
        projectRoleManager,
        notExistsRoleLevelGV);
    Assert.assertFalse(hasWorklogVisibility);

    DummyGenericValue notExistsGroupLevelGV = createDummyGenericValue(null, "no_group", 1);
    hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(null,
        issueManager,
        groupManager,
        projectRoleManager,
        notExistsGroupLevelGV);
    Assert.assertFalse(hasWorklogVisibility);

    DummyGenericValue defaultGV = createDummyGenericValue(null, null, 1);
    hasWorklogVisibility = WorklogUtil.hasWorklogVisibility(null,
        issueManager,
        groupManager,
        projectRoleManager,
        defaultGV);
    Assert.assertTrue(hasWorklogVisibility);
  }
}
