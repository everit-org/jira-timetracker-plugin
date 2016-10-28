package org.everit.jira.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

public final class TimetrackerUtil {

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
