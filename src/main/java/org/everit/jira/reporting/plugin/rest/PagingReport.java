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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.everit.jira.reporting.plugin.dto.OrderBy;
import org.everit.jira.reporting.plugin.dto.ProjectSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.UserSummaryReportDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsReportDTO;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.timetracker.plugin.DurationFormatter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;

/**
 * Responsible to help table report paging.
 */
@Path("/paging-report")
public class PagingReport {

  private static final String TEMPLATE_DIRECTORY = "/templates/reporting/";

  private Gson gson;

  private ReportingPlugin reportingPlugin;

  private TimeTrackerSettingsHelper settingsHelper;

  private TemplateRenderer templateRenderer;

  /**
   * Simple constructor. Initialize required members.
   */
  public PagingReport(final ReportingPlugin reportingPlugin,
      final TimeTrackerSettingsHelper settingsHelper, final TemplateRenderer templateRenderer) {
    this.reportingPlugin = reportingPlugin;
    this.settingsHelper = settingsHelper;
    gson = new Gson();
    this.templateRenderer = templateRenderer;
  }

  private void appendRequiredContextParameters(final Map<String, Object> contextParameters,
      final FilterCondition filterCondition) {
    contextParameters.put("durationFormatter", new DurationFormatter());
    contextParameters.put("filterCondition", filterCondition);

    contextParameters.put("dateTimeFormatterDate", getDateTimeFormatterDate());
    contextParameters.put("dateTimeFormatterDateTime", getDateTimeFormatterDateTime());

    IssueRenderContext issueRenderContext = new IssueRenderContext(null);
    contextParameters.put("issueRenderContext", issueRenderContext);

    RendererManager rendererManager = ComponentAccessor.getRendererManager();
    JiraRendererPlugin atlassianWikiRenderer =
        rendererManager.getRendererForType("atlassian-wiki-renderer");
    contextParameters.put("atlassianWikiRenderer", atlassianWikiRenderer);

    contextParameters.put("contextPath", getContextPath());

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(locale);
    contextParameters.put("i18n", i18nHelper);
  }

  private Response buildResponse(final String templateFileName,
      final Map<String, Object> contextParameters) {
    StringWriter sw = new StringWriter();
    try {
      templateRenderer.render(TEMPLATE_DIRECTORY + templateFileName, contextParameters, sw);
    } catch (RenderingException | IOException e) {
      throw new RuntimeException(e);
    }
    return Response.ok(sw.toString()).build();
  }

  private FilterCondition convertJsonToFilterCondition(final String filterConditionJson) {
    return gson.fromJson(filterConditionJson, FilterCondition.class);
  }

  private String getContextPath() {
    return ComponentAccessor.getWebResourceUrlProvider().getBaseUrl();
  }

  private DateTimeFormatter getDateTimeFormatterDate() {
    return ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class).formatter()
        .forLoggedInUser().withStyle(DateTimeStyle.DATE).withSystemZone();
  }

  private DateTimeFormatter getDateTimeFormatterDateTime() {
    return ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class).formatter()
        .withStyle(DateTimeStyle.COMPLETE).withSystemZone();
  }

  /**
   * Paging the issue summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageIssueSummary")
  public Response pageIssueSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    IssueSummaryReportDTO issueSummaryReport =
        reportingPlugin.getIssueSummaryReport(converSearchParam.reportSearchParam);

    Map<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("issueSummaryReport", issueSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_issue_summary.vm", contextParameters);
  }

  /**
   * Paging the project summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageProjectSummary")
  public Response pageProjectSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    ProjectSummaryReportDTO projectSummaryReport =
        reportingPlugin.getProjectSummaryReport(converSearchParam.reportSearchParam);

    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("projectSummaryReport", projectSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_project_summary.vm", contextParameters);
  }

  /**
   * Paging the user summary report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageUserSummary")
  public Response pageUserSummary(
      @QueryParam("filterConditionJson") final String filterConditionJson) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    UserSummaryReportDTO userSummaryReport =
        reportingPlugin.getUserSummaryReport(converSearchParam.reportSearchParam);

    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("userSummaryReport", userSummaryReport);

    appendRequiredContextParameters(contextParameters, filterCondition);

    return buildResponse("reporting_result_user_summary.vm", contextParameters);
  }

  /**
   * Paging the worklog details report table.
   *
   * @param filterConditionJson
   *          the {@link FilterCondition} in JSON format.
   * @param selectedColumnsJson
   *          the JSON array from the selected columns.
   *
   * @return the page content in HTML.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageWorklogDetails")
  public Response pageWorklogDetails(
      @QueryParam("filterConditionJson") final String filterConditionJson,
      @QueryParam("selectedColumnsJson") final String selectedColumnsJson,
      @QueryParam("orderBy") final String orderByString) {
    FilterCondition filterCondition = convertJsonToFilterCondition(filterConditionJson);

    String[] selectedColumns = gson.fromJson(selectedColumnsJson, String[].class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition, settingsHelper);

    OrderBy orderBy = ConverterUtil.convertToOrderBy(orderByString);

    WorklogDetailsReportDTO worklogDetailsReport =
        reportingPlugin.getWorklogDetailsReport(converSearchParam.reportSearchParam, orderBy);

    HashMap<String, Object> contextParameters = new HashMap<>();
    contextParameters.put("worklogDetailsReport", worklogDetailsReport);
    contextParameters.put("selectedWorklogDetailsColumns", Arrays.asList(selectedColumns));

    appendRequiredContextParameters(contextParameters, filterCondition);

    contextParameters.put("order", orderBy.order);
    contextParameters.put("orderColumn", orderBy.columnName);

    return buildResponse("reporting_result_worklog_details.vm", contextParameters);
  }
}
