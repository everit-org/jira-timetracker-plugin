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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.jira.core.SupportManager;
import org.everit.jira.core.impl.SupportComponent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;

public class GetProjectIdsTest {

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
  }

  private GenericValue createDummyGenericValue(final int projectId) {
    HashMap<String, Object> values = new HashMap<>();
    values.put("id", projectId);
    return new DummyGenericValue(values);
  }

  private void initMockComponentWorker() {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    OfBizDelegator ofBizDelegator = Mockito.mock(OfBizDelegator.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(ofBizDelegator.findAll(Matchers.eq("Project")))
        .thenReturn(new ArrayList<>(
            Arrays.asList(
                createDummyGenericValue(1),
                createDummyGenericValue(2))));

    mockComponentWorker.addMock(OfBizDelegator.class, ofBizDelegator)
        .init();
  }

  @Test
  public void testGetProjectIds() {
    initMockComponentWorker();

    SupportManager supportManager = new SupportComponent(null);

    List<String> projectsId = supportManager.getProjectsId();
    Assert.assertEquals(2, projectsId.size());
    Assert.assertEquals("1", projectsId.get(0));
    Assert.assertEquals("2", projectsId.get(1));
  }
}
