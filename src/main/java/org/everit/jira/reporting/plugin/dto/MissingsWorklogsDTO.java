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

import java.io.Serializable;
import java.util.Date;

/**
 * Missings Worklogs data hodler.
 */
public class MissingsWorklogsDTO implements Serializable {

  /**
   * Serial Version UID.
   */
  private static final long serialVersionUID = 3330484579876043764L;

  private Date date;

  private String hour;

  /**
   * Constructor for a MissingsWorklogsDTO.
   *
   * @param date
   *          The missings day date.
   * @param hour
   *          The amount of missings hours
   */
  public MissingsWorklogsDTO(final Date date, final String hour) {
    super();
    this.date = date;
    this.hour = hour;
  }

  public Date getDate() {
    return (Date) date.clone();
  }

  public String getHour() {
    return hour;
  }

  public void setDate(final Date date) {
    this.date = date;
  }

  public void setHour(final String hour) {
    this.hour = hour;
  }

}
