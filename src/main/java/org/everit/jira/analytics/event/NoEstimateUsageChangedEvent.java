package org.everit.jira.analytics.event;

import java.util.Objects;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

public class NoEstimateUsageChangedEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/ReportingWebAction!default.jspa";

  private static final String EVENT_ACTION = "nonEstUsage";

  private static final String EVENT_CATEGORY = "Non-estimated usage";

  private static final String NON_EST_EMPTY = "false";

  private static final String NON_EST_NOT_EMPTY = "false";

  private final boolean emptyNonEstUsage;

  private final String hashUserId;

  private final String pluginId;

  public NoEstimateUsageChangedEvent(final String pluginId,
      final boolean emptyNonEstUsage) {
    this.pluginId = Objects.requireNonNull(pluginId);
    hashUserId = JiraTimetrackerAnalytics.getUserId();
    this.emptyNonEstUsage = emptyNonEstUsage;
  }

  @Override
  public String getUrl() {
    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_ADMINISTRATION_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addEventName(emptyNonEstUsage
                ? NON_EST_EMPTY
                : NON_EST_NOT_EMPTY)
            .buildUrl();
  }

}
