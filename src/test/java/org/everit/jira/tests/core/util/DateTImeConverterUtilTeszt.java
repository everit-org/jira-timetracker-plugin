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
package org.everit.jira.tests.core.util;

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class DateTImeConverterUtilTeszt {

  @Test
  public void setDayBeginingTest() {
    DateTime dayStart =
        DateTimeConverterUtil.setDateToDayStart(new DateTime(1483236000000L, DateTimeZone.UTC));
    Assert.assertEquals(1483228800000L, dayStart.getMillis());

    dayStart =
        DateTimeConverterUtil
            .setDateToDayStart(new DateTime(1483236000000L, DateTimeZone.forID("Etc/GMT+3")));
    Assert.assertEquals(1483153200000L, dayStart.getMillis());
  }

}
