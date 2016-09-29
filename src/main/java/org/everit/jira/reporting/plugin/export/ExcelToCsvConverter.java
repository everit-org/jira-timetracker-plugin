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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

/**
 *
 * Converter class to convert excel into CSV format.
 *
 */
public class ExcelToCsvConverter {

  private static final String DEFAULT_SEPARATOR = ",";

  private ArrayList<ArrayList<String>> csvData;

  private HSSFFormulaEvaluator evaluator;

  private DataFormatter formatter = new DataFormatter(true);

  private int maxRowWidth;

  private HSSFWorkbook workbook;

  public ExcelToCsvConverter(final HSSFWorkbook workbook) {
    this.workbook = workbook;
    evaluator = workbook.getCreationHelper().createFormulaEvaluator();
  }

  private void convertToCSV() {
    HSSFSheet sheet;
    HSSFRow row;
    int lastRowNum = 0;
    csvData = new ArrayList<>();
    int numSheets = workbook.getNumberOfSheets();
    for (int i = 0; i < numSheets; i++) {
      sheet = workbook.getSheetAt(i);
      if (sheet.getPhysicalNumberOfRows() > 0) {
        lastRowNum = sheet.getLastRowNum();
        for (int j = 0; j <= lastRowNum; j++) {
          row = sheet.getRow(j);
          rowToCSV(row);
        }
      }
    }
  }

  private String escapeEmbeddedCharacters(final String field) {
    StringBuffer buffer;
    if (field.contains("\"")) {
      buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
      buffer.insert(0, "\"");
      buffer.append("\"");
    } else {
      buffer = new StringBuffer(field);
      if (((buffer.indexOf(DEFAULT_SEPARATOR)) > -1)
          || ((buffer.indexOf("\n")) > -1)) {
        buffer.insert(0, "\"");
        buffer.append("\"");
      }
    }
    return (buffer.toString().trim());
  }

  /**
   * Print the converted CSV in the {@link ByteArrayOutputStream}.
   */
  public void printCSV(final ByteArrayOutputStream arrayOutputStream)
      throws IOException {
    convertToCSV();
    ArrayList<String> line = null;
    StringBuffer buffer = null;
    String csvLineElement = null;
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(arrayOutputStream, Charset.forName("UTF-8").newEncoder()))) {
      for (int i = 0; i < csvData.size(); i++) {
        buffer = new StringBuffer();
        line = csvData.get(i);
        for (int j = 0; j < maxRowWidth; j++) {
          if (line.size() > j) {
            csvLineElement = line.get(j);
            if (csvLineElement != null) {
              buffer.append(escapeEmbeddedCharacters(
                  csvLineElement));
            }
          }
          if (j < (maxRowWidth - 1)) {
            buffer.append(DEFAULT_SEPARATOR);
          }
        }

        bw.write(buffer.toString().trim());
        if (i < (csvData.size() - 1)) {
          bw.newLine();
        }
      }
    }

  }

  private void rowToCSV(final HSSFRow row) {
    HSSFCell cell = null;
    int lastCellNum = 0;
    ArrayList<String> csvLine = new ArrayList<>();
    if (row != null) {
      lastCellNum = row.getLastCellNum();
      for (int i = 0; i <= lastCellNum; i++) {
        cell = row.getCell(i);
        if (cell == null) {
          csvLine.add("");
        } else {
          if (cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
            csvLine.add(formatter.formatCellValue(cell));
          } else {
            csvLine.add(formatter.formatCellValue(cell, evaluator));
          }
        }
      }
      if (lastCellNum > maxRowWidth) {
        maxRowWidth = lastCellNum;
      }
    }
    csvData.add(csvLine);
  }
}
