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
 * The user settings keys enumeration.
 */
public enum UserSettingKey implements SettingsMapper {

  END_TIME_CHANGE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE;
    }

  },

  IS_ACTUAL_DATE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE;
    }

  },
  IS_COLORING {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_IS_COLORIG;
    }

  },
  IS_ROUNDED {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_IS_ROUNDED;
    }

  },
  IS_SHOW_TUTORIAL {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_IS_SHOW_TUTORIAL;
    }

  },
  PROGRESS_INDICATOR {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_PROGRESS_INDICATOR;
    }

  },
  REPORTING_SETTINGS_PAGER_SIZE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_PAGER_SIZE;
    }

  },
  REPORTING_SETTINGS_WORKLOG_IN_SEC {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_REPORTING_SETTINGS_WORKLOG_IN_SEC;
    }

  },

  SHOW_FUTURE_LOG_WARNING {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_SHOW_FUTURE_LOG_WARNING;
    }

  },
  SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE;
    }
  },
  SHOW_REMANING_ESTIMATE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_SHOW_REMANING_ESTIMATE;
    }

  },
  SHOW_TUTORIAL_VERSION {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_SHOW_TUTORIAL_VERSION;
    }

  },
  START_TIME_CHANGE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE;
    }
  },
  USER_CANCELED_UPDATE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_USER_CANCELED_UPDATE;
    }

  },
  USER_WD_SELECTED_COLUMNS {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_USER_WD_SELECTED_COLUMNS;
    }

  };

}
