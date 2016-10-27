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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;

public class GetWorklogsTest {

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

  static class DummyWorklog extends WorklogImpl {

    private final long worklogId;

    public DummyWorklog(final long worklogId) {
      super(null, null, null, null, null, null, null, null, 10L);
      this.worklogId = worklogId;
    }

    @Override
    public Long getId() {
      return worklogId;
    }

    @Override
    public Issue getIssue() {
      return new MockIssue((int) worklogId, "KEY-" + worklogId);
    }
  }

  private DummyWorklog dummyWorklog;

  private WorklogComponent worklogManager;

  @Before
  public void before() throws ParseException {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    // mocked components
    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    I18nHelper i18nHelper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogManager mockWorklogManager =
        Mockito.mock(WorklogManager.class, Mockito.RETURNS_DEEP_STUBS);
    ApplicationProperties mockApplicationProperties =
        Mockito.mock(ApplicationProperties.class, Mockito.RETURNS_DEEP_STUBS);
    UserUtil userUtil = Mockito.mock(UserUtil.class, Mockito.RETURNS_DEEP_STUBS);
    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class, Mockito.RETURNS_DEEP_STUBS);
    GroupManager groupManager = Mockito.mock(GroupManager.class, Mockito.RETURNS_DEEP_STUBS);
    IssueManager issueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);

    // logged user
    MockApplicationUser loggedUser = new MockApplicationUser("test_userkey", "test_username");
    Mockito.when(mockJiraAuthenticationContext.getUser())
        .thenReturn(loggedUser);
    Mockito.when(mockJiraAuthenticationContext.getI18nHelper())
        .thenReturn(i18nHelper);
    Mockito.when(i18nHelper.getLocale())
        .thenReturn(Locale.ENGLISH);

    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.complete")))
        .thenReturn("yy-MM-dd h:mm");
    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.dmy")))
        .thenReturn("yy-MM-dd");

    Mockito.when(userUtil.getUserByName(Matchers.anyString()))
        .thenReturn(new MockApplicationUser("dummy", "dummy"));

    dummyWorklog = new DummyWorklog(1);
    Mockito.when(mockWorklogManager.getById(dummyWorklog.getId()))
        .thenReturn(dummyWorklog);

    List<Project> projects = new ArrayList<>();
    projects.add(new MockProject(1));
    projects.add(new MockProject(2));
    Mockito.when(permissionManager.getProjects(Permissions.BROWSE, loggedUser))
        .thenReturn(projects);

    List<GenericValue> genericValues = new ArrayList<>();
    genericValues.add(createDummyGenericValue(null, null, dummyWorklog.getIssue().getId()));
    MockIssue mockIssue = new MockIssue(30, "NO-1");
    genericValues.add(createDummyGenericValue(null, null, mockIssue.getId()));

    MockGenericValue mockGenericValue = new MockGenericValue("dummy");
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
        Matchers.anyList()))
        .thenReturn(genericValues);

    List<String> groups = new ArrayList<>(Arrays.asList("one", "group_1", "group_2"));
    Mockito.when(groupManager.getGroupNamesForUser((ApplicationUser) Matchers.any()))
        .thenReturn(groups);

    Mockito.when(issueManager.getIssueObject(dummyWorklog.getIssue().getId()))
        .thenReturn((MockIssue) dummyWorklog.getIssue());
    Mockito.when(issueManager.getIssueObject(mockIssue.getId()))
        .thenReturn(mockIssue);
    mockComponentWorker.addMock(IssueManager.class, issueManager);

    // init components
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .addMock(WorklogManager.class, mockWorklogManager)
        .addMock(ApplicationProperties.class, mockApplicationProperties)
        .addMock(UserUtil.class, userUtil)
        .addMock(PermissionManager.class, permissionManager)
        .addMock(OfBizDelegator.class, ofBizDelegator)
        .addMock(GroupManager.class, groupManager)
        .init();

    worklogManager = new WorklogComponent();
  }

  private DummyGenericValue createDummyGenericValue(final Long roleLevelId, final String groupLevel,
      final long issueId) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("rolelevel", roleLevelId);
    values.put("grouplevel", groupLevel);
    values.put("issue", issueId);
    return new DummyGenericValue(values);
  }

  @Test
  public void testGetWorklogs()
      throws DataAccessException, GenericEntityException, SQLException, ParseException {
    Date date = new Date();
    List<EveritWorklog> worklogs = worklogManager.getWorklogs(null, date, null);

  }
}
