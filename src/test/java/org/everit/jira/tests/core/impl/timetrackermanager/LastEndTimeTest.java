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
import java.util.Date;
import java.util.Locale;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.tests.core.DummyDateTimeFromatter;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

public class LastEndTimeTest {

  static class DummyEveritWorklog extends EveritWorklog {

    private static Worklog getWorklog() {
      Worklog worklog = Mockito.mock(Worklog.class);
      Mockito.when(worklog.getIssue())
          .thenReturn(new MockIssue(1, "KEY-1"));
      Mockito.when(worklog.getStartDate())
          .thenReturn(new Date());
      return worklog;
    }

    public DummyEveritWorklog(final String startDate, final String endTime) throws ParseException {
      super(DummyEveritWorklog.getWorklog());
      setEndTime(endTime);
    }

  }

  private TimetrackerManager timetrackerManager = new TimetrackerComponent(null);

  @Before
  public void before() {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    I18nHelper i18nHelper = Mockito.mock(I18nHelper.class, Mockito.RETURNS_DEEP_STUBS);

    Mockito.when(jiraAuthenticationContext.getI18nHelper())
        .thenReturn(i18nHelper);

    Mockito.when(i18nHelper.getLocale())
        .thenReturn(Locale.ENGLISH);

    DateTimeFormatterFactory mockDateTimeFormatterFactory =
        Mockito.mock(DateTimeFormatterFactory.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockDateTimeFormatterFactory.formatter())
        .thenReturn(new DummyDateTimeFromatter());

    ApplicationProperties applicationProperties =
        Mockito.mock(ApplicationProperties.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        applicationProperties.getDefaultBackedString(Matchers.eq(APKeys.JIRA_LF_DATE_DMY)))
        .thenReturn("yyyy-MM-dd hh:mm");
    mockComponentWorker.addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .addMock(ApplicationProperties.class, applicationProperties)
        .addMock(DateTimeFormatterFactory.class, mockDateTimeFormatterFactory)
        .init();
  }

  @Test
  public void testLastEndTime() throws ParseException {
    EveritWorklog everitWorklog0 = new DummyEveritWorklog("08:00", "08:20");
    EveritWorklog everitWorklog1 = new DummyEveritWorklog("08:20", "09:40");
    EveritWorklog everitWorklog2 = new DummyEveritWorklog("09:40", "10:50");
    EveritWorklog everitWorklog3 = new DummyEveritWorklog("10:50", "16:10");

    String lastEndTime = timetrackerManager
        .lastEndTime(Arrays.asList(everitWorklog0, everitWorklog1, everitWorklog2, everitWorklog3));
    Assert.assertEquals("16:10", lastEndTime);

    lastEndTime = timetrackerManager.lastEndTime(Arrays.asList(everitWorklog0, everitWorklog2));
    Assert.assertEquals("10:50", lastEndTime);

    lastEndTime = timetrackerManager.lastEndTime(Arrays.asList(everitWorklog0));
    Assert.assertEquals("08:20", lastEndTime);

    lastEndTime = timetrackerManager.lastEndTime(null);
    Assert.assertEquals("08:00", lastEndTime);

    lastEndTime = timetrackerManager.lastEndTime(new ArrayList<EveritWorklog>());
    Assert.assertEquals("08:00", lastEndTime);
  }
}
