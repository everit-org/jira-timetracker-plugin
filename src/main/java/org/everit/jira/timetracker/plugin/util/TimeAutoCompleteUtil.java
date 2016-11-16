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
package org.everit.jira.timetracker.plugin.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Util class for time input fields autocomplet function.
 */
public final class TimeAutoCompleteUtil {

  /**
   * Generate the auto complete time list. The list elements are formatted by jira
   * DateTimeStyle.TIME value.
   *
   * @return The generated autocomplete list.
   */
  public static List<String> generateAutoCompleteList() {
    Calendar timeCalendar = Calendar.getInstance();
    List<String> result = new ArrayList<String>();
    for (int hour = 0; hour < DateTimeConverterUtil.HOURS_IN_DAY; hour++) {
      for (int quater = 0; quater < DateTimeConverterUtil.QUATERS_IN_HOUR; quater++) {
        timeCalendar.set(Calendar.HOUR_OF_DAY, hour);
        timeCalendar.set(Calendar.MINUTE, quater * DateTimeConverterUtil.MINS_IN_QUATER);
        String timeString = DateTimeConverterUtil.dateTimeToString(timeCalendar.getTime());
        if (!result.contains(timeString)) {
          result.add(timeString);
        }
      }
    }
    return result;
  }

  private TimeAutoCompleteUtil() {

  }

}
