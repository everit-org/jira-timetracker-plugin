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
public class UserReportingSettingsHelper {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private PluginSettings pluginSettings;

  /**
   * Simple constructore.
   *
   * @param pluginSettingsFactory
   *          Jira plugin Settings Factory.
   * @param userName
   *          The user name.
   */
  public UserReportingSettingsHelper(
      final PluginSettingsFactory pluginSettingsFactory,
      final String userName) {
    pluginSettings =
        pluginSettingsFactory.createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + userName);
  }

  // private void setPageSize() {
  // pageSize = DEFAULT_PAGE_SIZE;
  // String pageSizeValue =
  // (String) reportingSettings.get(JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
  // + JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE);
  // if (pageSizeValue != null) {
  // pageSize = Integer.parseInt(pageSizeValue);
  // }
  // }

  /**
   * Get the user saved show tutorial value.
   *
   * @return The saved value from settigns.
   */
  public boolean getIsShowTutorialDialog() {
    if ("false".equals(
        pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL))) {
      return false;
    }
    return true;
  }

  /**
   * Get the user saved page size setting.
   *
   * @return The saved value from settigns.
   */
  public int getPageSize() {
    int pageSize = DEFAULT_PAGE_SIZE;
    String pageSizeValue =
        (String) pluginSettings.get(GlobalSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE);
    if (pageSizeValue != null) {
      pageSize = Integer.parseInt(pageSizeValue);
    }
    return pageSize;
  }

  /**
   * Get the user saved show tutorial value.
   *
   * @param isShowTutorial
   *          The new value.
   */
  public void saveIsShowTutorialDialog(final boolean isShowTutorial) {
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL,
        Boolean.toString(isShowTutorial));
  }

  /**
   * Get the user saved show tutorial value.
   *
   * @param isShowTutorial
   *          The new value.
   */
  public void savePageSize(final int pageSize) {
    pluginSettings.put(GlobalSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE, pageSize);
  }

}
