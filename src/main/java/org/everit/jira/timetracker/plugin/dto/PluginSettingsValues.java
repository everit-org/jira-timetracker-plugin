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
   * Plugin analytics set.
   */
  public boolean analyticsCheck;

  /**
   * The collector issues pattern list.
   */
  public List<Pattern> collectorIssues;

  /**
   * End time rounding change value.
   */
  public int endTimeChange;

  /**
   * Exclude dates.
   */
  public String excludeDates;

  /**
   * The non working issues list.
   */
  public List<Pattern> filteredSummaryIssues;

  /**
   * Include dates.
   */
  public String includeDates;

  /**
   * The plugin calendar show the actual date when start or the latest day what not contains
   * worklog.
   */
  public Boolean isActualDate;

  /**
   * Coloring settings.
   */
  public Boolean isColoring;

  /**
   * The timetracker progress indicator is daily type.
   */
  public Boolean isProgressIndicatorDaily;

  /**
   * The plugin groups.
   */
  public List<String> pluginGroups;

  /**
   * The plugin UUID.
   */
  public String pluginUUID;

  /**
   * Start time rounding value change.
   */
  public int startTimeChange;

  /**
   * The timetarckning groups.
   */
  public List<String> timetrackingGroups;

  public PluginSettingsValues() {
  }

  public PluginSettingsValues actualDate(final boolean actualDateOrLastWorklogDate) {
    isActualDate = actualDateOrLastWorklogDate;
    return this;
  }

  public PluginSettingsValues analyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
    return this;
  }

  public PluginSettingsValues collectorIssues(final List<Pattern> collectorIssues) {
    this.collectorIssues = collectorIssues;
    return this;
  }

  public PluginSettingsValues coloring(final boolean isColoring) {
    this.isColoring = isColoring;
    return this;
  }

  public PluginSettingsValues endTimeChange(final int endTimeChange) {
    this.endTimeChange = endTimeChange;
    return this;
  }

  public PluginSettingsValues excludeDates(final String excludeDates) {
    this.excludeDates = excludeDates;
    return this;
  }

  public PluginSettingsValues filteredSummaryIssues(
      final List<Pattern> filteredSummaryIssues) {
    this.filteredSummaryIssues = filteredSummaryIssues;
    return this;
  }

  public PluginSettingsValues includeDates(final String includeDates) {
    this.includeDates = includeDates;
    return this;
  }

  public PluginSettingsValues isProgressIndicatordaily(final boolean isProgressIndicatorDaily) {
    this.isProgressIndicatorDaily = isProgressIndicatorDaily;
    return this;
  }

  public PluginSettingsValues pluginGroups(final List<String> pluginGroups) {
    this.pluginGroups = pluginGroups;
    return this;
  }

  public PluginSettingsValues pluginUUID(final String pluginUUID) {
    this.pluginUUID = pluginUUID;
    return this;
  }

  public PluginSettingsValues startTimeChange(final int startTimeChange) {
    this.startTimeChange = startTimeChange;
    return this;
  }

  public PluginSettingsValues timetrackingGroups(final List<String> timetrackingGroups) {
    this.timetrackingGroups = timetrackingGroups;
    return this;
  }

}
