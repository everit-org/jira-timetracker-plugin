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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.mock.component.MockComponentWorker;

@RunWith(Parameterized.class)
public class DateTimeConverterUtilTest {

  private static DurationBuilder duration(final double hoursPerDayParam,
      final double daysPerWeekParam) {
    return new DurationBuilder(hoursPerDayParam, daysPerWeekParam);
  }

  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(
        new Object[] { "0m", duration(8, 5), 8, 5 },
        new Object[] { "3m", duration(8, 5).min(3), 8, 5 },
        new Object[] { "2h 3m", duration(8, 5).hour(2).min(3), 8, 5 },
        new Object[] { "2h 0m", duration(8, 5).hour(2), 8, 5 },
        new Object[] { "2d 2h 0m", duration(8, 5).day(2).hour(2), 8, 5 },
        new Object[] { "3w 2d 0h 0m", duration(8, 5).week(3).day(2), 8, 5 },
        new Object[] { "1d 0h 0m", duration(7, 5).hour(7), 7, 5 },
        new Object[] { "7h 0m", duration(7.5, 5).hour(7), 7.5, 5 },
        new Object[] { "1d 0h 0m", duration(7.5, 5).hour(7).min(30), 7.5, 5 },
        new Object[] { "1d 0h 30m", duration(7.5, 5).hour(8), 7.5, 5 },
        new Object[] { "1w 0d 4h 0m", duration(8, 4.5).hour(40), 8, 4.5 },
        new Object[] { "1w 0d 6h 15m", duration(7.5, 4.5).hour(40), 7.5, 4.5 });
  }

  private final double dayPerWeek;

  private final String expectedString;

  private final double hoursPerDay;

  private final long inputSeconds;

  public DateTimeConverterUtilTest(final String expectedString,
      final DurationBuilder inputSeconds, final double hoursPerDay, final double dayPerWeek) {
    super();
    this.expectedString = expectedString;
    this.inputSeconds = inputSeconds.toSeconds();
    this.hoursPerDay = hoursPerDay;
    this.dayPerWeek = dayPerWeek;
  }

  /**
   * Mocks the {@code ComponentAccessor.getComponent(TimeTrackingConfiguration.class);} call in the
   * {@link DateTimeConverterUtil.secondConvertToString} constructor.
   */
  public void setupMockTimeTrackerConfig(final double hoursPerDayParam,
      final double daysPerWeekParam) {
    BigDecimal daysPerWeek = new BigDecimal(daysPerWeekParam);
    BigDecimal hoursPerDay = new BigDecimal(hoursPerDayParam);
    TimeTrackingConfiguration ttConfig = EasyMock.createNiceMock(TimeTrackingConfiguration.class);
    EasyMock.expect(ttConfig.getDaysPerWeek()).andReturn(daysPerWeek)
        .anyTimes();
    EasyMock.expect(ttConfig.getHoursPerDay()).andReturn(hoursPerDay)
        .anyTimes();
    EasyMock.replay(ttConfig);
    new MockComponentWorker().addMock(TimeTrackingConfiguration.class, ttConfig).init();
  }

  @Test
  public void testSecondConvertToString() {
    setupMockTimeTrackerConfig(hoursPerDay, dayPerWeek);
    Assert.assertEquals(expectedString, DateTimeConverterUtil.secondConvertToString(inputSeconds));
  }
}
