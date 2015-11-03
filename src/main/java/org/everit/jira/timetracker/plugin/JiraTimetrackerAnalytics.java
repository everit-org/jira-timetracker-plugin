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

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;

/**
 * The Jira Timetracker plugin analytics class.
 */
public final class JiraTimetrackerAnalytics {

  /**
   * The JiraTimetrackerAnalytics logger.
   */
  private static Logger log = Logger.getLogger(JiraTimetrackerAnalytics.class);

  private static final String ERROR_BASE_URL_HASH = "errorBaseUrlHash";

  private static final String ERROR_USER_ID_HASH = "errorUserIdHash";

  /**
   * Get the base URL.
   *
   * @return The base URL.
   */
  public static String getBaseUrl() {
    String baseUrl;
    try {
      baseUrl = Hash.encryptString(ComponentAccessor
          .getApplicationProperties()
          .getString("jira.baseurl"));
      return baseUrl;
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Error when try to hash the base URL.", e);
      return ERROR_BASE_URL_HASH;

    }

  }

  /**
   * Get the version of the plugin.
   *
   * @return The version.
   */
  public static String getPluginVersion() {
    final String pluginVersion = ComponentAccessor.getPluginAccessor()
        .getPlugin("org.everit.jira.timetracker.plugin")
        .getPluginInformation().getVersion();
    return pluginVersion;
  }

  /**
   * Get the user ID.
   *
   * @return The user.
   */
  public static String getUserId() {
    String userId;
    try {
      userId = Hash.encryptString(ComponentAccessor
          .getJiraAuthenticationContext().getUser().getKey());
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Error when try to hash the user ID.", e);
      return ERROR_USER_ID_HASH;
    }
    return userId;
  }

  private JiraTimetrackerAnalytics() {
  }

}
