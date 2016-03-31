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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.ReportSearchParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/downloadWorklogDetailsReport")
  public Response downloadWorklogDetailsReport(final ReportSearchParam reportSearchParam) {
    // ArrayList<ReportSearchParam> arrayList = new ArrayList<>();
    // arrayList.add(reportSearchParam);
    return Response.ok(reportSearchParam).build();
  }

}
