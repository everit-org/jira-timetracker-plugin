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
package org.everit.jira.timetracker.plugin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.everit.jira.timetracker.plugin.dto.WorklogValues;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.google.gson.Gson;

/**
 * The Jira Timetracker plugin utils class.
 */
public final class JiraTimetrackerUtil {

  /**
   * The plugin calendar both type code.
   */
  public static final int BOTH_TYPE_CALENDAR_CODE = 3;

  public static final int DAY_INTERVAL = 0;

  private static I18nHelper defaultLocalI18n =
      ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());

  public static final int FIFTEEN_MINUTES = 15;

  public static final int FIVE_MINUTES = 5;

  /**
   * The plugin calendar inline code.
   */
  public static final int INLINE_CALENDAR_CODE = 2;

  /**
   * Monday first day of the week.
   */
  public static final int MONDAY_CALENDAR_FDOW = 1;

  public static final int MONTH_INTERVAL = 2;

  /**
   * Sunday first day of the week.
   */
  public static final int SUNDAY_CALENDAR_FDOW = 0;

  public static final int TEN_MINUTES = 10;

  public static final int THIRTY_MINUTES = 30;

  public static final int TWENTY_MINUTES = 20;

  public static final int WEEK_INTERVAL = 1;

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
    return new Gson()
        .fromJson(json, WorklogValues.class);
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
    if (defaultLocalI18n.isKeyDefined(i18nKey)) {
      return defaultLocalI18n.getText(i18nKey);
    }
    return i18nKey;
  }

  /**
   * Give back the logged user userName.
   *
   * @return The logged application user userName or empty String.
   */
  public static String getLoggedUserName() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
    if (user == null) {
      return "";
    }
    return user.getUsername().toLowerCase(Locale.getDefault());
  }

  /**
   * Check the user is logged or not.
   *
   * @return True if we have logged user else false.
   */
  public static boolean isUserLogged() {
    JiraAuthenticationContext authenticationContext = ComponentAccessor
        .getJiraAuthenticationContext();
    ApplicationUser user = authenticationContext.getUser();
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

  private JiraTimetrackerUtil() {
  }

}
