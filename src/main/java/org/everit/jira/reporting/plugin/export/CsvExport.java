package org.everit.jira.reporting.plugin.export;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class CsvExport {

  private static final String DEFAULT_SEPARATOR = ",";

  private ArrayList<ArrayList<String>> csvData;

  private HSSFFormulaEvaluator evaluator;

  private DataFormatter formatter = new DataFormatter(true);

  private int maxRowWidth;

  private HSSFWorkbook workbook;

  public CsvExport(final HSSFWorkbook workbook) {
    this.workbook = workbook;
    evaluator = workbook.getCreationHelper().createFormulaEvaluator();
  }

  public ByteArrayOutputStream convertToCSV() {
    HSSFSheet sheet = null;

    HSSFRow row = null;
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
    try {
      return saveCSVFile();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  private String escapeEmbeddedCharacters(final String field) {
    StringBuffer buffer = null;

    if (field.contains("\"")) {
      buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
      buffer.insert(0, "\"");
      buffer.append("\"");
    } else {

      buffer = new StringBuffer(field);
      if (((buffer.indexOf(DEFAULT_SEPARATOR)) > -1) ||
          ((buffer.indexOf("\n")) > -1)) {
        buffer.insert(0, "\"");
        buffer.append("\"");
      }
    }
    return (buffer.toString().trim());
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

  private ByteArrayOutputStream saveCSVFile()
      throws FileNotFoundException, IOException {

    BufferedWriter bw = null;
    ArrayList<String> line = null;
    StringBuffer buffer = null;
    String csvLineElement = null;
    try {
      ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
      bw = new BufferedWriter(new OutputStreamWriter(arrayOutputStream));

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
      return arrayOutputStream;
    } finally {
      if (bw != null) {
        bw.flush();
        bw.close();
      }
    }
  }
}
