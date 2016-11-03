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
package org.everit.jira.core.impl.worklogmanager;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.everit.jira.core.EVWorklogManager;
import org.everit.jira.core.impl.WorklogComponent;
import org.everit.jira.core.impl.WorklogComponent.PropertiesKey;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;

public class EditWorklogTest {

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
      return new MockIssue((int) worklogId + 1000, "DUMMYKEY-" + worklogId);
    }
  }

  private DummyWorklog invalidIssueWorklog;

  private DummyWorklog notSameIssueCreateFailIssueWorklog;

  private DummyWorklog notSameIssueDeleteFailIssueWorklog;

  private DummyWorklog notSameIssueNoPermissionWorklog;

  private MockIssue notSameIssueToCreateFail;

  private MockIssue notSameIssueToDeleteFail;

  private MockIssue notSameIssueToNoPermission;

  private DummyWorklog sameIssueDateParseWorklog;

  private DummyWorklog sameIssueNoPermissionToUpdateWorklog;

  private DummyWorklog sameIssueSuccessWorklog;

  private DummyWorklog sameIssueValidateFailWorklog;

  private EVWorklogManager worklogManager;

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
    I18nHelper i18nHelper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogManager mockWorklogManager =
        Mockito.mock(WorklogManager.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogService worklogService = Mockito.mock(WorklogService.class, Mockito.RETURNS_DEEP_STUBS);
    IssueManager issueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);
    PermissionManager permissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);

    // logged user
    MockApplicationUser loggedUser = new MockApplicationUser("test_userkey", "test_username");
    Mockito.when(mockJiraAuthenticationContext.getUser())
        .thenReturn(loggedUser);
    Mockito.when(mockJiraAuthenticationContext.getI18nHelper())
        .thenReturn(i18nHelper);
    Mockito.when(i18nHelper.getLocale())
        .thenReturn(Locale.ENGLISH);

    // invalid issue worklog
    invalidIssueWorklog = new DummyWorklog(0);
    Mockito.when(mockWorklogManager.getById(invalidIssueWorklog.getId()))
        .thenReturn(invalidIssueWorklog);
    Issue invalidIssue = invalidIssueWorklog.getIssue();
    Mockito.when(issueManager.getIssueObject(invalidIssue.getKey()))
        .thenReturn(null);

    // not same issue no permission worklog
    notSameIssueNoPermissionWorklog = new DummyWorklog(1);
    Mockito.when(mockWorklogManager.getById(notSameIssueNoPermissionWorklog.getId()))
        .thenReturn(notSameIssueNoPermissionWorklog);
    notSameIssueToNoPermission = new MockIssue(101, "NOTSAME-1");
    Mockito.when(issueManager.getIssueObject(notSameIssueToNoPermission.getKey()))
        .thenReturn(notSameIssueToNoPermission);
    Mockito.when(
        permissionManager.hasPermission(Permissions.WORK_ISSUE,
            notSameIssueToNoPermission,
            loggedUser))
        .thenReturn(false);

    // not same issue delete fail worklog
    notSameIssueDeleteFailIssueWorklog = new DummyWorklog(2);
    Mockito.when(mockWorklogManager.getById(notSameIssueDeleteFailIssueWorklog.getId()))
        .thenReturn(notSameIssueDeleteFailIssueWorklog);
    notSameIssueToDeleteFail = new MockIssue(102, "NOTSAME-2");
    Mockito.when(issueManager.getIssueObject(notSameIssueToDeleteFail.getKey()))
        .thenReturn(notSameIssueToDeleteFail);
    Mockito.when(
        permissionManager.hasPermission(Permissions.WORK_ISSUE,
            notSameIssueToDeleteFail,
            loggedUser))
        .thenReturn(true);
    Mockito.when(worklogService.hasPermissionToDelete(Matchers.any(JiraServiceContext.class),
        Matchers.eq(notSameIssueDeleteFailIssueWorklog)))
        .thenReturn(false);

    // not same issue create fail worklog
    notSameIssueCreateFailIssueWorklog = new DummyWorklog(3);
    Mockito.when(mockWorklogManager.getById(notSameIssueCreateFailIssueWorklog.getId()))
        .thenReturn(notSameIssueCreateFailIssueWorklog);
    notSameIssueToCreateFail = new MockIssue(103, "NOTSAME-3");
    Mockito.when(issueManager.getIssueObject(notSameIssueToCreateFail.getKey()))
        .thenReturn(notSameIssueToCreateFail);
    Mockito.when(
        permissionManager.hasPermission(Permissions.WORK_ISSUE,
            notSameIssueToCreateFail,
            loggedUser))
        .thenReturn(true);
    Mockito.when(worklogService.hasPermissionToDelete(Matchers.any(JiraServiceContext.class),
        Matchers.eq(notSameIssueCreateFailIssueWorklog)))
        .thenReturn(true);
    Mockito.when(
        permissionManager.hasPermission(Permissions.WORK_ISSUE,
            notSameIssueCreateFailIssueWorklog.getIssue(),
            loggedUser))
        .thenReturn(true);

    // same issue date parse error worklog
    sameIssueDateParseWorklog = new DummyWorklog(4);
    Mockito.when(mockWorklogManager.getById(sameIssueDateParseWorklog.getId()))
        .thenReturn(sameIssueDateParseWorklog);
    Mockito.when(issueManager.getIssueObject(sameIssueDateParseWorklog.getIssue().getKey()))
        .thenReturn((MockIssue) sameIssueDateParseWorklog.getIssue());

    // same issue no permission to update worklog
    sameIssueNoPermissionToUpdateWorklog = new DummyWorklog(5);
    Mockito.when(mockWorklogManager.getById(sameIssueNoPermissionToUpdateWorklog.getId()))
        .thenReturn(sameIssueNoPermissionToUpdateWorklog);
    Mockito.when(
        issueManager.getIssueObject(sameIssueNoPermissionToUpdateWorklog.getIssue().getKey()))
        .thenReturn((MockIssue) sameIssueNoPermissionToUpdateWorklog.getIssue());
    Mockito.when(worklogService.hasPermissionToUpdate(Matchers.any(JiraServiceContext.class),
        Matchers.eq(sameIssueNoPermissionToUpdateWorklog)))
        .thenReturn(false);

    // same issue validate fail worklog
    sameIssueValidateFailWorklog = new DummyWorklog(6);
    Mockito.when(mockWorklogManager.getById(sameIssueValidateFailWorklog.getId()))
        .thenReturn(sameIssueValidateFailWorklog);
    Mockito.when(
        issueManager.getIssueObject(sameIssueValidateFailWorklog.getIssue().getKey()))
        .thenReturn((MockIssue) sameIssueValidateFailWorklog.getIssue());
    Mockito.when(worklogService.hasPermissionToUpdate(Matchers.any(JiraServiceContext.class),
        Matchers.eq(sameIssueValidateFailWorklog)))
        .thenReturn(true);
    Mockito.when(worklogService.validateUpdate(Matchers.any(JiraServiceContext.class),
        Matchers.argThat(new ArgumentMatcher<WorklogNewEstimateInputParameters>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            return sameIssueValidateFailWorklog.getIssue().getKey()
                .equals(((WorklogNewEstimateInputParameters) argument).getIssue().getKey());
          }
        })))
        .thenReturn(null);

    // same issue success worklog
    sameIssueSuccessWorklog = new DummyWorklog(7);
    Mockito.when(mockWorklogManager.getById(sameIssueSuccessWorklog.getId()))
        .thenReturn(sameIssueSuccessWorklog);
    Mockito.when(
        issueManager.getIssueObject(sameIssueSuccessWorklog.getIssue().getKey()))
        .thenReturn((MockIssue) sameIssueSuccessWorklog.getIssue());
    Mockito.when(worklogService.hasPermissionToUpdate(Matchers.any(JiraServiceContext.class),
        Matchers.eq(sameIssueSuccessWorklog)))
        .thenReturn(true);
    Mockito.when(worklogService.validateUpdate(Matchers.any(JiraServiceContext.class),
        Matchers.argThat(new ArgumentMatcher<WorklogNewEstimateInputParameters>() {
          @Override
          public boolean matches(final Object argument) {
            if (argument == null) {
              return false;
            }
            return sameIssueSuccessWorklog.getIssue().getKey()
                .equals(((WorklogNewEstimateInputParameters) argument).getIssue().getKey());
          }
        })))
        .thenReturn(new DummySuccessWorklogResult(sameIssueSuccessWorklog));

    // init components
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .addMock(WorklogManager.class, mockWorklogManager)
        .addMock(IssueManager.class, issueManager)
        .addMock(WorklogService.class, worklogService)
        .addMock(PermissionManager.class, permissionManager)
        .init();

    worklogManager = new WorklogComponent();
  }

  @Test
  public void testEditWorklog() {
    String defaultComment = "comment";
    Date defaultDate = new Date();
    String defaultTime = "08:00";
    String defaultTimeSpent = "10";
    ActionResult result = worklogManager.editWorklog(invalidIssueWorklog.getId(),
        invalidIssueWorklog.getIssue().getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        invalidIssueWorklog.getIssue().getKey(),
        PropertiesKey.INVALID_ISSUE);

    result = worklogManager.editWorklog(notSameIssueNoPermissionWorklog.getId(),
        notSameIssueToNoPermission.getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        notSameIssueToNoPermission.getKey(),
        PropertiesKey.NOPERMISSION_ISSUE);

    result = worklogManager.editWorklog(notSameIssueNoPermissionWorklog.getId(),
        notSameIssueToNoPermission.getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        notSameIssueToNoPermission.getKey(),
        PropertiesKey.NOPERMISSION_ISSUE);

    result = worklogManager.editWorklog(notSameIssueDeleteFailIssueWorklog.getId(),
        notSameIssueToDeleteFail.getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        notSameIssueDeleteFailIssueWorklog.getIssue().getKey(),
        PropertiesKey.NOPERMISSION_DELETE_WORKLOG);

    result = worklogManager.editWorklog(notSameIssueCreateFailIssueWorklog.getId(),
        notSameIssueToCreateFail.getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        notSameIssueToCreateFail.getKey(),
        PropertiesKey.NOPERMISSION_CREATE_WORKLOG);

    String wrongTime = "wrong";
    result = worklogManager.editWorklog(sameIssueDateParseWorklog.getId(),
        sameIssueDateParseWorklog.getIssue().getKey(),
        defaultComment,
        defaultDate,
        wrongTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        defaultDate + " " + wrongTime,
        PropertiesKey.DATE_PARSE);

    result = worklogManager.editWorklog(sameIssueNoPermissionToUpdateWorklog.getId(),
        sameIssueNoPermissionToUpdateWorklog.getIssue().getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        sameIssueNoPermissionToUpdateWorklog.getIssue().getKey(),
        PropertiesKey.NOPERMISSION_UPDATE_WORKLOG);

    result = worklogManager.editWorklog(sameIssueValidateFailWorklog.getId(),
        sameIssueValidateFailWorklog.getIssue().getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.FAIL,
        "",
        PropertiesKey.WORKLOG_UPDATE_FAIL);

    result = worklogManager.editWorklog(sameIssueSuccessWorklog.getId(),
        sameIssueSuccessWorklog.getIssue().getKey(),
        defaultComment,
        defaultDate,
        defaultTime,
        defaultTimeSpent);
    assertActionResult(result,
        ActionResultStatus.SUCCESS,
        "",
        PropertiesKey.WORKLOG_UPDATE_SUCCESS);
  }
}
