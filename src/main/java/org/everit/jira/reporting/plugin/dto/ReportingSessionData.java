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
package org.everit.jira.reporting.plugin.dto;

import java.io.Serializable;

/**
 * Session data for the reporting page.
 */
public class ReportingSessionData implements Serializable {

  /**
   * Serial Version UID.
   */
  private static final long serialVersionUID = 6059112912342079835L;

  public String collapsedDetailsModuleVal;

  public String collapsedSummaryModuleVal;

  public String filterConditionJson;

  public String selectedActiveTab;

  public String selectedMoreJson;

  public String selectedWorklogDetailsColumnsJson;

  public ReportingSessionData collapsedDetailsModuleVal(final String collapsedDetailsModuleVal) {
    this.collapsedDetailsModuleVal = collapsedDetailsModuleVal;
    return this;
  }

  public ReportingSessionData collapsedSummaryModuleVal(final String collapsedSummaryModuleVal) {
    this.collapsedSummaryModuleVal = collapsedSummaryModuleVal;
    return this;
  }

  public ReportingSessionData filterConditionJson(final String filterConditionJson) {
    this.filterConditionJson = filterConditionJson;
    return this;
  }

  public ReportingSessionData selectedActiveTab(final String selectedActiveTab) {
    this.selectedActiveTab = selectedActiveTab;
    return this;
  }

  public ReportingSessionData selectedMoreJson(final String selectedMoreJson) {
    this.selectedMoreJson = selectedMoreJson;
    return this;
  }

  public ReportingSessionData selectedWorklogDetailsColumnsJson(
      final String selectedWorklogDetailsColumnsJson) {
    this.selectedWorklogDetailsColumnsJson = selectedWorklogDetailsColumnsJson;
    return this;
  }
}
