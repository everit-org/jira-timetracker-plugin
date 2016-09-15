package org.everit.jira.analytics.event;

import java.util.Objects;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

public class NonWorkingUsageEvent implements AnalyticsEvent {

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/ReportingWebAction!default.jspa";

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
    this.pluginId = Objects.requireNonNull(pluginId);
    hashUserId = JiraTimetrackerAnalytics.getUserId();
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
