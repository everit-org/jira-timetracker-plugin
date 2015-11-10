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
package org.everit.jira.timetracker.plugin;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JiraTimetrackerWebActionTest {

  private JiraTimetrackerWebAction subject;

  private void assertParsedEditAllIds(final String raw, final int... expected) {
    ArrayList<Long> expectedList = new ArrayList<>(expected.length);
    for (int exp : expected) {
      expectedList.add((long) exp);
    }
    subject.setEditAllIds(raw);
    Assert.assertEquals(expectedList, subject.parseEditAllIds());
  }

  @Before
  public void before() {
    subject = new JiraTimetrackerWebAction(new JiraTimetrackerPluginImpl(null));
  }

  @Test
  public void parseEditAllIds() {
    assertParsedEditAllIds("[1, 2, 3]", 1, 2, 3);
  }

  @Test
  public void parseEmptyEditAllIds() {
    assertParsedEditAllIds("[]");
  }

}
