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

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;

import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.ActiveFieldDurationChangedEvent;
import org.everit.jira.analytics.event.AnalyticsStatusChangedEvent;
import org.everit.jira.analytics.event.ProgressIndicatorChangedEvent;
import org.everit.jira.analytics.event.ShowFutureLogWarningChangedEvent;
import org.everit.jira.analytics.event.ShowIssueSummaryChangedEvent;
import org.everit.jira.settings.dto.GlobalSettingsKey;
import org.everit.jira.settings.dto.JTTPSettingsKey;
import org.everit.jira.settings.dto.ReportingGlobalSettings;
import org.everit.jira.settings.dto.ReportingSettingKey;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.settings.dto.UserSettingKey;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The implementation of {@link TimetrackerSettingsHelper} interface.
 */
public class TimeTrackerSettingsHelperImpl
    implements TimetrackerSettingsHelper, InitializingBean, Serializable {

  private static final long serialVersionUID = 8873665767837959963L;

  private transient AnalyticsSender analyticsSender;

  private transient PluginSettingsFactory settingsFactory;

  /**
   * Crate the settings helper. Set the plugin UUID in global settings.
   */
  public TimeTrackerSettingsHelperImpl(final PluginSettingsFactory settingsFactory,
      final AnalyticsSender analyticsSender) {
    this.settingsFactory = settingsFactory;
    this.analyticsSender = analyticsSender;
    generateAndSavePluginUUID();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    PluginSettings globalSettings = settingsFactory.createGlobalSettings();
    String tempExcludeDates =
        (String) globalSettings.get(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.EXCLUDE_DATES.getSettingsKey());
    if ((tempExcludeDates != null) && !tempExcludeDates.isEmpty()) {
      if (tempExcludeDates.matches("^[0-9,]+$")) { // CONVERSATION ALREADY DONE NOThing to do
        return;
      }
      StringBuilder sb = new StringBuilder();
      for (String excludeDate : tempExcludeDates.split(",")) {
        try {
          Date excludeDateAsDate =
              DateTimeConverterUtil.fixFormatStringToDateWithValidation(excludeDate.trim());
          sb.append(excludeDateAsDate.getTime()).append(",");
        } catch (ParseException e) {
          continue;
        }
      }
      globalSettings.put(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
          + GlobalSettingsKey.EXCLUDE_DATES.getSettingsKey(), sb.toString());
    }
    String tempIncludeDates =
        (String) globalSettings.get(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + GlobalSettingsKey.INCLUDE_DATES.getSettingsKey());
    if ((tempIncludeDates != null) && !tempIncludeDates.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (String includeDate : tempIncludeDates.split(",")) {
        try {
          Date includeDateAsDate =
              DateTimeConverterUtil.fixFormatStringToDateWithValidation(includeDate.trim());
          sb.append(includeDateAsDate.getTime()).append(",");
        } catch (ParseException e) {
          continue;
        }
      }
      globalSettings.put(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
          + GlobalSettingsKey.INCLUDE_DATES.getSettingsKey(), sb.toString());
    }
  }

  private void checkAnalyticsForActiveFieldDuration(final PluginSettings pluginSettings,
      final boolean activeFieldDuration) {
    Object activeFieldDurationObj =
        pluginSettings.get(UserSettingKey.ACTIVE_FIELD_DURATION.getSettingsKey());
    boolean oldActiveFieldDuration = activeFieldDurationObj == null
        ? true
        : Boolean.parseBoolean(activeFieldDurationObj.toString());

    if ((oldActiveFieldDuration != activeFieldDuration)
        || (activeFieldDurationObj == null)) {
      analyticsSender.send(new ActiveFieldDurationChangedEvent(
          loadGlobalSettings().getPluginUUID(),
          activeFieldDuration));
    }
  }

  private void checkAnalyticsForProgressIndicator(final PluginSettings pluginSettings,
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

  private void checkAnalyticsForShowFutureLogWarning(final PluginSettings pluginSettings,
      final boolean showFutureLogWarning) {
    Object showFutureLogWarningObj =
        pluginSettings.get(UserSettingKey.SHOW_FUTURE_LOG_WARNING.getSettingsKey());
    boolean oldShowFutureLogWarning = showFutureLogWarningObj == null
        ? true
        : Boolean.parseBoolean(showFutureLogWarningObj.toString());

    if ((oldShowFutureLogWarning != showFutureLogWarning)
        || (showFutureLogWarningObj == null)) {
      analyticsSender.send(new ShowFutureLogWarningChangedEvent(
          loadGlobalSettings().getPluginUUID(),
          showFutureLogWarning));
    }
  }

  private void checkAnalyticsForShowIssueSummary(final PluginSettings pluginSettings,
      final boolean showIssueSummary) {
    Object showIssueSummaryObj =
        pluginSettings.get(UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE.getSettingsKey());
    boolean oldShowIssueSummary = showIssueSummaryObj == null
        ? true
        : Boolean.parseBoolean(showIssueSummaryObj.toString());

    if ((oldShowIssueSummary != showIssueSummary)
        || (showIssueSummaryObj == null)) {
      analyticsSender.send(new ShowIssueSummaryChangedEvent(
          loadGlobalSettings().getPluginUUID(),
          showIssueSummary));
    }
  }

  private void checkSendAnalyticsForAnalytics(final PluginSettings globalSettings,
      final boolean newValue) {
    Object analyticsCheckObj = globalSettings.get(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
        + GlobalSettingsKey.ANALYTICS_CHECK_CHANGE.getSettingsKey());
    boolean analyticsCheck = analyticsCheckObj == null
        ? true
        : Boolean.parseBoolean(analyticsCheckObj.toString());

    if (analyticsCheck != newValue) {
      analyticsSender
          .send(new AnalyticsStatusChangedEvent(loadGlobalSettings().getPluginUUID(),
              newValue));
    }
  }

  private void generateAndSavePluginUUID() {
    String temppluginUUID = loadGlobalSettings().getPluginUUID();
    if ((temppluginUUID == null) || temppluginUUID.isEmpty()) {
      temppluginUUID = UUID.randomUUID().toString();
      TimeTrackerGlobalSettings globalSettings =
          new TimeTrackerGlobalSettings().pluginUUID(temppluginUUID);
      saveGlobalSettings(globalSettings);
    }
  }

  private PluginSettings getUserPluginSettings() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    PluginSettings pluginSettings = settingsFactory
        .createSettingsForKey(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
            + user.getName());
    return pluginSettings;
  }

  @Override
  public TimeTrackerGlobalSettings loadGlobalSettings() {
    PluginSettings globalSettings = settingsFactory.createGlobalSettings();
    TimeTrackerGlobalSettings timeTrackerGlobalSettings = new TimeTrackerGlobalSettings();
    for (GlobalSettingsKey settingKey : GlobalSettingsKey.values()) {
      timeTrackerGlobalSettings.putGlobalSettingValue(settingKey,
          globalSettings.get(
              JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX + settingKey.getSettingsKey()));
    }
    return timeTrackerGlobalSettings;
  }

  @Override
  public ReportingGlobalSettings loadReportingGlobalSettings() {
    PluginSettings reportingSettings = settingsFactory
        .createSettingsForKey(JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX);
    ReportingGlobalSettings reportingGlobalSettings = new ReportingGlobalSettings();
    for (ReportingSettingKey reportingSettingsKey : ReportingSettingKey.values()) {
      reportingGlobalSettings.putSettings(reportingSettingsKey,
          reportingSettings.get(JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
              + reportingSettingsKey.getSettingsKey()));
    }
    return reportingGlobalSettings;
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

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  @Override
  public void saveGlobalSettings(final TimeTrackerGlobalSettings pluginSettingsValues) {
    PluginSettings globalSettings = settingsFactory.createGlobalSettings();
    String newValue = (String) pluginSettingsValues.getPluginSettingsKeyValues()
        .get(GlobalSettingsKey.ANALYTICS_CHECK_CHANGE);
    if (newValue != null) {
      checkSendAnalyticsForAnalytics(globalSettings, Boolean.valueOf(newValue));
    }
    for (Entry<GlobalSettingsKey, Object> globalSettingEntry : pluginSettingsValues
        .getPluginSettingsKeyValues().entrySet()) {
      globalSettings.put(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX
          + globalSettingEntry.getKey().getSettingsKey(),
          globalSettingEntry.getValue());
    }
  }

  @Override
  public void saveReportingGlobalSettings(final ReportingGlobalSettings settings) {
    PluginSettings reportingSettings = settingsFactory
        .createSettingsForKey(JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX);
    for (Entry<ReportingSettingKey, Object> settingEntry : settings.getPluginSettingsKeyValues()
        .entrySet()) {
      reportingSettings.put(JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_KEY_PREFIX
          + settingEntry.getKey().getSettingsKey(),
          settingEntry.getValue());
    }

  }

  @Override
  public void saveUserSettings(final TimeTrackerUserSettings userSettings) {
    PluginSettings pluginSettings = getUserPluginSettings();
    checkAnalyticsForProgressIndicator(pluginSettings,
        userSettings.getUserSettingValue(UserSettingKey.PROGRESS_INDICATOR));
    checkAnalyticsForShowFutureLogWarning(pluginSettings, userSettings.isShowFutureLogWarning());
    checkAnalyticsForShowIssueSummary(pluginSettings, userSettings.getIsShowIssueSummary());
    checkAnalyticsForActiveFieldDuration(pluginSettings, userSettings.isActiveFieldDuration());
    for (Entry<UserSettingKey, String> settingEntry : userSettings.getPluginSettingsKeyValues()
        .entrySet()) {
      pluginSettings.put(settingEntry.getKey().getSettingsKey(),
          settingEntry.getValue());
    }

  }

}
