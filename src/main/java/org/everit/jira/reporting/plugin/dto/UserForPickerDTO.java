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
 * User information for picker.
 */
public class UserForPickerDTO {

  public static final String CURRENT_USER_DISPLAY_NAME = "jtrp.picker.current.user.value";

  public static final String CURRENT_USER_KEY = "currentUser";

  public static final String NONE_DISPLAY_NAME = "jtrp.picker.value.none";

  public static final String NONE_USER_KEY = "none";

  public static final String UNASSIGNED_DISPLAY_NAME = "jtrp.picker.unassigned.value";

  public static final String UNASSIGNED_USER_KEY = "empty";

  private String avatarURL;

  private String displayName;

  private String userKey;

  /**
   * Simple constructor.
   */
  public UserForPickerDTO(final String avatarURL, final String displayName, final String userKey) {
    this.avatarURL = avatarURL;
    this.displayName = displayName;
    this.userKey = userKey;
  }

  public String getAvatarURL() {
    return avatarURL;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUserKey() {
    return userKey;
  }

  public void setAvatarURL(final String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public void setUserKey(final String userKey) {
    this.userKey = userKey;
  }

}
