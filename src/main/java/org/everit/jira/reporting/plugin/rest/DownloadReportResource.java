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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.everit.jira.reporting.plugin.export.ExportSummariesListReport;
import org.everit.jira.reporting.plugin.export.ExportWorklogDetailsListReport;
import org.everit.jira.reporting.plugin.rest.dto.DownloadWorklogDetailsParam;
import org.everit.jira.reporting.plugin.rest.dto.FilterCondition;
import org.everit.jira.reporting.plugin.rest.exception.JTTPException;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.google.gson.Gson;

/**
 * Responsible to define - and call implemented - export report process.
 */
@Path("/download-report")
public class DownloadReportResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PickerResource.class);

  private QuerydslSupport querydslSupport;

  /**
   * Simple constructor.
   */
  public DownloadReportResource() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      LOGGER.error("Problem to create querydslSupport.", e);
    }
  }

  private Response buildResponse(final HSSFWorkbook workbook, final String fileName) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
      workbook.write(bos);
      return Response.ok(bos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
          .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
          .build();
    } catch (IOException e) {
      return Response.serverError().build();
    }
  }

  /**
   * Download summaries reports (project-, issue-, user summary).
   *
   * @param json
   *          the json string from which the object is to be deserialized to {@link FilterCondition}
   *          object.
   * @return the generated XLS document.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/downloadSummariesReport")
  public Response downloadSummariesReport(
      @QueryParam("json") @DefaultValue("{}") final String json) {
    FilterCondition filterCondition = new Gson()
        .fromJson(json, FilterCondition.class);

    ExportSummariesListReport exportSummariesListReport =
        new ExportSummariesListReport(querydslSupport, getReportSearchParam(filterCondition));

    HSSFWorkbook workbook = exportSummariesListReport.exportToXLS();
    return buildResponse(workbook, "summaries-report.xls");
  }

  /**
   * Download worklog details report.
   *
   * @param json
   *          the json string from which the object is to be deserialized to
   *          {@link DownloadWorklogDetailsParam} object.
   * @return the generated XLS document.
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/downloadWorklogDetailsReport")
  public Response downloadWorklogDetailsReport(
      @QueryParam("json") @DefaultValue("{}") final String json) {
    DownloadWorklogDetailsParam downloadWorklogDetailsParam = new Gson()
        .fromJson(json, DownloadWorklogDetailsParam.class);
    ReportSearchParam reportSearchParam =
        getReportSearchParam(downloadWorklogDetailsParam.filterCondition);

    ExportWorklogDetailsListReport exportWorklogDetailsListReport =
        new ExportWorklogDetailsListReport(querydslSupport,
            downloadWorklogDetailsParam.selectedWorklogDetailsColumns,
            reportSearchParam);

    HSSFWorkbook workbook = exportWorklogDetailsListReport.exportToXLS();
    return buildResponse(workbook, "worklog-details-report.xls");
  }

  private Date getDate(final String date) {
    if ((date == null) || date.isEmpty()) {
      return null;
    }
    try {
      return DateTimeConverterUtil.stringToDate(date);
    } catch (ParseException e) {
      throw new JTTPException("Cannot parse String format to Date.", e);
    }
  }

  private ReportSearchParam getReportSearchParam(
      final FilterCondition filterCondition) {
    ReportSearchParam reportSearchParam = new ReportSearchParam()
        .issueAffectedVersions(filterCondition.issueAffectedVersions)
        .issueAssignees(filterCondition.issueAssignees)
        .issueComponents(filterCondition.issueComponents)
        .issueCreateDate(getDate(filterCondition.issueCreateDate))
        .issueEpicLinkIssueIds(filterCondition.issueEpicLinkIssueIds)
        .issueFixedVersions(filterCondition.issueFixedVersions)
        .issuePriorityIds(filterCondition.issuePriorityIds)
        .issueReporters(filterCondition.issueReporters)
        .issueResolutionIds(filterCondition.issueResolutionIds)
        .issueStatusIds(filterCondition.issueStatusIds)
        .issueTypeIds(filterCondition.issueTypeIds)
        .labels(filterCondition.labels)
        .projectIds(filterCondition.projectIds)
        .worklogEndDate(getDate(filterCondition.worklogEndDate))
        .worklogStartDate(getDate(filterCondition.worklogStartDate))
        .selectNoAffectedVersionIssue(filterCondition.selectNoAffectedVersionIssue)
        .selectNoComponentIssue(filterCondition.selectNoComponentIssue)
        .selectNoFixedVersionIssue(filterCondition.selectNoFixedVersionIssue)
        .selectReleasedFixVersion(filterCondition.selectReleasedFixVersion)
        .selectUnassgined(filterCondition.selectUnassgined)
        .selectUnreleasedFixVersion(filterCondition.selectUnreleasedFixVersion)
        .selectUnresolvedResolution(filterCondition.selectUnresolvedResolution)
        .issueKeys(filterCondition.issueKeys);

    String epicName = filterCondition.issueEpicName;
    if ((epicName != null) && !epicName.isEmpty()) {
      reportSearchParam.issueEpicName(epicName);
    }

    List<String> users = filterCondition.users;
    if (filterCondition.users.isEmpty()
        && !filterCondition.groups.isEmpty()) {
      users = getUserNamesFromGroup(filterCondition.groups);
    }
    reportSearchParam.users(users);
    return reportSearchParam;
  }

  private List<String> getUserNamesFromGroup(final List<String> groupNames) {
    List<String> userNames = new ArrayList<String>();
    GroupManager groupManager = ComponentAccessor.getGroupManager();
    for (String groupName : groupNames) {
      Collection<String> userNamesInGroup = groupManager.getUserNamesInGroup(groupName);
      userNames.addAll(userNamesInGroup);
    }
    return userNames;
  }
}
