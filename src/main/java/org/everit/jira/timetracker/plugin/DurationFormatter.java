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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used for creating the string representation of
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getExactRemaining()} and
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getRoundedRemaining()} properties.
 */
public final class DurationFormatter {

  public static String exactDuration(final long durationInSeconds, final long workDaysPerWeek,
      final long workHoursPerDay) {
    return new DurationFormatter(durationInSeconds, workDaysPerWeek, workHoursPerDay)
        .buildFromFragments(false);
  }

  public static String roundedDuration(final long durationInSeconds, final long workDaysPerWeek,
      final long workHoursPerDay) {
    return new DurationFormatter(durationInSeconds, workDaysPerWeek, workHoursPerDay)
        .calculateFormattedRemaining();
  }

  private final long durationInSeconds;

  private LinkedHashMap<String, Long> fragments = new LinkedHashMap<>();

  private final long workDaysPerWeek;

  private final long workHoursPerDay;

  private DurationFormatter(final long durationInSeconds, final long workDaysPerWeek,
      final long workHoursPerDay) {
    this.durationInSeconds = durationInSeconds;
    this.workDaysPerWeek = workDaysPerWeek;
    this.workHoursPerDay = workHoursPerDay;
    constructFragmentsOfRemainingEstimate();
  }

  private String buildFromFragments(final boolean needsTilde) {
    StringBuilder rval = new StringBuilder(needsTilde ? "~" : "");
    boolean nonzeroFragmentVisited = false;
    for (Map.Entry<String, Long> fragment : fragments.entrySet()) {
      Long value = fragment.getValue();
      if (value.longValue() > 0) {
        nonzeroFragmentVisited = true;
      }
      if (nonzeroFragmentVisited) {
        rval.append(value).append(fragment.getKey());
      }
    }
    if (nonzeroFragmentVisited) {
      return rval.toString().trim();
    } else {
      return "0m";
    }
  }

  private String buildRoundedEstimateString(final int firstNonzeroIdx, final int lastNonzeroIdx) {
    LinkedHashMap<String, Long> truncatedFragments = new LinkedHashMap<>();
    int idx = 0;
    int handledFragmentCount = 0;
    boolean needsTilde = false;
    for (Map.Entry<String, Long> fragment : fragments.entrySet()) {
      Long value = fragment.getValue();
      if ((firstNonzeroIdx <= idx) && (idx <= lastNonzeroIdx)) {
        if (value.longValue() > 0) {
          if (handledFragmentCount < 2) {
            truncatedFragments.put(fragment.getKey(), value);
          } else {
            needsTilde = true;
          }
        }
        ++handledFragmentCount;
      }
      ++idx;
    }
    fragments = truncatedFragments;
    return buildFromFragments(needsTilde);
  }

  private String calculateFormattedRemaining() {
    constructFragmentsOfRemainingEstimate();
    int firstNonzeroIdx = -1;
    int lastNonzeroIdx = 0;
    int idx = 0;
    for (Map.Entry<String, Long> fragment : fragments.entrySet()) {
      if (fragment.getValue().longValue() != 0) {
        if (firstNonzeroIdx == -1) {
          firstNonzeroIdx = idx;
        }
        lastNonzeroIdx = idx;
      }
      ++idx;
    }
    lastNonzeroIdx = Math.max(lastNonzeroIdx, firstNonzeroIdx + 1);
    return buildRoundedEstimateString(firstNonzeroIdx, lastNonzeroIdx);
  }

  private void constructFragmentsOfRemainingEstimate() {
    long estimate = durationInSeconds / DateTimeConverterUtil.MINUTES_PER_HOUR;
    long minutes = estimate % DateTimeConverterUtil.MINUTES_PER_HOUR;
    estimate /= DateTimeConverterUtil.MINUTES_PER_HOUR;
    long hours = estimate % workHoursPerDay;
    estimate /= workHoursPerDay;
    long days = estimate % workDaysPerWeek;
    estimate /= workDaysPerWeek;
    long weeks = estimate;
    fragments.put("w ", weeks);
    fragments.put("d ", days);
    fragments.put("h ", hours);
    fragments.put("m ", minutes);
  }

}
