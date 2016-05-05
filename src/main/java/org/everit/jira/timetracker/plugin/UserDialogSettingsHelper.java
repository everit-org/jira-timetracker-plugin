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
package org.everit.jira.timetracker.plugin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The User settings for dialog helper class.
 */
public final class UserDialogSettingsHelper {

  /**
   * Get the user saved show tutorial value.
   *
   * @param pluginSettingsFactory
   *          Plugin setting factory.
   * @param userName
   *          The userName parameter form user.
   * @return The saved value from settigns.
   */
  public static boolean getIsShowTutorialDialog(final PluginSettingsFactory pluginSettingsFactory,
      final String userName) {
    PluginSettings pluginSettings = pluginSettingsFactory
        .createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + userName);
    if ("false"
        .equals(
            pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL))) {
      return false;
    }
    return true;
  }

  /**
   * Get the user saved show tutorial value.
   *
   * @param pluginSettingsFactory
   *          Plugin setting factory.
   * @param userName
   *          The userName parameter form user.
   * @param isShowTutorial
   *          The new value.
   */
  public static void saveIsShowTutorialDialog(final PluginSettingsFactory pluginSettingsFactory,
      final String userName, final boolean isShowTutorial) {
    PluginSettings pluginSettings = pluginSettingsFactory
        .createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + userName);
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL,
        Boolean.toString(isShowTutorial));
  }

  private UserDialogSettingsHelper() {
  }

}
