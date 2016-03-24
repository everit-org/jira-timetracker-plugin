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
package org.everit.jira.reporting.plugin.query;

import java.sql.Connection;
import java.util.List;

import org.everit.jira.reporting.plugin.dto.UserSummaryDTO;
import org.everit.jira.reporting.plugin.dto.WorklogDetailsSearchParam;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.Configuration;

public class UserSummaryQuery extends AbstractReportQuery<UserSummaryDTO> {

  public UserSummaryQuery(final WorklogDetailsSearchParam worklogDetailsSearchParam) {
    super(worklogDetailsSearchParam);
  }

  @Override
  protected Expression<?>[] createGroupBy() {
    return new Expression<?>[] { qCwdUser.displayName };
  }

  @Override
  protected QBean<UserSummaryDTO> createSelectProjection() {
    return Projections.bean(UserSummaryDTO.class,
        qCwdUser.displayName.as(UserSummaryDTO.AliasNames.USER_DISPLAY_NAME),
        qWorklog.timeworked.sum().as(UserSummaryDTO.AliasNames.WORKLOGGED_TIME_SUM));
  }

  @Override
  protected void extendResult(final Connection connection, final Configuration configuration,
      final List<UserSummaryDTO> result) {
  }

}
