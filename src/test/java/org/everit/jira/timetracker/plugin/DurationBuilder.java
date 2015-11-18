package org.everit.jira.timetracker.plugin;

/**
 * Fluent API for building durations represented in seconds.
 */
public class DurationBuilder {

  private static final int MINUTE = 60;

  private static final int HOUR = MINUTE * 60;

  private static final int DAY = HOUR * 8;

  private static final int WEEK = DAY * 5;

  private int minutes;

  private int hours;

  private int days;

  private int weeks;

  public DurationBuilder day(final int dayCount) {
    days = dayCount;
    return this;
  }

  public DurationBuilder hour(final int hourCount) {
    hours = hourCount;
    return this;
  }

  public DurationBuilder min(final int minuteCount) {
    minutes = minuteCount;
    return this;
  }

  public int toSeconds() {
    return weeks * WEEK + days * DAY + hours * HOUR + minutes * MINUTE;
  }

  public DurationBuilder week(final int weekCount) {
    weeks = weekCount;
    return this;
  }

}
