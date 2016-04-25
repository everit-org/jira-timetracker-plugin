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
package org.everit.jira.analytics.event;

import java.util.Objects;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

/**
 * Export worklog details report event.
 */
public class ExportWorklogDetailsReportEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/ReportingWebAction!default.jspa";

  private static final String EVENT_ACTION = "Export Worklog Details";

  private static final String EVENT_CATEGORY = "Reporting";

  private static final String EVENT_NAME_ALL_FIELDS = "all-fields";

  private static final String EVENT_NAME_CUSTOM_FIELDS = "custom-fields";

  private final boolean allColumns;

  private final String hashUserId;

  private final String pluginId;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param allColumns
   *          selected all columns or not.
   */
  public ExportWorklogDetailsReportEvent(final String pluginId, final boolean allColumns) {
    this.pluginId = Objects.requireNonNull(pluginId);
    hashUserId = JiraTimetrackerAnalytics.getUserId();
    this.allColumns = allColumns;
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_REPORTING_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addEventName(allColumns
                ? EVENT_NAME_ALL_FIELDS
                : EVENT_NAME_CUSTOM_FIELDS)
            .buildUrl();
  }

}
