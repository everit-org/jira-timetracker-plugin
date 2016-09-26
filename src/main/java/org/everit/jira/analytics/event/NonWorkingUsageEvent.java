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
 * Event for Non-working issues field usage analytics.
 */
public class NonWorkingUsageEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/admin/JiraTimetrackerAdminSettingsWebAction!default.jspa";

  private static final String EVENT_ACTION_NAME = "noWorkUsage";

  private static final String EVENT_CATEGORY_NAME = "Non-working usage";

  private final String hashUserId;

  private final boolean nonWorkIsEmpty;

  private final String pluginId;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param nonWorkingIsEmpty
   *          the Non-working issues input filed is empty or not.
   */
  public NonWorkingUsageEvent(final String pluginId,
      final boolean nonWorkingIsEmpty) {
    this(pluginId, nonWorkingIsEmpty, JiraTimetrackerAnalytics.getUserId());
  }

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param nonWorkingIsEmpty
   *          the Non-working issues input filed is empty or not.
   * @param hashUserId
   *          the user hash id
   */
  public NonWorkingUsageEvent(final String pluginId,
      final boolean nonWorkingIsEmpty, final String hashUserId) {
    this.pluginId = Objects.requireNonNull(pluginId);
    this.hashUserId = hashUserId;
    nonWorkIsEmpty = nonWorkingIsEmpty;
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_ADMINISTRATION_SITEID,
        pluginId, hashUserId)
            .addEventAction(EVENT_ACTION_NAME)
            .addEventName(String.valueOf(!nonWorkIsEmpty))
            .addEventCategory(EVENT_CATEGORY_NAME)
            .buildUrl();
  }
}
