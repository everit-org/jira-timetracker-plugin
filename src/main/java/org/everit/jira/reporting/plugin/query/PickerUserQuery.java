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

import org.everit.jira.querydsl.schema.QCwdUser;
import org.everit.jira.querydsl.support.QuerydslCallable;
import org.everit.jira.reporting.plugin.dto.PickerUserDTO;
import org.everit.jira.reporting.plugin.query.util.QueryUtil;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets JIRA users to picker.
 */
public class PickerUserQuery implements QuerydslCallable<List<PickerUserDTO>> {

  /**
   * Type of picker user query.
   */
  public enum PickerUserQueryType {

    ASSIGNEE,

    DEFAULT,

    REPORTER;

    private static final PickerUserQueryType[] PICKER_USER_QUERY_TYPES =
        PickerUserQueryType.values();

    /**
     * Gets picker user query type based on name.
     */
    public static PickerUserQueryType getPickerUserQueryType(final String pickerUserQueryType) {
      for (PickerUserQueryType type : PICKER_USER_QUERY_TYPES) {
        if (type.name().equals(pickerUserQueryType)) {
          return type;
        }
      }
      return DEFAULT;
    }

  }

  private PickerUserQueryType pickerUserQueryType;

  private QCwdUser qCwdUser;

  /**
   * Simple constructor.
   */
  public PickerUserQuery(final PickerUserQueryType pickerUserQueryType) {
    qCwdUser = new QCwdUser("cwd_user");
    this.pickerUserQueryType = pickerUserQueryType;
  }

  @Override
  public List<PickerUserDTO> call(final Connection connection, final Configuration configuration)
      throws SQLException {

    SQLQuery<String> selectDisplayNameForUserByLowerUserName =
        QueryUtil.selectDisplayNameForUserByLowerUserName(qCwdUser.lowerUserName);
    SimpleExpression<String> displayNameExpression =
        selectDisplayNameForUserByLowerUserName
            .as(PickerUserDTO.AliasNames.DISPLAY_NAME);
    List<PickerUserDTO> result = new SQLQuery<PickerUserDTO>(connection, configuration)
        .select(Projections.bean(PickerUserDTO.class,
            qCwdUser.lowerUserName.as(PickerUserDTO.AliasNames.USER_NAME),
            qCwdUser.userName.as(PickerUserDTO.AliasNames.AVATAR_OWNER),
            displayNameExpression,
            qCwdUser.active.as(PickerUserDTO.AliasNames.ACTIVE)))
        .from(qCwdUser)
        .orderBy(new OrderSpecifier<String>(Order.ASC, selectDisplayNameForUserByLowerUserName))
        .fetch();

    if (PickerUserQueryType.ASSIGNEE.equals(pickerUserQueryType)) {
      result.add(0, PickerUserDTO.createUnassignedUser());
    }
    result.add(0, PickerUserDTO.createCurrentUser());
    if (PickerUserQueryType.DEFAULT.equals(pickerUserQueryType)) {
      result.add(0, PickerUserDTO.createNoneUser());
    }

    return result;
  }

}
