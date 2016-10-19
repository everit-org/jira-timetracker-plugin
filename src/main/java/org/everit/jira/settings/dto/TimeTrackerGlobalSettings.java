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
package org.everit.jira.settings.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Mapping and converter class for the timetracker global settings.
 */
public class TimeTrackerGlobalSettings {

  private Map<GlobalSettingsKey, Object> pluginSettingsKeyValues = new HashMap<>();

  /**
   * Put the analytics check value.
   */
  public TimeTrackerGlobalSettings analyticsCheck(final boolean analyticsCheck) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.ANALYTICS_CHECK_CHANGE,
        String.valueOf(analyticsCheck));
    return this;
  }

  /**
   * Put collector issues.
   */
  public TimeTrackerGlobalSettings collectorIssues(final List<Pattern> collectorIssues) {
    // TODO in pattern out string
    pluginSettingsKeyValues.put(GlobalSettingsKey.NON_ESTIMATED_ISSUES, collectorIssues);
    return this;
  }

  /**
   * Put exclude dates.
   */
  public TimeTrackerGlobalSettings excludeDates(final String excludeDates) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.EXCLUDE_DATES, excludeDates);
    return this;
  }

  /**
   * Put filtered summary issues.
   */
  public TimeTrackerGlobalSettings filteredSummaryIssues(
      final List<Pattern> filteredSummaryIssues) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.SUMMARY_FILTERS, filteredSummaryIssues);
    return this;
  }

  /**
   * Gets analytics check value.
   *
   * @return the analytics check value.
   */
  public boolean getAnalyticsCheck() {
    boolean analyticsCheckValue = true;
    if ("false".equals(pluginSettingsKeyValues.get(GlobalSettingsKey.ANALYTICS_CHECK_CHANGE))) {
      analyticsCheckValue = false;
    }
    return analyticsCheckValue;
  }

  /**
   * Get the exclude dates as Set.
   */
  public Set<String> getExcludeDatesAsSet() {
    String excludeDatesString = getExcludeDatesAsString();
    Set<String> excludeDatesSet = new HashSet<>();
    if (excludeDatesString.isEmpty()) {
      return excludeDatesSet;
    }
    for (String excludeDate : excludeDatesString.split(",")) {
      excludeDatesSet.add(excludeDate);
    }
    return excludeDatesSet;
  }

  /**
   * Get the exclude dates as String.
   */
  public String getExcludeDatesAsString() {
    String tempSpecialDates =
        (String) pluginSettingsKeyValues.get(GlobalSettingsKey.EXCLUDE_DATES);
    String excludeDatesString = "";
    if (tempSpecialDates != null) {
      excludeDatesString = tempSpecialDates;
    }
    return excludeDatesString;
  }

  /**
   * Get include dates as Set.
   */
  public Set<String> getIncludeDatesAsSet() {
    String tempSpecialDates = getIncludeDatesAsString();
    Set<String> includeDatesSet = new HashSet<>();
    if (tempSpecialDates.isEmpty()) {
      return includeDatesSet;
    }
    for (String includeDate : tempSpecialDates.split(",")) {
      includeDatesSet.add(includeDate);
    }
    return includeDatesSet;
  }

  /**
   * Get include dates as String.
   */
  public String getIncludeDatesAsString() {
    String tempSpecialDates =
        (String) pluginSettingsKeyValues.get(GlobalSettingsKey.INCLUDE_DATES);
    String includeDatesString = "";
    if (tempSpecialDates != null) {
      includeDatesString = tempSpecialDates;
    }
    return includeDatesString;
  }

  /**
   * Get the issue patterns.
   */
  public List<Pattern> getIssuePatterns() {
    // TODO in pattern out string
    List<String> tempIssuePatternList = (List<String>) pluginSettingsKeyValues.get(
        GlobalSettingsKey.NON_ESTIMATED_ISSUES);
    List<Pattern> collectorIssuePatterns = new ArrayList<>();
    if (tempIssuePatternList != null) {
      // add collector issues
      for (String tempIssuePattern : tempIssuePatternList) {
        collectorIssuePatterns.add(Pattern.compile(tempIssuePattern));
      }
    }
    return collectorIssuePatterns;
  }

  /**
   * Get the last update time in milisec.
   */
  public Long getLastUpdate() {
    String lastUpdateInMilisec = (String) pluginSettingsKeyValues.get(
        GlobalSettingsKey.UPDATE_NOTIFIER_LAST_UPDATE);
    return lastUpdateInMilisec == null ? null : Long.parseLong(lastUpdateInMilisec);

  }

  /**
   * Get the latest version which the user is canceled.
   */
  public String getLatestVersion() {
    return (String) pluginSettingsKeyValues.get(GlobalSettingsKey.UPDATE_NOTIFIER_LATEST_VERSION);
  }

  /**
   * Get the non working issue filters.
   */
  public List<Pattern> getNonWorkingIssuePatterns() {
    List<String> tempSummaryFilter =
        (List<String>) pluginSettingsKeyValues.get(GlobalSettingsKey.SUMMARY_FILTERS);
    List<Pattern> nonWorkingIssuePatterns = new ArrayList<>();
    if (tempSummaryFilter != null) {
      // add non working issues
      for (String tempIssuePattern : tempSummaryFilter) {
        nonWorkingIssuePatterns.add(Pattern.compile(tempIssuePattern));
      }
    }
    return nonWorkingIssuePatterns;
  }

  /**
   * Get the plugin groups.
   */
  public List<String> getPluginGroups() {
    List<String> pluginGroupsNames =
        (List<String>) pluginSettingsKeyValues.get(GlobalSettingsKey.PLUGIN_PERMISSION);
    List<String> pluginGroups = new ArrayList<>();
    if (pluginGroupsNames != null) {
      pluginGroups = pluginGroupsNames;
    }
    return pluginGroups;
  }

  public Map<GlobalSettingsKey, Object> getPluginSettingsKeyValues() {
    return pluginSettingsKeyValues;
  }

  public String getPluginUUID() {
    return (String) pluginSettingsKeyValues.get(GlobalSettingsKey.PLUGIN_UUID);
  }

  /**
   * Get the time tracker groups.
   */
  public List<String> getTimetrackerGroups() {
    List<String> timetrackerGroupsNames =
        (List<String>) pluginSettingsKeyValues.get(GlobalSettingsKey.TIMETRACKER_PERMISSION);
    List<String> timetrackerGroups = new ArrayList<>();
    if (timetrackerGroupsNames != null) {
      timetrackerGroups = timetrackerGroupsNames;
    }
    return timetrackerGroups;
  }

  /**
   * Put the include dates.
   */
  public TimeTrackerGlobalSettings includeDates(final String includeDates) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.INCLUDE_DATES, includeDates);
    return this;
  }

  /**
   * Put the update notifier last update time.
   */
  public TimeTrackerGlobalSettings lastUpdateTime(final long time) {
    pluginSettingsKeyValues.put(
        GlobalSettingsKey.UPDATE_NOTIFIER_LAST_UPDATE, String.valueOf(time));
    return this;
  }

  /**
   * Put the latest version of the JTTP plugin.
   */
  public TimeTrackerGlobalSettings latestVersion(final String latestVersion) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.UPDATE_NOTIFIER_LATEST_VERSION, latestVersion);
    return this;
  }

  /**
   * Put the JTTP plugin groups.
   */
  public TimeTrackerGlobalSettings pluginGroups(final List<String> pluginGroups) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.PLUGIN_PERMISSION, pluginGroups);
    return this;
  }

  /**
   * Put the plugin UUID.
   */
  public TimeTrackerGlobalSettings pluginUUID(final String pluginUUID) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.PLUGIN_UUID, pluginUUID);
    return this;
  }

  /**
   * Put a settings value with the specified key.
   */
  public void putGlobalSettingValue(final GlobalSettingsKey key, final Object value) {
    pluginSettingsKeyValues.put(key, value);
  }

  /**
   * Put the timetracking groups.
   */
  public TimeTrackerGlobalSettings timetrackerGroups(final List<String> timetrackingGroups) {
    pluginSettingsKeyValues.put(GlobalSettingsKey.TIMETRACKER_PERMISSION, timetrackingGroups);
    return this;
  }
}
