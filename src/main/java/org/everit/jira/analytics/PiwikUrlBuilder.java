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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;
import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

/**
 * Builder for constructing Piwik url.
 */
public class PiwikUrlBuilder {

  private List<String> parameters = new ArrayList<String>();

  private void addApivParam() {
    parameters.add("apiv=1");
  }

  public PiwikUrlBuilder addEventAction(final String eventAction) {
    parameters.add("e_a" + eventAction);
    return this;
  }

  public PiwikUrlBuilder addEventCategory(final String eventCategory) {
    parameters.add("e_c" + eventCategory);
    return this;
  }

  public PiwikUrlBuilder addEventName(final String eventName) {
    parameters.add("e_n" + eventName);
    return this;
  }

  private void addIdSitedIdParam(final String siteId) {
    parameters.add("idsite=" + siteId);
  }

  private void addRecParam() {
    parameters.add("rec=1");
  }

  private void addUrlParam(final String url) {
    parameters.add("url=" + url);
  }

  private void addVisitorIdParam(final String pluginId) {
    parameters.add("_id=" + pluginId);
  }

  /**
   * Build piwik url.
   *
   * @param currentActionUrl
   *          the the current action url.
   * @param pluginId
   *          the id of the plugin.
   * @return the url.
   */
  public String buildUrl(final String currentActionUrl, final String pluginId) throws IOException {
    Properties jttpBuildProperties = PropertiesUtil.getJttpBuildProperties();
    String piwikHost = jttpBuildProperties
        .getProperty(PiwikPropertiesUtil.PIWIK_HOST);
    String piwikAdministrationSiteId = jttpBuildProperties
        .getProperty(PiwikPropertiesUtil.PIWIK_ADMINISTRATION_SITEID, "0");

    // required and recommended parameters.
    addIdSitedIdParam(piwikAdministrationSiteId);
    addRecParam();
    addUrlParam(currentActionUrl);
    addVisitorIdParam(pluginId);
    addApivParam();

    StringBuffer sb = new StringBuffer();
    for (String string : parameters) {
      sb.append(string);
      sb.append("&");
    }
    String paramters = sb.toString();
    return piwikHost + "piwik.php?" + paramters.substring(0, paramters.length() - 1);
  }

}
