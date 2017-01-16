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
