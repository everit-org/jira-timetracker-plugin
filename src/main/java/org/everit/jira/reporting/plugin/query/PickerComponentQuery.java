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
import java.sql.SQLException;
import java.util.List;

import org.everit.jira.querydsl.schema.QComponent;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.PickerComponentDTO;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets versions to picker.
 */
public class PickerComponentQuery implements QuerydslCallable<List<PickerComponentDTO>> {

  private QComponent qComponent;

  public PickerComponentQuery() {
    qComponent = new QComponent("component");
  }

  @Override
  public List<PickerComponentDTO> call(final Connection connection,
      final Configuration configuration)
          throws SQLException {

    List<PickerComponentDTO> result = new SQLQuery<PickerComponentDTO>(connection, configuration)
        .select(Projections.bean(PickerComponentDTO.class,
            qComponent.cname.as(PickerComponentDTO.AliasNames.COMPONENT_NAME)))
        .from(qComponent)
        .groupBy(qComponent.cname)
        .fetch();

    result.add(PickerComponentDTO.createNoComponent());

    return result;
  }

}
