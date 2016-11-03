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
package org.everit.jira.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Utility class for timetracker.
 */
public final class TimetrackerUtil {

  /**
   * Check the date is contains the dates or not.
   *
   * @param dates
   *          the dates set.
   * @param date
   *          the date that check contains.
   * @return true if contains, otherwise false.
   */
  public static boolean containsSetTheSameDay(final Set<Date> dates,
      final Calendar date) {
    for (Date d : dates) {
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      if (DateUtils.isSameDay(c, date)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check the date is contains the dates or not.
   *
   * @param dates
   *          the dates set.
   * @param date
   *          the date that check contains.
   * @return true if contains, otherwise false.
   */
  public static boolean containsSetTheSameDay(final Set<Date> dates, final Date date) {
    Calendar instance = Calendar.getInstance();
    instance.setTime(date);
    return TimetrackerUtil.containsSetTheSameDay(dates, instance);
  }

  /**
   * Check the given date, the user have worklogs or not.
   *
   * @param date
   *          The date what have to check.
   * @return If The user have worklogs the given date then true, esle false.
   * @throws GenericEntityException
   *           GenericEntity Exception.
   */
  public static boolean isContainsWorklog(final Date date)
      throws GenericEntityException {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();

    Calendar startDate = DateTimeConverterUtil.setDateToDayStart(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user, startDate,
            endDate);

    List<GenericValue> worklogGVList =
        ComponentAccessor.getOfBizDelegator().findByAnd("IssueWorklogView", exprList);

    return !((worklogGVList == null) || worklogGVList.isEmpty());
  }

  private TimetrackerUtil() {
  }
}
