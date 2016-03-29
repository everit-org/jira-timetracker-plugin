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
 * Representation of version to picker.
 */
public class PickerVersionDTO {

  /**
   * Alias names to projections bean.
   */
  public final class AliasNames {

    public static final String VERSION_NAME = "name";

    private AliasNames() {
    }
  }

  public static final String NO_VERSION = "No version";

  public static final String RELEASED_VERSION = "Released version";

  public static final String UNRELEASED_VERSION = "Unreleased version";

  /**
   * Create no version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createNoVersion() {
    PickerVersionDTO noversion = new PickerVersionDTO();
    noversion.setName(NO_VERSION);
    return noversion;
  }

  /**
   * Create released version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createReleasedVersion() {
    PickerVersionDTO releasedVersion = new PickerVersionDTO();
    releasedVersion.setName(RELEASED_VERSION);
    return releasedVersion;
  }

  /**
   * Create unreleased version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createUnReleasedVersion() {
    PickerVersionDTO unreleasedVersion = new PickerVersionDTO();
    unreleasedVersion.setName(UNRELEASED_VERSION);
    return unreleasedVersion;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
