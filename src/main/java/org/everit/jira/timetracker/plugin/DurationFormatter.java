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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.jira.util.JiraDurationUtils.DaysDurationFormatter;
import com.atlassian.jira.util.JiraDurationUtils.HoursDurationFormatter;
import com.atlassian.jira.util.JiraDurationUtils.PrettyDurationFormatter;

/**
 * Used for creating the string representation of
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getExactRemaining()} and
 * {@link org.everit.jira.timetracker.plugin.dto.EveritWorklog#getRoundedRemaining()} properties.
 */
public class DurationFormatter implements Serializable {

  /**
   * Time format names in JIRA system.
   */
  private static final class TimeFormat {

    public static final String DAYS = "days";

    public static final String HOURS = "hours";

    public static final String PRETTY = "pretty";
  }

  private static final String DAY = "d ";

  private static final int DAYIDX = 1;

  private static final String HOUR = "h ";

  private static final int HOURIDX = 2;

  private static final String MIN = "m ";

  private static final int MINIDX = 3;

  private static final long serialVersionUID = 8497858288910306739L;

  private static final String WEEK = "w ";

  private static final int WEEKIDX = 0;

  private DaysDurationFormatter daysDurationFormatter;

  private long durationInSeconds;

  private LinkedHashMap<String, Long> fragments = new LinkedHashMap<>();

  private HoursDurationFormatter hoursDurationFormatter;

  private PrettyDurationFormatter prettyDurationFormatter;

  private String timeFormat;

  private double workDaysPerWeek;

  private double workHoursPerDay;

  /**
   * Simple constructor.
   */
  public DurationFormatter() {

    TimeTrackingConfiguration timeTrackingConfiguration =
        ComponentAccessor.getComponent(TimeTrackingConfiguration.class);

    ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
    timeFormat = applicationProperties.getDefaultBackedString("jira.timetracking.format");

    workDaysPerWeek = timeTrackingConfiguration.getDaysPerWeek().doubleValue();
    workHoursPerDay = timeTrackingConfiguration.getHoursPerDay().doubleValue();

    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    ApplicationUser loggedInUser = jiraAuthenticationContext.getUser();
    BeanFactory i18nHelperFactory = ComponentAccessor.getI18nHelperFactory();
    I18nHelper i18nHelper = i18nHelperFactory.getInstance(loggedInUser);

    daysDurationFormatter = new DaysDurationFormatter(new BigDecimal(workHoursPerDay), i18nHelper);
    hoursDurationFormatter = new HoursDurationFormatter(i18nHelper);
    prettyDurationFormatter = new PrettyDurationFormatter(new BigDecimal(workHoursPerDay),
        new BigDecimal(workDaysPerWeek), i18nHelper);

  }

  private boolean appendValue(final StringBuilder rval, final Long value, final String key,
      final boolean nonzeroFragmentVisited) {
    if (value == null) {
      return nonzeroFragmentVisited;
    }
    if (nonzeroFragmentVisited || (value.longValue() > 0)) {
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

    com.atlassian.jira.util.JiraDurationUtils.DurationFormatter formatter = null;
    if (TimeFormat.DAYS.equals(timeFormat)) {
      formatter = daysDurationFormatter;
    } else if (TimeFormat.HOURS.equals(timeFormat)) {
      formatter = hoursDurationFormatter;
    } else {
      formatter = prettyDurationFormatter;
    }
    return formatter.shortFormat(durationInSeconds);
  }

  private int fragmentCount(final int firstNonzeroIdx, final int lastNonzeroIdx,
      final int handledFragmentCount, final int idx) {
    int count = handledFragmentCount;
    if ((firstNonzeroIdx <= idx) && (idx <= lastNonzeroIdx)) {
      ++count;
    }
    return count;
  }

  private int getIdx(final String key) {
    switch (key) {
      case WEEK:
        return WEEKIDX;
      case DAY:
        return DAYIDX;
      case HOUR:
        return HOURIDX;
      case MIN:
        return MINIDX;
      default:
        return -1;
    }
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
    return industryFormat(hours);
  }

  private String industryFormat(final double hours) {
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
    int idx = getIdx(key);
    if ((firstNonzeroIdx <= idx) && (idx <= lastNonzeroIdx) && (longValue > 0)) {
      if (handledFragmentCount < 2) {
        truncatedFragments.put(key, value);
      } else {
        tilde = true;
      }
    }
    return tilde;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
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
    if (TimeFormat.PRETTY.equals(timeFormat)) {
      constructFragmentsOfRemainingEstimate();
      return calculateFormattedRemaining();
    } else {
      return exactDuration(durationInSeconds);
    }
  }

  /**
   * Convert the working hours per day to Industry rounded format (8.5h) String.
   *
   * @return the formatted work hours per day string.
   */
  public String workHoursDayIndustryDuration() {
    return industryFormat(workHoursPerDay);
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
