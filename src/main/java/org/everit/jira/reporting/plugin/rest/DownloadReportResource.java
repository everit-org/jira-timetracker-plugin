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
package org.everit.jira.reporting.plugin.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.analytics.AnalyticsSender;
import org.everit.jira.analytics.event.ExportSummaryReportEvent;
import org.everit.jira.analytics.event.ExportWorklogDetailsReportEvent;
import org.everit.jira.analytics.event.ExportWorklogDetailsReportEvent.WorkLogDetailsExportFormat;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.column.WorklogDetailsColumns;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.DownloadWorklogDetailsParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.export.ExcelToCsvConverter;
import org.everit.jira.reporting.plugin.export.ExportSummariesListReport;
import org.everit.jira.reporting.plugin.export.ExportWorklogDetailsListReport;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;

import com.google.gson.Gson;

/**
 * Responsible to define - and call implemented - export report process.
 */
@Path("/download-report")
public class DownloadReportResource {

  private static final String CSV_FILE_EXTENSION = "csv";

  private static final String XLS_FILE_EXTENSION = "xls";

  private final AnalyticsSender analyticsSender;

  private String pluginId;

  private final QuerydslSupport querydslSupport;

  private TimeTrackerSettingsHelper settingsHelper;

  /**
   * Simple constructor.
   */
  public DownloadReportResource(final AnalyticsSender analyticsSender,
      final TimeTrackerSettingsHelper settingsHelper) {
    pluginId = settingsHelper.loadGlobalSettings().getPluginUUID();
    this.analyticsSender = analyticsSender;
    this.settingsHelper = settingsHelper;
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Response buildCsvResponse(final HSSFWorkbook workbook, final String fileName,
      final String fileExtension) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      new ExcelToCsvConverter(workbook).printCSV(bos);
      return buildResponse(bos, fileName, fileExtension);
    } catch (IOException e) {
      return Response.serverError().build();
    }
  }

  private Response buildExcelResponse(final HSSFWorkbook workbbok, final String fileName,
      final String fileExtension) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      workbbok.write(bos);
      return buildResponse(bos, fileName, fileExtension);
    } catch (IOException e) {
      return Response.serverError().build();
    }
  }

  private Response buildResponse(final ByteArrayOutputStream bos, final String fileName,
      final String fileExtension) {
    String timeStamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
    return Response.ok(bos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition",
            "attachment; filename=\"" + fileName + timeStamp + "." + fileExtension + "\"")
        .build();
  }

  private HSSFWorkbook createSummaryExcel(final String json) {
    FilterCondition filterCondition = new Gson()
        .fromJson(json, FilterCondition.class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    ExportSummariesListReport exportSummariesListReport =
        new ExportSummariesListReport(querydslSupport, converSearchParam.reportSearchParam,
            converSearchParam.notBrowsableProjectKeys, settingsHelper.loadUserSettings());

    HSSFWorkbook workbook = exportSummariesListReport.exportToXLS();
    return workbook;
  }

  private HSSFWorkbook createWorkBook(final String orderByString,
      final DownloadWorklogDetailsParam downloadWorklogDetailsParam) {
    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(downloadWorklogDetailsParam.filterCondition,
            settingsHelper);
    OrderBy orderBy = ConverterUtil.convertToOrderBy(orderByString);
    HSSFWorkbook workbook =
        createWorklogDetailsExcel(downloadWorklogDetailsParam, converSearchParam, orderBy);
    return workbook;
  }

  private HSSFWorkbook createWorklogDetailsExcel(
      final DownloadWorklogDetailsParam downloadWorklogDetailsParam,
      final ConvertedSearchParam converSearchParam, final OrderBy orderBy) {

    ExportWorklogDetailsListReport exportWorklogDetailsListReport =
        new ExportWorklogDetailsListReport(querydslSupport,
            downloadWorklogDetailsParam.selectedWorklogDetailsColumns,
            converSearchParam.reportSearchParam,
            converSearchParam.notBrowsableProjectKeys,
            orderBy, settingsHelper.loadUserSettings());

    HSSFWorkbook workbook = exportWorklogDetailsListReport.exportToXLS();
    return workbook;
  }

  /**
   * Download summaries reports (project-, issue-, user summary).
   *
   * @param json
   *          the json string from which the object is to be deserialized to {@link FilterCondition}
   *          object.
   * @return the generated XLS_FILE_EXTENSION document.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/downloadSummariesReport")
  public Response downloadSummariesReport(
      @QueryParam("json") @DefaultValue("{}") final String json) {
    HSSFWorkbook workbook = createSummaryExcel(json);
    ExportSummaryReportEvent exportSummaryReportEvent =
        new ExportSummaryReportEvent(pluginId, ExportSummaryReportEvent.EVENT_ACTION_EXCEL);
    analyticsSender.send(exportSummaryReportEvent);
    return buildExcelResponse(workbook, "summaries-report", XLS_FILE_EXTENSION);
  }

  /**
   * Download summaries reports (project-, issue-, user summary).
   *
   * @param json
   *          the json string from which the object is to be deserialized to {@link FilterCondition}
   *          object.
   * @return the generated XLS_FILE_EXTENSION document.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/downloadSummariesReportAsCSV")
  public Response downloadSummariesReportAsCSV(
      @QueryParam("json") @DefaultValue("{}") final String json) {
    HSSFWorkbook workbook = createSummaryExcel(json);
    ExportSummaryReportEvent exportSummaryReportEvent =
        new ExportSummaryReportEvent(pluginId, ExportSummaryReportEvent.EVENT_ACTION_CSV);
    analyticsSender.send(exportSummaryReportEvent);
    return buildCsvResponse(workbook, "summaries-report", CSV_FILE_EXTENSION);
  }

  /**
   * Download worklog details report.
   *
   * @param json
   *          the json string from which the object is to be deserialized to
   *          {@link DownloadWorklogDetailsParam} object.
   * @return the generated XLS_FILE_EXTENSION document.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/downloadWorklogDetailsReport")
  public Response downloadWorklogDetailsReport(
      @QueryParam("json") @DefaultValue("{}") final String json,
      @QueryParam("orderBy") final String orderByString) {
    DownloadWorklogDetailsParam downloadWorklogDetailsParam = new Gson()
        .fromJson(json, DownloadWorklogDetailsParam.class);
    HSSFWorkbook workbook = createWorkBook(orderByString, downloadWorklogDetailsParam);
    sendWorklogDetailsAnalytics(WorkLogDetailsExportFormat.EXCEL, downloadWorklogDetailsParam);
    return buildExcelResponse(workbook, "worklog-details-report", XLS_FILE_EXTENSION);
  }

  /**
   * Download worklog details report.
   *
   * @param json
   *          the json string from which the object is to be deserialized to
   *          {@link DownloadWorklogDetailsParam} object.
   * @return the generated CSV_FILE_EXTENSION file.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/downloadWorklogDetailsReportAsCSV")
  public Response downloadWorklogDetailsReportAsCSV(
      @QueryParam("json") @DefaultValue("{}") final String json,
      @QueryParam("orderBy") final String orderByString) {
    DownloadWorklogDetailsParam downloadWorklogDetailsParam = new Gson()
        .fromJson(json, DownloadWorklogDetailsParam.class);
    HSSFWorkbook workbook = createWorkBook(orderByString, downloadWorklogDetailsParam);
    sendWorklogDetailsAnalytics(WorkLogDetailsExportFormat.CSV, downloadWorklogDetailsParam);
    return buildCsvResponse(workbook, "worklog-details-report", CSV_FILE_EXTENSION);
  }

  private void sendWorklogDetailsAnalytics(final WorkLogDetailsExportFormat exportFormat,
      final DownloadWorklogDetailsParam downloadWorklogDetailsParam) {
    boolean allFields = downloadWorklogDetailsParam.selectedWorklogDetailsColumns
        .containsAll(WorklogDetailsColumns.ALL_COLUMNS);
    ExportWorklogDetailsReportEvent exportWorklogDetailsReportEvent =
        new ExportWorklogDetailsReportEvent(pluginId, allFields, exportFormat);
    analyticsSender.send(exportWorklogDetailsReportEvent);
  }

}
