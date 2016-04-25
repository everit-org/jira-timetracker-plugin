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
package org.everit.jira.analytics;

import java.io.Serializable;

/**
 * Contains required details to analytics.
 */
public class AnalyticsDTO implements Serializable {

  private static final long serialVersionUID = 8679468686190076439L;

  private boolean analyticsCheck;

  private String baseUrl;

  private String installedPluginId;

  private String jiraVersion;

  private String piwikHost;

  private String piwikSiteId;

  private String pluginVersion;

  private String userId;

  public AnalyticsDTO analyticsCheck(final boolean analyticsCheck) {
    this.analyticsCheck = analyticsCheck;
    return this;
  }

  public AnalyticsDTO baseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getInstalledPluginId() {
    return installedPluginId;
  }

  public String getJiraVersion() {
    return jiraVersion;
  }

  public String getPiwikHost() {
    return piwikHost;
  }

  public String getPiwikSiteId() {
    return piwikSiteId;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public String getUserId() {
    return userId;
  }

  public AnalyticsDTO installedPluginId(final String installedPluginId) {
    this.installedPluginId = installedPluginId;
    return this;
  }

  public boolean isAnalyticsCheck() {
    return analyticsCheck;
  }

  public AnalyticsDTO jiraVersion(final String jiraVersion) {
    this.jiraVersion = jiraVersion;
    return this;
  }

  public AnalyticsDTO piwikHost(final String piwikHost) {
    this.piwikHost = piwikHost;
    return this;
  }

  public AnalyticsDTO piwikSiteId(final String piwikSiteId) {
    this.piwikSiteId = piwikSiteId;
    return this;
  }

  public AnalyticsDTO pluginVersion(final String pluginVersion) {
    this.pluginVersion = pluginVersion;
    return this;
  }

  public AnalyticsDTO userId(final String userId) {
    this.userId = userId;
    return this;
  }

}
