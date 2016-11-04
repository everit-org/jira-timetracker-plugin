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
package org.everit.jira.tests.core.impl.worklogmanager;

import java.text.ParseException;
import java.util.Locale;

import org.everit.jira.core.impl.WorklogComponent;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;

public class GetWorklogTest {

  static class DummyWorklog extends WorklogImpl {

    private final long worklogId;

    public DummyWorklog(final long worklogId) {
      super(null, null, null, null, null, null, null, null, 10L);
      this.worklogId = worklogId;
    }

    @Override
    public String getComment() {
      return "dummy";
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

  private DummyWorklog dummyWorklog;

  private WorklogComponent worklogManager;

  @Before
  public void before() throws ParseException {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    // mocked components
    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    I18nHelper i18nHelper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);
    WorklogManager mockWorklogManager =
        Mockito.mock(WorklogManager.class, Mockito.RETURNS_DEEP_STUBS);
    ApplicationProperties mockApplicationProperties =
        Mockito.mock(ApplicationProperties.class, Mockito.RETURNS_DEEP_STUBS);

    // logged user
    MockApplicationUser loggedUser = new MockApplicationUser("test_userkey", "test_username");
    Mockito.when(mockJiraAuthenticationContext.getUser())
        .thenReturn(loggedUser);
    Mockito.when(mockJiraAuthenticationContext.getI18nHelper())
        .thenReturn(i18nHelper);
    Mockito.when(i18nHelper.getLocale())
        .thenReturn(Locale.ENGLISH);

    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.complete")))
        .thenReturn("yy-MM-dd h:mm");
    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.dmy")))
        .thenReturn("yy-MM-dd");

    dummyWorklog = new DummyWorklog(1);
    Mockito.when(mockWorklogManager.getById(dummyWorklog.getId()))
        .thenReturn(dummyWorklog);

    // init components
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
        .addMock(WorklogManager.class, mockWorklogManager)
        .addMock(ApplicationProperties.class, mockApplicationProperties)
        .init();

    worklogManager = new WorklogComponent();
  }

  @Test
  public void testGetWorklog() throws ParseException {
    EveritWorklog worklog = worklogManager.getWorklog(dummyWorklog.getId());
    Assert.assertNotNull(worklog);
    Assert.assertEquals(dummyWorklog.getId(), worklog.getWorklogId());
    Assert.assertEquals(dummyWorklog.getIssue().getKey(), worklog.getIssue());
    Assert.assertEquals(dummyWorklog.getComment(), worklog.getBody());
  }
}
