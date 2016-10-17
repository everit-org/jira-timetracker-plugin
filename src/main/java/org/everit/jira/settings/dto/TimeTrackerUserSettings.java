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
import org.everit.jira.timetracker.plugin.JiraTimetrackerPluginImpl;
import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;
import org.everit.jira.timetracker.plugin.util.VersionComperatorUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.google.gson.Gson;

public class TimeTrackerUserSettings {

  private static final int DEFAULT_PAGE_SIZE = 20;

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TimeTrackerUserSettings.class);

  private Map<UserSettingKey, String> pluginSettingsKeyValues = new HashMap<>();

  public TimeTrackerUserSettings() {
  }

  public TimeTrackerUserSettings actualDate(final boolean actualDateOrLastWorklogDate) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_ACTUAL_DATE,
        String.valueOf(actualDateOrLastWorklogDate));
    return this;
  }

  public TimeTrackerUserSettings coloring(final boolean isColoring) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_COLORING,
        String.valueOf(isColoring));
    return this;
  }

  public TimeTrackerUserSettings endTimeChange(final int endTimeChange) {
    pluginSettingsKeyValues.put(UserSettingKey.END_TIME_CHANGE,
        String.valueOf(endTimeChange));
    return this;
  }

  public boolean getActualDate() {
    // the default is the Actual Date
    boolean isActualDate = true;
    if ("false"
        .equals(pluginSettingsKeyValues.get(UserSettingKey.IS_ACTUAL_DATE))) {
      isActualDate = false;
    }
    return isActualDate;
  }

  public boolean getColoring() {
    // the default coloring is TRUE
    boolean isColoring = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_COLORING))) {
      isColoring = false;
    }
    return isColoring;
  }

  public int getEndTimeChange() {
    int endTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;

    Object endTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.END_TIME_CHANGE);
    if (endTimeChangeObj != null) {
      try {
        endTimeChange = Integer.parseInt(endTimeChangeObj.toString());
        if (!JiraTimetrackerUtil.validateTimeChange(Integer.toString(endTimeChange))) {
          endTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated endTime change value. Set the default value (1).", e);
      }
    }
    return endTimeChange;
  }

  public boolean getisProgressIndicatordaily() {
    // the default is the Daily Progress Indicator
    boolean isProgressIndicatorDaily = true;
    if ("false"
        .equals(pluginSettingsKeyValues.get(UserSettingKey.PROGRESS_INDICATOR))) {
      isProgressIndicatorDaily = false;
    }
    return isProgressIndicatorDaily;
  }

  public boolean getisRounded() {
    // the default rounded is TRUE
    boolean isRounded = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_ROUNDED))) {
      isRounded = false;
    }
    return isRounded;
  }

  public boolean getisShowFutureLogWarning() {
    // the default show warning is TRUE
    boolean isShowFutureLogWarning = true;
    if ("false".equals(
        pluginSettingsKeyValues.get(UserSettingKey.SHOW_FUTURE_LOG_WARNING))) {
      isShowFutureLogWarning = false;
    }
    return isShowFutureLogWarning;
  }

  public boolean getisShowIssueSummary() {
    // the default show issue summary is FALSE
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
   * Get the user saved page size setting.
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

  public int getstartTimeChange() {
    int startTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;

    Object startTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.START_TIME_CHANGE);
    if (startTimeChangeObj != null) {
      try {
        startTimeChange = Integer.parseInt(startTimeChangeObj.toString());
        if (!JiraTimetrackerUtil.validateTimeChange(Integer.toString(startTimeChange))) {
          startTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated startTime change value. Set the default value (1).", e);
      }
    }
    return startTimeChange;
  }

  public int getStartTimeChange() {
    int startTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;

    Object startTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.START_TIME_CHANGE);
    if (startTimeChangeObj != null) {
      try {
        startTimeChange = Integer.parseInt(startTimeChangeObj.toString());
        if (!JiraTimetrackerUtil.validateTimeChange(Integer.toString(startTimeChange))) {
          startTimeChange = JiraTimetrackerPluginImpl.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated startTime change value. Set the default value (1).", e);
      }
    }
    return startTimeChange;
  }

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

  public TimeTrackerUserSettings isProgressIndicatordaily(final boolean isProgressIndicatorDaily) {
    pluginSettingsKeyValues.put(UserSettingKey.PROGRESS_INDICATOR,
        String.valueOf(isProgressIndicatorDaily));
    return this;
  }

  public TimeTrackerUserSettings isRounded(final boolean isRounded) {
    pluginSettingsKeyValues.put(UserSettingKey.IS_ROUNDED,
        String.valueOf(isRounded));
    return this;
  }

  public TimeTrackerUserSettings isShowFutureLogWarning(final boolean isShowFutureLogWarning) {
    pluginSettingsKeyValues.put(UserSettingKey.SHOW_FUTURE_LOG_WARNING,
        String.valueOf(isShowFutureLogWarning));
    return this;
  }

  public TimeTrackerUserSettings isShowIssueSummary(final boolean isShowIssueSummary) {
    pluginSettingsKeyValues.put(
        UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE,
        String.valueOf(isShowIssueSummary));
    return this;
  }

  /**
   * Save the SHOW_TUTORIAL_VERSION and the IS_SHOW_TUTORIAL values.
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

  public TimeTrackerUserSettings pageSize(final int pageSize) {
    pluginSettingsKeyValues.put(UserSettingKey.REPORTING_SETTINGS_PAGER_SIZE,
        String.valueOf(pageSize));
    return this;
  }

  public void putUserSettingValue(final UserSettingKey key, final String value) {
    pluginSettingsKeyValues.put(key, value);
  }

  public TimeTrackerUserSettings selectedColumnsJSon(final String selectedColumnsJson) {
    pluginSettingsKeyValues.put(UserSettingKey.USER_WD_SELECTED_COLUMNS, selectedColumnsJson);
    return this;
  }

  public TimeTrackerUserSettings startTimeChange(final int startTimeChange) {
    pluginSettingsKeyValues.put(UserSettingKey.START_TIME_CHANGE,
        String.valueOf(startTimeChange));
    return this;
  }

  public TimeTrackerUserSettings userCanceledUpdate(final String latestVersionWithoutUpdate) {
    pluginSettingsKeyValues.put(UserSettingKey.USER_CANCELED_UPDATE,
        latestVersionWithoutUpdate);
    return this;
  }

  public TimeTrackerUserSettings worklogTimeInSeconds(final boolean worklogTimeInSeconds) {
    pluginSettingsKeyValues.put(UserSettingKey.REPORTING_SETTINGS_WORKLOG_IN_SEC,
        Boolean.toString(worklogTimeInSeconds));
    return this;
  }
}
