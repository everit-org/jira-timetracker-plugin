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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

import org.everit.jira.reporting.plugin.export.column.WorklogDetailsColumns;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

/**
 * Builder for constructing Piwik URL.
 */
public class PiwikUrlBuilder {

  private static final String DIMENSION = "dimension";

  private final String actionUrl;

  private String activeFilterCondtionParam;

  private final String baseUrl;

  private final String jiraVersion;

  private List<String> parameters = new ArrayList<String>();

  private final String piwikHost;

  private final String pluginId;

  private final String pluginVersion;

  private String searcherValueParam;

  private String selectedActiveTabParam;

  private String selectedWorklogDetailColumnsParam;

  private final String siteId;

  private final String uid;

  private String userSelectionParam;

  /**
   * Simple constructor.
   *
   * @param actionUrl
   *          the action URL. Example:
   *          "http://customer.jira.com/secure/admin/JiraTimetrackerAdminSettingsWebAction!default.jspa".
   *          Cannot be <code>null</code>.
   * @param siteIdKey
   *          the properties key to site id. Found keys in {@link PiwikPropertiesUtil} class. Cannot
   *          be <code>null</code>.
   * @param pluginId
   *          the installed plugin id. Cannot be <code>null</code>.
   * @param hashUserId
   *          the hash user id.Cannot be <code>null</code>.
   */
  public PiwikUrlBuilder(final String actionUrl, final String siteIdKey, final String pluginId,
      final String hashUserId) {
    this.actionUrl = Objects.requireNonNull(actionUrl);
    uid = Objects.requireNonNull(hashUserId);
    pluginVersion = JiraTimetrackerAnalytics.getPluginVersion();
    baseUrl = JiraTimetrackerAnalytics.getBaseUrl();
    this.pluginId = Objects.requireNonNull(pluginId);
    jiraVersion = JiraTimetrackerAnalytics.getJiraVersionFromBuildUtilsInfo();

    try {
      Properties jttpBuildProperties = PropertiesUtil.getJttpBuildProperties();
      piwikHost = jttpBuildProperties.getProperty(PiwikPropertiesUtil.PIWIK_HOST);
      siteId = jttpBuildProperties.getProperty(siteIdKey);
      activeFilterCondtionParam = DIMENSION + jttpBuildProperties
          .getProperty(PiwikPropertiesUtil.PIWIK_ACTIVE_FILTER_CONDITION_CUSTOM_DIMENSION_INDEX);
      searcherValueParam = DIMENSION + jttpBuildProperties
          .getProperty(PiwikPropertiesUtil.PIWIK_SEARCHER_VALUE_CUSTOM_DIMENSION_INDEX);
      selectedActiveTabParam = DIMENSION + jttpBuildProperties
          .getProperty(PiwikPropertiesUtil.PIWIK_SELECTED_ACTIVE_TAB_CUSTOM_DIMENSION_INDEX);
      selectedWorklogDetailColumnsParam = DIMENSION + jttpBuildProperties.getProperty(
          PiwikPropertiesUtil.PIWIK_SELECTED_WORKLOG_DETAIL_COLUMNS_CUSTOM_DIMENSION_INDEX);
      userSelectionParam = DIMENSION + jttpBuildProperties
          .getProperty(PiwikPropertiesUtil.PIWIK_USER_SELECTION_CUSTOM_DIMENSION_INDEX);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addActionNameParam() {
    parameters.add("action_name=" + encodeParamValue(baseUrl));
  }

  private void addApivParam() {
    parameters.add("apiv=1");
  }

  /**
   * Add active filter condition custom dimension parameter to URL.
   */
  public PiwikUrlBuilder addCustomDimesionActiveFilterCondition(
      final String activeFilterCondition) {
    parameters.add(activeFilterCondtionParam + "=" + encodeParamValue(activeFilterCondition));
    return this;
  }

  /**
   * Add searcher value custom dimension parameter to URL.
   */
  public PiwikUrlBuilder addCustomDimesionSearcherValue(final SearcherValue searcherValue) {
    parameters.add(searcherValueParam + "=" + encodeParamValue(searcherValue.name()));
    return this;
  }

  /**
   * Add selected active tab custom dimension parameter to URL.
   */
  public PiwikUrlBuilder addCustomDimesionSelectedActiveTab(final String selectedActiveTab) {
    parameters.add(selectedActiveTabParam + "=" + encodeParamValue(selectedActiveTab));
    return this;
  }

  /**
   * Add selected worklog detail columns custom dimension parameter to URL.
   */
  public PiwikUrlBuilder addCustomDimesionSelectedWorklogDetailColumns(
      final List<String> selectedWorklogDetailColumns) {
    StringBuilder sb = new StringBuilder();
    for (String column : selectedWorklogDetailColumns) {
      if (!(WorklogDetailsColumns.PROJECT.equals(column)
          || WorklogDetailsColumns.ISSUE_KEY.equals(column)
          || WorklogDetailsColumns.ISSUE_SUMMARY.equals(column)
          || WorklogDetailsColumns.TIME_SPENT.equals(column))) {
        // only append not required columns
        sb.append(column.replace("jtrp_col_", ""));
        sb.append(",");
      }
    }
    String columns = sb.toString();
    if (!columns.isEmpty()) {
      parameters.add(selectedWorklogDetailColumnsParam + "=" + encodeParamValue(columns));
    }
    return this;
  }

  /**
   * Add user selection custom dimension parameter to URL.
   */
  public PiwikUrlBuilder addCustomDimesionUserSelecton(final UserSelection userSelection) {
    parameters.add(userSelectionParam + "=" + encodeParamValue(userSelection.name()));
    return this;
  }

  private void addCvarParam() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"1\":[\"UserKey\",\"" + encodeParamValue(uid) + "\"]");
    sb.append(",");
    sb.append("\"2\":[\"Version\",\"" + encodeParamValue(pluginVersion) + "\"]");
    sb.append(",");
    sb.append("\"3\":[\"BaseURL\",\"" + encodeParamValue(baseUrl) + "\"]");
    sb.append(",");
    sb.append("\"4\":[\"JiraVersion\",\"" + encodeParamValue(jiraVersion) + "\"]");
    sb.append(",");
    sb.append("\"5\":[\"InstalledPluginId\",\"" + encodeParamValue(pluginId) + "\"]");
    sb.append("}");
    parameters.add("cvar=" + encodeParamValue(sb.toString()));
  }

