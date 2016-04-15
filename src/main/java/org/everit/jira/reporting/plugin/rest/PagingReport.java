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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.everit.jira.reporting.plugin.ReportingPlugin;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.IssueSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.velocity.VelocityManager;
import com.google.gson.Gson;

/**
 * FIXME zs.cz add javadoc.
 */
@Path("/paging-report")
public class PagingReport {

  private ReportingPlugin reportingPlugin;

  public PagingReport(final ReportingPlugin reportingPlugin) {
    this.reportingPlugin = reportingPlugin;
  }

  /**
   * FIXME zs.cz add javadoc.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageIssueSummary")
  public Response pageIssueSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    Gson gson = new Gson();
    FilterCondition filterCondition = gson.fromJson(filterConditionJson, FilterCondition.class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition);

    IssueSummaryReportDTO issueSummaryReport =
        reportingPlugin.getIssueSummaryReport(converSearchParam.reportSearchParam);

    VelocityManager velocityManager = ComponentAccessor.getVelocityManager();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("issueSummaryReport", issueSummaryReport);
    hashMap.put("durationFormatter", new DurationFormatter());
    hashMap.put("filterCondition", filterCondition);

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    OutlookDate outlookDate = new OutlookDate(locale);
    hashMap.put("outlookDate", outlookDate);

    String encodedBody = velocityManager.getEncodedBody("/templates/reporting/",
        "reporting_result_issue_summary.vm",
        "UTF-8", hashMap);
    return Response.ok(encodedBody).build();
  }

  /**
   * FIXME zs.cz add javadoc.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageProjectSummary")
  public Response pageProjectSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    Gson gson = new Gson();
    FilterCondition filterCondition = gson.fromJson(filterConditionJson, FilterCondition.class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition);

    ProjectSummaryReportDTO projectSummaryReport =
        reportingPlugin.getProjectSummaryReport(converSearchParam.reportSearchParam);

    VelocityManager velocityManager = ComponentAccessor.getVelocityManager();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("projectSummaryReport", projectSummaryReport);
    hashMap.put("durationFormatter", new DurationFormatter());
    hashMap.put("filterCondition", filterCondition);

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    OutlookDate outlookDate = new OutlookDate(locale);
    hashMap.put("outlookDate", outlookDate);

    String encodedBody = velocityManager.getEncodedBody("/templates/reporting/",
        "reporting_result_project_summary.vm",
        "UTF-8", hashMap);
    return Response.ok(encodedBody).build();
  }

  /**
   * FIXME zs.cz add javadoc.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageUserSummary")
  public Response pageUserSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    Gson gson = new Gson();
    FilterCondition filterCondition = gson.fromJson(filterConditionJson, FilterCondition.class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition);

    UserSummaryReportDTO userSummaryReport =
        reportingPlugin.getUserSummaryReport(converSearchParam.reportSearchParam);

    VelocityManager velocityManager = ComponentAccessor.getVelocityManager();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("userSummaryReport", userSummaryReport);
    hashMap.put("durationFormatter", new DurationFormatter());
    hashMap.put("filterCondition", filterCondition);

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    OutlookDate outlookDate = new OutlookDate(locale);
    hashMap.put("outlookDate", outlookDate);

    String encodedBody = velocityManager.getEncodedBody("/templates/reporting/",
        "reporting_result_user_summary.vm",
        "UTF-8", hashMap);
    return Response.ok(encodedBody).build();
  }

  /**
   * FIXME zs.cz add javadoc.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageWorklogDetails")
  public Response pageWorklogDetails(
      @QueryParam("filterConditionJson") final String filterConditionJson,
      @QueryParam("selectedColumnsJson") final String selectedColumnsJson) {
    Gson gson = new Gson();
    FilterCondition filterCondition = gson.fromJson(filterConditionJson, FilterCondition.class);

    String[] selectedColumns = gson.fromJson(selectedColumnsJson, String[].class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition);

    WorklogDetailsReportDTO worklogDetailsReport =
        reportingPlugin.getWorklogDetailsReport(converSearchParam.reportSearchParam);

    VelocityManager velocityManager = ComponentAccessor.getVelocityManager();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("worklogDetailsReport", worklogDetailsReport);
    hashMap.put("durationFormatter", new DurationFormatter());
    hashMap.put("filterCondition", filterCondition);
    hashMap.put("selectedWorklogDetailsColumns", Arrays.asList(selectedColumns));

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    OutlookDate outlookDate = new OutlookDate(locale);
    hashMap.put("outlookDate", outlookDate);

    String encodedBody = velocityManager.getEncodedBody("/templates/reporting/",
        "reporting_result_worklog_details.vm",
        "UTF-8", hashMap);
    return Response.ok(encodedBody).build();
  }
}
