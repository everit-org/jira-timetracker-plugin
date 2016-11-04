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
package org.everit.jira.tests.timetracker.plugin;

import java.util.Arrays;
import java.util.List;

import org.everit.jira.timetracker.plugin.util.VersionComperatorUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class VersionComperatorTest {

  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(
        new Object[] { "1.1.1", "1.1.1", 0 },
        new Object[] { "1.1.1", "1.1.2", -1 },
        new Object[] { "1.1.2", "1.1.1", 1 },
        new Object[] { "1.1.1", "1.1.1-SNAPSHOT", 0 },
        new Object[] { "1.2.1", "1.1.1-SNAPSHOT", 1 },
        new Object[] { "1.2.1", "1.2.2-SNAPSHOT", -1 },
        new Object[] { "2.0.1", "2.1.0-SNAPSHOT", -1 },
        new Object[] { "3.0.0-RC", "2.1.0-SNAPSHOT", 1 },
        new Object[] { "", "2.1.0-SNAPSHOT", -1 },
        new Object[] { "1.2.0", "", 1 });
  }

  private final int expectedResult;

  private final String version1;

  private final String version2;

  public VersionComperatorTest(final String version1,
      final String version2, final int expectedResult) {
    super();
    this.version1 = version1;
    this.version2 = version2;
    this.expectedResult = expectedResult;
  }

  @Test
  public void testVersionComperator() {
    int versionCompare = VersionComperatorUtil.versionCompare(version1, version2);
    Assert.assertEquals(expectedResult, versionCompare);
  }

}
