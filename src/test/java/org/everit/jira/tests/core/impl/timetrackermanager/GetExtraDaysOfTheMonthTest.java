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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.tests.util.converterUtilForTests;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;

public class GetExtraDaysOfTheMonthTest {

  private TimetrackerManager timetrackerManager;

  @Before
  public void before() {
    timetrackerManager = new TimetrackerComponent(null, null);

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
    mockComponentWorker.addMock(BeanFactory.class, mockBeanFactory).init();

  }

  @Test
  public void testOneExcludeDate() throws ParseException {
    List<DateTime> excludeDaysOfTheMonth = timetrackerManager.getExcludeDaysOfTheMonth(
        new DateTime(converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-05")),
        new HashSet<>(
            Arrays.asList(
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-05"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-02-05"))));
    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-05"))),
        new HashSet<>(excludeDaysOfTheMonth));
  }

  @Test
  public void testOneIncludeDate() throws ParseException {
    List<DateTime> excludeDaysOfTheMonth = timetrackerManager.getIncludeDaysOfTheMonth(
        new DateTime(1451952000000L, DateTimeZone.UTC),
        new HashSet<>(
            Arrays.asList(
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-01"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-02-04"))));
    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-01"))),
        new HashSet<>(excludeDaysOfTheMonth));
  }

  @Test
  public void testWithUserTimeZone() throws ParseException {
    List<DateTime> excludeDaysOfTheMonth = timetrackerManager.getIncludeDaysOfTheMonth(
        new DateTime(1451606400000L, DateTimeZone.forID("Pacific/Apia")),
        new HashSet<>(
            Arrays.asList(
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-01"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-02-04"))));
    Assert.assertEquals(
        new HashSet<>(),
        new HashSet<>(excludeDaysOfTheMonth));
  }

  @Test
  public void testWithUserTimeZone2() throws ParseException {
    List<DateTime> excludeDaysOfTheMonth = timetrackerManager.getIncludeDaysOfTheMonth(
        new DateTime(1451606400000L, DateTimeZone.forID("Pacific/Chatham")),
        new HashSet<>(
            Arrays.asList(
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-01"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2016-02-04"))));
    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(converterUtilForTests.fixFormatStringToUTCDateTime("2016-01-01"))),
        new HashSet<>(excludeDaysOfTheMonth));
  }

}
