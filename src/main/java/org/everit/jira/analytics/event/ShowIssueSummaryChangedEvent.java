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
 * Progress indicator appearance changed (daily/summary) event.
 */
public class ShowIssueSummaryChangedEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/TimetrackerUserSettingsWebAction!default.jspa";

  private static final String EVENT_ACTION = "KeyOrSummary";

  private static final String EVENT_CATEGORY = "User";

  private static final String EVENT_NAME_KEY = "issueKey";

  private static final String EVENT_NAME_SUMMARY = "issueSummary";

  private final String hashUserId;

  private final String pluginId;

  private final boolean showIssueSummary;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param showIssueSummary
   *          the status of show issue summary or not.
   */
  public ShowIssueSummaryChangedEvent(final String pluginId, final boolean showIssueSummary) {
    this.pluginId = Objects.requireNonNull(pluginId);
    this.showIssueSummary = showIssueSummary;
    hashUserId = JiraTimetrackerAnalytics.getUserId();
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_USERSETTINGS_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addEventName(showIssueSummary
                ? EVENT_NAME_SUMMARY
                : EVENT_NAME_KEY)
            .buildUrl();
  }

}
