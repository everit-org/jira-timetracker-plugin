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
package org.everit.jira.reporting.plugin;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.jira.reporting.plugin.dto.PickerComponentDTO;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.reporting.plugin.dto.PickerVersionDTO;
import org.everit.jira.reporting.plugin.query.PickerComponentQuery;
import org.everit.jira.reporting.plugin.query.PickerUserQuery;
import org.everit.jira.reporting.plugin.query.PickerUserQuery.PickerUserQueryType;
import org.everit.jira.reporting.plugin.query.PickerVersionQuery;
import org.everit.jira.reporting.plugin.query.PickerVersionQuery.PickerVersionQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to define - and call implemented - list methods to pickers.
 */
@Path("/picker")
public class PickerResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PickerResource.class);

  private QuerydslSupport querydslSupport;

  /**
   * Simple constructor.
   */
  public PickerResource() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      LOGGER.error("Problem to create querydslSupport.", e);
    }
  }

  private Response buildResponse(final List<?> collection) {
    return Response.ok(collection)
        .build();
  }

  /**
   * List all components that defined in JIRA system. If no components return empty list response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/listComponents")
  public Response listComponents() {
    List<PickerComponentDTO> components = querydslSupport.execute(new PickerComponentQuery());

    return buildResponse(components);
  }

  /**
   * List JIRA users.
   *
   * @param pickerUserQueryType
   *          the type name, that define how to modify list (add static no users to result or not).
   * @return the JIRA users. If no one return empty list response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/listUsers")
  public Response listUsers(@QueryParam("pickerUserQueryType") final String pickerUserQueryType) {
    PickerUserQueryType type = PickerUserQueryType.getPickerUserQueryType(pickerUserQueryType);

    List<PickerUserDTO> users = querydslSupport.execute(new PickerUserQuery(type));

    return buildResponse(users);
  }

  /**
   * List versions.
   *
   * @param pickerVersionQueryType
   *          the type name, that define how to modify list (add static no versions to result or
   *          not).
   * @return the versions. If no one return empty list response.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/listVersions")
  public Response listVersions(
      @QueryParam("pickerVersionQueryType") final String pickerVersionQueryType) {
    PickerVersionQueryType type =
        PickerVersionQueryType.getPickerVersionQueryType(pickerVersionQueryType);

    List<PickerVersionDTO> versions = querydslSupport.execute(new PickerVersionQuery(type));

    return buildResponse(versions);
  }
}
