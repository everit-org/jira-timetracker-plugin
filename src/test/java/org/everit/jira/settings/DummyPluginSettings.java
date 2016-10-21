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

import java.util.HashMap;
import java.util.Map;

import org.everit.jira.settings.dto.JTTPSettingsKey;
import org.everit.jira.settings.dto.SettingsMapper;
import org.everit.jira.settings.dto.UserSettingKey;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class DummyPluginSettings implements PluginSettings {

  private final Map<String, Object> map = new HashMap<>();

  @Override
  public Object get(final String key) {
    return map.get(key);
  }

  public Object getGlobalSetting(final SettingsMapper settingsKey) {
    return map.get(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX + settingsKey.getSettingsKey());

  }

  public Map<String, Object> getMap() {
    return map;
  }

  @Override
  public Object put(final String key, final Object value) {
    return map.put(key, value);
  }

  public void putGlobalSetting(final SettingsMapper settingsKey, final Object value) {
    map.put(JTTPSettingsKey.JTTP_PLUGIN_SETTINGS_KEY_PREFIX + settingsKey.getSettingsKey(),
        value);
  }

  public void putUserSetting(final UserSettingKey key, final String value) {
    map.put(key.getSettingsKey(),
        value);

  }

  @Override
  public Object remove(final String key) {
    return map.remove(key);
  }
}
