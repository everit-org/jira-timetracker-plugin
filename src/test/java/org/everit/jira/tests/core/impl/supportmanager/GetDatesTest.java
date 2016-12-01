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
package org.everit.jira.tests.core.impl.supportmanager;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.everit.jira.core.SupportManager;
import org.everit.jira.core.impl.DateTimeServer;
import org.everit.jira.core.impl.SupportComponent;
import org.everit.jira.reporting.plugin.dto.MissingsWorklogsDTO;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;

public class GetDatesTest {

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

  private static final String NOWORK_ISSUE_KEY = "NOWORK-1";

  private static final String WORK_ISSUE_KEY = "WORK-1";

  private SupportManager supportManager;

  private TimeTrackerGlobalSettings timeTrackerGlobalSettings;

  private DateTime today;

  private DateTime todayPlus1;

  private DateTime todayPlus2;

  private DateTime todayPlus3;

  private DateTime todayPlus4;

  private GenericValue createDummyGenericValue(final long issueId, final long timeworked) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("issue", issueId);
    values.put("timeworked", timeworked);
    return new DummyGenericValue(values);

  }

  private void initMockComponentWorker() {
    timeTrackerGlobalSettings = new TimeTrackerGlobalSettings();
    DateTime date = new DateTime();
    today = date.toDateTime();
    date = date.plusDays(1);
    todayPlus1 = date.toDateTime();
    date = date.plusDays(1);
    todayPlus2 = date.toDateTime();
    date = date.plusDays(1);
    todayPlus3 = date.toDateTime();
    date = date.plusDays(1);
    todayPlus4 = date.toDateTime();

    timeTrackerGlobalSettings
        .excludeDates(new HashSet<>(Arrays.asList(todayPlus1.getMillis())));
    timeTrackerGlobalSettings
        .includeDates(new HashSet<>(Arrays.asList(today.getMillis(),
            todayPlus2.getMillis(),
            todayPlus3.getMillis(),
            todayPlus4.getMillis())));
    timeTrackerGlobalSettings
        .filteredSummaryIssues(new ArrayList<>(Arrays.asList(Pattern.compile(NOWORK_ISSUE_KEY))));

    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(new MockApplicationUser("userkey", "username"));

    TimeTrackingConfiguration timeTrackingConfiguration =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(timeTrackingConfiguration.getHoursPerDay())
        .thenReturn(new BigDecimal(1.0));

    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        permissionManager.getProjects(Matchers.eq(Permissions.BROWSE), Matchers.any(User.class)))
        .thenReturn(new ArrayList<GenericValue>());

    IssueManager issueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);
    MockIssue noworkIssue = new MockIssue(1, NOWORK_ISSUE_KEY);
    MockIssue workIssue = new MockIssue(2, WORK_ISSUE_KEY);
    Mockito.when(issueManager.getIssueObject(noworkIssue.getId()))
        .thenReturn(noworkIssue);
    Mockito.when(issueManager.getIssueObject(workIssue.getId()))
        .thenReturn(workIssue);

    OfBizDelegator ofBizDelegator = mockOfbizDelagator(noworkIssue, workIssue);

    supportManager = new SupportComponent(timeTrackingConfiguration);

    JiraUserPreferences mockJiraUserPreferences =
        Mockito.mock(JiraUserPreferences.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockJiraUserPreferences.getString("jira.user.timezone"))
        .thenReturn("UTC");

    UserPreferencesManager mockUserPreferencesManager =
        Mockito.mock(UserPreferencesManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockUserPreferencesManager.getPreferences(Matchers.any(ApplicationUser.class)))
        .thenReturn(mockJiraUserPreferences);
    mockComponentWorker.addMock(UserPreferencesManager.class, mockUserPreferencesManager);

    I18nHelper i18nHelper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);
    BeanFactory mockBeanFactory = Mockito.mock(BeanFactory.class, Mockito.RETURNS_DEEP_STUBS);

    Mockito.when(mockBeanFactory.getInstance(Matchers.any(ApplicationUser.class)))
        .thenReturn(i18nHelper);

    Mockito.when(i18nHelper.getLocale())
        .thenReturn(Locale.ENGLISH);
    mockComponentWorker.addMock(I18nHelper.class, i18nHelper);
    mockComponentWorker.addMock(BeanFactory.class, mockBeanFactory);

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .addMock(TimeTrackingConfiguration.class, timeTrackingConfiguration)
        .addMock(PermissionManager.class, permissionManager)
        .addMock(IssueManager.class, issueManager)
        .addMock(OfBizDelegator.class, ofBizDelegator)
        .init();

  }

  private OfBizDelegator mockOfbizDelagator(final MockIssue noworkIssue,
      final MockIssue workIssue) {
    OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(ofBizDelegator.findByAnd(Matchers.anyString(),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && sdf.format(DateTimeConverterUtil.convertDateTimeToDate(today))
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(
            new ArrayList<>(Arrays.asList(createDummyGenericValue(workIssue.getId(), 3600L))));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.anyString(),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && sdf.format(DateTimeConverterUtil.convertDateTimeToDate(todayPlus1))
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(new ArrayList<>(Arrays.asList(
            createDummyGenericValue(workIssue.getId(), 1000L),
            createDummyGenericValue(noworkIssue.getId(), 2000L))));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.anyString(),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && sdf.format(DateTimeConverterUtil.convertDateTimeToDate(todayPlus2))
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(new ArrayList<>(Arrays.asList(
            createDummyGenericValue(noworkIssue.getId(), 2600L),
            createDummyGenericValue(workIssue.getId(), 1000L))));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.anyString(),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && sdf.format(DateTimeConverterUtil.convertDateTimeToDate(todayPlus3))
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(new ArrayList<>(Arrays.asList(
            createDummyGenericValue(workIssue.getId(), 500L),
            createDummyGenericValue(workIssue.getId(), 100L),
            createDummyGenericValue(workIssue.getId(), 2000L))));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.anyString(),
        Matchers.argThat(new ArgumentMatcher<List<EntityCondition>>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            List<EntityCondition> exprList = (List<EntityCondition>) argument;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && sdf.format(DateTimeConverterUtil.convertDateTimeToDate(todayPlus4))
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(null);
    return ofBizDelegator;
  }

  @Test
  public void testGetDates() throws GenericEntityException {
    initMockComponentWorker();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DateTimeServer today = new DateTimeServer(this.today);
    DateTimeServer todayPlus4 = new DateTimeServer(this.todayPlus4);
    DateTimeServer todayPlus3 = new DateTimeServer(this.todayPlus3);
    DateTimeServer todayPlus2 = new DateTimeServer(this.todayPlus2);

    List<MissingsWorklogsDTO> dates =
        supportManager.getDates(today, todayPlus4, false, false, timeTrackerGlobalSettings);
    Assert.assertEquals(1, dates.size());
    MissingsWorklogsDTO dto1 = dates.get(0);
    Assert.assertEquals("1", dto1.getHour());
    Assert.assertEquals(sdf.format(todayPlus4.getUserTimeZoneDate()),
        sdf.format(dto1.getDate()));

    dates =
        supportManager.getDates(today, todayPlus4, false, true, timeTrackerGlobalSettings);
    Assert.assertEquals(1, dates.size());
    dto1 = dates.get(0);
    Assert.assertEquals("1", dto1.getHour());
    Assert.assertEquals(sdf.format(todayPlus4.getUserTimeZoneDate()),
        sdf.format(dto1.getDate()));

    dates =
        supportManager.getDates(today, todayPlus4, true, false, timeTrackerGlobalSettings);
    DecimalFormat df = new DecimalFormat("#.#");
    Assert.assertEquals(2, dates.size());
    dto1 = dates.get(0);
    MissingsWorklogsDTO dto2 = dates.get(1);
    Assert.assertEquals("1", dto1.getHour());
    Assert.assertEquals(df.format(0.3), dto2.getHour());
    Assert.assertEquals(sdf.format(todayPlus4.getUserTimeZoneDate()),
        sdf.format(dto1.getDate()));
    Assert.assertEquals(sdf.format(todayPlus3.getUserTimeZoneDate()),
        sdf.format(dto2.getDate()));

    dates =
        supportManager.getDates(today, todayPlus4, true, true, timeTrackerGlobalSettings);
    Assert.assertEquals(3, dates.size());
    dto1 = dates.get(0);
    dto2 = dates.get(1);
    MissingsWorklogsDTO dto3 = dates.get(2);
    Assert.assertEquals("1", dto1.getHour());
    Assert.assertEquals(df.format(0.3), dto2.getHour());
    Assert.assertEquals(df.format(0.7), dto3.getHour());
    Assert.assertEquals(sdf.format(todayPlus4.getUserTimeZoneDate()),
        sdf.format(dto1.getDate()));
    Assert.assertEquals(sdf.format(todayPlus3.getUserTimeZoneDate()),
        sdf.format(dto2.getDate()));
    Assert.assertEquals(sdf.format(todayPlus2.getUserTimeZoneDate()),
        sdf.format(dto3.getDate()));
  }
}
