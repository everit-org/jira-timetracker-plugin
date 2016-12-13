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
package org.everit.jira.settings;

import org.everit.jira.settings.dto.ReportingGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;

/**
 * The time tracker plugin configuration helper class. Provides save and load methods for the
 * configurations.
 */
public interface TimeTrackerSettingsHelper {
  /**
   * Load all timetracker global configuration from the
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @return the timetracker settings.
   */
  TimeTrackerGlobalSettings loadGlobalSettings();

  /**
   * Load all reporting global configuration from the
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @return the timetracker settings.
   */
  ReportingGlobalSettings loadReportingGlobalSettings();

  /**
   * Load all user configuration from the
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @return the timetracker settings.
   */
  TimeTrackerUserSettings loadUserSettings();

  /**
   * Save all specified settings from the {@link TimeTrackerGlobalSettings} into
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @param settings
   *          the timetracker global settings.
   */
  void saveGlobalSettings(TimeTrackerGlobalSettings settings);

  /**
   * Save all specified settings from the {@link ReportingGlobalSettings} into
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @param settings
   *          the reporting settings.
   */
  void saveReportingGlobalSettings(ReportingGlobalSettings settings);

  /**
   * Save all specified settings from the {@link TimeTrackerUserSettings} into
   * {@link com.atlassian.sal.api.pluginsettings.PluginSettings}.
   *
   * @param settings
   *          the timetracker user settings.
   */
  void saveUserSettings(TimeTrackerUserSettings userSettings);
}
