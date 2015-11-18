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
package org.everit.jira.timetracker.plugin.dto;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;

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

  private static final int MINUTE = 60;

  private static final int HOUR = MINUTE * 60;

  private static final int DAY = HOUR * 8;

  private static final int WEEK = DAY * 5;

  private static Object[] param(final String expectedRemaining, final int remainingInSeconds) {
    return new Object[] { expectedRemaining, remainingInSeconds };
  }

  @Parameters(name = "{0} from {1}")
  public static final List<Object[]> params() {
    return Arrays.asList(
        param("", 0),
        param("3m", 3 * MINUTE),
        param("1h 3m", HOUR + 3 * MINUTE),
        param("2h 3m", 2 * HOUR + 3 * MINUTE),
        param("2w 2d", 2 * WEEK + 2 * DAY),
        param("2d", 2 * DAY),
        param("~2d 1h", (2 * DAY) + (1 * HOUR) + (3 * MINUTE)),
        param("~2w", (2 * WEEK) + (3 * HOUR) + (3 * MINUTE)),
        param("~5w 4d", (5 * WEEK) + (4 * DAY) + (3 * MINUTE)),
        param("~5w", (5 * WEEK) + (4 * HOUR) + (3 * MINUTE)));
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
    MockIssue mockIssue = new MockIssue() {
      @Override
      public Issue getParentObject() {
        return null;
      }
    };
    mockIssue.setKey("KEY-12");
    mockIssue.setEstimate((long) remainingTimeInSec);
    IssueManager mgr = EasyMock.createNiceMock(IssueManager.class);
    EasyMock.expect(mgr.getIssueObject(EasyMock.<Long> anyObject())).andReturn(mockIssue)
        .anyTimes();
    EasyMock.replay(mgr);
    new MockComponentWorker().addMock(IssueManager.class, mgr).init();
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
