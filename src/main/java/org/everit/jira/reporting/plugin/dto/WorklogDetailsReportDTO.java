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
package org.everit.jira.reporting.plugin.dto;

import java.util.Collections;
import java.util.List;

public class WorklogDetailsReportDTO {

  private Long grandTotal = 0L;

  private PagingDTO paging = new PagingDTO();

  private List<WorklogDetailsDTO> worklogDetails = Collections.emptyList();

  private Long worklogDetailsCount = 0L;

  public Long getGrandTotal() {
    return grandTotal;
  }

  public PagingDTO getPaging() {
    return paging;
  }

  public List<WorklogDetailsDTO> getWorklogDetails() {
    return worklogDetails;
  }

  public Long getWorklogDetailsCount() {
    return worklogDetailsCount;
  }

  public WorklogDetailsReportDTO grandTotal(final Long grandTotal) {
    this.grandTotal = grandTotal;
    return this;
  }

  public WorklogDetailsReportDTO paging(final PagingDTO paging) {
    this.paging = paging;
    return this;
  }

  public WorklogDetailsReportDTO worklogDetails(final List<WorklogDetailsDTO> worklogDetails) {
    this.worklogDetails = worklogDetails;
    return this;
  }

  public WorklogDetailsReportDTO worklogDetailsCount(final Long worklogDetailsCount) {
    this.worklogDetailsCount = worklogDetailsCount;
    return this;
  }

}
