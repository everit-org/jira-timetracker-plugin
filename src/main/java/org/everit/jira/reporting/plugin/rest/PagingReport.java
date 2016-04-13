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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.ConvertedSearchParam;
import org.everit.jira.reporting.plugin.dto.FilterCondition;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsDTO;
import org.everit.jira.reporting.plugin.query.WorklogDetailsReportQueryBuilder;
import org.everit.jira.reporting.plugin.util.ConverterUtil;
import org.everit.jira.timetracker.plugin.DurationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.velocity.VelocityManager;
import com.google.gson.Gson;

/**
 * FIXME zs.cz add javadoc.
 */
@Path("/paging-report")
public class PagingReport {

  private static final Logger LOGGER = LoggerFactory.getLogger(PagingReport.class);

  private QuerydslSupport querydslSupport;

  /**
   * Simple constructor.
   */
  public PagingReport() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      LOGGER.error("Problem to create querydslSupport.", e);
    }
  }

  /**
   * FIXME zs.cz add javadoc.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("/pageWorklogDetails")
  public Response pageWorklogDetails(@QueryParam("json") @DefaultValue("{}") final String json) {
    FilterCondition filterCondition = new Gson()
        .fromJson(json, FilterCondition.class);

    ConvertedSearchParam converSearchParam = ConverterUtil
        .convertFilterConditionToConvertedSearchParam(filterCondition);

    WorklogDetailsReportQueryBuilder worklogDetailsReportQueryBuilder =
        new WorklogDetailsReportQueryBuilder(converSearchParam.reportSearchParam);

    QuerydslCallable<List<WorklogDetailsDTO>> worklogDetailsQuery = worklogDetailsReportQueryBuilder
        .buildQuery();

    QuerydslCallable<Long> worklogDetailsCountQuery =
        worklogDetailsReportQueryBuilder.buildCountQuery();

    QuerydslCallable<Long> grandTotalQuery =
        worklogDetailsReportQueryBuilder.buildGrandTotalQuery();

    List<WorklogDetailsDTO> worklogDetails = querydslSupport.execute(worklogDetailsQuery);

    Long worklogDetailsCount = querydslSupport.execute(worklogDetailsCountQuery);

    Long grandTotal = querydslSupport.execute(grandTotalQuery);

    Integer maxPageNumber = null;
    Integer newActPageNumber = 1;
    if ((converSearchParam.reportSearchParam.limit != null)
        && (converSearchParam.reportSearchParam.limit != 0)) {
      maxPageNumber = (int) Math
          .ceil(worklogDetailsCount / converSearchParam.reportSearchParam.limit.doubleValue());

      if ((converSearchParam.reportSearchParam.offset != null)
          && (converSearchParam.reportSearchParam.offset != 0)) {
        newActPageNumber = (int) Math
            .ceil(converSearchParam.reportSearchParam.offset
                / converSearchParam.reportSearchParam.limit.doubleValue())
            + 1;
      }
    }

    VelocityManager velocityManager = ComponentAccessor.getVelocityManager();

    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put("worklogDetails", worklogDetails);
    hashMap.put("worklogDetailsCount", worklogDetailsCount);
    hashMap.put("grandTotal", grandTotal);
    hashMap.put("durationFormatter", new DurationFormatter());
    hashMap.put("maxPageNumber", maxPageNumber);
    hashMap.put("newActPageNumber", newActPageNumber);
    hashMap.put("jsonFilterCondition", json);
    hashMap.put("filterCondition", filterCondition);

    Locale locale = ComponentAccessor.getJiraAuthenticationContext().getLocale();
    OutlookDate outlookDate = new OutlookDate(locale);
    hashMap.put("outlookDate", outlookDate);

    String encodedBody = velocityManager.getEncodedBody("/templates/reporting/",
        "reporting_result_worklog_details.vm",
        "UTF-8", hashMap);
    return Response.ok(encodedBody).build();
  }
}
