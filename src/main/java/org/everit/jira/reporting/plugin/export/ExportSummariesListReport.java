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

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.dto.IssueSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryDTO;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.export.column.IssueSummaryColumns;
import org.everit.jira.reporting.plugin.export.column.ProjectSummaryColumns;
import org.everit.jira.reporting.plugin.export.column.UserSummaryColumns;
import org.everit.jira.reporting.plugin.query.IssueSummaryReportQuery;
import org.everit.jira.reporting.plugin.query.ProjectSummaryReportQuery;
import org.everit.jira.reporting.plugin.query.UserSummaryReportQuery;

/**
 * Class that export summaries list report (project summary, issue summary, user summary).
 */
public class ExportSummariesListReport extends AbstractExportListReport {

  private static final String ISSUE_SUMMARY_PREFIX = "jtrp.report.is.col.";

  private static final String PROJECT_SUMMARY_PREFIX = "jtrp.report.ps.col.";

  private static final String USER_SUMMARY_PREFIX = "jtrp.report.us.col.";

  public ExportSummariesListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam) {
    super(querydslSupport, reportSearchParam);
  }

  private void addIssueSummarySheet(final HSSFWorkbook workbook) {
    HSSFSheet issueSummarySheet = workbook.createSheet("Issue Summary");
    int rowIndex = 0;

    rowIndex = insertIssueSummaryHeaderRow(rowIndex, issueSummarySheet);

    List<IssueSummaryDTO> issueSummary =
        querydslSupport.execute(new IssueSummaryReportQuery(reportSearchParam)
            .buildQuery());

    for (IssueSummaryDTO issueSummaryDTO : issueSummary) {
      rowIndex = insertIssueSummaryBodyRow(rowIndex, issueSummarySheet, issueSummaryDTO);
    }
  }

  private void addProjectSummarySheet(final HSSFWorkbook workbook) {
    HSSFSheet projectSummarySheet = workbook.createSheet("Project Summary");
    int rowIndex = 0;

    rowIndex = insertProjectSummaryHeaderRow(rowIndex, projectSummarySheet);

    List<ProjectSummaryDTO> projectSummary =
        querydslSupport.execute(new ProjectSummaryReportQuery(reportSearchParam)
            .buildQuery());

    for (ProjectSummaryDTO projectSummaryDTO : projectSummary) {
      rowIndex = insertProjectSummaryBodyRow(rowIndex, projectSummarySheet, projectSummaryDTO);
    }
  }

  private void addUserSummarySheet(final HSSFWorkbook workbook) {
    HSSFSheet userSummarySheet = workbook.createSheet("User Summary");
    int rowIndex = 0;

    rowIndex = insertUserSummaryHeaderRow(rowIndex, userSummarySheet);

    List<UserSummaryDTO> userSummary =
        querydslSupport.execute(new UserSummaryReportQuery(reportSearchParam)
            .buildQuery());
    for (UserSummaryDTO userSummaryDTO : userSummary) {
      rowIndex = insertUserSummaryBodyRow(rowIndex, userSummarySheet, userSummaryDTO);
    }
  }

  @Override
  protected void appendContent(final HSSFWorkbook workbook) {
    addProjectSummarySheet(workbook);
    addIssueSummarySheet(workbook);
    addUserSummarySheet(workbook);

  }

  private int insertIssueSummaryBodyRow(final int rowIndex, final HSSFSheet issueSummarySheet,
      final IssueSummaryDTO issueSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = issueSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueKey());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueSummary());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getIssueTypeName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getPriorityName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getStatusName());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getAssignee());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getOrginalEstimatedSum());
    columnIndex = insertBodyCell(row, columnIndex, issueSummaryDTO.getReaminingTimeSum());
    insertBodyCell(row, columnIndex, issueSummaryDTO.getWorkloggedTimeSum());

    return newRowIndex;
  }

  private int insertIssueSummaryHeaderRow(final int rowIndex,
      final HSSFSheet issueSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = issueSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ISSUE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ISSUE_SUMMARY));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.TYPE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.PRIORITY));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.STATUS));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ASSIGNEE));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.REMAINING));
    insertHeaderCell(row, columnIndex,
        i18nHelper.getText(ISSUE_SUMMARY_PREFIX + IssueSummaryColumns.TOTAL_LOGGED));

    return newRowIndex;
  }

  private int insertProjectSummaryBodyRow(final int rowIndex, final HSSFSheet projectSummarySheet,
      final ProjectSummaryDTO projectSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, projectSummaryDTO.getProjectName());
    columnIndex = insertBodyCell(row, columnIndex, projectSummaryDTO.getProjectSummary());

    long estimateTimeSum = projectSummaryDTO.getIssuesOrginalEstimatedSum();
    long loggedTimeSum = projectSummaryDTO.getWorkloggedTimeSum();
    long remainingTimeSum = projectSummaryDTO.getIssuesReaminingTimeSum();
    long loggedVsEstimated = loggedTimeSum - estimateTimeSum;
    long expectedTotal = loggedTimeSum + remainingTimeSum;
    long expectedTotalVsEstimated = expectedTotal - estimateTimeSum;

    columnIndex = insertBodyCell(row, columnIndex, estimateTimeSum);
    columnIndex = insertBodyCell(row, columnIndex, loggedTimeSum);
    columnIndex = insertBodyCell(row, columnIndex, remainingTimeSum);
    columnIndex = insertBodyCell(row, columnIndex, loggedVsEstimated);
    columnIndex = insertBodyCell(row, columnIndex, expectedTotal);
    insertBodyCell(row, columnIndex, expectedTotalVsEstimated);

    return newRowIndex;
  }

  private int insertProjectSummaryHeaderRow(final int rowIndex,
      final HSSFSheet projectSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = projectSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.PROJECT));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.PROJECT_DESCRIPTION));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.ESTIMATED));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.TOTAL_LOGGED));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.REMAINING));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.LOGGED_VS_ESTIMATED));
    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX + ProjectSummaryColumns.EXPECTED_TOTAL));
    insertHeaderCell(row, columnIndex,
        i18nHelper.getText(PROJECT_SUMMARY_PREFIX
            + ProjectSummaryColumns.EXPECTED_TOTAL_VS_ESTIMATED));

    return newRowIndex;
  }

  private int insertUserSummaryBodyRow(final int rowIndex, final HSSFSheet userSummarySheet,
      final UserSummaryDTO userSummaryDTO) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = userSummarySheet.createRow(newRowIndex++);

    columnIndex = insertBodyCell(row, columnIndex, userSummaryDTO.getUserDisplayName());
    insertBodyCell(row, columnIndex, userSummaryDTO.getWorkloggedTimeSum());
    return newRowIndex;
  }

  private int insertUserSummaryHeaderRow(final int rowIndex,
      final HSSFSheet userSummarySheet) {
    int newRowIndex = rowIndex;
    int columnIndex = 0;

    HSSFRow row = userSummarySheet.createRow(newRowIndex++);

    columnIndex = insertHeaderCell(row, columnIndex,
        i18nHelper.getText(USER_SUMMARY_PREFIX + UserSummaryColumns.USER));
    insertHeaderCell(row, columnIndex,
        i18nHelper.getText(USER_SUMMARY_PREFIX + UserSummaryColumns.TOTAL_LOGGED));

    return newRowIndex;
  }

}
