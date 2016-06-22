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
 * Session data for the reports.
 */
public class TimetrackerReportsSessionData implements Serializable {

  private static final long serialVersionUID = 7694409127990223607L;

  public String currentUser;

  public Long dateFrom;

  public Long dateTo;

  public TimetrackerReportsSessionData currentUser(final String currentUser) {
    this.currentUser = currentUser;
    return this;
  }

  public TimetrackerReportsSessionData dateFrom(final Long dateFrom) {
    this.dateFrom = dateFrom;
    return this;
  }

  public TimetrackerReportsSessionData dateTo(final Long dateTo) {
    this.dateTo = dateTo;
    return this;
  }

}
