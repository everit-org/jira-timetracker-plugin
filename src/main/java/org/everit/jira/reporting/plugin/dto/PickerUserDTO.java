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

/**
 * Representation of user to picker.
 */
public class PickerUserDTO {

  /**
   * Alias names to projections bean.
   */
  public final class AliasNames {

    // public static final String AVATAR_FILE_NAME = "avatarFileName";

    public static final String DISPLAY_NAME = "displayName";

    public static final String USER_NAME = "userName";

    private AliasNames() {
    }
  }

  public static final String UNASSIGNED_USER_NAME = "Unassigned";

  /**
   * Create unassigned 'user'.
   */
  public static PickerUserDTO createUnassignedUser() {
    PickerUserDTO unassigned = new PickerUserDTO();
    unassigned.setDisplayName(UNASSIGNED_USER_NAME);
    unassigned.setUserName(UNASSIGNED_USER_NAME);
    return unassigned;
  }

  // private String avatarFileName;

  private String displayName;

  private String userName;

  // public String getAvatarFileName() {
  // return avatarFileName;
  // }

  public String getDisplayName() {
    return displayName;
  }

  public String getUserName() {
    return userName;
  }

  // public void setAvatarFileName(final String avatarFileName) {
  // this.avatarFileName = avatarFileName;
  // }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public void setUserName(final String userKey) {
    userName = userKey;
  }

}
