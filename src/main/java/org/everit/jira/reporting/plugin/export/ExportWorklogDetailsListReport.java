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

import java.sql.Timestamp;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.query.WorklogDetailsReportQueryBuilder;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

/**
 * Class that export worklog details list report.
 */
public class ExportWorklogDetailsListReport extends AbstractExportListReport {

  private static final String WORKLOG_DETAILS_PREFIX = "jtrp.report.export.wd.col.";

  private OrderBy orderBy;

  private int rowIndex = 0;

  private List<String> selectedWorklogDetailsColumns;

  /**
   * Simple constructor.
   *
   * @param querydslSupport
   *          the {@link QuerydslSupport} instance.
   * @param selectedWorklogDetailsColumns
   *          the list of the selected columns.
   * @param reportSearchParam
   *          the {@link ReportSearchParam} object, that contains parameters to filter condition.
   * @param notBrowsableProjectKeys
   *          the list of not browsable project keys.
   * @param orderBy
   *          the {@link OrderBy} object.
   * @param userSettings
   *          the user settings.
   */
  public ExportWorklogDetailsListReport(final QuerydslSupport querydslSupport,
      final List<String> selectedWorklogDetailsColumns, final ReportSearchParam reportSearchParam,
      final List<String> notBrowsableProjectKeys, final OrderBy orderBy,
      final TimeTrackerUserSettings userSettings) {
    super(querydslSupport, reportSearchParam, notBrowsableProjectKeys, userSettings);
    this.selectedWorklogDetailsColumns = selectedWorklogDetailsColumns;
    this.orderBy = orderBy;
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    HSSFSheet worklogDetailsSheet = workbook.createSheet("Worklog details");

    insertHeaderRow(worklogDetailsSheet);

    List<WorklogDetailsDTO> worklogDetails =
        querydslSupport.execute(new WorklogDetailsReportQueryBuilder(reportSearchParam,
            orderBy)
                .buildQuery());
    for (WorklogDetailsDTO worklogDetail : worklogDetails) {
      worklogDetail.setIssueCreated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getIssueCreated()));
      worklogDetail.setIssueUpdated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getIssueUpdated()));
      worklogDetail.setWorklogCreated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getWorklogCreated()));
      worklogDetail.setWorklogStartDate(
          DateTimeConverterUtil
              .addTimeZoneToTimestamp(worklogDetail.getWorklogStartDate()));
      worklogDetail.setWorklogUpdated(
          DateTimeConverterUtil.addTimeZoneToTimestamp(worklogDetail.getWorklogUpdated()));
    }
    for (WorklogDetailsDTO worklogDetailsDTO : worklogDetails) {
      insertBodyRow(worklogDetailsSheet, worklogDetailsDTO);
    }
  }

  private boolean containsColumn(final String columnName) {
    return selectedWorklogDetailsColumns.contains(columnName);
  }

  private void insertBodyRow(final HSSFSheet worklogDetailsSheet,
      final WorklogDetailsDTO worklogDetailsDTO) {
    HSSFRow row = worklogDetailsSheet.createRow(rowIndex++);
    int columnIndex = 0;

    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.PROJECT, worklogDetailsDTO.getProjectName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.PROJECT_DESCRIPTION, worklogDetailsDTO.getProjectDescription());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.ISSUE_KEY, worklogDetailsDTO.getIssueKey());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.ISSUE_SUMMARY, worklogDetailsDTO.getIssueSummary());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.TYPE, worklogDetailsDTO.getIssueTypeName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.STATUS, worklogDetailsDTO.getIssueStatusName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.PRIORITY, worklogDetailsDTO.getPriorityName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.ASSIGNEE, worklogDetailsDTO.getIssueAssignee());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.REPORTER, worklogDetailsDTO.getIssueReporter());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex, WorklogDetailsColumns.ESTIMATED,
        worklogInSec(worklogDetailsDTO.getIssueOriginalEstimate()));
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex, WorklogDetailsColumns.REMAINING,
        worklogInSec(worklogDetailsDTO.getIssueRemainingEstimate()));
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.CREATED, worklogDetailsDTO.getIssueCreated());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.UPDATED, worklogDetailsDTO.getIssueUpdated());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.COMPONENTS, worklogDetailsDTO.getIssueComponents());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.AFFECTED_VERIONS, worklogDetailsDTO.getIssueAffectedVersions());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.FIX_VERSIONS, worklogDetailsDTO.getIssueFixedVersions());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.RESOLUTION, worklogDetailsDTO.getResolutionName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.WORKLOG_DESCRIPTION, worklogDetailsDTO.getWorklogBody());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.ISSUE_EPIC_NAME, worklogDetailsDTO.getIssueEpicName());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.ISSUE_EPIC_LINK, worklogDetailsDTO.getIssueEpicLink());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.USER, worklogDetailsDTO.getWorklogUser());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.START_TIME, worklogDetailsDTO.getWorklogStartDate());
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.TIME_SPENT, worklogInSec(worklogDetailsDTO.getWorklogTimeWorked()));
    columnIndex = insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.WORKLOG_CREATED, worklogDetailsDTO.getWorklogCreated());
    insertWorklogDetailsBodyCell(row, columnIndex,
        WorklogDetailsColumns.WORKLOG_UPDATED, worklogDetailsDTO.getWorklogUpdated());

  }

  private void insertHeaderRow(final HSSFSheet worklogDetailsSheet) {
    HSSFRow row = worklogDetailsSheet.createRow(rowIndex++);
    int columnIndex = 0;

    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.PROJECT);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.PROJECT_DESCRIPTION);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.ISSUE_KEY);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.ISSUE_SUMMARY);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.TYPE);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.STATUS);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.PRIORITY);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.ASSIGNEE);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.REPORTER);
    columnIndex =
        insertWorklogDetailsHeaderCellInSec(row, columnIndex, WorklogDetailsColumns.ESTIMATED);
    columnIndex =
        insertWorklogDetailsHeaderCellInSec(row, columnIndex, WorklogDetailsColumns.REMAINING);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.CREATED);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.UPDATED);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.COMPONENTS);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.AFFECTED_VERIONS);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.FIX_VERSIONS);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.RESOLUTION);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.WORKLOG_DESCRIPTION);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.ISSUE_EPIC_NAME);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.ISSUE_EPIC_LINK);
    columnIndex = insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.USER);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.START_TIME);
    columnIndex =
        insertWorklogDetailsHeaderCellInSec(row, columnIndex, WorklogDetailsColumns.TIME_SPENT);
    columnIndex =
        insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.WORKLOG_CREATED);
    insertWorklogDetailsHeaderCell(row, columnIndex, WorklogDetailsColumns.WORKLOG_UPDATED);

  }

  private int insertWorklogDetailsBodyCell(final HSSFRow headerRow, final int columnIndex,
      final String column, final List<String> value) {
    if (containsColumn(column)) {
      return insertBodyCell(headerRow, columnIndex, value);
    }
    return columnIndex;
  }

  private int insertWorklogDetailsBodyCell(final HSSFRow headerRow, final int columnIndex,
      final String column, final String value) {
    if (containsColumn(column)) {
      return insertBodyCell(headerRow, columnIndex, value);
    }
    return columnIndex;
  }

  private int insertWorklogDetailsBodyCell(final HSSFRow headerRow, final int columnIndex,
      final String column, final Timestamp value) {
    if (containsColumn(column)) {
      return insertBodyCell(headerRow, columnIndex, value);
    }
    return columnIndex;
  }

  private int insertWorklogDetailsHeaderCell(final HSSFRow headerRow, final int columnIndex,
      final String column) {
    if (containsColumn(column)) {
      return insertHeaderCell(headerRow, columnIndex,
          i18nHelper.getText(WORKLOG_DETAILS_PREFIX + column));
    }
    return columnIndex;
  }

  private int insertWorklogDetailsHeaderCellInSec(final HSSFRow headerRow, final int columnIndex,
      final String column) {
    if (containsColumn(column)) {
      return insertHeaderCellInSec(headerRow, columnIndex,
          i18nHelper.getText(WORKLOG_DETAILS_PREFIX + column));
    }
    return columnIndex;
  }

}
