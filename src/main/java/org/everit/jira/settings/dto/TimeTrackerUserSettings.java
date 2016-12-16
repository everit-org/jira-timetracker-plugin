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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.everit.jira.core.util.TimetrackerUtil;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.everit.jira.timetracker.plugin.util.VersionComperatorUtil;
import org.joda.time.DateTime;

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
   * Put the active field value that is duration or not.
   */
  public TimeTrackerUserSettings activeFieldDuration(final boolean activeFieldDuration) {
    pluginSettingsKeyValues.put(UserSettingKey.ACTIVE_FIELD_DURATION,
        String.valueOf(activeFieldDuration));
    return this;
  }

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
   * Put the default start time.
   */
  public TimeTrackerUserSettings defaultStartTime(final String defaultStartTime) {
    Date dateTime = DateTimeConverterUtil.stringTimeToDateTime(defaultStartTime);
    String defaultStarTimeForSave = DateTimeConverterUtil.dateTimeToStringWithFixFormat(dateTime);
    pluginSettingsKeyValues.put(UserSettingKey.DEFAULT_START_TIME, defaultStarTimeForSave);
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
   * Gets the default start time.
   */
  public String getDefaultStartTime() {
    String savedDefaultStartTime = pluginSettingsKeyValues.get(UserSettingKey.DEFAULT_START_TIME);
    if (savedDefaultStartTime == null) {
      DateTime dateTime = new DateTime(TimetrackerUtil.getLoggedUserTimeZone());
      dateTime = dateTime.withHourOfDay(DateTimeConverterUtil.HOUR_EIGHT);
      dateTime = dateTime.withMinuteOfHour(0);
      dateTime = dateTime.withSecondOfMinute(0);
      return DateTimeConverterUtil
          .dateTimeToString(DateTimeConverterUtil.convertDateTimeToDate(dateTime));
    }
    Date date;
    try {
      date = DateTimeConverterUtil.stringTimeToDateTimeWithFixFormat(savedDefaultStartTime);
    } catch (ParseException e) {
      // we save defautl start time with HH:mm format. We parse with this format at now. Not
      // possible to throw exception.
      throw new RuntimeException("Cannot be parse default start time.");
    }

    return DateTimeConverterUtil.dateTimeToString(date);
  }

  /**
   * Get the end time change settings value. The default is 5.
   */
  public int getEndTimeChange() {
    int endTimeChange = TimetrackerUtil.FIVE_MINUTES;

    Object endTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.END_TIME_CHANGE);
    if (endTimeChangeObj != null) {
      try {
        endTimeChange = Integer.parseInt(endTimeChangeObj.toString());
        if (!TimetrackerUtil.validateTimeChange(Integer.toString(endTimeChange))) {
          endTimeChange = TimetrackerUtil.FIVE_MINUTES;
        }
      } catch (NumberFormatException e) {
        LOGGER.error("Wrong formated endTime change value. Set the default value (1).", e);
      }
    }
    return endTimeChange;
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
    int startTimeChange = TimetrackerUtil.FIVE_MINUTES;

    Object startTimeChangeObj =
        pluginSettingsKeyValues.get(UserSettingKey.START_TIME_CHANGE);
    if (startTimeChangeObj != null) {
      try {
        startTimeChange = Integer.parseInt(startTimeChangeObj.toString());
        if (!TimetrackerUtil.validateTimeChange(Integer.toString(startTimeChange))) {
          startTimeChange = TimetrackerUtil.FIVE_MINUTES;
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

  public boolean isActiveFieldDuration() {
    return Boolean.valueOf(pluginSettingsKeyValues.get(UserSettingKey.ACTIVE_FIELD_DURATION));
  }

  /**
   * Get the actual date setting value. The default is true.
   */
  public boolean isActualDate() {
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
  public boolean isColoring() {
    boolean isColoring = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_COLORING))) {
      isColoring = false;
    }
    return isColoring;
  }

  /**
   * Get the progress indicator daily or not. The default is true.
   */
  public boolean isProgressIndicatordaily() {
    boolean isProgressIndicatorDaily = true;
    if ("false"
        .equals(pluginSettingsKeyValues.get(UserSettingKey.PROGRESS_INDICATOR))) {
      isProgressIndicatorDaily = false;
    }
    return isProgressIndicatorDaily;
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
   * Get the rounding settings value. The default is true.
   */
  public boolean isRounded() {
    boolean isRounded = true;
    if ("false".equals(pluginSettingsKeyValues.get(UserSettingKey.IS_ROUNDED))) {
      isRounded = false;
    }
    return isRounded;
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
   * Get the show future worklog warning settings value. Default is true.
   */
  public boolean isShowFutureLogWarning() {
    boolean isShowFutureLogWarning = true;
    if ("false".equals(
        pluginSettingsKeyValues.get(UserSettingKey.SHOW_FUTURE_LOG_WARNING))) {
      isShowFutureLogWarning = false;
    }
    return isShowFutureLogWarning;
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
   * Get the settings value of show issue summary column visible or not. Default is false.
   */
  public boolean isShowIssueSummary() {
    boolean isShowIssueSummary = false;
    if ("true".equals(
        pluginSettingsKeyValues.get(
            UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE))) {
      isShowIssueSummary = true;
    }
    return isShowIssueSummary;
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

  public boolean isShowRemaningEstimate() {
    return Boolean.parseBoolean(pluginSettingsKeyValues.get(UserSettingKey.SHOW_REMANING_ESTIMATE));
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
   * Put the show remaning estimate value.
   */
  public TimeTrackerUserSettings showRemaningEstimate(final boolean showRemaningEstimate) {
    pluginSettingsKeyValues.put(UserSettingKey.SHOW_REMANING_ESTIMATE,
        String.valueOf(showRemaningEstimate));
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
