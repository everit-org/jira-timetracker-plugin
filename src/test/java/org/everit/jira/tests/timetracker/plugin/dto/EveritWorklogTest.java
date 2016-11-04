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
package org.everit.jira.tests.timetracker.plugin.dto;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.everit.jira.tests.timetracker.plugin.DurationBuilder;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;

@RunWith(Parameterized.class)
public class EveritWorklogTest {

  static class DummyGenericValue extends GenericValue {
    private static final long serialVersionUID = 3415063923321743460L;

    private final Map<String, Object> values;

    @SuppressWarnings("deprecation")
    public DummyGenericValue(final Map<String, Object> values) {
      super(new ModelEntity());
      this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    @Override
    public Long getLong(final String key) {
      return (Long) values.get(key);
    }

    @Override
    public String getString(final String key) {
      return (String) values.get(key);
    }

  }

  private static DurationBuilder duration() {
    return new DurationBuilder(8, 5);
  }

  private static Object[] param(final String expectedRemaining,
      final DurationBuilder remainingInSeconds) {
    return new Object[] { expectedRemaining, remainingInSeconds.toSeconds() };
  }

  @Parameters(name = "{0} from {1}")
  public static final List<Object[]> params() {
    return Arrays.asList(
        EveritWorklogTest.param("0m", EveritWorklogTest.duration()),
        EveritWorklogTest.param("3m", EveritWorklogTest.duration().min(3)),
        EveritWorklogTest.param("1h 3m", EveritWorklogTest.duration().hour(1).min(3)),
        EveritWorklogTest.param("2h 3m", EveritWorklogTest.duration().hour(2).min(3)),
        EveritWorklogTest.param("2w 2d", EveritWorklogTest.duration().week(2).day(2)),
        EveritWorklogTest.param("2d", EveritWorklogTest.duration().day(2)),
        EveritWorklogTest.param("~2d 1h", EveritWorklogTest.duration().day(2).hour(1).min(3)),
        EveritWorklogTest.param("~2w", EveritWorklogTest.duration().week(2).hour(3).min(3)),
        EveritWorklogTest.param("~5w 4d", EveritWorklogTest.duration().week(5).day(4).min(3)),
        EveritWorklogTest.param("~5w", EveritWorklogTest.duration().week(5).hour(4).min(3)));
  }

  private final String expectedRemaining;

  private final int remainingInSecond;

  public EveritWorklogTest(final String expectedRemaining, final Integer remainingInSecond) {
    this.expectedRemaining = expectedRemaining;
    this.remainingInSecond = remainingInSecond;
  }

  /**
   * Mocks the {@code ComponentAccessor.getIssueManager()} call in the
   * {@link EveritWorklog(GenericValue)} constructor.
   */
  public void setupMockIssueManager(final int remainingTimeInSec) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    IssueManager mockIssueManager = Mockito.mock(IssueManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockIssueManager.getIssueObject(Matchers.anyLong()).getKey()).thenReturn("KEY-12");
    Mockito.when(mockIssueManager.getIssueObject(Matchers.anyLong()).getEstimate())
        .thenReturn((long) remainingTimeInSec);
    Mockito.when(mockIssueManager.getIssueObject(Matchers.anyLong()).getStatusObject()
        .getSimpleStatus().getStatusCategory().getKey()).thenReturn("done");
    mockComponentWorker.addMock(IssueManager.class, mockIssueManager);

    PermissionManager mockPermissionManager =
        Mockito.mock(PermissionManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockPermissionManager.hasPermission((ProjectPermissionKey) Matchers.anyObject(),
        (Issue) Matchers.anyObject(),
        (ApplicationUser) Matchers.anyObject())).thenReturn(true);
    mockComponentWorker.addMock(PermissionManager.class, mockPermissionManager);

    ApplicationProperties mockApplicationProperties =
        Mockito.mock(ApplicationProperties.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.complete")))
        .thenReturn("yy-MM-dd h:mm");
    Mockito.when(
        mockApplicationProperties.getDefaultBackedString(Matchers.matches("jira.lf.date.dmy")))
        .thenReturn("yy-MM-dd");
    mockComponentWorker.addMock(ApplicationProperties.class, mockApplicationProperties);

    JiraAuthenticationContext mockJiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockJiraAuthenticationContext.getI18nHelper().getLocale())
        .thenReturn(new Locale("en", "US"));
    Mockito.when(mockJiraAuthenticationContext.getUser()).thenReturn(null);
    mockComponentWorker.addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext);

    BigDecimal daysPerWeek = new BigDecimal(5);
    BigDecimal hoursPerDay = new BigDecimal(8);
    TimeTrackingConfiguration ttConfig = EasyMock.createNiceMock(TimeTrackingConfiguration.class);
    EasyMock.expect(ttConfig.getDaysPerWeek()).andReturn(daysPerWeek).anyTimes();
    EasyMock.expect(ttConfig.getHoursPerDay()).andReturn(hoursPerDay).anyTimes();
    EasyMock.replay(ttConfig);
    mockComponentWorker.addMock(TimeTrackingConfiguration.class, ttConfig).init();
  }

  @Test
  public void test() {
    EveritWorklog subject = worklogWithRemaining(remainingInSecond);
    Assert.assertEquals(expectedRemaining, subject.getRoundedRemaining());
  }

  private EveritWorklog worklogWithRemaining(final int remainingInSeconds) {
    HashMap<String, Object> values = new HashMap<String, Object>();
    setupMockIssueManager(remainingInSeconds);
    values.put("startdate", "2011-11-11 11:11");
    values.put("issue", "11");
    values.put("timeworked", 10L);
    try {
      return new EveritWorklog(new DummyGenericValue(values));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

}
