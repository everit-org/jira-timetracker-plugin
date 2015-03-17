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

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * PluginSettingsValues class contains the result of the JiraTimetrackerPlugin's loadPluginSetting
 * method.
 */
public class PluginSettingsValues implements Serializable {

  private static final long serialVersionUID = 4176034622671140618L;

  /**
   * The plugin calendar is popup.
   */
  private int isCalendarPopup;

  /**
   * The plugin calendar show the actual date when start or the latest day what not contains
   * worklog.
   */
  private Boolean isActualDate;
  /**
   * The non working issues list.
   */
  private List<Pattern> filteredSummaryIssues;
  /**
   * The collector issues pattern list.
   */
  private List<Pattern> collectorIssues;

  // TODO javadoc !!! WHY MISSING!!!

  private int startTimeChange;

  private int endTimeChange;

  private String excludeDates;

  private String includeDates;

  private Boolean isColoring;

  public PluginSettingsValues() {

  }

  /**
   * Constructor for a PluginSettingsValues.
   *
   * @param calendarSettingValues
   *          Calendar settings
   * @param filteredSummaryIssues
   *          Non working issues
   * @param collectorIssues
   *          Collector issues
   * @param startTimeChange
   *          Start time change value
   * @param endTimeChange
   *          End time change value
   */
  public PluginSettingsValues(final CalendarSettingsValues calendarSettingValues,
      final List<Pattern> filteredSummaryIssues,
      final List<Pattern> collectorIssues,
      final int startTimeChange, final int endTimeChange) {
    super();
    isCalendarPopup = calendarSettingValues.getIsCalendarPopup();
    isActualDate = calendarSettingValues.isActualDate();
    this.filteredSummaryIssues = filteredSummaryIssues;
    this.collectorIssues = collectorIssues;
    excludeDates = calendarSettingValues.getExcludeDates();
    includeDates = calendarSettingValues.getIncludeDates();
    this.startTimeChange = startTimeChange;
    this.endTimeChange = endTimeChange;
    isColoring = calendarSettingValues.isColoring();
  }

  public List<Pattern> getCollectorIssues() {
    return collectorIssues;
  }

  public int getEndTimeChange() {
    return endTimeChange;
  }

  public String getExcludeDates() {
    return excludeDates;
  }

  public List<Pattern> getFilteredSummaryIssues() {
    return filteredSummaryIssues;
  }

  public String getIncludeDates() {
    return includeDates;
  }

  public int getIsCalendarPopup() {
    return isCalendarPopup;
  }

  public int getStartTimeChange() {
    return startTimeChange;
  }

  public Boolean isActualDate() {
    return isActualDate;
  }

  public int isCalendarPopup() {
    return isCalendarPopup;
  }

  public Boolean isColoring() {
    return isColoring;
  }

  public void setActualDate(final boolean actualDateOrLastWorklogDate) {
    isActualDate = actualDateOrLastWorklogDate;
  }

  public void setCalendarPopup(final int isCalendarPopup) {
    this.isCalendarPopup = isCalendarPopup;
  }

  public void setCollectorIssues(final List<Pattern> collectorIssues) {
    this.collectorIssues = collectorIssues;
  }

  public void setColoring(final boolean isColoring) {
    this.isColoring = isColoring;
  }

  public void setEndTimeChange(final int endTimeChange) {
    this.endTimeChange = endTimeChange;
  }

  public void setExcludeDates(final String excludeDates) {
    this.excludeDates = excludeDates;
  }

  public void setFilteredSummaryIssues(
      final List<Pattern> filteredSummaryIssues) {
    this.filteredSummaryIssues = filteredSummaryIssues;
  }

  public void setIncludeDates(final String includeDates) {
    this.includeDates = includeDates;
  }

  public void setIsCalendarPopup(final int isCalendarPopup) {
    this.isCalendarPopup = isCalendarPopup;
  }

  public void setStartTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
  }

}
