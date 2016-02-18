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
package org.everit.jira.timetracker.plugin.dto;

import java.io.Serializable;
import java.util.List;

/**
 * ReportingSettingsValues class contains the result of the JiraTimetrackerPlugin's
 * loadReportingSetting method.
 */
public class ReportingSettingsValues implements Serializable {

  /**
   * Serial Version UID.
   */
  private static final long serialVersionUID = -9056110737937671869L;
  /**
   * Reporting is use nowork set.
   */
  public boolean isUseNoWorks;

  /**
   * The reporting groups.
   */
  public List<String> reportingGroups;

  public ReportingSettingsValues() {
  }

  public ReportingSettingsValues isUseNoWorks(final boolean isUseNoWorks) {
    this.isUseNoWorks = isUseNoWorks;
    return this;
  }

  public ReportingSettingsValues reportingGroups(final List<String> reportingGroups) {
    this.reportingGroups = reportingGroups;
    return this;
  }

}
