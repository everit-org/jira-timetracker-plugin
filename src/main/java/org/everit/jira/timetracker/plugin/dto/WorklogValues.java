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
package org.everit.jira.timetracker.plugin.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represent the Timetracker worklog input parameters.
 */
@XmlRootElement
public class WorklogValues {

  @XmlElement
  private String comment;

  @XmlElement
  private String durationTime;

  @XmlElement
  private String endTime;

  @XmlElement
  private Boolean isDuration;

  @XmlElement
  private String issueKey;

  @XmlElement
  private String startTime;

  @XmlElement
  public String getComment() {
    return comment;
  }

  public String getDurationTime() {
    return durationTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getStartTime() {
    return startTime;
  }

  public Boolean isDuration() {
    return isDuration;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setDurationTime(final String durationTime) {
    this.durationTime = durationTime;
  }

  public void setEndTime(final String endTime) {
    this.endTime = endTime;
  }

  public void setIsDuration(final Boolean isDuration) {
    this.isDuration = isDuration;
  }

  public void setIssueKey(final String issueKey) {
    this.issueKey = issueKey;
  }

  public void setStartTime(final String startTime) {
    this.startTime = startTime;
  }

}
