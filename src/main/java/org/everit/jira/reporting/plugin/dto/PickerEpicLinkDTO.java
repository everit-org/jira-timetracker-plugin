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

/**
 * Representation of label to picker.
 */
@XmlRootElement
public class PickerEpicLinkDTO {

  /**
   * Alias names to projections.
   */
  public static final class AliasNames {

    public static final String EPIC_LINK_ID = "epicLinkId";

    public static final String EPIC_NAME = "epicName";

    public static final String ISSUE_KEY = "issueKey";

    private AliasNames() {
    }
  }

  @XmlElement
  private Long epicLinkId;

  @XmlElement
  private String epicName;

  @XmlElement
  private String issueKey;

  public Long getEpicLinkId() {
    return epicLinkId;
  }

  public String getEpicName() {
    return epicName;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public void setEpicLinkId(final Long epicLinkId) {
    this.epicLinkId = epicLinkId;
  }

  public void setEpicName(final String name) {
    epicName = name;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }
}
