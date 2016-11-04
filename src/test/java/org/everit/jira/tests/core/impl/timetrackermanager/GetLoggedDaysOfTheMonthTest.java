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
import java.util.List;

import org.everit.jira.core.impl.TimetrackerComponent;
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

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

public class GetLoggedDaysOfTheMonthTest {

  private TimetrackerComponent timetrackerComponent = new TimetrackerComponent(null);

  public void initMockComponents(final Calendar dateOfMonth) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        permissionManager.getProjects(Matchers.eq(Permissions.BROWSE),
            Matchers.any(ApplicationUser.class)))
        .thenReturn(new ArrayList<Project>());
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            for (EntityCondition expression : exprList) {
              EntityExpr expr = (EntityExpr) expression;
              try {
                if ("startdate".equals(expr.getLhs())
                    && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator())
                    && !sdf.format(dateOfMonth.getTime())
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
    mockComponentWorker.addMock(OfBizDelegator.class, ofBizDelegator);

    mockComponentWorker.init();
  }

  @Test
  public void testGetLoggedDaysOfMonth() throws GenericEntityException {
    Calendar dateOfMonth = Calendar.getInstance();
    dateOfMonth.set(Calendar.MONTH, 0);
    initMockComponents(dateOfMonth);

    List<String> loggedDaysOfTheMonth =
        timetrackerComponent.getLoggedDaysOfTheMonth(dateOfMonth.getTime());

    Assert.assertEquals(31, loggedDaysOfTheMonth.size());

    dateOfMonth.set(Calendar.MONTH, 3);
    initMockComponents(dateOfMonth);

    loggedDaysOfTheMonth =
        timetrackerComponent.getLoggedDaysOfTheMonth(dateOfMonth.getTime());

    Assert.assertEquals(30, loggedDaysOfTheMonth.size());

    dateOfMonth.set(Calendar.MONTH, 1);
    dateOfMonth.set(Calendar.YEAR, 2016);
    initMockComponents(dateOfMonth);

    loggedDaysOfTheMonth =
        timetrackerComponent.getLoggedDaysOfTheMonth(dateOfMonth.getTime());

    Assert.assertEquals(29, loggedDaysOfTheMonth.size());

    dateOfMonth.set(Calendar.MONTH, 1);
    dateOfMonth.set(Calendar.YEAR, 2015);
    initMockComponents(dateOfMonth);

    loggedDaysOfTheMonth =
        timetrackerComponent.getLoggedDaysOfTheMonth(dateOfMonth.getTime());

    Assert.assertEquals(28, loggedDaysOfTheMonth.size());
  }
}
