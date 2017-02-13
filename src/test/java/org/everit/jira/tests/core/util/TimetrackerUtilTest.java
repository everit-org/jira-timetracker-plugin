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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.tests.core.DummyDateTimeFromatter;
import org.everit.jira.timetracker.plugin.dto.WorklogValues;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;

public class TimetrackerUtilTest {

  static class DummyIssue extends MockIssue {

    private final Long estimate;

    private final String statusId;

    public DummyIssue(final int id, final String key, final String statusId, final Long estimate) {
      super(id, key);
      this.statusId = statusId;
      this.estimate = estimate;
    }

    @Override
    public Long getEstimate() {
      return estimate;
    }

    @Override
    public Status getStatusObject() {
      return new MockStatus(statusId, "status_" + statusId);
    }
  }

  private static final String CLOSED_STATUS_ID = "6";

  private static final String OPEN_STATUS_ID = "1";

  public void initMockComponents(final Date containsWorklogDate,
      final Date notContainsWorklogDate) {
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
                    && sdf.format(containsWorklogDate)
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
                    && sdf.format(notContainsWorklogDate)
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

    DateTimeFormatterFactory mockDateTimeFormatterFactory =
        Mockito.mock(DateTimeFormatterFactory.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockDateTimeFormatterFactory.formatter())
        .thenReturn(new DummyDateTimeFromatter());
    mockComponentWorker.addMock(DateTimeFormatterFactory.class, mockDateTimeFormatterFactory);

    mockComponentWorker.init();
  }

  @Test
  public void isContainsWorklog() throws ParseException, GenericEntityException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date containsWorklogDate = sdf.parse("2015-10-21");
    Date notContainsWorklogDate = sdf.parse("2016-10-21");

    initMockComponents(containsWorklogDate, notContainsWorklogDate);

    Assert.assertTrue(TimetrackerUtil.isContainsWorklog(containsWorklogDate));

