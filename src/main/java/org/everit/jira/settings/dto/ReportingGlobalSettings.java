package org.everit.jira.settings.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportingGlobalSettings {

  private Map<ReportingSettingKey, Object> pluginSettingsKeyValues = new HashMap<>();

  public ReportingGlobalSettings() {
  }

  public ReportingGlobalSettings browseGroups(final List<String> browseGroups) {
    pluginSettingsKeyValues.put(ReportingSettingKey.BROWSE_GROUPS, browseGroups);
    return this;
  }

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

  public List<String> getReportingGroups() {
    List<String> reportingGroupsNames = (List<String>) pluginSettingsKeyValues
        .get(ReportingSettingKey.GROUPS);
    List<String> reportingGroups = new ArrayList<>();
    if (reportingGroupsNames != null) {
      reportingGroups = reportingGroupsNames;
    }
    return reportingGroups;
  }

  public ReportingGlobalSettings reportingGroups(final List<String> reportingGroups) {
    pluginSettingsKeyValues.put(ReportingSettingKey.GROUPS, reportingGroups);
    return this;
  }
}
