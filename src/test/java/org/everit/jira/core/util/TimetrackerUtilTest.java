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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;

public class TimetrackerUtilTest {

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
  public void testContainsSetTheSameDay() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date1 = sdf.parse("2015-10-21");
    Date date2 = sdf.parse("2016-10-21");
    Date date3 = sdf.parse("2017-10-21");
    Set<Date> dates = new HashSet<>();
    dates.add(date1);
    dates.add(date2);
    dates.add(date3);

    Date sameDate = sdf.parse("2017-10-21");
    Assert.assertTrue(TimetrackerUtil.containsSetTheSameDay(dates, sameDate));

    Date notSameDate = sdf.parse("2017-10-23");
    Assert.assertFalse(TimetrackerUtil.containsSetTheSameDay(dates, notSameDate));
  }
}
