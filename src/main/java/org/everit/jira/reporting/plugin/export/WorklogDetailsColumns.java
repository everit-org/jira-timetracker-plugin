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
package org.everit.jira.reporting.plugin.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorklogDetailsColumns {

  public static final String AFFECTED_VERIONS = "affectedVersions";

  public static final List<String> ALL_COLUMNS;

  public static final String ASSIGNEE = "assignee";

  public static final String COMPONENTS = "components";

  public static final String CREATED = "created";

  public static final String ESTIMATED = "estimated";

  public static final String FIX_VERSIONS = "fixVersions";

  public static final String ISSUE_KEY = "issueKey";

  public static final String ISSUE_SUMMARY = "issueSummary";

  public static final String PRIORITY = "priority";

  public static final String PROJECT = "project";

  public static final String PROJECT_DESCRIPTION = "projectDescription";

  public static final String REMAINING = "remainingTime";

  public static final String REPORTER = "reporter";

  public static final String RESOLUTION = "resolution";

  public static final String START_TIME = "startTime";

  public static final String STATUS = "status";

  public static final String TIME_SPENT = "timeSpent";

  public static final String TYPE = "type";

  public static final String UPDATED = "updated";

  public static final String USER = "user";

  public static final String WORKLOG_CREATED = "worklogCreated";

  public static final String WORKLOG_DESCRIPTION = "worklogDescription";

  public static final String WORKLOG_UPDATED = "worklogUpdated";

  static {
    ALL_COLUMNS = new ArrayList<String>();
    ALL_COLUMNS.add(AFFECTED_VERIONS);
    ALL_COLUMNS.add(ASSIGNEE);
    ALL_COLUMNS.add(COMPONENTS);
    ALL_COLUMNS.add(CREATED);
    ALL_COLUMNS.add(ESTIMATED);
    ALL_COLUMNS.add(FIX_VERSIONS);
    ALL_COLUMNS.add(ISSUE_KEY);
    ALL_COLUMNS.add(ISSUE_SUMMARY);
    ALL_COLUMNS.add(PRIORITY);
    ALL_COLUMNS.add(PROJECT);
    ALL_COLUMNS.add(PROJECT_DESCRIPTION);
    ALL_COLUMNS.add(REMAINING);
    ALL_COLUMNS.add(REPORTER);
    ALL_COLUMNS.add(RESOLUTION);
    ALL_COLUMNS.add(START_TIME);
    ALL_COLUMNS.add(STATUS);
    ALL_COLUMNS.add(TIME_SPENT);
    ALL_COLUMNS.add(TYPE);
    ALL_COLUMNS.add(UPDATED);
    ALL_COLUMNS.add(USER);
    ALL_COLUMNS.add(WORKLOG_CREATED);
    ALL_COLUMNS.add(WORKLOG_DESCRIPTION);
    ALL_COLUMNS.add(WORKLOG_UPDATED);
    Collections.unmodifiableList(ALL_COLUMNS);
  }

  private WorklogDetailsColumns() {
  }

}
