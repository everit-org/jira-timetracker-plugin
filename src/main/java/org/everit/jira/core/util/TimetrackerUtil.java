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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.everit.jira.settings.TimeTrackerSettingsHelper;
import org.everit.jira.settings.dto.TimeTrackerGlobalSettings;
import org.everit.jira.settings.dto.TimeZoneTypes;
import org.everit.jira.timetracker.plugin.dto.WorklogValues;
import org.everit.jira.timetracker.plugin.util.DateTimeConverterUtil;
import org.joda.time.DateTimeZone;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneServiceImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.google.gson.Gson;

/**
 * Utility class for timetracker.
 */
public final class TimetrackerUtil {

  public static final int FIFTEEN_MINUTES = 15;

  public static final int FIVE_MINUTES = 5;

  public static final int TEN_MINUTES = 10;

  public static final int THIRTY_MINUTES = 30;

  public static final int TWENTY_MINUTES = 20;

  /**
   * Check the issue original estimated time. If null then the original estimated time wasn't
   * specified, else compare the spent time with the original estimated time.
   *
   * @param issue
   *          The issue.
   * @return True if not specified, bigger or equals whit spent time else false.
   */
  public static boolean checkIssueEstimatedTime(final MutableIssue issue,
      final List<Pattern> collectorIssueIds) {
    String issueKey = issue.getKey();
    if (collectorIssueIds != null) {
      for (Pattern issuePattern : collectorIssueIds) {
        // check matches
        boolean isCollectorIssue = issuePattern.matcher(issueKey).matches();
        if (isCollectorIssue) {
          return true;
        }
      }
    }
    Long estimated = issue.getEstimate();
    Status issueStatus = issue.getStatusObject();
    String issueStatusId = issueStatus.getId();
    if (((estimated == null) || (estimated == 0)) && !"6".equals(issueStatusId)) {
      return false;
    }
    return true;
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
   * Convert Json in to WorklogValues.
   *
   * @param json
   *          The worklog values in Json.
   * @return The Worklog values objcet.
   */
  public static WorklogValues convertJsonToWorklogValues(final String json) {
    if (json == null) {
      throw new NullPointerException("EMPTY_JSON");
    }
    WorklogValues worklogValues = new Gson()
        .fromJson(json, WorklogValues.class);

    if (worklogValues.getEndTime() == null) {
      worklogValues.setEndTime(DateTimeConverterUtil.dateTimeToString(new Date()));
    }
    if (worklogValues.getComment() != null) {
      worklogValues.setCommentForActions(worklogValues.getComment());
      String comment = worklogValues.getComment();
      comment = comment.replace("\"", "\\\"");
      comment = comment.replace("\r", "\\r");
      comment = comment.replace("\n", "\\n");

      worklogValues.setComment(comment);
    } else {
      worklogValues.setCommentForActions("");
      worklogValues.setComment("");
    }

    return worklogValues;
  }

  /**
   * Convert {@link WorklogValues} class to json string.
   *
   * @param worklogValues
   *          the {@link WorklogValues} object. Cannot be <code>null</code>.
   *
   * @return the json string.
   *
   * @throws NullPointerException
   *           if worklogValues parameter is <code>null</code>.
   */
  public static String convertWorklogValuesToJson(final WorklogValues worklogValues) {
    if (worklogValues == null) {
      throw new NullPointerException("EMPTY_FILTER");
    }
    return new Gson().toJson(worklogValues);
  }

  /**
   * Get the i18nKey property value in default locale language or if the key not defined give back
   * the key.
   *
   * @param i18nKey
   *          The property key.
   * @return The property value or the key.
   */
  public static String getI18nText(final String i18nKey) {
    JiraAuthenticationContext jiraAuthenticationContext =
        ComponentAccessor.getJiraAuthenticationContext();
    I18nHelper i18Helper = jiraAuthenticationContext.getI18nHelper();

    if (i18Helper.isKeyDefined(i18nKey)) {
      return i18Helper.getText(i18nKey);
    }
    return i18nKey;
  }

  private static TimeZoneServiceImpl getInitializedTimeZoneServeice() {
    return new TimeZoneServiceImpl(ComponentAccessor.getApplicationProperties(),
        ComponentAccessor.getPermissionManager(),
        ComponentAccessor.getUserPreferencesManager());
  }

  /**
   * Give back the logged user userName.
   *
   * @return The logged application user userName or empty String.
   */
  public static String getLoggedUserName() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();
    if (user == null) {
      return "";
    }
    return user.getUsername().toLowerCase(Locale.getDefault());
  }

