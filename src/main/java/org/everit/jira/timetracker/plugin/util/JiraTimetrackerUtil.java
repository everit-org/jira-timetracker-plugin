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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

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

  private static final String CUSTOMER_EMAIL_PREFIX = "The customer email address is ";

  public static final int DAY_INTERVAL = 0;

  private static I18nHelper defaultLocalI18n =
      ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());

  private static final String FEEDBACK_EMAIL_SUBJECT = "[JTTP] feedback";

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
   * The plugin calendar popup code.
   */
  public static final int POPUP_CALENDAR_CODE = 1;

  private static final String PREFIX_PLUGIN_RATED = "The plugin rated to: ";

  private static final String REPORTING_EMAIL_SUBJECT = "[JTTP] reporting";

  private static StringBuilder reportingMailBody;

  private static final int SECOND_BEETWEN_FEEDBACKS = 30;

  private static final String SESSION_DATE_KEY = "jttpFeedbackTS";

  /**
   * Sunday first day of the week.
   */
  public static final int SUNDAY_CALENDAR_FDOW = 0;

  public static final int WEEK_INTERVAL = 1;

  /**
   * Add answer to reporting mail body.
   *
   * @param answerKey
   *          The answer key.
   * @param answers
   *          The answers from report.
   */
  public static void addAnswersToReportingMailBody(final String answerKey, final String[] answers) {
    if ((answers != null) && (answers.length != 0)) {
      reportingMailBody.append(getI18nQuestion(answerKey));
      reportingMailBody.append(": ");
      for (String answer : answers) {
        if ((answer != null) && !answer.isEmpty()) {
          reportingMailBody.append(getI18nAnswerOrTheAnswer(answerKey, answer.trim()));
          reportingMailBody.append("; ");
        }
      }
      reportingMailBody.append("\n");
    }
  }

  /**
   * Add answer to reporting mail body.
   *
   * @param answerKey
   *          The answer key.
   * @param answer
   *          The answer from report.
   */
  public static void addAnswerToReportingMailBody(final String answerKey, final String answer) {
    if ((answer != null) && !answer.isEmpty()) {
      reportingMailBody.append(getI18nQuestion(answerKey));
      reportingMailBody.append(": ");
      reportingMailBody.append(getI18nAnswerOrTheAnswer(answerKey, answer.trim()));
      reportingMailBody.append("\n");
    }
  }

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
   * Create the feedback email content.
   *
   * @param customerEmail
   *          The customer email address.
   * @param rating
   *          The plugin rating.
   * @param feedBack
   *          The feedback content.
   * @return The appended feedback content.
   */
  public static String createFeedbackMailBody(final String customerEmail, final String rating,
      final String feedBack) {
    StringBuilder mailBody = new StringBuilder();
    if ((customerEmail != null) && !customerEmail.isEmpty()) {
      mailBody.append(CUSTOMER_EMAIL_PREFIX);
      mailBody.append(customerEmail);
      mailBody.append("\n");
    }
    mailBody.append(PREFIX_PLUGIN_RATED);
    mailBody.append(rating);
    mailBody.append("\n");
    mailBody.append(feedBack);
    return mailBody.toString();
  }

  /**
   * Generate subject to the feedback email.
   *
   * @param pluginVersion
   *          The plugin version.
   * @return The complete feedback email subject.
   */
  public static String createFeedbackMailSubject(final String pluginVersion) {
    return FEEDBACK_EMAIL_SUBJECT + " " + pluginVersion + " - "
        + DateTimeConverterUtil.dateToString(new Date());
  }

  /**
   * Generate subject to the reporting email.
   *
   * @param pluginVersion
   *          The plugin version.
   * @return The complete reportin email subject.
   */
  public static String createReportingMailSubject(final String pluginVersion) {
    return REPORTING_EMAIL_SUBJECT + " " + pluginVersion + " - "
        + DateTimeConverterUtil.dateToString(new Date());
  }

  /**
   * Create the feedback email content.
   *
   * @param customerEmail
   *          The customer email address.
   */
  public static void createReportMailBody(final String customerEmail) {
    reportingMailBody = new StringBuilder();
    if ((customerEmail != null) && !customerEmail.isEmpty()) {
      reportingMailBody.append(CUSTOMER_EMAIL_PREFIX);
      reportingMailBody.append(customerEmail);
      reportingMailBody.append("\n");
    }
  }

  /**
   * Return the reporting maily body.
   *
   * @return The mail complete body.
   */
  public static String finishReportingMailBody() {
    return reportingMailBody.toString();
  }

  /**
   * Check the customer mail form feedback and reporting dialog and remove the unexpected parts.
   *
   * @param originalCustomerMail
   *          The original mail.
   * @return The checked mail.
   */
  public static String getCheckCustomerMail(final String originalCustomerMail) {
    String customerMail = originalCustomerMail.trim();
    if (customerMail.contains("\n")) {
      customerMail = customerMail.substring(0, customerMail.indexOf("\n"));
    }
    if (customerMail.contains("\t")) {
      customerMail = customerMail.substring(0, customerMail.indexOf("\t"));
    }
    return customerMail;
  }

  private static String getI18nAnswerOrTheAnswer(final String answerKey, final String answer) {
    String i18nKey = "jttp.reporting." + answerKey + "." + answer;
    if (defaultLocalI18n.isKeyDefined(i18nKey)) {
      return defaultLocalI18n.getText(i18nKey);
    }
    return answer;
  }

  private static String getI18nQuestion(final String answerKey) {
    String[] answerKeySplit = answerKey.split("\\.");
    String i18nKey = "jttp.reporting.question." + answerKeySplit[answerKeySplit.length - 1];
    if (defaultLocalI18n.isKeyDefined(i18nKey)) {
      return defaultLocalI18n.getText(i18nKey);
    }
    return answerKey;
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
   * Load and check the SESSION_DATE_KEY value from the session. If the key value is a Date then
   * check the difference the stored and the current time.
   *
   * @param session
   *          The session where we except the key.
   * @return True if no key found or the time difference big enough.
   */
  public static boolean loadAndCheckFeedBackTimeStampFromSession(final HttpSession session) {
    Object data = session.getAttribute(SESSION_DATE_KEY);
    if (!(data instanceof Date)) {
      return true;
    }
    Date fromSession = (Date) data;
    if (SECOND_BEETWEN_FEEDBACKS >= DateTimeConverterUtil.getDateDifference(fromSession,
        new Date())) {
      return false;
    }
    return true;
  }

  /**
   * Save the current date in to the session with SESSION_DATE_KEY.
   *
   * @param session
   *          The session.
   */
  public static void saveFeedBackTimeStampToSession(final HttpSession session) {
    session.setAttribute(SESSION_DATE_KEY, new Date());
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

  private JiraTimetrackerUtil() {
  }

}
