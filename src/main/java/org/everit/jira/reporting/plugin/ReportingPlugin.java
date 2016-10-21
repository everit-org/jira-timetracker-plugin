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
package org.everit.jira.reporting.plugin;

import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;

/**
 * The ReportingPlugin interface.
 */
public interface ReportingPlugin {

  IssueSummaryReportDTO getIssueSummaryReport(ReportSearchParam reportSearchParam);

  ProjectSummaryReportDTO getProjectSummaryReport(ReportSearchParam reportSearchParam);

  UserSummaryReportDTO getUserSummaryReport(ReportSearchParam reportSearchParam);

  WorklogDetailsReportDTO getWorklogDetailsReport(ReportSearchParam reportSearchParam,
      OrderBy orderBy);

}