    Assert.assertFalse(TimetrackerUtil.isContainsWorklog(notContainsWorklogDate));
  }

  @Test
  public void testCheckIssueEstimatedTime() {
    MutableIssue dummyIssue = new DummyIssue(1, "KEY-1", OPEN_STATUS_ID, null);
    List<Pattern> collectorIssueIds =
        new ArrayList<Pattern>(Arrays.asList(Pattern.compile("KEY-1")));
    Assert.assertTrue(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));

    dummyIssue = new DummyIssue(1, "WORK-1", OPEN_STATUS_ID, 1L);
    Assert.assertTrue(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));

    dummyIssue = new DummyIssue(1, "KEY-1", OPEN_STATUS_ID, 1L);
    Assert.assertTrue(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, null));

    collectorIssueIds.clear();
    dummyIssue = new DummyIssue(1, "KEY-1", CLOSED_STATUS_ID, null);
    Assert.assertTrue(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));

    dummyIssue = new DummyIssue(1, "KEY-1", CLOSED_STATUS_ID, 0L);
    Assert.assertTrue(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));

    dummyIssue = new DummyIssue(1, "KEY-1", OPEN_STATUS_ID, null);
    Assert.assertFalse(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));

    dummyIssue = new DummyIssue(1, "KEY-1", OPEN_STATUS_ID, 0L);
    Assert.assertFalse(TimetrackerUtil.checkIssueEstimatedTime(dummyIssue, collectorIssueIds));
  }

  @Test
  public void testContainsSetTheSameDay() throws ParseException {
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    DateTime date1 = formatter.parseDateTime("2015-10-21");
    DateTime date2 = formatter.parseDateTime("2016-10-21");
    DateTime date3 = formatter.parseDateTime("2017-10-21");
    Set<DateTime> dates = new HashSet<>();
    dates.add(date1);
    dates.add(date2);
    dates.add(date3);

    DateTime sameDate = formatter.parseDateTime("2017-10-21");
    Assert.assertTrue(TimetrackerUtil.containsSetTheSameDay(dates, sameDate));

    DateTime notSameDate = formatter.parseDateTime("2017-10-23");
    Assert.assertFalse(TimetrackerUtil.containsSetTheSameDay(dates, notSameDate));
  }

  @Test
  public void testConvertJsonToWorklogValues() {
    try {
      TimetrackerUtil.convertJsonToWorklogValues(null);
      Assert.fail("Expect NPE.");
    } catch (NullPointerException e) {
      Assert.assertNotNull(e);
    }

    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(null);
    I18nHelper i18helper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getI18nHelper())
        .thenReturn(i18helper);
    Mockito.when(i18helper.getLocale())
        .thenReturn(Locale.ENGLISH);

    DateTimeFormatterFactory mockDateTimeFormatterFactory =
        Mockito.mock(DateTimeFormatterFactory.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockDateTimeFormatterFactory.formatter())
        .thenReturn(new DummyDateTimeFromatter());

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .addMock(DateTimeFormatterFactory.class, mockDateTimeFormatterFactory)
        .init();

    WorklogValues worklogValues = TimetrackerUtil.convertJsonToWorklogValues("{}");
    Assert.assertEquals("", worklogValues.getComment());
    Assert.assertEquals("", worklogValues.getDurationTime());
    Assert.assertFalse(worklogValues.isDuration());
    Assert.assertEquals("", worklogValues.getIssueKey());
    Assert.assertNull("", worklogValues.getStartTime());

    worklogValues = TimetrackerUtil.convertJsonToWorklogValues("{\"comment\":\"dummy-comment\","
        + "\"durationTime\":\"dummy-duration\",\"endTime\":\"dummy-endtime\",\"isDuration\":false,"
        + "\"issueKey\":\"dummy-issuekey\",\"startTime\":\"dummy-starttime\"}");
    Assert.assertEquals("dummy-comment", worklogValues.getComment());
    Assert.assertEquals("dummy-duration", worklogValues.getDurationTime());
    Assert.assertEquals("dummy-endtime", worklogValues.getEndTime());
    Assert.assertFalse(worklogValues.isDuration());
    Assert.assertEquals("dummy-issuekey", worklogValues.getIssueKey());
    Assert.assertEquals("dummy-starttime", worklogValues.getStartTime());

    worklogValues = TimetrackerUtil.convertJsonToWorklogValues("{\"comment\":\"dummy-comment\","
        + "\"durationTime\":\"dummy-duration\",\"endTime\":\"dummy-endtime\",\"isDuration\":true,"
        + "\"issueKey\":\"dummy-issuekey\",\"startTime\":\"dummy-starttime\"}");
    Assert.assertEquals("dummy-comment", worklogValues.getComment());
    Assert.assertEquals("dummy-duration", worklogValues.getDurationTime());
    Assert.assertEquals("dummy-endtime", worklogValues.getEndTime());
    Assert.assertTrue(worklogValues.isDuration());
    Assert.assertEquals("dummy-issuekey", worklogValues.getIssueKey());
    Assert.assertEquals("dummy-starttime", worklogValues.getStartTime());
  }

  @Test
  public void testConvertWorklogValuesToJson() {
    WorklogValues worklogValues = new WorklogValues();
    String json = TimetrackerUtil.convertWorklogValuesToJson(worklogValues);
    Assert.assertEquals("{\"adjustmentAmount\":\"\",\"comment\":\"\",\"commentForActions\":\"\","
        + "\"durationTime\":\"\",\"isDuration\":false,\"issueKey\":\"\",\"newEstimate\":\"\"}",
        json);

    worklogValues.setComment("dummy-comment");
    json = TimetrackerUtil.convertWorklogValuesToJson(worklogValues);
    Assert.assertEquals("{\"adjustmentAmount\":\"\",\"comment\":\"dummy-comment\","
        + "\"commentForActions\":\"\",\"durationTime\":\"\","
        + "\"isDuration\":false,\"issueKey\":\"\",\"newEstimate\":\"\"}",
        json);
  }

  @Test
  public void testGetLoggedUserName() {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(null);

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .init();
    Assert.assertEquals("", TimetrackerUtil.getLoggedUserKey());

    mockComponentWorker = new MockComponentWorker();

    jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(new MockApplicationUser("lower-user-key-default", "lower-user-name-default"));

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .init();
    Assert.assertEquals("lower-user-key-default", TimetrackerUtil.getLoggedUserKey());
  }

  @Test
  public void testIsUserLogged() {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(null);

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .init();
    Assert.assertFalse(TimetrackerUtil.isUserLogged());

    mockComponentWorker = new MockComponentWorker();

    jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getUser())
        .thenReturn(new MockApplicationUser("userKey", "username"));

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .init();
    Assert.assertTrue(TimetrackerUtil.isUserLogged());
  }
}
