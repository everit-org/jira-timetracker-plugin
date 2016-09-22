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

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;

/**
 * Used for creating the string representation of
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getExactRemaining()} and
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getRoundedRemaining()} properties.
 */
public class DurationFormatter implements Serializable {

  private static final String DAY = "d ";

  private static final int DAYIDX = 1;

  private static final String HOUR = "h ";

  private static final int HOURIDX = 2;

  private static final String MIN = "m ";

  private static final int MINIDX = 3;

  private static final long serialVersionUID = 8497858288910306739L;

  private static final String WEEK = "w ";

  private static final int WEEKIDX = 0;

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

  private boolean appendValue(final StringBuilder rval, final Long value, final String key,
      final boolean nonzeroFragmentVisited) {
    if (value == null) {
      return nonzeroFragmentVisited;
    }
    if (nonzeroFragmentVisited || value.longValue() > 0) {
      rval.append(value.longValue()).append(key);
      return true;
    }
    return false;
  }

  private String buildFromFragments(final boolean needsTilde) {
    StringBuilder rval = new StringBuilder(needsTilde ? "~" : "");
    boolean nonzeroFragmentVisited = false;
    nonzeroFragmentVisited = appendValue(rval, fragments.get(WEEK), WEEK, nonzeroFragmentVisited);
    nonzeroFragmentVisited = appendValue(rval, fragments.get(DAY), DAY, nonzeroFragmentVisited);
    nonzeroFragmentVisited = appendValue(rval, fragments.get(HOUR), HOUR, nonzeroFragmentVisited);
    nonzeroFragmentVisited = appendValue(rval, fragments.get(MIN), MIN, nonzeroFragmentVisited);
    if (nonzeroFragmentVisited) {
      return rval.toString().trim();
    } else {
      return "0m";
    }
  }

  private String buildRoundedEstimateString(final int firstNonzeroIdx, final int lastNonzeroIdx) {
    LinkedHashMap<String, Long> truncatedFragments = new LinkedHashMap<>();
    int handledFragmentCount = 0;
    boolean needsTilde = false;
    needsTilde = needsTildeValue(firstNonzeroIdx, lastNonzeroIdx, truncatedFragments,
        handledFragmentCount, needsTilde, fragments.get(WEEK), WEEK);
    handledFragmentCount =
        fragmentCount(firstNonzeroIdx, lastNonzeroIdx, handledFragmentCount, WEEKIDX);
    needsTilde = needsTildeValue(firstNonzeroIdx, lastNonzeroIdx, truncatedFragments,
        handledFragmentCount, needsTilde, fragments.get(DAY), DAY);
    handledFragmentCount =
        fragmentCount(firstNonzeroIdx, lastNonzeroIdx, handledFragmentCount, DAYIDX);
    needsTilde = needsTildeValue(firstNonzeroIdx, lastNonzeroIdx, truncatedFragments,
        handledFragmentCount, needsTilde, fragments.get(HOUR), HOUR);
    handledFragmentCount =
        fragmentCount(firstNonzeroIdx, lastNonzeroIdx, handledFragmentCount, HOURIDX);
    needsTilde = needsTildeValue(firstNonzeroIdx, lastNonzeroIdx, truncatedFragments,
        handledFragmentCount, needsTilde, fragments.get(MIN), MIN);
    fragments = truncatedFragments;
    return buildFromFragments(needsTilde);
  }

  private String calculateFormattedRemaining() {
    constructFragmentsOfRemainingEstimate();
    int firstNonzeroIdx = -1;
    int lastNonzeroIdx = 0;
    if (fragments.get(WEEK).longValue() != 0) {
      if (firstNonzeroIdx == -1) {
        firstNonzeroIdx = WEEKIDX;
      }
      lastNonzeroIdx = WEEKIDX;
    }
    if (fragments.get(DAY).longValue() != 0) {
      if (firstNonzeroIdx == -1) {
        firstNonzeroIdx = DAYIDX;
      }
      lastNonzeroIdx = DAYIDX;
    }
    if (fragments.get(HOUR).longValue() != 0) {
      if (firstNonzeroIdx == -1) {
        firstNonzeroIdx = HOURIDX;
      }
      lastNonzeroIdx = HOURIDX;
    }
    if (fragments.get(MIN).longValue() != 0) {
      if (firstNonzeroIdx == -1) {
        firstNonzeroIdx = MINIDX;
      }
      lastNonzeroIdx = MINIDX;
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

  private int fragmentCount(final int firstNonzeroIdx, final int lastNonzeroIdx,
      final int handledFragmentCount, final int idx) {
    int count = handledFragmentCount;
    if (firstNonzeroIdx <= idx && idx <= lastNonzeroIdx) {
      ++count;
    }
    return count;
  }

  private int getIdx(final String key) {
    int idx;
    switch (key) {
      case WEEK:
        idx = WEEKIDX;
        break;
      case DAY:
        idx = DAYIDX;
        break;
      case HOUR:
        idx = HOURIDX;
        break;
      case MIN:
        idx = MINIDX;
        break;
      default:
        idx = -1;
        break;
    }
    return idx;
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

  private boolean needsTildeValue(final int firstNonzeroIdx, final int lastNonzeroIdx,
      final LinkedHashMap<String, Long> truncatedFragments,
      final int handledFragmentCount,
      final boolean needsTilde, final Long value, final String key) {
    boolean tilde = needsTilde;
    long longValue = value.longValue();
    int idx;
    idx = getIdx(key);
    if (firstNonzeroIdx <= idx && idx <= lastNonzeroIdx && longValue > 0) {
      if (handledFragmentCount < 2) {
        truncatedFragments.put(key, value);
      } else {
        tilde = true;
      }
    }
    return tilde;
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
