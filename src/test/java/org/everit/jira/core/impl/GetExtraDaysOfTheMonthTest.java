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
package org.everit.jira.core.impl;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.everit.jira.core.TimetrackerManager;
import org.everit.jira.core.impl.TimetrackerComponent;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetExtraDaysOfTheMonthTest {

  private TimetrackerManager timetrackerManager;

  @Before
  public void before() {
    timetrackerManager = new TimetrackerComponent(null);
  }

  @Test
  public void testOneExcludeDate() throws ParseException {
    List<Date> excludeDaysOfTheMonth = timetrackerManager.getExcludeDaysOfTheMonth(
        DateTimeConverterUtil.fixFormatStringToDate("2016-01-05"),
        new HashSet<>(Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-01-05"),
            DateTimeConverterUtil.fixFormatStringToDate("2016-02-05"))));
    Assert.assertEquals(
        new HashSet<>(Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-01-05"))),
        new HashSet<>(excludeDaysOfTheMonth));
  }

  @Test
  public void testOneIncludeDate() throws ParseException {
    List<Date> excludeDaysOfTheMonth = timetrackerManager.getIncludeDaysOfTheMonth(
        DateTimeConverterUtil.fixFormatStringToDate("2016-01-05"),
        new HashSet<>(Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-01-01"),
            DateTimeConverterUtil.fixFormatStringToDate("2016-02-01"))));
    Assert.assertEquals(
        new HashSet<>(Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2016-01-01"))),
        new HashSet<>(excludeDaysOfTheMonth));
  }
}
