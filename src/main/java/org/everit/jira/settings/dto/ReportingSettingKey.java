package org.everit.jira.settings.dto;

public enum ReportingSettingKey {
  BROWSE_GROUPS {

    @Override
    public String getSettingsKey() {
      return GlobalSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_BROWSE_GROUPS;
    }

  },
  GROUPS {

    @Override
    public String getSettingsKey() {
      return GlobalSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_GROUPS;
    }

  };
  public abstract String getSettingsKey();
}
