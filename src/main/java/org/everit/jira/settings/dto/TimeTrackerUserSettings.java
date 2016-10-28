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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.VersionComperatorUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.gson.Gson;

/**
 * The Time tracker user settings mapper class.
 */
public class TimeTrackerUserSettings {

  private static final int DEFAULT_PAGE_SIZE = 20;

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TimeTrackerUserSettings.class);

  private Map<UserSettingKey, String> pluginSettingsKeyValues = new HashMap<>();

  /**
   * Put the actual date or last non work logged day configuration.
   *
   * @param actualDateOrLastWorklogDate
   *          true if show the actual date, false if show the last non work logged date.
   */
  public TimeTrackerUserSettings actualDate(final boolean actualDateOrLastWorklogDate) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_ACTUAL_DATE,
        String.valueOf(actualDateOrLastWorklogDate));
    return this;
  }

  /**
   * Put the coloring the calendar or not.
   */
  public TimeTrackerUserSettings coloring(final boolean isColoring) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_COLORING,
        String.valueOf(isColoring));
    return this;
  }

  /**
   * Put the end time change.
   */
  public TimeTrackerUserSettings endTimeChange(final int endTimeChange) {
    pluginSettingsKeyValues.put(UserSettingKey.END_TIME_CHANGE,
        String.valueOf(endTimeChange));
    return this;
  }

  /**
   * Get the actual date setting value. The default is true.
   */
  public boolean getActualDate() {
    boolean isActualDate = true;
    if ("false"
        .equals(pluginSettingsKeyValues.get(UserSettingKey.IS_ACTUAL_DATE))) {
      isActualDate = false;
    }
    return isActualDate;
  }

  /**
   * Get the coloring setting value. The default is true.
   */
  public boolean getColoring() {
    boolean isColoring = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_COLORING))) {
      isColoring = false;
    }
    return isColoring;
  }

  /**
   * Get the end time change settings value. The default is 5.
   */
  public int getEndTimeChange() {
    int endTimeChange = JiraTimetrackerUtil.FIVE_MINUTES;

    Object endTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.END_TIME_CHANGE);
    if (endTimeChangeObj != null) {
      try {
        endTimeChange = Integer.parseInt(endTimeChangeObj.toString());
        if (!JiraTimetrackerUtil.validateTimeChange(Integer.toString(endTimeChange))) {
          endTimeChange = JiraTimetrackerUtil.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated endTime change value. Set the default value (1).", e);
      }
    }
    return endTimeChange;
  }

  /**
   * Get the progress indicator daily or not. The default is true.
   */
  public boolean getisProgressIndicatordaily() {
    boolean isProgressIndicatorDaily = true;
    if ("false"
        .equals(pluginSettingsKeyValues.get(UserSettingKey.PROGRESS_INDICATOR))) {
      isProgressIndicatorDaily = false;
    }
    return isProgressIndicatorDaily;
  }

  /**
   * Get the rounding settings value. The default is true.
   */
  public boolean getIsRounded() {
    boolean isRounded = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_ROUNDED))) {
      isRounded = false;
    }
    return isRounded;
  }

  /**
   * Get the show future worklog warning settings value. Default is true.
   */
  public boolean getIsShowFutureLogWarning() {
    boolean isShowFutureLogWarning = true;
    if ("false".equals(
        pluginSettingsKeyValues.get(UserSettingKey.SHOW_FUTURE_LOG_WARNING))) {
      isShowFutureLogWarning = false;
    }
    return isShowFutureLogWarning;
  }

  /**
   * Get the settings value of show issue summary column visible or not. Default is false.
   */
  public boolean getIsShowIssueSummary() {
    boolean isShowIssueSummary = false;
    if ("true".equals(
        pluginSettingsKeyValues.get(
            UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE))) {
      isShowIssueSummary = true;
    }
    return isShowIssueSummary;
  }

  /**
   * Get the user saved show tutorial value.
   *
   * @return The saved value from settings.
   */
  public boolean getIsShowTutorialDialog() {
    String showTurorilaVersion =
        pluginSettingsKeyValues.get(UserSettingKey.SHOW_TUTORIAL_VERSION);
    String pluginVersion =
        ComponentAccessor.getPluginAccessor().getPlugin("org.everit.jira.timetracker.plugin")
            .getPluginInformation().getVersion();
    if (VersionComperatorUtil.versionCompare(showTurorilaVersion, pluginVersion) < 0) {
      return true;
    }
    if ("false".equals(
        pluginSettingsKeyValues.get(UserSettingKey.IS_SHOW_TUTORIAL))) {
      return false;
    }
    return true;
  }

  /**
   * Get the user saved page size setting. The default is 20.
   *
   * @return The saved value from settigns.
   */
  public int getPageSize() {
    int pageSize = DEFAULT_PAGE_SIZE;
    String pageSizeValue =
        pluginSettingsKeyValues.get(UserSettingKey.REPORTING_SETTINGS_PAGER_SIZE);
    if (pageSizeValue != null) {
      pageSize = Integer.parseInt(pageSizeValue);
    }
    return pageSize;
  }

  public Map<UserSettingKey, String> getPluginSettingsKeyValues() {
    return pluginSettingsKeyValues;
  }

  /**
   * Get the start time change settings value. The default is 5.
   */
  public int getStartTimeChange() {
    int startTimeChange = JiraTimetrackerUtil.FIVE_MINUTES;

    Object startTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.START_TIME_CHANGE);
    if (startTimeChangeObj != null) {
      try {
        startTimeChange = Integer.parseInt(startTimeChangeObj.toString());
        if (!JiraTimetrackerUtil.validateTimeChange(Integer.toString(startTimeChange))) {
          startTimeChange = JiraTimetrackerUtil.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated startTime change value. Set the default value (1).", e);
      }
    }
    return startTimeChange;
  }

  /**
   * Get the settings value of the user canceled last update version.
   */
  public String getUserCanceledUpdate() {
    return pluginSettingsKeyValues.get(UserSettingKey.USER_CANCELED_UPDATE);
  }

  /**
   * Get the user saved worklog detials selected columns value.
   *
   * @return The saved value from settigns.
   */
  public String getUserSelectedColumns() {
    String selectedColumnsJson =
        pluginSettingsKeyValues.get(UserSettingKey.USER_WD_SELECTED_COLUMNS);
    if (selectedColumnsJson != null) {
      return selectedColumnsJson;
    }
    Gson gson = new Gson();
    return gson.toJson(WorklogDetailsColumns.DEFAULT_COLUMNS);
  }

  public String getUserSettingValue(final UserSettingKey key) {
    return pluginSettingsKeyValues.get(key);
  }

  /**
   * Get the user saved worklog time format value.
   *
   * @return The saved value from settings.
   */
  public boolean getWorklogTimeInSeconds() {
    boolean worklogValue = true;
    if ("false".equals(
        pluginSettingsKeyValues.get(UserSettingKey.REPORTING_SETTINGS_WORKLOG_IN_SEC))) {
      worklogValue = false;
    }
    return worklogValue;
  }

  /**
   * Put progress indicator setting.
   */
  public TimeTrackerUserSettings isProgressIndicatordaily(final boolean isProgressIndicatorDaily) {
    pluginSettingsKeyValues.put(UserSettingKey.PROGRESS_INDICATOR,
        String.valueOf(isProgressIndicatorDaily));
    return this;
  }

  /**
   * Put rounded setting.
   */
  public TimeTrackerUserSettings isRounded(final boolean isRounded) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_ROUNDED,
        String.valueOf(isRounded));
    return this;
  }

  /**
   * Put show future worklog warning setting.
   */
  public TimeTrackerUserSettings isShowFutureLogWarning(final boolean isShowFutureLogWarning) {
    pluginSettingsKeyValues.put(UserSettingKey.SHOW_FUTURE_LOG_WARNING,
        String.valueOf(isShowFutureLogWarning));
    return this;
  }

  /**
   * Put the show issue summary setting.
   */
  public TimeTrackerUserSettings isShowIssueSummary(final boolean isShowIssueSummary) {
    pluginSettingsKeyValues.put(
        UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE,
        String.valueOf(isShowIssueSummary));
    return this;
  }

  /**
   * Put the is show tutorial setting value and store the tutorial version.
   */
  public TimeTrackerUserSettings isShowTutorialDialog(final boolean isShowTutorial) {
    String pluginVersion =
        ComponentAccessor.getPluginAccessor().getPlugin("org.everit.jira.timetracker.plugin")
            .getPluginInformation().getVersion();
    pluginSettingsKeyValues.put(UserSettingKey.SHOW_TUTORIAL_VERSION, pluginVersion);
    pluginSettingsKeyValues.put(UserSettingKey.IS_SHOW_TUTORIAL,
        Boolean.toString(isShowTutorial));
    return this;
  }

  /**
   * Put the page size setting value.
   */
  public TimeTrackerUserSettings pageSize(final int pageSize) {
    pluginSettingsKeyValues.put(UserSettingKey.REPORTING_SETTINGS_PAGER_SIZE,
        String.valueOf(pageSize));
    return this;
  }

  public void putUserSettingValue(final UserSettingKey key, final String value) {
    pluginSettingsKeyValues.put(key, value);
  }

  /**
   * Put the selected columns in JSON format.
   */
  public TimeTrackerUserSettings selectedColumnsJSon(final String selectedColumnsJson) {
    pluginSettingsKeyValues.put(UserSettingKey.USER_WD_SELECTED_COLUMNS, selectedColumnsJson);
    return this;
  }

  /**
   * Put the start time change value.
   */
  public TimeTrackerUserSettings startTimeChange(final int startTimeChange) {
    pluginSettingsKeyValues.put(UserSettingKey.START_TIME_CHANGE,
        String.valueOf(startTimeChange));
    return this;
  }

  /**
   * Put the user canceled update JTTP version.
   */
  public TimeTrackerUserSettings userCanceledUpdate(final String latestVersionWithoutUpdate) {
    pluginSettingsKeyValues.put(UserSettingKey.USER_CANCELED_UPDATE,
        latestVersionWithoutUpdate);
    return this;
  }

  /**
   * Put the worklog time in seconds value.
   */
  public TimeTrackerUserSettings worklogTimeInSeconds(final boolean worklogTimeInSeconds) {
    pluginSettingsKeyValues.put(UserSettingKey.REPORTING_SETTINGS_WORKLOG_IN_SEC,
        Boolean.toString(worklogTimeInSeconds));
    return this;
  }
}
