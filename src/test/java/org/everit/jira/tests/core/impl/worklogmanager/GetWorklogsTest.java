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
package org.everit.jira.tests.core.impl.worklogmanager;

import java.math.BigDecimal;
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

import org.easymock.EasyMock;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.impl.WorklogComponent;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeZoneTypes;
import org.everit.jira.tests.core.DummyDateTimeFromatter;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;

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
      Object object = values.get(key);
      if (object == null) {
        return null;
      }
      return Long.valueOf(object.toString());
    }

    @Override
    public String getString(final String key) {
      Object object = values.get(key);
      if (object == null) {
        return null;
      }
      return String.valueOf(object);
    }
  }

  static class DummyWorklog extends WorklogImpl {

    private MutableIssue issue;
    private final long worklogId;

    public DummyWorklog(final long worklogId, final MutableIssue issue) {
      super(null, null, null, null, null, null, null, null, 10L);
      this.worklogId = worklogId;
      this.issue = issue;
    }

    @Override
    public Long getId() {
      return worklogId;
    }

    @Override
    public Issue getIssue() {
      return issue;
    }

  }

  private static final String SELECTED_USER = "selectedUserForTest";

  private Date defaultStatDate = new Date();

  private DummyWorklog dummyWorklog;

  private MockApplicationUser loggedUser;

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

    loggedUser = new MockApplicationUser("test_userkey", "test_username");
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

    Mockito.when(userUtil.getUserByName(SELECTED_USER))
        .thenReturn(new MockApplicationUser(SELECTED_USER, SELECTED_USER + "username"));

    dummyWorklog = new DummyWorklog(1, createIssue(1L, "WORKLOG-"));
    Mockito.when(mockWorklogManager.getById(dummyWorklog.getId()))
        .thenReturn(dummyWorklog);

    JiraUserPreferences mockJiraUserPreferences =
        Mockito.mock(JiraUserPreferences.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockJiraUserPreferences.getString("jira.user.timezone"))
        .thenReturn("UTC");

    UserPreferencesManager mockUserPreferencesManager =
        Mockito.mock(UserPreferencesManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockUserPreferencesManager.getPreferences(Matchers.any(ApplicationUser.class)))
        .thenReturn(mockJiraUserPreferences);
    mockComponentWorker.addMock(UserPreferencesManager.class, mockUserPreferencesManager);

    BeanFactory mockBeanFactory = Mockito.mock(BeanFactory.class, Mockito.RETURNS_DEEP_STUBS);

    Mockito.when(mockBeanFactory.getInstance(Matchers.any(ApplicationUser.class)))
        .thenReturn(i18nHelper);

    mockComponentWorker.addMock(BeanFactory.class, mockBeanFactory);

    List<Project> projects = new ArrayList<>();
    projects.add(new MockProject(1));
    projects.add(new MockProject(2));
    Mockito.when(permissionManager.getProjects(Permissions.BROWSE, loggedUser))
        .thenReturn(projects);
    Mockito.when(
        permissionManager.hasPermission(Matchers.eq(ProjectPermissions.BROWSE_PROJECTS),
            Matchers.any(MutableIssue.class),
            Matchers.eq(loggedUser)))
        .thenReturn(true);
    List<GenericValue> genericValues = new ArrayList<>();
    genericValues.add(createDummyGenericValue(null, null, dummyWorklog.getIssue().getId()));
    MutableIssue mockIssue = createIssue(30L, "NO-");
    genericValues.add(createDummyGenericValue(null, null, mockIssue.getId()));

    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              if (loggedUser.getKey().equals(expr.getRhs())) {
                return true;
              }
            }
            return false;
          }
        })))
        .thenReturn(genericValues);

    genericValues = new ArrayList<>();
    genericValues.add(createDummyGenericValue(null, null, dummyWorklog.getIssue().getId()));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              if (SELECTED_USER.equals(expr.getRhs())) {
                return true;
              }
            }
            return false;
          }
        })))
        .thenReturn(genericValues);

    genericValues = new ArrayList<>();
    genericValues.add(createDummyGenericValue(null, null, dummyWorklog.getIssue().getId()));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              if (SELECTED_USER.equals(expr.getRhs())) {
                return true;
              }
            }
            return false;
          }
        })))
        .thenReturn(genericValues);

    List<String> groups = new ArrayList<>(Arrays.asList("one", "group_1", "group_2"));
    Mockito.when(groupManager.getGroupNamesForUser((ApplicationUser) Matchers.any()))
        .thenReturn(groups);

    Mockito.when(issueManager.getIssueObject(dummyWorklog.getIssue().getId()))
        .thenReturn((MutableIssue) dummyWorklog.getIssue());
    Mockito.when(issueManager.getIssueObject(mockIssue.getId()))
        .thenReturn(mockIssue);
    mockComponentWorker.addMock(IssueManager.class, issueManager);

    BigDecimal daysPerWeek = new BigDecimal(5);
    BigDecimal hoursPerDay = new BigDecimal(8);
    TimeTrackingConfiguration ttConfig = EasyMock.createNiceMock(TimeTrackingConfiguration.class);
    EasyMock.expect(ttConfig.getDaysPerWeek()).andReturn(daysPerWeek).anyTimes();
    EasyMock.expect(ttConfig.getHoursPerDay()).andReturn(hoursPerDay).anyTimes();
    EasyMock.replay(ttConfig);

    DateTimeFormatterFactory mockDateTimeFormatterFactory =
        Mockito.mock(DateTimeFormatterFactory.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockDateTimeFormatterFactory.formatter())
        .thenReturn(new DummyDateTimeFromatter());

    TimeTrackerGlobalSettings ttGlobalSettings = new TimeTrackerGlobalSettings();
    ttGlobalSettings.timeZone(TimeZoneTypes.SYSTEM);
    TimeTrackerSettingsHelper settingsHelper =
        Mockito.mock(TimeTrackerSettingsHelper.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(settingsHelper.loadGlobalSettings()).thenReturn(ttGlobalSettings);
    mockComponentWorker.addMock(TimeTrackerSettingsHelper.class, settingsHelper);

    // init components
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .addMock(WorklogManager.class, mockWorklogManager)
        .addMock(ApplicationProperties.class, mockApplicationProperties)
        .addMock(UserUtil.class, userUtil)
        .addMock(PermissionManager.class, permissionManager)
        .addMock(OfBizDelegator.class, ofBizDelegator)
        .addMock(GroupManager.class, groupManager)
        .addMock(TimeTrackingConfiguration.class, ttConfig)
        .addMock(DateTimeFormatterFactory.class, mockDateTimeFormatterFactory)
        .init();

    worklogManager = new WorklogComponent();
  }

  private DummyGenericValue createDummyGenericValue(final Long roleLevelId, final String groupLevel,
      final long issueId) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("rolelevel", roleLevelId);
    values.put("grouplevel", groupLevel);
    values.put("issue", issueId);
    values.put("startdate", "2000-11-22 08:00:00");
    values.put("timeworked", 10);
    return new DummyGenericValue(values);
  }

  private MutableIssue createIssue(final long id, final String key) {
    StatusCategory statusCategory =
        Mockito.mock(StatusCategory.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(statusCategory.getKey()).thenReturn(StatusCategory.COMPLETE);

    SimpleStatus simpleStatus = Mockito.mock(SimpleStatus.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(simpleStatus.getStatusCategory()).thenReturn(statusCategory);

    Status status = Mockito.mock(Status.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(status.getSimpleStatus()).thenReturn(simpleStatus);

    MutableIssue issue = Mockito.mock(MutableIssue.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(issue.getId()).thenReturn(id + 2000);
    Mockito.when(issue.getKey()).thenReturn(key + id);
    Mockito.when(issue.getStatusObject()).thenReturn(status);
    return issue;
  }

  @Test
  public void testGetWorklogs()
      throws DataAccessException, GenericEntityException, SQLException, ParseException {
    List<EveritWorklog> worklogs = worklogManager.getWorklogs(null,
        DateTimeServer.getInstanceBasedOnUserTimeZone(new DateTime(defaultStatDate.getTime())),
        null);
    Assert.assertEquals(2, worklogs.size());
    Assert.assertEquals(worklogs.get(0).getIssue(), "WORKLOG-1");
    Assert.assertEquals(worklogs.get(1).getIssue(), "NO-30");

    worklogs = worklogManager.getWorklogs(SELECTED_USER,
        DateTimeServer.getInstanceBasedOnUserTimeZone(new DateTime(defaultStatDate.getTime())),
        DateTimeServer.getInstanceBasedOnUserTimeZone(new DateTime()));
    Assert.assertEquals(1, worklogs.size());
    Assert.assertEquals(worklogs.get(0).getIssue(), "WORKLOG-1");

  }
}
