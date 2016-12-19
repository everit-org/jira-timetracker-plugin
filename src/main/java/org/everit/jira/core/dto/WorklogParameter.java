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
package org.everit.jira.core.dto;

import org.everit.jira.core.RemainingEstimateType;
import org.everit.jira.core.impl.DateTimeServer;

/**
 * Contains information of worklog to create or edit process.
 */
public class WorklogParameter {

  private final String comment;

  private final DateTimeServer date;

  private final String issueKey;

  private final String optinalValue;

  private final RemainingEstimateType remainingEstimateType;

  private final String timeSpent;

  /**
   * Simple constructor.
   *
   * @param issueKey
   *          the key of the issue.
   * @param comment
   *          the note of the worklog.
   * @param date
   *          the date of the worklog.
   * @param timeSpent
   *          the spent time in the worklog (JIRA format: 1h 30m).
   * @param optinalValue
   *          the optional value of the remaining estimate type. Example: newEstimate or
   *          adjustEstimate value.
   * @param remainingEstimateType
   *          the type of the remaining estimate.
   */
  public WorklogParameter(final String issueKey, final String comment, final DateTimeServer date,
      final String timeSpent, final String optinalValue,
      final RemainingEstimateType remainingEstimateType) {
    this.issueKey = issueKey;
    this.comment = comment;
    this.date = date;
    this.timeSpent = timeSpent;
    this.optinalValue = optinalValue;
    this.remainingEstimateType = remainingEstimateType;
  }

  public String getComment() {
    return comment;
  }

  public DateTimeServer getDate() {
    return date != null ? date : null;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getOptinalValue() {
    return optinalValue;
  }

  public RemainingEstimateType getRemainingEstimateType() {
    return remainingEstimateType;
  }

  public String getTimeSpent() {
    return timeSpent;
  }

}
