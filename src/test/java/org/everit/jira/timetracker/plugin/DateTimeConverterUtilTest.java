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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DateTimeConverterUtilTest {

  private static DurationBuilder duration() {
    return new DurationBuilder();
  }

  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(
        new Object[] { "0m", duration() },
        new Object[] { "3m", duration().min(3) },
        new Object[] { "2h 3m", duration().hour(2).min(3) },
        new Object[] { "2h 0m", duration().hour(2) },
        new Object[] { "2d 2h 0m", duration().day(2).hour(2) },
        new Object[] { "3w 2d 0h 0m", duration().week(3).day(2) });
  }

  private final String expectedString;

  private final long inputSeconds;

  public DateTimeConverterUtilTest(final String expectedString,
      final DurationBuilder inputSeconds) {
    super();
    this.expectedString = expectedString;
    this.inputSeconds = inputSeconds.toSeconds();
  }

  @Test
  public void testSecondConvertToString() {
    Assert.assertEquals(expectedString, DateTimeConverterUtil.secondConvertToString(inputSeconds));
  }
}
