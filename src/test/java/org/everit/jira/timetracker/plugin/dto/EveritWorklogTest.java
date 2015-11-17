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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;

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

  @Before
  public void before() {
    MockIssue mockIssue = new MockIssue() {
      @Override
      public Issue getParentObject() {
        return null;
      }
    };
    mockIssue.setKey("KEY-12");
    mockIssue.setEstimate(18000L);
    IssueManager mgr = EasyMock.createNiceMock(IssueManager.class);
    EasyMock.expect(mgr.getIssueObject(EasyMock.<Long> anyObject())).andReturn(mockIssue)
        .anyTimes();
    EasyMock.replay(mgr);
    new MockComponentWorker().addMock(IssueManager.class, mgr).init();
  }

  @Test
  public void dummyWorks() {
    HashMap<String, Object> values = new HashMap<String, Object>();
    values.put("id", 10L);
    DummyGenericValue dummy = new DummyGenericValue(values);
    Assert.assertEquals(new Long(10L), dummy.getLong("id"));
  }

  @Test
  public void min3() {
    EveritWorklog subject = worklogWithRemaining(180);
    System.out.println(subject.getRemaining());
  }

  private EveritWorklog worklogWithRemaining(final int remainingInSeconds) {
    HashMap<String, Object> values = new HashMap<String, Object>();
    values.put("remaining", new Long(remainingInSeconds));
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
