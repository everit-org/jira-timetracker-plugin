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

import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

/**
 * Query for gets JIRA users.
 */
public class PickerUserQuery implements QuerydslCallable<List<PickerUserDTO>> {

  /**
   * Type of user query.
   */
  public enum UserQueryType {

    ASSIGNEE,

    DEFAULT;
  }
  // private QAppUser qAppUser;
  //
  // private QAvatar qAvatar;

  private QCwdUser qCwdUser;

  // private QPropertyentry qPropertyentry;
  //
  // private QPropertynumber qPropertynumber;

  private UserQueryType userQueryType;

  /**
   * Simple constructor.
   */
  public PickerUserQuery(final UserQueryType userQueryType) {
    qCwdUser = new QCwdUser("cwd_user");
    this.userQueryType = userQueryType;
    // qAppUser = new QAppUser("app_user");
    // qPropertyentry = new QPropertyentry("prop_entry");
    // qPropertynumber = new QPropertynumber("prop_number");
    // qAvatar = new QAvatar("avatar");
  }

  @Override
  public List<PickerUserDTO> call(final Connection connection, final Configuration configuration)
      throws SQLException {

    List<PickerUserDTO> result = new SQLQuery<PickerUserDTO>(connection, configuration)
        .select(Projections.bean(PickerUserDTO.class,
            qCwdUser.lowerUserName.as(PickerUserDTO.AliasNames.USER_NAME),
            // qAvatar.filename.as(UserDTO.AliasNames.AVATAR_FILE_NAME),
            qCwdUser.displayName.as(PickerUserDTO.AliasNames.DISPLAY_NAME)))
        .from(qCwdUser)
        // .leftJoin(qAppUser).on(qAppUser.lowerUserName.eq(qCwdUser.lowerUserName))
        // .leftJoin(qPropertyentry).on(qPropertyentry.entityId.eq(qAppUser.id)
        // .and(qPropertyentry.entityName.eq(Entity.APPLICATION_USER.getEntityName()))
        // .and(qPropertyentry.propertyKey.eq(AvatarManager.USER_AVATAR_ID_KEY)))
        // .leftJoin(qPropertynumber).on(qPropertynumber.id.eq(qPropertyentry.id))
        // .leftJoin(qAvatar).on(qAvatar.id.eq(qPropertynumber.propertyvalue))
        .fetch();

    if (UserQueryType.ASSIGNEE.equals(userQueryType)) {
      result.add(PickerUserDTO.createUnassignedUser());
    }

    return result;
  }

}
