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
package org.everit.jira.timetracker.popup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.everit.jira.timetracker.plugin.util.PropertiesUtil;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Survey dialog web action.
 */
public class SurveyDialogWebAction extends JiraWebActionSupport {

  private static final String KEY_POPUP_SAVED_DATE =
      "org.everit.jira.timetracker.survey.saved.date";

  private static final long serialVersionUID = -423758684539179269L;

  private static final String SURVEY_FORM_URL = "SURVEY_FORM_URL";

  private static final String SURVEY_SCRIPT_URL = "SURVEY_SCRIPT_URL";

  private String lastSavedSurveyDate;

  private PluginSettingsFactory pluginSettingsFactory;

  private String surveyFormUrl;

  private String surveyScriptUrl;

  public SurveyDialogWebAction(final PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  @Override
  public String doDefault() throws Exception {
    Properties jttpBuildProperties = PropertiesUtil.getJttpBuildProperties();

    surveyScriptUrl = getProperty(jttpBuildProperties, SURVEY_SCRIPT_URL);
    surveyFormUrl = getProperty(jttpBuildProperties, SURVEY_FORM_URL);

    PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
    Object formattedDate = globalSettings.get(KEY_POPUP_SAVED_DATE);
    lastSavedSurveyDate = formattedDate != null
        ? formattedDate.toString()
        : "";
    return SUCCESS;
  }

  @Override
  protected String doExecute() throws Exception {
    String action = getHttpRequest().getParameter("action");
    if ("save".equals(action)) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
      globalSettings.put(KEY_POPUP_SAVED_DATE, sdf.format(new Date()));
    }
    return NONE;
  }

  public String getLastSavedSurveyDate() {
    return lastSavedSurveyDate;
  }

  private String getProperty(final Properties jttpBuildProperties, final String key) {
    Object value = jttpBuildProperties.get(key);
    return value != null ? (String) value : null;
  }

  public String getSurveyFormUrl() {
    return surveyFormUrl;
  }

  public String getSurveyScriptUrl() {
    return surveyScriptUrl;
  }

  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }

  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    stream.close();
    throw new java.io.NotSerializableException(getClass().getName());
  }
}
