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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.everit.jira.analytics.PiwikUrlBuilder;
import org.everit.jira.analytics.SearcherValue;
import org.everit.jira.analytics.UserSelection;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.timetracker.plugin.JiraTimetrackerAnalytics;
import org.everit.jira.timetracker.plugin.util.PiwikPropertiesUtil;

/**
 * Create report event.
 */
public class CreateReportEvent implements AnalyticsEvent {

  /**
   * The active filter condition name.
   */
  private enum ActiveFilterConditionName {

    AFFECTED_VERSION,

    ASSIGNEE,

    COMPONENT,

    CREATED,

    EPIC_LINK,

    EPIC_NAME,

    FIXED_VERSION,

    GROUP,

    ISSUE,

    LABEL,

    PRIORITY,

    PROJECT,

    REPORTER,

    RESOLUTION,

    STATUS,

    TYPE,

    USER;
  }

  private static final String ACTION_URL =
      "http://customer.jira.com/secure/ReportingWebAction!default.jspa";

  private static final String EVENT_ACTION = "Create Report";

  private static final String EVENT_CATEGORY = "Reporting";

  private final FilterCondition filterCondition;

  private final String hashUserId;

  private final String pluginId;

  private final String selectedActiveTab;

  private final List<String> selectedWorklogDetailColumns;

  /**
   * Simple constructor.
   *
   * @param pluginId
   *          the installed plugin id.
   * @param filterCondition
   *          the {@link FilterCondition} object.
   * @param selectedWorklogDetailColumns
   *          the selected worklog details report columns.
   * @param selectedActiveTab
   *          the selected active tab in reporting quick summary page.
   */
  public CreateReportEvent(final String pluginId, final FilterCondition filterCondition,
      final List<String> selectedWorklogDetailColumns, final String selectedActiveTab) {
    this.pluginId = Objects.requireNonNull(pluginId);
    hashUserId = JiraTimetrackerAnalytics.getUserId();
    this.filterCondition = filterCondition;
    this.selectedWorklogDetailColumns = selectedWorklogDetailColumns;
    this.selectedActiveTab = selectedActiveTab;
  }

  private void appendActiveFilterCondition(final StringBuilder sb, final List<?> condition,
      final ActiveFilterConditionName activeFilterConditionName) {
    if (!condition.isEmpty()) {
      sb.append(activeFilterConditionName.name() + ",");
    }
  }

  private void appendActiveFilterCondition(final StringBuilder sb, final String condition,
      final ActiveFilterConditionName activeFilterConditionName) {
    if ((condition != null) && !condition.isEmpty()) {
      sb.append(activeFilterConditionName.name() + ",");
    }
  }

  @Override
  public String getUrl() {
    // TODO zs.cz check SearcherValue! Maybe jql string not null or empty???
    StringBuilder sb = new StringBuilder();

    appendActiveFilterCondition(sb, filterCondition.getProjectIds(),
        ActiveFilterConditionName.PROJECT);
    appendActiveFilterCondition(sb, filterCondition.getIssueAffectedVersions(),
        ActiveFilterConditionName.AFFECTED_VERSION);
    appendActiveFilterCondition(sb, filterCondition.getIssueAssignees(),
        ActiveFilterConditionName.ASSIGNEE);
    appendActiveFilterCondition(sb, filterCondition.getIssueComponents(),
        ActiveFilterConditionName.COMPONENT);
    appendActiveFilterCondition(sb, filterCondition.getIssueEpicLinkIssueIds(),
        ActiveFilterConditionName.EPIC_LINK);
    appendActiveFilterCondition(sb, filterCondition.getIssueFixedVersions(),
        ActiveFilterConditionName.FIXED_VERSION);
    appendActiveFilterCondition(sb, filterCondition.getIssueKeys(),
        ActiveFilterConditionName.ISSUE);
    appendActiveFilterCondition(sb, filterCondition.getIssuePriorityIds(),
        ActiveFilterConditionName.PRIORITY);
    appendActiveFilterCondition(sb, filterCondition.getIssueReporters(),
        ActiveFilterConditionName.REPORTER);
    appendActiveFilterCondition(sb, filterCondition.getIssueResolutionIds(),
        ActiveFilterConditionName.RESOLUTION);
    appendActiveFilterCondition(sb, filterCondition.getIssueStatusIds(),
        ActiveFilterConditionName.STATUS);
    appendActiveFilterCondition(sb, filterCondition.getIssueTypeIds(),
        ActiveFilterConditionName.TYPE);
    appendActiveFilterCondition(sb, filterCondition.getLabels(),
        ActiveFilterConditionName.LABEL);
    appendActiveFilterCondition(sb, filterCondition.getIssueCreateDate(),
        ActiveFilterConditionName.CREATED);
    appendActiveFilterCondition(sb, filterCondition.getIssueEpicName(),
        ActiveFilterConditionName.EPIC_NAME);

    List<String> users = new ArrayList<>(filterCondition.getUsers());
    boolean removedNoneUser = users.remove(PickerUserDTO.NONE_USER_NAME);
    appendActiveFilterCondition(sb, users, ActiveFilterConditionName.USER);

    List<String> groups = new ArrayList<>(filterCondition.getGroups());
    groups.remove("-1"); // FIXME zs.cz read from constants ConverterUtil after merge!
    appendActiveFilterCondition(sb, groups, ActiveFilterConditionName.GROUP);

    String activeFilterCondition = sb.toString();

    UserSelection userSelection;
    if (removedNoneUser) {
      userSelection = UserSelection.GROUP;
    } else if ((users.size() == 1) && PickerUserDTO.CURRENT_USER_NAME.equals(users.get(0))) {
      userSelection = UserSelection.ONLY_OWN;
    } else {
      userSelection = UserSelection.USER;
    }

    return new PiwikUrlBuilder(ACTION_URL, PiwikPropertiesUtil.PIWIK_REPORTING_SITEID,
        pluginId, hashUserId)
            .addEventCategory(EVENT_CATEGORY)
            .addEventAction(EVENT_ACTION)
            .addCustomDimesionSelectedActiveTab(selectedActiveTab)
            .addCustomDimesionSearcherValue(SearcherValue.BASIC)
            .addCustomDimesionSelectedWorklogDetailColumns(selectedWorklogDetailColumns)
            .addCustomDimesionActiveFilterCondition(activeFilterCondition)
            .addCustomDimesionUserSelecton(userSelection)
            .buildUrl();
  }

}
