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
