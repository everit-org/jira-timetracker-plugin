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

/**
 * The plugin global setting keys.
 */
public final class GlobalSettingsKey {

  /**
   * The plugin reporting settings is use Noworks.
   */
  static final String JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE = "pagerSize";

  /**
   * The plugin reporting settings worklog time in seconds value.
   */
  static final String JTTP_PLUGIN_REPORTING_SETTINGS_WORKLOG_IN_SEC = "worklogTimeInSeconds";

  /**
   * The plugin settings analytics check.
   */
  static final String JTTP_PLUGIN_SETTINGS_ANALYTICS_CHECK_CHANGE = "analyticsCheckChange";

  /**
   * The plugin setting is calendar popup key.
   */
  static final String JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE = "endTimechange";

  /**
   * The plugin setting Exclude dates key.
   */
  static final String JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES = "ExcludeDates";

  /**
   * The plugin setting Include dates key.
   */
  static final String JTTP_PLUGIN_SETTINGS_INCLUDE_DATES = "IncludeDates";

  /**
   * The plugin setting is actual date key.
   */
  static final String JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE = "isActualDate";

  /**
   * The plugin setting is coloring key.
   */
  static final String JTTP_PLUGIN_SETTINGS_IS_COLORIG = "isColoring";

  /**
   * The plugin setting is rounded remaining time key.
   */
  static final String JTTP_PLUGIN_SETTINGS_IS_ROUNDED = "isRounded";

  /**
   * The plugin setting is show tutorial key.
   */
  static final String JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL = "isShowTutorial";

  /**
   * The plugin settings key prefix.
   */
  public static final String JTTP_PLUGIN_SETTINGS_KEY_PREFIX = "jttp";

  /**
   * The plugin setting Summary Filters key.
   */
  static final String JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES = "NonEstimated";

  /**
   * The plugin setting Plugin Permission key.
   */
  static final String JTTP_PLUGIN_SETTINGS_PLUGIN_PERMISSION = "pluginPermission";

  /**
   * The plugin setting is progress indicator date key.
   */
  static final String JTTP_PLUGIN_SETTINGS_PROGRESS_INDICATOR = "progressIndicator";

  /**
   * The plugin setting is show warning message in future log.
   */
  static final String JTTP_PLUGIN_SETTINGS_SHOW_FUTURE_LOG_WARNING = "showFutureLogWarning";

  /**
   * User specific key for show the issue summary or the issue key in worklog table.
   */
  static final String JTTP_PLUGIN_SETTINGS_SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE =
      "showIssueSummaryInWoroklogTable";

  /**
   * The plugin setting is show tutoriak version key.
   */
  static final String JTTP_PLUGIN_SETTINGS_SHOW_TUTORIAL_VERSION = "showTutorialVersion";

  /**
   * The plugin setting is calendar popup key.
   */
  static final String JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE = "startTimeChange";

  /**
   * The plugin setting Summary Filters key.
   */
  static final String JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS = "SummaryFilters";

  /**
   * The plugin setting Timetracker Permission key.
   */
  static final String JTTP_PLUGIN_SETTINGS_TIMETRACKER_PERMISSION = "timetrackerPermission";

  /**
   * The plugin setting is show tutorila key.
   */
  static final String JTTP_PLUGIN_USER_WD_SELECTED_COLUMNS = "worklogDetialsSelectedColumns";

  /**
   * The plugin UUDI global setting key.
   */
  static final String JTTP_PLUGIN_UUID = "PluginUUID";

  /**
   * The update notifier last update time global setting key.
   */
  static final String JTTP_UPDATE_NOTIFIER_LAST_UPDATE = "UPDATE_NOTIFIER";
  /**
   * The update notifier latest version of the JTTP global setting key.
   */
  static final String JTTP_UPDATE_NOTIFIER_LATEST_VERSION = "JTTP_LATEST_VERSION";
  /**
   * User specific key for the version which the user canceled.
   */
  static final String JTTP_USER_CANCELED_UPDATE = "USER_CANCELED_UPDATE";

  private GlobalSettingsKey() {
  }
}
