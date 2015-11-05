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

package org.everit.jira.timetracker.plugin;

import java.util.List;
import java.util.regex.Pattern;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

/**
 * The Jira Timetracker plugin utils class.
 */
public final class JiraTimetrackerUtil {

  /**
   * The plugin calendar both type code.
   */
  public static final int BOTH_TYPE_CALENDAR_CODE = 3;

  public static final int DAY_INTERVAL = 0;

  /**
   * The plugin calendar inline code.
   */
  public static final int INLINE_CALENDAR_CODE = 2;

  /**
   * Monday first day of the week.
   */
  public static final int MONDAY_CALENDAR_FDOW = 1;

  public static final int MONTH_INTERVAL = 2;

  /**
   * The plugin calendar popup code.
   */
  public static final int POPUP_CALENDAR_CODE = 1;

  /**
   * Sunday first day of the week.
   */
  public static final int SUNDAY_CALENDAR_FDOW = 0;

  public static final int WEEK_INTERVAL = 1;

  /**
   * Check the issue original estimated time. If null then the original estimated time wasn't
   * specified, else compare the spent time with the original estimated time.
   *
   * @param issue
   *          The issue.
   * @return True if not specified, bigger or equals whit spent time else false.
   */
  public static boolean checkIssueEstimatedTime(final MutableIssue issue,
      final List<Pattern> collectorIssueIds) {
    String issueKey = issue.getKey();
    if (collectorIssueIds != null) {
      for (Pattern issuePattern : collectorIssueIds) {
        // check matches
        boolean isCollectorIssue = issuePattern.matcher(issueKey).matches();
        if (isCollectorIssue) {
          return true;
        }
      }
    }
    Long estimated = issue.getEstimate();
    Status issueStatus = issue.getStatusObject();
    String issueStatusId = issueStatus.getId();
    if (((estimated == null) || (estimated == 0)) && !"6".equals(issueStatusId)) {
      return false;
    }
    return true;
  }

  /**
   * Check the user is logged or not.
   *
   * @return True if we have logged user else false.
   */
  public static boolean isUserLogged() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    if (user == null) {
      return false;
    }
    return true;
  }

  private JiraTimetrackerUtil() {
  }

}
