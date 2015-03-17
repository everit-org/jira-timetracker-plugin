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
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import org.everit.jira.timetracker.plugin.DateTimeConverterUtil;

/**
 * The comparator of the {@link EveritWorklog}.
 */
public class EveritWorklogComparator implements Comparator<EveritWorklog>, Serializable {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -4563970131275373338L;

  @Override
  public int compare(final EveritWorklog o1, final EveritWorklog o2) {
    Date o1StartDate = null;
    Date o2StartDate = null;
    try {
      o1StartDate = DateTimeConverterUtil.stringTimeToDateTime(o1.getStartTime());
      o2StartDate = DateTimeConverterUtil.stringTimeToDateTime(o2.getStartTime());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Faild to convert startDate to Date", e);
    }
    int result = o1StartDate.compareTo(o2StartDate);
    if (result != 0) {
      return result;
    }
    Date o1EndDate = null;
    Date o2EndDate = null;
    try {
      o1EndDate = DateTimeConverterUtil.stringTimeToDateTime(o1.getEndTime());
      o2EndDate = DateTimeConverterUtil.stringTimeToDateTime(o2.getEndTime());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Faild to convert endDate to Date", e);
    }
    return o1EndDate.compareTo(o2EndDate);
  }

}
