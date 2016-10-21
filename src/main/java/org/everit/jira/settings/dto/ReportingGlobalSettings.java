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
import java.util.List;
import java.util.Map;

/**
 * Mapping and converter class for the reporting global settings.
 */
public class ReportingGlobalSettings {

  private Map<ReportingSettingKey, Object> pluginSettingsKeyValues = new HashMap<>();

  public ReportingGlobalSettings() {
  }

  public ReportingGlobalSettings browseGroups(final List<String> browseGroups) {
    pluginSettingsKeyValues.put(ReportingSettingKey.BROWSE_GROUPS, browseGroups);
    return this;
  }

  /**
   * Convert and get the browse groups. Default is empty list.
   */
  public List<String> getBrowseGroups() {
    List<String> browseGroupsNames = (List<String>) pluginSettingsKeyValues
        .get(ReportingSettingKey.BROWSE_GROUPS);
    List<String> browseGroups = new ArrayList<>();
    if (browseGroupsNames != null) {
      browseGroups = browseGroupsNames;
    }
    return browseGroups;
  }

  public Map<ReportingSettingKey, Object> getPluginSettingsKeyValues() {
    return pluginSettingsKeyValues;
  }

  /**
   * Convert and get the reporting groups. Default is empty list.
   */
  public List<String> getReportingGroups() {
    List<String> reportingGroupsNames = (List<String>) pluginSettingsKeyValues
        .get(ReportingSettingKey.GROUPS);
    List<String> reportingGroups = new ArrayList<>();
    if (reportingGroupsNames != null) {
      reportingGroups = reportingGroupsNames;
    }
    return reportingGroups;
  }

  public void putSettings(final ReportingSettingKey reportingSettingsKey, final Object object) {
    pluginSettingsKeyValues.put(reportingSettingsKey, object);

  }

  public ReportingGlobalSettings reportingGroups(final List<String> reportingGroups) {
    pluginSettingsKeyValues.put(ReportingSettingKey.GROUPS, reportingGroups);
    return this;
  }
}