  /**
   * Add event action parameter to URL.
   */
  public PiwikUrlBuilder addEventAction(final String eventAction) {
    parameters.add("e_a=" + encodeParamValue(eventAction));
    return this;
  }

  /**
   * Add event category parameter to URL.
   */
  public PiwikUrlBuilder addEventCategory(final String eventCategory) {
    parameters.add("e_c=" + encodeParamValue(eventCategory));
    return this;
  }

  /**
   * Add event name parameter to URL.
   */
  public PiwikUrlBuilder addEventName(final String eventName) {
    parameters.add("e_n=" + encodeParamValue(eventName));
    return this;
  }

  private void addIdSiteParam() {
    parameters.add("idsite=" + encodeParamValue(siteId));
  }

  private void addRecParam() {
    parameters.add("rec=1");
  }

  private void addUidParam() {
    parameters.add("uid=" + encodeParamValue(uid));
  }

  private void addUrlParam() {
    parameters.add("url=" + encodeParamValue(actionUrl));
  }

  private void addVisitorIdParam() {
    parameters.add("_id=" + encodeParamValue(uid));
  }

  /**
   * Build Piwik URL.
   *
   * @return the URL.
   */
  public String buildUrl() {
    addActionNameParam();
    addIdSiteParam();
    addRecParam();
    addUrlParam();
    addUidParam();
    addVisitorIdParam();
    addCvarParam();
    addApivParam();

    Random random = new Random();
    long nextLong = random.nextLong();
    parameters.add("rand=" + nextLong);
    StringBuffer sb = new StringBuffer();
    for (String string : parameters) {
      sb.append(string);
      sb.append("&");
    }
    String paramters = sb.toString();
    return piwikHost + "piwik.php?" + paramters.substring(0, paramters.length() - 1);
  }

  private String encodeParamValue(final String paramValue) {
    try {
      return URLEncoder.encode(paramValue, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return paramValue;
    }
  }

}
