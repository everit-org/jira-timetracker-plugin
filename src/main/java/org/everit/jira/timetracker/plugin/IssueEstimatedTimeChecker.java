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
package org.everit.jira.timetracker.plugin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.mail.Email;
import com.atlassian.mail.queue.SingleMailQueueItem;

/**
 * Issue Estimated time checker.
 */
public class IssueEstimatedTimeChecker implements Runnable {

  // /**
  // * Logger.
  // */
  // private static final Logger LOGGER = Logger.getLogger(IssueEstimatedTimeChecker.class);
  // /**
  // * The email address of the sender.
  // */
  // private String emailSender;
  /**
   * The check dates calendar.
   */
  private Calendar checkerCalendar;

  /**
   * The {@link JiraTimetrackerPlugin}.
   */
  private JiraTimetrackerPlugin jiraTimetrackerPlugin;

  /**
   * Simple constructor.
   *
   * @param emailSender
   *          TThe email address of the sender, come from the plugin properties file.
   */
  public IssueEstimatedTimeChecker(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
    this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
  }

  /**
   * Create the mail body String.
   *
   * @param issue
   *          The issue object.
   * @return The email body.
   */
  private String createBodyString(final MutableIssue issue) {
    String baseURL = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
    String bodyString = "Work was logged on issue " + issue.getKey() + " without remaining time."
        + "\n"
        + "\n" + "The issue summary: " + issue.getSummary()
        + "\n" + "The issue description: " + issue.getDescription()
        + "\n" + "The issue key: " + issue.getKey()
        + "\n" + "The issue URL: " + baseURL + "/browse/" + issue.getKey();
    return bodyString;
  }

  // public String getEmailSender() {
  // return emailSender;
  // }

  /**
   * The IssueEstimatedTimeChecker run method. Check the last 24 hour. Make a worklog query for the
   * last 24 hour added and updated worklogs. Check the worklogs issues. If the original estimated
   * time less then the spent time send a notification email.
   */
  @Override
  public void run() {
    // check the last 24 hour
    checkerCalendar = Calendar.getInstance();
    Date end = checkerCalendar.getTime();
    checkerCalendar.add(Calendar.DAY_OF_MONTH, -1);
    Date start = checkerCalendar.getTime();

    // query for all worklogs issues from yesterday. check the worklog update parameter
    EntityExpr startExpr = new EntityExpr("updated", EntityOperator.GREATER_THAN_EQUAL_TO,
        new Timestamp(
            start.getTime()));
    EntityExpr endExpr = new EntityExpr("updated", EntityOperator.LESS_THAN,
        new Timestamp(end.getTime()));

    List<EntityCondition> exprList = new ArrayList<EntityCondition>();
    exprList.add(startExpr);
    exprList.add(endExpr);
    Set<Long> issueIdSet = null;
    List<GenericValue> worklogGVList = ComponentAccessor.getOfBizDelegator().findByAnd("Worklog",
        exprList);
    issueIdSet = new HashSet<Long>();
    for (GenericValue worklogGv : worklogGVList) {
      Long issueId = Long.valueOf(worklogGv.getString("issue"));
      issueIdSet.add(issueId);
    }

    IssueManager issueManager = ComponentAccessor.getIssueManager();
    for (Long issueId : issueIdSet) {
      MutableIssue issueObject = issueManager.getIssueObject(issueId);
      if (!JiraTimetrackerUtil.checkIssueEstimatedTime(issueObject,
          jiraTimetrackerPlugin.getCollectorIssuePatterns())) {
        // send mail
        sendNotificationEmail(issueObject.getReporterUser().getEmailAddress(), issueObject
            .getProjectObject()
            .getProjectLead().getEmailAddress(), issueObject);
      }
    }

  }

  /**
   * Create and send a notification mail.
   *
   * @param issueReporter
   *          The mail address where have to send the notification.
   * @param issue
   *          The notification subject.
   */
  private void sendNotificationEmail(final String issueReporter, final String projectLead,
      final MutableIssue issue) {
    Email email = new Email(issueReporter);
    // email.setFrom(emailSender);
    if (!issueReporter.equals(projectLead)) {
      email.setCc(projectLead);
    }
    email.setSubject("No more estimated time in the " + issue.getKey() + " issue.");
    email.setBody(createBodyString(issue));
    SingleMailQueueItem singleMailQueueItem = new SingleMailQueueItem(email);
    singleMailQueueItem.setMailThreader(null);
    ComponentAccessor.getMailQueue().addItem(singleMailQueueItem);
  }
}
