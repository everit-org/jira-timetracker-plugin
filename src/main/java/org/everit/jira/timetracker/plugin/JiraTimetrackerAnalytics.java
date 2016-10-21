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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.everit.jira.analytics.AnalyticsDTO;
import org.everit.jira.settings.TimetrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.timetracker.plugin.util.HashUtil;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.BuildUtilsInfo;

/**
 * The Jira Timetracker plugin analytics class.
 */
public final class JiraTimetrackerAnalytics {

  private static final String BASE_URL = "org.everit.jira.timetracker.plugin.base.url.hash";

  private static final String ERROR_BASE_URL_HASH = "errorBaseUrlHash";

  private static final String ERROR_USER_ID_HASH = "errorUserIdHash";

  /**
   * The JiraTimetrackerAnalytics logger.
   */
  private static Logger log = Logger.getLogger(JiraTimetrackerAnalytics.class);

  private static final String USER_ID = "org.everit.jira.timetracker.plugin.user.id.hash";

  /**
   * Gets {@link AnalyticsDTO} that contains all required detail to collect usage.
   *
   * @param siteIdPropertyKey
   *          the site id property key.
   * @return the {@link AnalyticsDTO} object.
   */
  public static AnalyticsDTO getAnalyticsDTO(final String siteIdPropertyKey,
      final TimetrackerSettingsHelper settingsHelper) {
    Properties jttpBuildProperties = PropertiesUtil.getJttpBuildProperties();

    String piwikHost =
        JiraTimetrackerAnalytics.getProperty(jttpBuildProperties, PiwikPropertiesUtil.PIWIK_HOST);
    String siteId = JiraTimetrackerAnalytics.getProperty(jttpBuildProperties, siteIdPropertyKey);

    TimeTrackerGlobalSettings loadGlobalSettings = settingsHelper.loadGlobalSettings();
    return new AnalyticsDTO()
        .analyticsCheck(loadGlobalSettings.getAnalyticsCheck())
        .baseUrl(JiraTimetrackerAnalytics.getBaseUrl())
        .installedPluginId(loadGlobalSettings.getPluginUUID())
        .jiraVersion(JiraTimetrackerAnalytics.getJiraVersionFromBuildUtilsInfo())
        .piwikHost(piwikHost)
        .piwikSiteId(siteId)
        .pluginVersion(JiraTimetrackerAnalytics.getPluginVersion())
        .userId(JiraTimetrackerAnalytics.getUserId());
  }

  /**
   * Get the base URL.
   *
   * @return The base URL.
   */
  public static String getBaseUrl() {
    String baseUrl;
    try {
      baseUrl = HashUtil.encryptString(ComponentAccessor
          .getApplicationProperties()
          .getString("jira.baseurl"));
      return baseUrl;
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Error when try to hash the base URL.", e);
      return ERROR_BASE_URL_HASH;

    }

  }

  /**
   * Gets JIRA version.
   */
  public static String getJiraVersionFromBuildUtilsInfo() {
    BuildUtilsInfo component = ComponentAccessor.getComponent(BuildUtilsInfo.class);
    return component.getVersion();

  }

  /**
   * Get the version of the plugin.
   *
   * @return The version.
   */
  public static String getPluginVersion() {
    String pluginVersion = ComponentAccessor.getPluginAccessor()
        .getPlugin("org.everit.jira.timetracker.plugin")
        .getPluginInformation().getVersion();
    return pluginVersion;
  }

  private static String getProperty(final Properties jttpBuildProperties, final String key) {
    Object value = jttpBuildProperties.get(key);
    return value != null ? (String) value : null;
  }

  /**
   * Get the user ID.
   *
   * @return The user.
   */
  public static String getUserId() {
    String userId;
    try {
      userId = HashUtil.encryptString(ComponentAccessor
          .getJiraAuthenticationContext().getUser().getKey());
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Error when try to hash the user ID.", e);
      return ERROR_USER_ID_HASH;
    }
    return userId;
  }

  /**
   * Set the hashed base URL.
   *
   * @param session
   *          Session.
   * @return The hashed base URL.
   */
  public static String setUserSessionBaseUrl(final HttpSession session) {
    String resultBaseUrl;
    String hashedBaseUrl = (String) session.getAttribute(BASE_URL);
    if (hashedBaseUrl == null) {
      resultBaseUrl = JiraTimetrackerAnalytics.getBaseUrl();
      session.setAttribute(BASE_URL, resultBaseUrl);
    } else {
      resultBaseUrl = hashedBaseUrl;
    }
    return resultBaseUrl;
  }

  /**
   * Set the hashed user ID.
   *
   * @param session
   *          Session
   * @return The hashed user ID.
   */
  public static String setUserSessionUserId(final HttpSession session) {
    String resultUserId;
    String hashedUserId = (String) session.getAttribute(USER_ID);
    if (hashedUserId == null) {
      resultUserId = JiraTimetrackerAnalytics.getUserId();
      session.setAttribute(USER_ID, resultUserId);
    } else {
      resultUserId = hashedUserId;
    }
    return resultUserId;
  }

  private JiraTimetrackerAnalytics() {
  }

}
