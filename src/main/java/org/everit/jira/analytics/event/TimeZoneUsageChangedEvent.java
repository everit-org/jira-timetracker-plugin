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
import org.everit.jira.settings.dto.TimeZoneTypes;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

/**
 * TimeZone usage changed event.
 */
public class TimeZoneUsageChangedEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/admin/TimetrackerAdminSettingsWebAction!default.jspa";

  private static final String EVENT_ACTION = "timeZone";

  private static final String EVENT_CATEGORY = "TimeZone usage";

  private final String hashUserId;

  private final String pluginId;

  private final TimeZoneTypes status;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param status
   *          the status of analytics (enabled (true) or disabled (false)).
   */
  public TimeZoneUsageChangedEvent(final String pluginId, final TimeZoneTypes status) {
    this.pluginId = Objects.requireNonNull(pluginId);
    this.status = status;
    hashUserId = JiraTimetrackerAnalytics.getUserId();
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_ADMINISTRATION_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addEventName(status.getValue())
            .buildUrl();
  }

}
