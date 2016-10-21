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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.settings.dto.TimeTrackerUserSettings;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

/**
 * Helper class to export list reports to XLS.
 */
public abstract class AbstractExportListReport {

  private HSSFCellStyle bodyCellStyle;

  private HSSFCellStyle headerCellStyle;

  protected I18nHelper i18nHelper;

  protected boolean isWorklogInSec;

  protected List<String> notBrowsableProjectKeys;

  protected QuerydslSupport querydslSupport;

  protected ReportSearchParam reportSearchParam;

  protected TimeTrackerUserSettings userSettings;

  /**
   * Simple constructor.
   *
   * @param querydslSupport
   *          the {@link QuerydslSupport} instance.
   * @param reportSearchParam
   *          the {@link ReportSearchParam} object, that contains parameters to filter condition.
   * @param notBrowsableProjectKeys
   *          the list of not browsable project keys.
   * @param userSettings
   *          the user settings.
   */
  public AbstractExportListReport(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam, final List<String> notBrowsableProjectKeys,
      final TimeTrackerUserSettings userSettings) {
    this.querydslSupport = querydslSupport;
    this.reportSearchParam = reportSearchParam;
    this.notBrowsableProjectKeys = notBrowsableProjectKeys;
    this.userSettings = userSettings;
    i18nHelper = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
  }

  protected abstract void appendContent(HSSFWorkbook workbook);

  private void appendNotBrowsalbeProjectsSheet(final HSSFWorkbook workbook) {
    if (!notBrowsableProjectKeys.isEmpty()) {
      HSSFSheet noBrowsableProjectsSheet = workbook.createSheet("No Browsable Projects");
      int rowIndex = 0;
      HSSFRow headerRow = noBrowsableProjectsSheet.createRow(rowIndex++);
      insertHeaderCell(headerRow, 0, i18nHelper.getText("jtrp.report.projectKeys"));
      for (String projectKey : notBrowsableProjectKeys) {
        HSSFRow bodyRow = noBrowsableProjectsSheet.createRow(rowIndex++);
        insertBodyCell(bodyRow, 0, projectKey);
      }
    }
  }

  private void createBodyCellStyle(final HSSFWorkbook workbook) {
    bodyCellStyle = workbook.createCellStyle();
    bodyCellStyle.setWrapText(true);
  }

  private void createHeaderCellStyle(final HSSFWorkbook workbook) {
    headerCellStyle = workbook.createCellStyle();
    HSSFFont headerFont = workbook.createFont();
    headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    headerCellStyle.setFont(headerFont);
    headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
    headerCellStyle.setWrapText(true);
  }

  /**
   * Export list report to Workbook (XLS).
   */
  public HSSFWorkbook exportToXLS() {
    HSSFWorkbook workbook = new HSSFWorkbook();
    createHeaderCellStyle(workbook);
    createBodyCellStyle(workbook);

    appendContent(workbook);

    appendNotBrowsalbeProjectsSheet(workbook);

    return workbook;
  }

  /**
   * Insert body cell to workbook. The cell value is "v1; v2; v3".
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex,
      final List<String> value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    String cValue = "";
    if (value.size() == 1) {
      cValue = value.get(0);
    } else {
      StringBuffer sb = new StringBuffer();
      for (String v : value) {
        sb.append(v);
        sb.append(", ");
      }
      cValue = sb.toString();
    }
    cell.setCellValue(cValue);
    return newColumnIndex;
  }

  /**
   * Insert body cell to workbook.
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex, final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

  /**
   * Insert body cell to workbook.
   *
   * @param bodyRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the List that contains cell value.
   * @return the next column index.
   */
  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex,
      final Timestamp value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(DateTimeConverterUtil.dateToString(value));
    }
    return newColumnIndex;
  }

  /**
   * Insert header cell to workbook.
   *
   * @param headerRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the cell value.
   * @return the next column index.
   */
  protected int insertHeaderCell(final HSSFRow headerRow, final int columnIndex,
      final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = headerRow.createCell(newColumnIndex++);
    cell.setCellStyle(headerCellStyle);
    cell.setCellValue(value);
    return newColumnIndex;
  }

  /**
   * Insert header cell to workbook in seconds.
   *
   * @param headerRow
   *          the row where to insert cell.
   * @param columnIndex
   *          the columns in row.
   * @param value
   *          the cell value.
   * @return the next column index.
   */
  protected int insertHeaderCellInSec(final HSSFRow headerRow, final int columnIndex,
      final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = headerRow.createCell(newColumnIndex++);
    cell.setCellStyle(headerCellStyle);
    if (userSettings.getWorklogTimeInSeconds()) {
      cell.setCellValue(value + " (s)");
    } else {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

  /**
   * Worklog in seconds or default.
   *
   * @param worklog
   *          worklog in seconds
   */
  protected String worklogInSec(final Long worklog) {
    DurationFormatter durationFormatter = new DurationFormatter();
    isWorklogInSec = true;
    if (!userSettings.getWorklogTimeInSeconds()) {
      isWorklogInSec = false;
    }
    if (worklog != null) {
      if (isWorklogInSec) {
        return worklog.toString();
      }
      return durationFormatter.exactDuration(worklog);
    }
    return "";
  }

}
