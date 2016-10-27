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

import java.text.ParseException;

import org.everit.jira.core.impl.WorklogComponent.PropertiesKey;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;

public class DeleteWorklogTest {

  static class DummySuccessWorklogResult implements WorklogResult {

    private final Worklog worklog;

    public DummySuccessWorklogResult(final Worklog worklog) {
      this.worklog = worklog;
    }

    @Override
    public Worklog getWorklog() {
      return worklog;
    }

    @Override
    public boolean isEditableCheckRequired() {
      return true;
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

  private DummyWorklog deleteSuccessWorklog;

  private DummyWorklog noPermissionWorklog;

  private DummyWorklog validateErrorWorklog;

  private WorklogComponent worklogManager;

  private void assertActionResult(final ActionResult result,
      final ActionResultStatus expectedStatus, final String expectedIssueId,
      final String expectedMessage) {
    Assert.assertNotNull(result);
    Assert.assertEquals(expectedStatus, result.getStatus());
    Assert.assertEquals(expectedIssueId, result.getMessageParameter());
    Assert.assertEquals(expectedMessage, result.getMessage());
  }

  @Before
  public void before() throws ParseException {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    // mocked components
    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogService worklogService = Mockito.mock(WorklogService.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogManager mockWorklogManager =
        Mockito.mock(WorklogManager.class, Mockito.RETURNS_DEEP_STUBS);

    // logged user
    MockApplicationUser loggedUser = new MockApplicationUser("test_userkey", "test_username");
    Mockito.when(mockJiraAuthenticationContext.getUser())
        .thenReturn(loggedUser);

    // no permission worklog
    noPermissionWorklog = new DummyWorklog(0);
    Mockito.when(mockWorklogManager.getById(noPermissionWorklog.getId()))
        .thenReturn(noPermissionWorklog);
    Mockito.when(
        worklogService.hasPermissionToDelete(Matchers.any(JiraServiceContext.class),
            Matchers.eq(noPermissionWorklog)))
        .thenReturn(false);

    // validate error worklog
    validateErrorWorklog = new DummyWorklog(1);
    Mockito.when(mockWorklogManager.getById(validateErrorWorklog.getId()))
        .thenReturn(validateErrorWorklog);
    Mockito.when(
        worklogService.hasPermissionToDelete(Matchers.any(JiraServiceContext.class),
            Matchers.eq(validateErrorWorklog)))
        .thenReturn(true);
    Mockito.when(
        worklogService.validateDelete(Matchers.any(JiraServiceContext.class),
            Matchers.eq(validateErrorWorklog.getId())))
        .thenReturn(null);

    // delete success worklog
    deleteSuccessWorklog = new DummyWorklog(2);
    Mockito.when(mockWorklogManager.getById(deleteSuccessWorklog.getId()))
        .thenReturn(deleteSuccessWorklog);
    Mockito.when(
        worklogService.hasPermissionToDelete(Matchers.any(JiraServiceContext.class),
            Matchers.eq(deleteSuccessWorklog)))
        .thenReturn(true);
    Mockito.when(
        worklogService.validateDelete(Matchers.any(JiraServiceContext.class),
            Matchers.eq(deleteSuccessWorklog.getId())))
        .thenReturn(new DummySuccessWorklogResult(deleteSuccessWorklog));

    // init components
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .addMock(WorklogService.class, worklogService)
        .addMock(WorklogManager.class, mockWorklogManager)
        .init();

    worklogManager = new WorklogComponent();
  }

  @Test
  public void testDeleteWorklog() {
    ActionResult result = worklogManager.deleteWorklog(noPermissionWorklog.getId());
    assertActionResult(result,
        ActionResultStatus.FAIL,
        "KEY-" + noPermissionWorklog.getId(),
        PropertiesKey.NOPERMISSION_DELETE_WORKLOG);

    result = worklogManager.deleteWorklog(validateErrorWorklog.getId());
    assertActionResult(result,
        ActionResultStatus.FAIL,
        validateErrorWorklog.getId().toString(),
        PropertiesKey.WORKLOG_DELETE_FAIL);

    result = worklogManager.deleteWorklog(deleteSuccessWorklog.getId());
    assertActionResult(result,
        ActionResultStatus.SUCCESS,
        deleteSuccessWorklog.getId().toString(),
        PropertiesKey.WORKLOG_DELETE_SUCCESS);
  }
}
