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
package org.everit.jira.timetracker.plugin.dto;

/**
 * Settings of the shown calendars.
 */
public class CalendarSettingsValues {
  private int isCalendarPopup;

  private boolean isActualDate;

  private String excludeDates;

  private String includeDates;

  private boolean isColoring;

  /**
   * Constructor for the calendar settings.
   *
   * @param isCalendarPopup
   *          isCalendarPopup
   * @param isActualDate
   *          isActualDate
   * @param excludeDates
   *          excludeDates
   * @param includeDates
   *          includeDates
   * @param isColoring
   *          isColoring
   */
  public CalendarSettingsValues(final int isCalendarPopup, final boolean isActualDate,
      final String excludeDates, final String includeDates, final boolean isColoring) {
    this.isCalendarPopup = isCalendarPopup;
    this.isActualDate = isActualDate;
    this.excludeDates = excludeDates;
    this.includeDates = includeDates;
    this.isColoring = isColoring;
  }

  public String getExcludeDates() {
    return excludeDates;
  }

  public String getIncludeDates() {
    return includeDates;
  }

  public int getIsCalendarPopup() {
    return isCalendarPopup;
  }

  public boolean isActualDate() {
    return isActualDate;
  }

  public boolean isColoring() {
    return isColoring;
  }
}
