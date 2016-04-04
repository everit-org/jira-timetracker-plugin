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
package org.everit.jira.reporting.plugin.export.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Define Worklog Details report columns.
 */
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
    List<String> allColumns = new ArrayList<String>();
    allColumns.add(AFFECTED_VERIONS);
    allColumns.add(ASSIGNEE);
    allColumns.add(COMPONENTS);
    allColumns.add(CREATED);
    allColumns.add(ESTIMATED);
    allColumns.add(FIX_VERSIONS);
    allColumns.add(ISSUE_KEY);
    allColumns.add(ISSUE_SUMMARY);
    allColumns.add(PRIORITY);
    allColumns.add(PROJECT);
    allColumns.add(PROJECT_DESCRIPTION);
    allColumns.add(REMAINING);
    allColumns.add(REPORTER);
    allColumns.add(RESOLUTION);
    allColumns.add(START_TIME);
    allColumns.add(STATUS);
    allColumns.add(TIME_SPENT);
    allColumns.add(TYPE);
    allColumns.add(UPDATED);
    allColumns.add(USER);
    allColumns.add(WORKLOG_CREATED);
    allColumns.add(WORKLOG_DESCRIPTION);
    allColumns.add(WORKLOG_UPDATED);

    ALL_COLUMNS = Collections.unmodifiableList(allColumns);
  }

  private WorklogDetailsColumns() {
  }

}
