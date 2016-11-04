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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.everit.jira.core.SupportManager;
import org.everit.jira.core.impl.SupportComponent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

public class SummaryTest {

  static class DummyGenericValue extends GenericValue {
    private static final long serialVersionUID = 3415063923321743460L;

    final Map<String, Object> values;

    @SuppressWarnings("deprecation")
    public DummyGenericValue(final Map<String, Object> values) {
      super(new ModelEntity());
      this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      DummyGenericValue other = (DummyGenericValue) obj;
      if (values == null) {
        if (other.values != null) {
          return false;
        }
      } else if (!values.equals(other.values)) {
        return false;
      }
      return true;
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

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + ((values == null) ? 0 : values.hashCode());
      return result;
    }
  }

  private static final String GROUPLEVEL_FOR_USER = "test";

  private MockIssue noWorkIssue = new MockIssue(1, "NOWORK-1");

  private MockIssue workIssue = new MockIssue(2, "WORK-1");

  private GenericValue createDummyGenericValue(final long issueId, final int timeworked,
      final String grouplevel) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("issue", issueId);
    values.put("grouplevel", grouplevel);
    values.put("timeworked", timeworked);
    return new DummyGenericValue(values);
  }

  private void initMockComponentWorker(final List<GenericValue> worklogs) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(jiraAuthenticationContext.getLoggedInUser())
        .thenReturn(new MockApplicationUser("userKey", "username"));

    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        permissionManager.getProjects(Matchers.eq(Permissions.BROWSE),
            Matchers.any(ApplicationUser.class)))
        .thenReturn(new ArrayList<Project>());

    IssueManager issueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(issueManager.getIssueObject(workIssue.getId()))
        .thenReturn(workIssue);
    Mockito.when(issueManager.getIssueObject(noWorkIssue.getId()))
        .thenReturn(noWorkIssue);

    GroupManager groupManager = Mockito.mock(GroupManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(groupManager.getGroupNamesForUser(Matchers.any(ApplicationUser.class)))
        .thenReturn(new ArrayList<>(Arrays.asList(GROUPLEVEL_FOR_USER)));

    ProjectRoleManager projectRoleManager =
        Mockito.mock(ProjectRoleManager.class, Mockito.RETURNS_DEEP_STUBS);

    OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(ofBizDelegator.findByAnd(Matchers.eq("IssueWorklogView"), Matchers.anyList()))
        .thenReturn(worklogs);

    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .addMock(PermissionManager.class, permissionManager)
        .addMock(IssueManager.class, issueManager)
        .addMock(GroupManager.class, groupManager)
        .addMock(ProjectRoleManager.class, projectRoleManager)
        .addMock(OfBizDelegator.class, ofBizDelegator)
        .init();
  }

  @Test
  public void testSummary() throws GenericEntityException {
    initMockComponentWorker(
        new ArrayList<>(
            Arrays.asList(
                createDummyGenericValue(workIssue.getId(), 1600, null),
                createDummyGenericValue(noWorkIssue.getId(), 2600, null),
                createDummyGenericValue(workIssue.getId(), 4600, GROUPLEVEL_FOR_USER),
                createDummyGenericValue(workIssue.getId(), 4600, "empty_group"),
                createDummyGenericValue(noWorkIssue.getId(), 5600, GROUPLEVEL_FOR_USER))));

    SupportManager supportManager = new SupportComponent(null);

    long summary = supportManager.summary(new Date(),
        new Date(),
        new ArrayList<>(Arrays.asList(Pattern.compile(noWorkIssue.getKey()))));

    Assert.assertEquals(6200L, summary);
  }
}
