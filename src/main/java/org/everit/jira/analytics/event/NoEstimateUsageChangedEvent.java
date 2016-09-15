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

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

/**
 * Non-estimated field changed event.
 */
public class NoEstimateUsageChangedEvent implements AnalyticsEvent {
  /**
   * Enumeration for representing the three option for the non estimate field.
   */
  private enum NonEstUsageValues {
    ALL("all"), NONE("none"), SELECTED("selected");
    public final String lowerCaseValue;

    private NonEstUsageValues(final String lowerCaseValue) {
      this.lowerCaseValue = lowerCaseValue;
    }
  }

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/ReportingWebAction!default.jspa";

  private static final String EVENT_ACTION_NAME = "nonEstUsage";

  private static final String EVENT_CATEGORY_NAME = "Non-estimated usage";

  private final String hashUserId;

  private final NonEstUsageValues nonEstValue;

  private final String pluginId;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param collectorIssuePatterns
   *          the Non-estimated field is empty or not.
   */
  public NoEstimateUsageChangedEvent(final String pluginId,
      final List<Pattern> collectorIssuePatterns) {
    this.pluginId = Objects.requireNonNull(pluginId);
    hashUserId = JiraTimetrackerAnalytics.getUserId();
    nonEstValue = decideNonEstValue(collectorIssuePatterns);
  }

  private NonEstUsageValues decideNonEstValue(final List<Pattern> collectorIssuePatterns) {
    if (collectorIssuePatterns.isEmpty()) {
      return NonEstUsageValues.ALL;
    }
    for (Pattern pattern : collectorIssuePatterns) {
      if (pattern.pattern().equals(".*")) {
        return NonEstUsageValues.NONE;
      }
    }
    return NonEstUsageValues.SELECTED;
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_ADMINISTRATION_SITEID,
        pluginId, hashUserId)
            .addEventAction(EVENT_ACTION_NAME)
            .addEventName(nonEstValue.lowerCaseValue)
            .addEventCategory(EVENT_CATEGORY_NAME)
            .buildUrl();
  }
}
