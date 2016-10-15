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

import java.util.Map.Entry;

import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.AnalyticsStatusChangedEvent;
import org.everit.jira.analytics.event.ProgressIndicatorChangedEvent;
import org.everit.jira.settings.dto.GlobalSettingsKey;
import org.everit.jira.settings.dto.GlobalsSettingsKey;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.settings.dto.UserSettingKey;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class TimeTrackerSettingsHelperImpl implements TimetrackerSettingsHelper {

  private AnalyticsSender analyticsSender;

  private PluginSettingsFactory settingsFactory;

  public TimeTrackerSettingsHelperImpl(final PluginSettingsFactory settingsFactory,
      final AnalyticsSender analyticsSender) {
    this.settingsFactory = settingsFactory;
    this.analyticsSender = analyticsSender;
  }

  private void checkAnalyticsForprogressIndicator(final PluginSettings pluginSettings,
      final String isProgressIndicatorDaily) {
    Object indicatorCheckObj =
        pluginSettings.get(UserSettingKey.PROGRESS_INDICATOR.getSettingsKey());
    boolean indicatorCheck = indicatorCheckObj == null
        ? true
        : Boolean.parseBoolean(indicatorCheckObj.toString());

    if ((indicatorCheck != Boolean.parseBoolean(isProgressIndicatorDaily))
        || (indicatorCheckObj == null)) {
      analyticsSender.send(new ProgressIndicatorChangedEvent(
          loadGlobalSettings().getPluginUUID(),
          Boolean.parseBoolean(isProgressIndicatorDaily)));
    }

  }

  private void checkSendAnalyticsForAnalytics(final PluginSettings globalSettings,
      final boolean AnalyticChange) {
    Object analyticsCheckObj = globalSettings.get(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalsSettingsKey.ANALYTICS_CHECK_CHANGE.getSettingsKey());
    boolean analyticsCheck = analyticsCheckObj == null
        ? true
        : Boolean.parseBoolean(analyticsCheckObj.toString());

    if (analyticsCheck != AnalyticChange) {
      analyticsSender
          .send(new AnalyticsStatusChangedEvent(loadGlobalSettings().getPluginUUID(),
              AnalyticChange));
    }
  }

  private PluginSettings getUserPluginSettings() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    PluginSettings pluginSettings = settingsFactory
        .createSettingsForKey(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + user.getName());
    return pluginSettings;
  }

  @Override
  public TimeTrackerGlobalSettings loadGlobalSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TimeTrackerUserSettings loadUserSettings() {
    PluginSettings pluginSettings = getUserPluginSettings();
    TimeTrackerUserSettings timeTrackerUserSettings = new TimeTrackerUserSettings();
    for (UserSettingKey settingKey : UserSettingKey.values()) {
      // FIXME JIRA store everything in string but returns in object
      timeTrackerUserSettings.putUserSettingValue(settingKey,
          (String) pluginSettings.get(settingKey.getSettingsKey()));
    }
    return timeTrackerUserSettings;
  }

  @Override
  public void saveGlobalSettings(final TimeTrackerGlobalSettings pluginSettingsValues) {
    PluginSettings globalSettings = settingsFactory.createGlobalSettings();
    checkSendAnalyticsForAnalytics(globalSettings,
        (boolean) pluginSettingsValues.getPluginSettingsKeyValues()
            .get(GlobalsSettingsKey.ANALYTICS_CHECK_CHANGE));
    for (Entry<GlobalsSettingsKey, Object> globalSettingEntry : pluginSettingsValues
        .getPluginSettingsKeyValues().entrySet()) {
      globalSettings.put(GlobalSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
          + globalSettingEntry.getKey().getSettingsKey(),
          globalSettingEntry.getValue());
    }
  }

  @Override
  public void saveUserSettings(final TimeTrackerUserSettings userSettings) {
    PluginSettings pluginSettings = getUserPluginSettings();
    checkAnalyticsForprogressIndicator(pluginSettings,
        userSettings.getUserSettingValue(UserSettingKey.PROGRESS_INDICATOR));
    for (Entry<UserSettingKey, String> settingEntry : userSettings.getPluginSettingsKeyValues()
        .entrySet()) {
      pluginSettings.put(settingEntry.getKey().getSettingsKey(),
          settingEntry.getValue());
    }

  }

}
