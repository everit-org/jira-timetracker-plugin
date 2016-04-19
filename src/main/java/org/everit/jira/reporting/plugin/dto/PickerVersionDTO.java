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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.everit.jira.timetracker.plugin.util.JiraTimetrackerUtil;

/**
 * Representation of version to picker.
 */
@XmlRootElement
public class PickerVersionDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String VERSION_NAME = "name";

    private AliasNames() {
    }
  }

  public static final String NO_VERSION = "jtrp.picker.no.version.value";

  public static final String RELEASED_VERSION = "jtrp.picker.released.version.value";

  public static final String UNRELEASED_VERSION = "jtrp.picker.unreleased.version.value";

  /**
   * Create no version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createNoVersion() {
    PickerVersionDTO noversion = new PickerVersionDTO();
    noversion.setName(JiraTimetrackerUtil.getI18nText(NO_VERSION));
    return noversion;
  }

  /**
   * Create released version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createReleasedVersion() {
    PickerVersionDTO releasedVersion = new PickerVersionDTO();
    releasedVersion.setName(JiraTimetrackerUtil.getI18nText(RELEASED_VERSION));
    return releasedVersion;
  }

  /**
   * Create unreleased version {@link PickerVersionDTO}.
   */
  public static PickerVersionDTO createUnReleasedVersion() {
    PickerVersionDTO unreleasedVersion = new PickerVersionDTO();
    unreleasedVersion.setName(JiraTimetrackerUtil.getI18nText(UNRELEASED_VERSION));
    return unreleasedVersion;
  }

  @XmlElement
  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
