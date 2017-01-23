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
package org.everit.jira.tests.settings;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.AnalyticsEvent;
import org.everit.jira.settings.TimeTrackerSettingsHelperImpl;
import org.everit.jira.settings.dto.GlobalSettingsKey;
import org.everit.jira.settings.dto.ReportingGlobalSettings;
import org.everit.jira.settings.dto.ReportingSettingKey;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.settings.dto.UserSettingKey;
import org.everit.jira.tests.util.converterUtilForTests;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper.BeanFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class SettingsLoadTest {

  private void assertPatterns(final Pattern[] expected, final Pattern[] actual) {
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(expected[i].pattern(), actual[i].pattern());
    }
  }

  @Before
  public void setUp() {
    PluginAccessor pluginAccessorMock =
        Mockito.mock(PluginAccessor.class, Mockito.RETURNS_DEEP_STUBS);
    BeanFactory beanFactoryMock = Mockito.mock(BeanFactory.class, Mockito.RETURNS_DEEP_STUBS);
    final ApplicationUser fred = new MockApplicationUser("Fred");
    final JiraAuthenticationContext jiraAuthenticationContext =
        Mockito.mock(JiraAuthenticationContext.class);
    Mockito.when(jiraAuthenticationContext.getUser()).thenReturn(fred);
    Mockito.when(pluginAccessorMock.getPlugin("org.everit.jira.timetracker.plugin")
        .getPluginInformation().getVersion()).thenReturn("2.6.7");

    Mockito.when(beanFactoryMock.getInstance(Locale.getDefault())).thenReturn(null);
    Mockito.when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(fred.getDirectoryUser());

    JiraUserPreferences mockJiraUserPreferences =
        Mockito.mock(JiraUserPreferences.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockJiraUserPreferences.getString("jira.user.timezone"))
        .thenReturn("UTC");
    UserPreferencesManager mockUserPreferencesManager =
        Mockito.mock(UserPreferencesManager.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockUserPreferencesManager.getPreferences(Matchers.any(ApplicationUser.class)))
        .thenReturn(mockJiraUserPreferences);
    new MockComponentWorker()
        .addMock(ConstantsManager.class, new MockConstantsManager())
        .addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
        .addMock(BeanFactory.class, beanFactoryMock)
        .addMock(PluginAccessor.class, pluginAccessorMock)
        .addMock(UserPreferencesManager.class, mockUserPreferencesManager)
        .init();
  }

  @Test
  public void testAfterProperties() throws Exception {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES,
        "2014-02-01, 2014-01-02, 2014-15-02, 2014-asdf-02");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.INCLUDE_DATES,
        "2014-02-04, 2014-01-05, ");
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);
    timeTrackerSettingsHelperImpl.afterPropertiesSet();
    String includeDatesValues =
        (String) dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.INCLUDE_DATES);
    Assert.assertEquals("1391472000000,1388880000000,", includeDatesValues);
    String excludeDatesValues =
        (String) dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES);
    Assert.assertEquals("1391212800000,1388620800000,", excludeDatesValues);
    Mockito.verify(settingsFactoryMock, Mockito.times(3)).createGlobalSettings();
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);
  }

  @Test
  public void testAfterPropertiesWithNewValues() throws Exception {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES,
        "1391209200000,1388617200000,");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.INCLUDE_DATES,
        "1391468400000,1388876400000,");
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);
    timeTrackerSettingsHelperImpl.afterPropertiesSet();
    String includeDatesValues =
        (String) dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.INCLUDE_DATES);
    Assert.assertEquals("1391468400000,1388876400000,", includeDatesValues);
    String excludeDatesValues =
        (String) dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES);
    Assert.assertEquals("1391209200000,1388617200000,", excludeDatesValues);
    Mockito.verify(settingsFactoryMock, Mockito.times(3)).createGlobalSettings();
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);
  }

  @Test
  public void testDates() throws ParseException {
    Assert.assertEquals(1355356800000L,
        DateTimeConverterUtil.fixFormatStringToDate("2012-12-13").getTime());

  }

  @Test
  public void testDefaultGlobalSetting() {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    TimeTrackerGlobalSettings loadGlobalSettings =
        timeTrackerSettingsHelperImpl.loadGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(3)).createGlobalSettings();
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);
    Assert.assertEquals(1, dummyPluginSettings.getMap().size());
    Assert.assertEquals(true, loadGlobalSettings.getAnalyticsCheck());
    Assert.assertEquals(0, loadGlobalSettings.getExcludeDatesAsLong().size());
    Assert.assertEquals(true, loadGlobalSettings.getExcludeDates().isEmpty());
    Assert.assertEquals(0, loadGlobalSettings.getIncludeDatesAsLong().size());
    Assert.assertEquals(true, loadGlobalSettings.getIncludeDates().isEmpty());
    Assert.assertEquals(true, loadGlobalSettings.getIssuePatterns().isEmpty());
    Assert.assertEquals(null, loadGlobalSettings.getLastUpdate());
    Assert.assertEquals(true, loadGlobalSettings.getNonWorkingIssuePatterns().isEmpty());
    Assert.assertEquals(UUID.randomUUID().toString().length(),
        loadGlobalSettings.getPluginUUID().length());
    Assert.assertEquals(true, loadGlobalSettings.getTimetrackerGroups().isEmpty());
  }

  @Test
  public void testDefaultReportingSetting() {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    DummyPluginSettings dummyReportingSettings = new DummyPluginSettings();

    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    Mockito.when(settingsFactoryMock.createSettingsForKey(Matchers.anyString()))
        .thenReturn(dummyReportingSettings);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    ReportingGlobalSettings loadReportingGlobalSettings =
        timeTrackerSettingsHelperImpl.loadReportingGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(2)).createGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(1))
        .createSettingsForKey(Matchers.anyString());
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);

    Assert.assertEquals(1, dummyPluginSettings.getMap().size());
    Assert.assertEquals(0, loadReportingGlobalSettings.getBrowseGroups().size());
    Assert.assertEquals(0, loadReportingGlobalSettings.getReportingGroups().size());
  }

  @Test
  public void testDefaultUserSetting() {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    DummyPluginSettings dummyUserSettings = new DummyPluginSettings();

    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    Mockito.when(settingsFactoryMock.createSettingsForKey(Matchers.anyString()))
        .thenReturn(dummyUserSettings);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    TimeTrackerUserSettings loadUserSettings = timeTrackerSettingsHelperImpl.loadUserSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(2)).createGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(1))
        .createSettingsForKey(Matchers.anyString());
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);

    Assert.assertEquals(1, dummyPluginSettings.getMap().size());
    Assert.assertEquals(0, dummyUserSettings.getMap().size());
    Assert.assertEquals(true, loadUserSettings.isActualDate());
    Assert.assertEquals(true, loadUserSettings.isColoring());
    Assert.assertEquals(5, loadUserSettings.getEndTimeChange());
    Assert.assertEquals(true, loadUserSettings.isProgressIndicatordaily());
    Assert.assertEquals(true, loadUserSettings.isRounded());
    Assert.assertEquals(true, loadUserSettings.isShowFutureLogWarning());
    Assert.assertEquals(true, loadUserSettings.getIsShowTutorialDialog());
    Assert.assertEquals(20, loadUserSettings.getPageSize());
    Assert.assertEquals(5, loadUserSettings.getStartTimeChange());
    Assert.assertNull(loadUserSettings.getUserCanceledUpdate());
  }

  // TODO find mock dependencies
  @Test
  public void testLoadPreConfiguredtGlobalSetting() throws ParseException {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    // initialize the settings
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.ANALYTICS_CHECK_CHANGE, "true");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES,
        "1388620800000,1388707200000,");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.INCLUDE_DATES,
        "1388880000000,1388966400000,");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.NON_ESTIMATED_ISSUES,
        Arrays.asList("sam-3", "sam-4"));
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.PLUGIN_PERMISSION,
        Arrays.asList("group-1", "group-2"));
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.PLUGIN_UUID, "123456");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.SUMMARY_FILTERS,
        Arrays.asList("sam-6", "sam-6"));
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.TIMETRACKER_PERMISSION,
        Arrays.asList("group-3", "group-4"));
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.UPDATE_NOTIFIER_LAST_UPDATE,
        "1287479054000");
    dummyPluginSettings.putGlobalSetting(GlobalSettingsKey.UPDATE_NOTIFIER_LATEST_VERSION,
        "2.6.7");

    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    TimeTrackerGlobalSettings loadGlobalSettings =
        timeTrackerSettingsHelperImpl.loadGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(2)).createGlobalSettings();
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);
    Assert.assertEquals(10, dummyPluginSettings.getMap().size());
    Assert.assertEquals(true, loadGlobalSettings.getAnalyticsCheck());
    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(converterUtilForTests.fixFormatStringToUTCDateTime("2014-01-02"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2014-01-03"))),
        loadGlobalSettings.getExcludeDates());
    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(converterUtilForTests.fixFormatStringToUTCDateTime("2014-01-05"),
                converterUtilForTests.fixFormatStringToUTCDateTime("2014-01-06"))),
        loadGlobalSettings.getIncludeDates());
    assertPatterns(new Pattern[] { Pattern.compile("sam-3"), Pattern.compile("sam-4") },
        loadGlobalSettings.getIssuePatterns().toArray(new Pattern[] {}));
    Assert.assertArrayEquals(new String[] { "group-1", "group-2" },
        loadGlobalSettings.getPluginGroups().toArray(new String[] {}));
    Assert.assertArrayEquals(new String[] { "group-3", "group-4" },
        loadGlobalSettings.getTimetrackerGroups().toArray(new String[] {}));
    Assert.assertEquals(new Long(1287479054000L), loadGlobalSettings.getLastUpdate());
    Assert.assertEquals("2.6.7", loadGlobalSettings.getLatestVersion());
    Assert.assertEquals("123456", loadGlobalSettings.getPluginUUID());
  }

  // TODO TEST AFTER PROPERTIES SET
  @Test
  public void testLoadPreConfiguredUserSetting() {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    DummyPluginSettings dummyGlobalSettings = new DummyPluginSettings();
    DummyPluginSettings dummyUserSettings = new DummyPluginSettings();
    dummyUserSettings.putUserSetting(UserSettingKey.END_TIME_CHANGE, "10");
    dummyUserSettings.putUserSetting(UserSettingKey.IS_ACTUAL_DATE, "true");
    dummyUserSettings.putUserSetting(UserSettingKey.IS_COLORING, "true");
    dummyUserSettings.putUserSetting(UserSettingKey.IS_ROUNDED, "true");
    dummyUserSettings.putUserSetting(UserSettingKey.IS_SHOW_TUTORIAL, "true");
    dummyUserSettings.putUserSetting(UserSettingKey.PROGRESS_INDICATOR, "false");
    dummyUserSettings.putUserSetting(UserSettingKey.REPORTING_SETTINGS_PAGER_SIZE, "30");
    dummyUserSettings.putUserSetting(UserSettingKey.REPORTING_SETTINGS_WORKLOG_IN_SEC, "false");
    dummyUserSettings.putUserSetting(UserSettingKey.SHOW_FUTURE_LOG_WARNING,
        "true");
    dummyUserSettings.putUserSetting(UserSettingKey.SHOW_ISSUE_SUMMARY_IN_WORKLOG_TABLE,
        "true");
    dummyUserSettings.putUserSetting(UserSettingKey.SHOW_TUTORIAL_VERSION,
        "2.6.7");
    dummyUserSettings.putUserSetting(UserSettingKey.START_TIME_CHANGE,
        "10");
    dummyUserSettings.putUserSetting(UserSettingKey.USER_CANCELED_UPDATE, "12.6.5");
    dummyUserSettings.putUserSetting(UserSettingKey.USER_WD_SELECTED_COLUMNS, "issue");
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyGlobalSettings);
    Mockito.when(settingsFactoryMock.createSettingsForKey(Matchers.anyString()))
        .thenReturn(dummyUserSettings);
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, null);

    TimeTrackerUserSettings loadUserSettings = timeTrackerSettingsHelperImpl.loadUserSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(2)).createGlobalSettings();
    Mockito.verify(settingsFactoryMock, Mockito.times(1))
        .createSettingsForKey(Matchers.anyString());
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);

    Assert.assertEquals(1, dummyGlobalSettings.getMap().size());
    Assert.assertEquals(true, loadUserSettings.isActualDate());
    Assert.assertEquals(true, loadUserSettings.isColoring());
    Assert.assertEquals(10, loadUserSettings.getEndTimeChange());
    Assert.assertEquals(false, loadUserSettings.isProgressIndicatordaily());
    Assert.assertEquals(true, loadUserSettings.isShowFutureLogWarning());
    Assert.assertEquals(true, loadUserSettings.getIsShowTutorialDialog());
    Assert.assertEquals(30, loadUserSettings.getPageSize());
    Assert.assertEquals(10, loadUserSettings.getStartTimeChange());
    Assert.assertEquals("12.6.5", loadUserSettings.getUserCanceledUpdate());
    Assert.assertEquals("issue", loadUserSettings.getUserSelectedColumns());
    Assert.assertEquals(false, loadUserSettings.getWorklogTimeInSeconds());

  }

  @Test
  public void testSaveGlobalSetting() throws ParseException {
    PluginSettingsFactory settingsFactoryMock = Mockito.mock(PluginSettingsFactory.class);
    AnalyticsSender analyticsSenderMock = Mockito.mock(AnalyticsSender.class);

    DummyPluginSettings dummyPluginSettings = new DummyPluginSettings();
    Mockito.when(settingsFactoryMock.createGlobalSettings()).thenReturn(dummyPluginSettings);
    Mockito.doNothing().when(analyticsSenderMock).send(Matchers.any(AnalyticsEvent.class));
    TimeTrackerSettingsHelperImpl timeTrackerSettingsHelperImpl =
        new TimeTrackerSettingsHelperImpl(settingsFactoryMock, analyticsSenderMock);

    TimeTrackerGlobalSettings globalSettings = new TimeTrackerGlobalSettings()
        .analyticsCheck(false)
        .collectorIssues(Arrays.asList(Pattern.compile("sam-3"), Pattern.compile("sam-4")))
        .excludeDates(
            Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2015-01-01").getTime()))
        .filteredSummaryIssues(Arrays.asList(Pattern.compile("sam-6"), Pattern.compile("sam-7")))
        .includeDates(
            Arrays.asList(DateTimeConverterUtil.fixFormatStringToDate("2015-01-06").getTime()))
        .lastUpdateTime(1287479054000L)
        .latestVersion("2.6.7")
        .pluginGroups(Arrays.asList("group1", "group2"))
        .timetrackerGroups(Arrays.asList("group3", "group4"));
    timeTrackerSettingsHelperImpl.saveGlobalSettings(globalSettings);

    Mockito.verify(settingsFactoryMock, Mockito.times(4)).createGlobalSettings();
    Mockito.verifyNoMoreInteractions(settingsFactoryMock);
    Map<String, Object> settingsMap = dummyPluginSettings.getMap();
    Assert.assertEquals(10, settingsMap.size());
    Assert.assertEquals("false",
        dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.ANALYTICS_CHECK_CHANGE));
    Assert.assertEquals("1420070400000,",
        dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.EXCLUDE_DATES));
    Assert.assertEquals("1420502400000,",
        dummyPluginSettings.getGlobalSetting(GlobalSettingsKey.INCLUDE_DATES));
    // TODO Continue here
  }

  @Test
  public void testUniqKeys() {
    Set<String> keys = new HashSet<>();
    for (GlobalSettingsKey key : GlobalSettingsKey.values()) {
      keys.add(key.getSettingsKey());
    }
    Assert.assertEquals(GlobalSettingsKey.values().length, keys.size());
    keys.clear();
    for (UserSettingKey key : UserSettingKey.values()) {
      keys.add(key.getSettingsKey());
    }
    Assert.assertEquals(UserSettingKey.values().length, keys.size());
    keys.clear();
    for (ReportingSettingKey key : ReportingSettingKey.values()) {
      keys.add(key.getSettingsKey());
    }
    Assert.assertEquals(ReportingSettingKey.values().length, keys.size());
  }
}
