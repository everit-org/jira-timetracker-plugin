package org.everit.jira.analytics;

public class AnalyticsDTO {

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
