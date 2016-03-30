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

import org.everit.jira.querydsl.schema.QProjectversion;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.PickerVersionDTO;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets versions to picker.
 */
public class PickerVersionQuery implements QuerydslCallable<List<PickerVersionDTO>> {

  /**
   * Type of picker user query.
   */
  public enum PickerVersionQueryType {

    AFFECTED_VERSION,

    DEFAULT,

    FIX_VERSION;

    private static final PickerVersionQueryType[] PICKER_VERSION_QUERY_TYPES =
        PickerVersionQueryType.values();

    /**
     * Gets picker version query type based on name.
     */
    public static PickerVersionQueryType getPickerVersionQueryType(
        final String pickerVersionQueryType) {
      for (PickerVersionQueryType type : PICKER_VERSION_QUERY_TYPES) {
        if (type.name().equals(pickerVersionQueryType)) {
          return type;
        }
      }
      return DEFAULT;
    }
  }

  private PickerVersionQueryType pickerVersionQueryType;

  private QProjectversion qProjectversion;

  public PickerVersionQuery(final PickerVersionQueryType pickerVersionQueryType) {
    qProjectversion = new QProjectversion("p_version");
    this.pickerVersionQueryType = pickerVersionQueryType;
  }

  @Override
  public List<PickerVersionDTO> call(final Connection connection, final Configuration configuration)
      throws SQLException {

    List<PickerVersionDTO> result = new SQLQuery<PickerVersionDTO>(connection, configuration)
        .select(Projections.bean(PickerVersionDTO.class,
            qProjectversion.vname.as(PickerVersionDTO.AliasNames.VERSION_NAME)))
        .from(qProjectversion)
        .groupBy(qProjectversion.vname)
        .orderBy(qProjectversion.vname.asc())
        .fetch();

    if (PickerVersionQueryType.AFFECTED_VERSION.equals(pickerVersionQueryType)) {
      result.add(0, PickerVersionDTO.createNoVersion());
    } else if (PickerVersionQueryType.FIX_VERSION.equals(pickerVersionQueryType)) {
      result.add(0, PickerVersionDTO.createUnReleasedVersion());
      result.add(0, PickerVersionDTO.createReleasedVersion());
      result.add(0, PickerVersionDTO.createNoVersion());
    }
    return result;
  }

}
