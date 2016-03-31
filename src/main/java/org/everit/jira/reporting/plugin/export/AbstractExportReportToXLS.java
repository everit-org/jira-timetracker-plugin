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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

public abstract class AbstractExportReportToXLS {

  private HSSFCellStyle bodyCellStyle;

  private HSSFCellStyle headerCellStyle;

  protected I18nHelper i18nHelper;

  protected QuerydslSupport querydslSupport;

  protected ReportSearchParam reportSearchParam;

  public AbstractExportReportToXLS(final QuerydslSupport querydslSupport,
      final ReportSearchParam reportSearchParam) {
    this.querydslSupport = querydslSupport;
    this.reportSearchParam = reportSearchParam;
    i18nHelper = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
  }

  protected abstract void appendContent(HSSFWorkbook workbook);

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

  public void export() {
    HSSFWorkbook workbook = new HSSFWorkbook();
    createHeaderCellStyle(workbook);
    createBodyCellStyle(workbook);

    appendContent(workbook);

    writeWorkbookToFile(workbook);
  }

  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex,
      final List<String> value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    String cValue = "";
    if (value.size() == 1) {
      cValue = value.get(0);
    } else {
      for (String v : value) {
        cValue += v + ", ";
      }
    }
    cell.setCellValue(cValue);
    return newColumnIndex;
  }

  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex, final Long value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

  protected int insertBodyCell(final HSSFRow bodyRow, final int columnIndex, final String value) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = bodyRow.createCell(newColumnIndex++);
    cell.setCellStyle(bodyCellStyle);
    if (value != null) {
      cell.setCellValue(value);
    }
    return newColumnIndex;
  }

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

  protected int insertHeaderCell(final HSSFRow headerRow, final int columnIndex,
      final String columnName) {
    int newColumnIndex = columnIndex;
    HSSFCell cell = headerRow.createCell(newColumnIndex++);
    cell.setCellStyle(headerCellStyle);
    cell.setCellValue(columnName);
    return newColumnIndex;
  }

  protected abstract void writeWorkbookToFile(final HSSFWorkbook workbook);
}
