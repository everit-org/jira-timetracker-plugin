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

import java.io.Serializable;

/**
 * Data holder for a chart.
 */
public class ChartData implements Serializable {

  private static final long serialVersionUID = 21551301923003314L;

  private String projectId;

  private Long duration;

  /**
   * Constructor for a chart data.
   *
   * @param projectId
   *          Id of the current project
   * @param duration
   *          Duration
   */
  public ChartData(final String projectId, final Long duration) {
    super();
    this.projectId = projectId;
    this.duration = duration;
  }

  public Long getDuration() {
    return duration;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setDuration(final Long duration) {
    this.duration = duration;
  }

  public void setProjectId(final String projectId) {
    this.projectId = projectId;
  }

}