  /**
   * Get the logged user {@link DateTimeZone}.
   *
   * @return The logged user {@link DateTimeZone}.
   */
  public static DateTimeZone getLoggedUserTimeZone() {
    TimeTrackerSettingsHelper settingsHelper =
        ComponentAccessor.getOSGiComponentInstanceOfType(TimeTrackerSettingsHelper.class);
    TimeTrackerGlobalSettings globalSettings = settingsHelper.loadGlobalSettings();
    TimeZoneTypes timeZoneTypes = globalSettings.getTimeZone();
    if (TimeZoneTypes.USER.equals(timeZoneTypes)) {
      TimeZoneServiceImpl timeZoneServiceImpl = getInitializedTimeZoneServeice();
      JiraServiceContext serviceContext = getServiceContext();
      TimeZone timeZone = timeZoneServiceImpl.getUserTimeZone(serviceContext);
      return DateTimeZone.forTimeZone(timeZone);
    } else {
      return getSystemTimeZone();
    }

  }

  private static JiraServiceContext getServiceContext() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    return new JiraServiceContextImpl(user);
  }

  /**
   * Get the system {@link DateTimeZone}.
   *
   * @return The system {@link DateTimeZone}.
   */
  public static DateTimeZone getSystemTimeZone() {
    TimeZoneServiceImpl timeZoneServiceImpl = getInitializedTimeZoneServeice();
    JiraServiceContext serviceContext = getServiceContext();
    TimeZone timeZone = timeZoneServiceImpl.getJVMTimeZoneInfo(serviceContext).toTimeZone();
    return DateTimeZone.forTimeZone(timeZone);
  }

  /**
   * Check the given date, the user have worklogs or not.
   *
   * @param date
   *          The date what have to check. The date have to be set to day start and after that
   *          convert to system Time Zone.
   * @return If The user have worklogs the given date then true, esle false.
   */
  public static boolean isContainsWorklog(final Date date) {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();

    Calendar startDate = Calendar.getInstance();
    startDate.setTime(date);
    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.DAY_OF_MONTH, 1);

    List<EntityCondition> exprList =
        WorklogUtil.createWorklogQueryExprListWithPermissionCheck(user, startDate.getTimeInMillis(),
            endDate.getTimeInMillis());

    List<GenericValue> worklogGVList =
        ComponentAccessor.getOfBizDelegator().findByAnd("IssueWorklogView", exprList);

    return !((worklogGVList == null) || worklogGVList.isEmpty());
  }

  /**
   * Check the user is logged or not.
   *
   * @return True if we have logged user else false.
   */
  public static boolean isUserLogged() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getLoggedInUser();
    if (user == null) {
      return false;
    }
    return true;
  }

  /**
   * URL encode the given String with UTF-8 charset.
   *
   * @param encode
   *          The string to encode.
   * @return The encoded String or the original if there was an exception.
   */
  public static String urlEndcodeHandleException(final String encode) {
    try {
      return URLEncoder.encode(encode, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return encode;
    }
  }

  /**
   * Validate the time change value. Possible values is 5, 10, 15, 20, 30.
   *
   * @param changeValue
   *          the change value.
   *
   * @return true if changeValue is valid change time value.
   * @throws NumberFormatException
   *           if parse failed.
   */
  public static boolean validateTimeChange(final String changeValue)
      throws NumberFormatException {
    int changeValueInt = Integer.parseInt(changeValue);

    switch (changeValueInt) {
      case FIVE_MINUTES:
        return true;
      case TEN_MINUTES:
        return true;
      case FIFTEEN_MINUTES:
        return true;
      case TWENTY_MINUTES:
        return true;
      case THIRTY_MINUTES:
        return true;
      default:
        return false;
    }
  }

  private TimetrackerUtil() {
  }
}
