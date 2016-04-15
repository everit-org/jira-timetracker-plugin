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

public class UserSummaryReportDTO {

  private PagingDTO paging = new PagingDTO();

  private List<UserSummaryDTO> userSummaries = Collections.emptyList();

  private Long userSummaryCount = 0L;

  public PagingDTO getPaging() {
    return paging;
  }

  public List<UserSummaryDTO> getUserSummaries() {
    return userSummaries;
  }

  public Long getUserSummaryCount() {
    return userSummaryCount;
  }

  public UserSummaryReportDTO paging(final PagingDTO paging) {
    this.paging = paging;
    return this;
  }

  public UserSummaryReportDTO userSummaries(final List<UserSummaryDTO> userSummaries) {
    this.userSummaries = userSummaries;
    return this;
  }

  public UserSummaryReportDTO userSummaryCount(final Long userSummaryCount) {
    this.userSummaryCount = userSummaryCount;
    return this;
  }

}
