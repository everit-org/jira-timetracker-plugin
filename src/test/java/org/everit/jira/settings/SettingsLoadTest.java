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

import org.everit.jira.settings.dto.GlobalSettingsKey;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class SettingsLoadTest {

  @Test
  public void test() {
    PluginSettingsFactory settingsFactoryMock =
        Mockito.mock(PluginSettingsFactory.class, Mockito.RETURNS_SMART_NULLS);
    PluginSettings pluginSettingsMock = Mockito.mock(PluginSettings.class);
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(pluginSettingsMock);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    Mockito.when(pluginSettingsMock.get(Matchers.anyString())).thenReturn(null);
    // TimeTrackerGlobalSettings loadGlobalSettings =
    // timeTrackerSettingsHelperImpl.loadGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(2)).createGlobalSettings();
    Mockito.verify(pluginSettingsMock, Mockito.times(GlobalSettingsKey.values().length))
        .get(Matchers.anyString());
  }

}
