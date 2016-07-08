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

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;

/**
 * Used for creating the string representation of
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getExactRemaining()} and
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getRoundedRemaining()} properties.
 */
public class DurationFormatter implements Serializable {

  private static final long serialVersionUID = 8497858288910306739L;

  private long durationInSeconds;

  private LinkedHashMap<String, Long> fragments = new LinkedHashMap<>();

  private double workDaysPerWeek;

  private double workHoursPerDay;

  /**
   * Simple constructor.
   */
  public DurationFormatter() {
    TimeTrackingConfiguration timeTrackingConfiguration =
        ComponentAccessor.getComponent(TimeTrackingConfiguration.class);
    workDaysPerWeek = timeTrackingConfiguration.getDaysPerWeek().doubleValue();
    workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();
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
        rval.append(value.longValue()).append(fragment.getKey());
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
    double weekInMin =
        workDaysPerWeek * workHoursPerDay * DateTimeConverterUtil.MINUTES_PER_HOUR;
    double dayInMin = workHoursPerDay * DateTimeConverterUtil.MINUTES_PER_HOUR;
    long weeks = (long) (estimate / weekInMin);
    estimate %= weekInMin;
    long days = (long) (estimate / dayInMin);
    estimate %= dayInMin;
    long hours = estimate / DateTimeConverterUtil.MINUTES_PER_HOUR;
    long minutes = estimate % DateTimeConverterUtil.MINUTES_PER_HOUR;
    fragments.put("w ", weeks);
    fragments.put("d ", days);
    fragments.put("h ", hours);
    fragments.put("m ", minutes);
  }

  /**
   * Convert the seconds to jira exact format (1h 30m) String.
   *
   * @param durationInSeconds
   *          dutation in seconds.
   * @return the formatted duration string.
   */
  public String exactDuration(final long durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
    constructFragmentsOfRemainingEstimate();
    return buildFromFragments(false);
  }

  /**
   * Convert the seconds to Industry rounded format (8.5h) String.
   *
   * @param durationInSeconds
   *          dutation in seconds.
   * @return the formatted duration string.
   */
  public String industryDuration(final long durationInSeconds) {
    double durationInMins = durationInSeconds / (double) DateTimeConverterUtil.SECONDS_PER_MINUTE;
    double hours = durationInMins / DateTimeConverterUtil.MINUTES_PER_HOUR;
    DecimalFormat df = new DecimalFormat("#.#");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df.format(hours) + "h";
  }

  /**
   * Convert the seconds to jira rounded format (1h 30m) String.
   *
   * @param durationInSeconds
   *          dutation in seconds.
   * @return the formatted duration string.
   */
  public String roundedDuration(final long durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
    constructFragmentsOfRemainingEstimate();
    return calculateFormattedRemaining();
  }

  /**
   * Convert the working hours per day to Industry rounded format (8.5h) String.
   *
   * @return the formatted work hours per day string.
   */
  public String workHoursDayIndustryDuration() {
    DecimalFormat df = new DecimalFormat("#.#");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df.format(workHoursPerDay) + "h";
  }
}
