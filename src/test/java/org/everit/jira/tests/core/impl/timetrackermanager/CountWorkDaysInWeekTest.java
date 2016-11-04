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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;

public class CountWorkDaysInWeekTest {

  @Test
  public void testWithExcludeAndIncludeDay() throws ParseException {
    TimeTrackingConfiguration mockConfiguration =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    TimetrackerManager timetrackerManager = new TimetrackerComponent(mockConfiguration);
    Mockito.when(mockConfiguration.getDaysPerWeek().doubleValue()).thenReturn(5.0);

    List<Date> weekdays = new ArrayList<>();
    List<String> days = Arrays.asList("2016-10-24", "2016-10-25", "2016-10-26", "2016-10-27",
        "2016-10-28", "2016-10-29", "2016-10-30");
    for (String date : days) {
      weekdays.add(DateTimeConverterUtil.fixFormatStringToDate(date));
    }
    double workDaysInWeek =
        timetrackerManager.countRealWorkDaysInWeek(weekdays, new HashSet<>(
            Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-10-24"),
                DateTimeConverterUtil.fixFormatStringToDate("2016-10-25"))),
            new HashSet<>(
                Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-10-29"))));
    Assert.assertEquals(4.0, workDaysInWeek, 0.0001);
  }

  @Test
  public void testWithExcludeDay() throws ParseException {
    TimeTrackingConfiguration mockConfiguration =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    TimetrackerManager timetrackerManager = new TimetrackerComponent(mockConfiguration);
    Mockito.when(mockConfiguration.getDaysPerWeek().doubleValue()).thenReturn(5.0);

    List<Date> weekdays = new ArrayList<>();
    List<String> days = Arrays.asList("2016-10-24", "2016-10-25", "2016-10-26", "2016-10-27",
        "2016-10-28", "2016-10-29", "2016-10-30");
    for (String date : days) {
      weekdays.add(DateTimeConverterUtil.fixFormatStringToDate(date));
    }
    double workDaysInWeek =
        timetrackerManager.countRealWorkDaysInWeek(weekdays, new HashSet<>(Arrays.asList(
            DateTimeConverterUtil.fixFormatStringToDate("2016-10-25"))),
            Collections.EMPTY_SET);
    Assert.assertEquals(4.0, workDaysInWeek, 0.0001);
  }

  @Test
  public void testWithIncludeDay() throws ParseException {
    TimeTrackingConfiguration mockConfiguration =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    TimetrackerManager timetrackerManager = new TimetrackerComponent(mockConfiguration);
    Mockito.when(mockConfiguration.getDaysPerWeek().doubleValue()).thenReturn(5.0);

    List<Date> weekdays = new ArrayList<>();
    List<String> days = Arrays.asList("2016-10-24", "2016-10-25", "2016-10-26", "2016-10-27",
        "2016-10-28", "2016-10-29", "2016-10-30");
    for (String date : days) {
      weekdays.add(DateTimeConverterUtil.fixFormatStringToDate(date));
    }
    double workDaysInWeek =
        timetrackerManager.countRealWorkDaysInWeek(weekdays, Collections.EMPTY_SET,
            new HashSet<>(
                Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-10-29"))));
    Assert.assertEquals(6.0, workDaysInWeek, 0.0001);
  }

  @Test
  public void testWithoutExcludeAndInclude() throws ParseException {
    TimeTrackingConfiguration mockConfiguration =
        Mockito.mock(TimeTrackingConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
    TimetrackerManager timetrackerManager = new TimetrackerComponent(mockConfiguration);
    Mockito.when(mockConfiguration.getDaysPerWeek().doubleValue()).thenReturn(5.0);

    List<Date> weekdays = new ArrayList<>();
    List<String> days = Arrays.asList("2016-10-24", "2016-10-25", "2016-10-26", "2016-10-27",
        "2016-10-28", "2016-10-29", "2016-10-30");
    for (String date : days) {
      weekdays.add(DateTimeConverterUtil.fixFormatStringToDate(date));
    }
    double workDaysInWeek =
        timetrackerManager.countRealWorkDaysInWeek(weekdays, Collections.EMPTY_SET,
            Collections.EMPTY_SET);
    Assert.assertEquals(5.0, workDaysInWeek, 0.0001);

  }
}
