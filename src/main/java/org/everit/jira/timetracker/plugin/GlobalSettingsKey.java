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

/**
 * The plugin global setting keys.
 */
public final class GlobalSettingsKey {

  /**
   * The plugin reporting settings is use Noworks.
   */
  public static final String JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE = "pagerSize";

  /**
   * The plugin settings analytics check.
   */
  public static final String JTTP_PLUGIN_SETTINGS_ANALYTICS_CHECK_CHANGE = "analyticsCheckChange";

  /**
   * The plugin setting is calendar popup key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE = "endTimechange";

  /**
   * The plugin setting Exclude dates key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES = "ExcludeDates";

  /**
   * The plugin setting Include dates key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_INCLUDE_DATES = "IncludeDates";

  /**
   * The plugin setting is actual date key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE = "isActualDate";

  /**
   * The plugin setting is calendar popup key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP = "isCalendarPopup";

  /**
   * The plugin setting is actual date key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_IS_COLORIG = "isColoring";

  /**
   * The plugin setting is show tutorila key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL = "isShowTutorial";

  /**
   * The plugin settings key prefix.
   */
  public static final String JTTP_PLUGIN_SETTINGS_KEY_PREFIX = "jttp";

  /**
   * The plugin setting Summary Filters key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES = "NonEstimated";

  /**
   * The plugin setting is calendar popup key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE = "startTimeChange";

  /**
   * The plugin setting Summary Filters key.
   */
  public static final String JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS = "SummaryFilters";

  /**
   * The plugin setting is show tutorila key.
   */
  public static final String JTTP_PLUGIN_USER_WD_SELECTED_COLUMNS = "worklogDetialsSelectedColumns";

  /**
   * The plugin UUDI global setting key.
   */
  public static final String JTTP_PLUGIN_UUID = "PluginUUID";

  private GlobalSettingsKey() {
  }
}
