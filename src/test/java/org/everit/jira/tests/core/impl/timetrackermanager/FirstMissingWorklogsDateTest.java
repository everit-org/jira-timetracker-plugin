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
package org.everit.jira.tests.core.impl.timetrackermanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeZoneTypes;
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

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
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

public class FirstMissingWorklogsDateTest {

  private TimetrackerComponent timetrackerComponent = new TimetrackerComponent(null, null);

  public void initMockComponents(final Calendar hasNoWorklogDate) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        permissionManager.getProjects(Matchers.eq(Permissions.BROWSE), Matchers.any(User.class)))
        .thenReturn(new ArrayList<GenericValue>());
    mockComponentWorker.addMock(PermissionManager.class, permissionManager);

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    final MockApplicationUser loggedUser = new MockApplicationUser("user-key", "username");
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(loggedUser);
    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext);

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

    TimeTrackerGlobalSettings ttGlobalSettings = new TimeTrackerGlobalSettings();
    ttGlobalSettings.timeZone(TimeZoneTypes.SYSTEM);
    TimeTrackerSettingsHelper settingsHelper =
        Mockito.mock(TimeTrackerSettingsHelper.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(settingsHelper.loadGlobalSettings()).thenReturn(ttGlobalSettings);
    mockComponentWorker.addMock(TimeTrackerSettingsHelper.class, settingsHelper);

    MockGenericValue mockGenericValue = new MockGenericValue("none");
    OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class, Mockito.RETURNS_DEEP_STUBS);
    ArrayList<GenericValue> genericValues =
        new ArrayList<>(Arrays.asList((GenericValue) mockGenericValue));
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
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
                    && !sdf.format(hasNoWorklogDate.getTime())
                        .equals(sdf.format(sdf.parse(expr.getRhs().toString())))) {
                  return true;
                }
              } catch (ParseException e) {
              }
            }
            return false;
          }
        })))
        .thenReturn(genericValues);
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"),
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
                    && sdf.format(hasNoWorklogDate.getTime())
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
    mockComponentWorker.addMock(OfBizDelegator.class, ofBizDelegator);

    mockComponentWorker.init();
  }

  @Test
  public void testFirstMissingWorklogsDateEnd() throws ParseException, GenericEntityException {

    Calendar today = Calendar.getInstance();
    Calendar todayPlus1 = (Calendar) today.clone();
    todayPlus1.add(Calendar.DAY_OF_YEAR, 1);

    Calendar todayMinus1 = (Calendar) today.clone();
    todayMinus1.add(Calendar.DAY_OF_YEAR, -1);

    Calendar todayMinus2 = (Calendar) today.clone();
    todayMinus2.add(Calendar.DAY_OF_YEAR, -2);

    Calendar todayMinus3 = (Calendar) today.clone();
    todayMinus3.add(Calendar.DAY_OF_YEAR, -3);

    Calendar todayMinus4 = (Calendar) today.clone();
    todayMinus4.add(Calendar.DAY_OF_YEAR, -4);

    Calendar todayMinus5 = (Calendar) today.clone();
    todayMinus5.add(Calendar.DAY_OF_YEAR, -5);

    Calendar todayMinus6 = (Calendar) today.clone();
    todayMinus6.add(Calendar.DAY_OF_YEAR, -6);

    Calendar todayMinus7 = (Calendar) today.clone();
    todayMinus7.add(Calendar.DAY_OF_YEAR, -7);

    initMockComponents(todayMinus7);

    Set<Date> excludeDatesSet =
        new HashSet<>(Arrays.asList(todayMinus1.getTime(),
            todayMinus2.getTime(),
            todayMinus4.getTime(),
            todayMinus5.getTime(),
            todayMinus6.getTime(),
            todayMinus3.getTime()));
    Set<Date> includeDatesSet =
        new HashSet<>(Arrays.asList(today.getTime(),
            todayPlus1.getTime(),
            todayMinus7.getTime()));

    Date firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Assert.assertEquals(sdf.format(todayMinus7.getTime()), sdf.format(firstMissingWorklogsDate));
  }

  @Test
  public void testFirstMissingWorklogsDateMiddle() throws ParseException, GenericEntityException {

    Calendar today = Calendar.getInstance();
    Calendar todayPlus1 = (Calendar) today.clone();
    todayPlus1.add(Calendar.DAY_OF_YEAR, 1);

    Calendar todayMinus1 = (Calendar) today.clone();
    todayMinus1.add(Calendar.DAY_OF_YEAR, -1);

    Calendar todayMinus2 = (Calendar) today.clone();
    todayMinus2.add(Calendar.DAY_OF_YEAR, -2);

    Calendar todayMinus3 = (Calendar) today.clone();
    todayMinus3.add(Calendar.DAY_OF_YEAR, -3);

    Calendar todayMinus4 = (Calendar) today.clone();
    todayMinus4.add(Calendar.DAY_OF_YEAR, -4);

    Calendar todayMinus5 = (Calendar) today.clone();
    todayMinus5.add(Calendar.DAY_OF_YEAR, -5);

    Calendar todayMinus6 = (Calendar) today.clone();
    todayMinus6.add(Calendar.DAY_OF_YEAR, -6);

    Calendar todayMinus7 = (Calendar) today.clone();
    todayMinus7.add(Calendar.DAY_OF_YEAR, -7);

    initMockComponents(todayMinus3);

    Set<Date> excludeDatesSet =
        new HashSet<>(Arrays.asList(todayMinus1.getTime(),
            todayMinus2.getTime(),
            todayMinus4.getTime(),
            todayMinus5.getTime(),
            todayMinus6.getTime(),
            todayMinus7.getTime()));
    Set<Date> includeDatesSet =
        new HashSet<>(Arrays.asList(today.getTime(),
            todayPlus1.getTime(),
            todayMinus3.getTime()));

    Date firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Assert.assertEquals(sdf.format(todayMinus3.getTime()), sdf.format(firstMissingWorklogsDate));
  }

  @Test
  public void testFirstMissingWorklogsDateToday() throws ParseException, GenericEntityException {

    Calendar today = Calendar.getInstance();

    initMockComponents(today);

    Set<Date> excludeDatesSet = new HashSet<>();
    Set<Date> includeDatesSet = new HashSet<>(Arrays.asList(today.getTime()));

    Date firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Assert.assertEquals(sdf.format(today.getTime()), sdf.format(firstMissingWorklogsDate));
  }
}
