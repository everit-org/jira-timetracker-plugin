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
package org.everit.jira.reporting.plugin.export.column;

/**
 * Define Project Summary report columns.
 */
public final class ProjectSummaryColumns {

  public static final String ESTIMATED = "estimated";

  public static final String EXPECTED_TOTAL = "expectedTotal";

  public static final String EXPECTED_TOTAL_VS_ESTIMATED = "expectedTotalVsEstimated";

  public static final String LOGGED_VS_ESTIMATED = "loggedVsEstimated";

  public static final String PROJECT = "project";

  public static final String PROJECT_DESCRIPTION = "projectDescription";

  public static final String REMAINING = "remaining";

  public static final String TOTAL_LOGGED = "totalLogged";

  private ProjectSummaryColumns() {
  }
}
