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

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeZoneTypes;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

  public void initMockComponents(final long hasNoWorklogDayStartUTC) {
    initMockComponents(hasNoWorklogDayStartUTC, "UTC", TimeZoneTypes.SYSTEM);
  }

  public void initMockComponents(final long hasNoWorklogDate, final String userTimeZone,
      final TimeZoneTypes jttpTimeZoneSettings) {
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
        .thenReturn(userTimeZone);

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
    ttGlobalSettings.timeZone(jttpTimeZoneSettings);
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
            DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              if ("startdate".equals(expr.getLhs())
                  && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                  && !(hasNoWorklogDate == ((Timestamp) expr.getRhs()).getTime())) {
                return true;
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
            DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              if ("startdate".equals(expr.getLhs())
                  && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                  && (hasNoWorklogDate == ((Timestamp) expr.getRhs()).getTime())) {
                return true;
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

    initMockComponents(
        DateTimeConverterUtil.setDateToDayStart(new DateTime(todayMinus7.getTimeInMillis()))
            .getMillis());

    Set<DateTime> excludeDatesSet =
        new HashSet<>(Arrays.asList(
            new DateTime(todayMinus1.getTime().getTime()),
            new DateTime(todayMinus2.getTime().getTime()),
            new DateTime(todayMinus4.getTime().getTime()),
            new DateTime(todayMinus5.getTime().getTime()),
            new DateTime(todayMinus6.getTime().getTime()),
            new DateTime(todayMinus3.getTime().getTime())));
    Set<DateTime> includeDatesSet =
        new HashSet<>(Arrays.asList(new DateTime(today.getTime().getTime()),
            new DateTime(todayPlus1.getTime().getTime()),
            new DateTime(todayMinus7.getTime().getTime())));

    DateTime firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet,
            new DateTime(TimetrackerUtil.getLoggedUserTimeZone()));

    DateTimeFormatter sdfdt = DateTimeFormat.forPattern("yyyy-MM-dd");
    Assert.assertEquals(sdfdt.print(new DateTime(todayMinus7.getTime().getTime())),
        sdfdt.print(firstMissingWorklogsDate));
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

    initMockComponents(
        DateTimeConverterUtil.setDateToDayStart(new DateTime(todayMinus3.getTimeInMillis()))
            .getMillis());
    Set<DateTime> excludeDatesSet =
        new HashSet<>(Arrays.asList(new DateTime(todayMinus1.getTime().getTime()),
            new DateTime(todayMinus2.getTime().getTime()),
            new DateTime(todayMinus4.getTime().getTime()),
            new DateTime(todayMinus5.getTime().getTime()),
            new DateTime(todayMinus6.getTime().getTime()),
            new DateTime(todayMinus7.getTime().getTime())));
    Set<DateTime> includeDatesSet =
        new HashSet<>(Arrays.asList(
            new DateTime(today.getTime()),
            new DateTime(todayPlus1.getTime()),
            new DateTime(todayMinus3.getTime())));

    DateTime firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet,
            new DateTime(TimetrackerUtil.getLoggedUserTimeZone()));

    DateTimeFormatter sdfdt = DateTimeFormat.forPattern("yyyy-MM-dd");
    Assert.assertEquals(sdfdt.print(new DateTime(todayMinus3.getTime().getTime())),
        sdfdt.print(firstMissingWorklogsDate));
  }

  @Test
  public void testFirstMissingWorklogsDateToday() throws ParseException, GenericEntityException {

    Calendar today = Calendar.getInstance();

    initMockComponents(
        DateTimeConverterUtil.setDateToDayStart(new DateTime(today.getTimeInMillis())).getMillis());
    Set<DateTime> excludeDatesSet = new HashSet<>();
    Set<DateTime> includeDatesSet =
        new HashSet<>(Arrays.asList(new DateTime(today.getTime().getTime())));

    DateTime firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet,
            new DateTime(TimetrackerUtil.getLoggedUserTimeZone()));
    DateTimeFormatter sdfdt = DateTimeFormat.forPattern("yyyy-MM-dd");
    Assert.assertEquals(sdfdt.print(new DateTime(today.getTime().getTime())),
        sdfdt.print(firstMissingWorklogsDate));
  }

  @Test
  public void testFirstMissingWorklogsDateWithUserSpecificTimeZone()
      throws ParseException, GenericEntityException {

    long hasNoWorklog = 1482710400000L;// 2016.12.26 00:00 in utc
    // 2016.12.26 00:00 in user time zone 03:00 in utc
    long hasNoWorklogDateMidNightInUserTImeZone = 1482721200000L;
    initMockComponents(hasNoWorklogDateMidNightInUserTImeZone, "Atlantic/Stanley",
        TimeZoneTypes.USER);

    Set<DateTime> excludeDatesSet = new HashSet<>();
    Set<DateTime> includeDatesSet =
        new HashSet<>(Arrays.asList(new DateTime(hasNoWorklog, DateTimeZone.UTC)));

    DateTime firstMissingWorklogsDate =
        timetrackerComponent.firstMissingWorklogsDate(excludeDatesSet, includeDatesSet,
            // in user timezone the current date is 2016.12.31. 23:00 in utc is 2017.01.01 02:00
            // no worklog found in 2016.12.26
            new DateTime(1483236000000L, DateTimeZone.forID("Atlantic/Stanley")));
    DateTimeFormatter sdfdt = DateTimeFormat.forPattern("yyyy-MM-dd");
    Assert.assertEquals(
        sdfdt.print(new DateTime(hasNoWorklogDateMidNightInUserTImeZone,
            DateTimeZone.forID("Atlantic/Stanley"))),
        sdfdt.print(firstMissingWorklogsDate));
  }
}
