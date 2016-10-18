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

import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.updatenotifier.exception.UpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for store update information in the plugin.
 */
public class UpdateNotifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotifier.class);

  private TimetrackerSettingsHelper settingsHelper;

  /**
   * Constructor for creating the settings.
   */
  public UpdateNotifier(final TimetrackerSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  /**
   * Get the latest JTTP version from global settings.
   */
  public Long getLastUpdateTime() {
    return settingsHelper.loadGlobalSettings().getLastUpdate();
  }

  /**
   * Get the latest JTTP version, if necessary query it from the marketplace.
   *
   * @return the latest JTTP version.
   */
  public String getLatestVersion() {
    try {
      new TimetrackerVersionUpdater(this).updateLatestVersion();
    } catch (UpdateException e) {
      LOGGER.error("Version update failed", e);
    }
    return getLatestVersionWithoutUpdate();
  }

  private String getLatestVersionWithoutUpdate() {
    return settingsHelper.loadGlobalSettings().getLatestVersion();
  }

  /**
   * Decide the update notifier is visible or not for the user.
   */
  boolean isShownUpdateForUser(final String currentPluginVersion) {
    String version = settingsHelper.loadUserSettings().getUserCanceledUpdate();
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

  /**
   * Set the settings to disable the notifier for the latest version.
   */
  public void putDisableNotifierForVersion() {
    TimeTrackerUserSettings userSettings =
        new TimeTrackerUserSettings().userCanceledUpdate(getLatestVersionWithoutUpdate());
    settingsHelper.saveUserSettings(userSettings);
  }

  public void putLastUpdateTime(final long time) {
    TimeTrackerGlobalSettings globalSettings = new TimeTrackerGlobalSettings().lastUpdateTime(time);
    settingsHelper.saveGlobalSettings(globalSettings);
  }

  /**
   * Put the latest JTTP version to the settings.
   */
  public void putLatestVersion(final String latestVersion) {
    TimeTrackerGlobalSettings globalSettings =
        new TimeTrackerGlobalSettings().latestVersion(latestVersion);
    settingsHelper.saveGlobalSettings(globalSettings);
  }
}
