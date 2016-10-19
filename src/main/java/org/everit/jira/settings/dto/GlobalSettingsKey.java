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
 * The timetracker global settings key enumeration.
 */
public enum GlobalSettingsKey implements SettingsMapper {
  ANALYTICS_CHECK_CHANGE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_ANALYTICS_CHECK_CHANGE;
    }

  },
  EXCLUDE_DATES {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES;
    }

  },
  INCLUDE_DATES {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_INCLUDE_DATES;
    }

  },
  NON_ESTIMATED_ISSUES {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES;
    }

  },
  PLUGIN_PERMISSION {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_PLUGIN_PERMISSION;
    }

  },
  PLUGIN_UUID {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_UUID;
    }

  },
  SUMMARY_FILTERS {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS;
    }

  },
  TIMETRACKER_PERMISSION {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_TIMETRACKER_PERMISSION;
    }
  },
  UPDATE_NOTIFIER_LAST_UPDATE {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_UPDATE_NOTIFIER_LAST_UPDATE;
    }
  },
  UPDATE_NOTIFIER_LATEST_VERSION {

    @Override
    public String getSettingsKey() {
      return JTTPSettingsKey.JTTP_UPDATE_NOTIFIER_LATEST_VERSION;
    }
  };
}
