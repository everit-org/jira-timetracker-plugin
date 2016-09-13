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
package org.everit.jira.updatenotifier;

import org.everit.jira.timetracker.plugin.GlobalSettingsKey;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 *
 * Helper class for store update information in the plugin.
 *
 */
public class UpdateNotifier {

  private PluginSettings globalSettings;

  private PluginSettings pluginSettings;

  /**
   * Constructor for creating the settings.
   */
  public UpdateNotifier(final PluginSettingsFactory pluginSettingsFactory,
      final String userName) {
    globalSettings = pluginSettingsFactory.createGlobalSettings();
    pluginSettings =
        pluginSettingsFactory.createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + userName);
  }

  /**
   * Get the latest JTTP version from global settings.
   */
  public Long getLastUpdateTime() {
    String rval = (String) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_UPDATE_NOTIFIER_LAST_UPDATE);
    return rval == null ? null : Long.parseLong(rval);
  }

  public String getLatestVersion() {
    new UpdateNotificationUpdater(this).updateLatestVersion();
    return getLatestVersionWithoutUpdate();
  }

  private String getLatestVersionWithoutUpdate() {
    return (String) globalSettings.get(
        GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.JTTP_UPDATE_NOTIFIER_LATEST_VERSION);
  }

  /**
   * Decide the update notifier is visible or not for the user.
   *
   */
  boolean isShownUpdateForUser(final String currentPluginVersion) {
    String version = (String) pluginSettings
        .get(GlobalSettingsKey.JTTP_USER_CANCELED_UPDATE);
    return !currentPluginVersion.equals(version);
  }

  /**
   * Decide the update bar is visible for the current user.
   *
   * @return true if there is newer version or the user already refused the update for the latest
   *         version.
   */
  public boolean isShowUpdater() {
    String pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    String latestVersion = getLatestVersion();
    if ((latestVersion == null) || pluginVersion.equals(latestVersion)) {
      return false;
    } else {
      return isShownUpdateForUser(latestVersion);
    }
  }

  public void putLastUpdateTime(final long time) {
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_UPDATE_NOTIFIER_LAST_UPDATE, String.valueOf(time));
  }

  public void putLatestVersion(final String latestVersion) {
    globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.JTTP_UPDATE_NOTIFIER_LATEST_VERSION, latestVersion);
  }

  public void saveDiableNotifierForVersion() {
    pluginSettings.put(GlobalSettingsKey.JTTP_USER_CANCELED_UPDATE,
        getLatestVersionWithoutUpdate());
  }
}
