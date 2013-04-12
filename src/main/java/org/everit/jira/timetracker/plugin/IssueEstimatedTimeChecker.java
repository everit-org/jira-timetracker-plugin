package org.everit.jira.timetracker.plugin;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.mail.Email;
import com.atlassian.mail.queue.SingleMailQueueItem;

public class IssueEstimatedTimeChecker implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(IssueEstimatedTimeChecker.class);
    /**
     * The email address of the sender.
     */
    private final String emailSender;
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
     *            TThe email address of the sender, come from the plugin properties file.
     */
    public IssueEstimatedTimeChecker(final String emailSender, final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.emailSender = emailSender;
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Create the mail body String.
     * 
     * @param issue
     *            The issue object.
     * @return The email body.
     */
    private String createBodyString(final MutableIssue issue) {
        String baseURL = ComponentManager.getInstance().getApplicationProperties().getString(APKeys.JIRA_BASEURL);
        String bodyString = "Work was logged on issue " + issue.getKey() + " without remaining time."
                + "\n"
                + "\n" + "The issue summary: " + issue.getSummary()
                + "\n" + "The issue description: " + issue.getDescription()
                + "\n" + "The issue key: " + issue.getKey()
                + "\n" + "The issue URL: " + baseURL + "/browse/" + issue.getKey();
        return bodyString;
    }

    /**
     * The IssueEstimatedTimeChecker rum method. Check the last 24 hour. Make a worklog query for the last 24 hour added
     * and updated worklogs. Check the worklogs issues. If the original estimated time less then the spent time send a
     * notification email.
     */
    @Override
    public void run() {
        // check the last 24 hour
        checkerCalendar = Calendar.getInstance();
        Date end = checkerCalendar.getTime();
        checkerCalendar.add(Calendar.DAY_OF_MONTH, -1);
        Date start = checkerCalendar.getTime();

        // query for all worklogs issues from yesterday. check the worklog update parameter
        EntityExpr startExpr =
                new EntityExpr("updated", EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(start.getTime()));
        EntityExpr endExpr =
                new EntityExpr("updated", EntityOperator.LESS_THAN, new Timestamp(end.getTime()));

        List<EntityExpr> exprList = new ArrayList<EntityExpr>();
        if (startExpr != null) {
            exprList.add(startExpr);
        }
        if (endExpr != null) {
            exprList.add(endExpr);
        }
        Set<Long> issueIdSet = null;
        try {
            List<GenericValue> worklogGVList = CoreFactory.getGenericDelegator().findByAnd("Worklog", exprList);
            issueIdSet = new HashSet<Long>();
            for (GenericValue worklogGv : worklogGVList) {
                Long issueId = new Long(worklogGv.getString("issue"));
                issueIdSet.add(issueId);
            }
        } catch (GenericEntityException e) {
            LOGGER.info("Error when try to make a worklog query. ", e);
        }

        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        for (Long issueId : issueIdSet) {
            MutableIssue issueObject = issueManager.getIssueObject(issueId);
            if (!JiraTimetrackerUtil.checkIssueEstimatedTime(issueObject, jiraTimetrackerPlugin.getCollectorIssuePatterns())) {
                // send mail
                sendNotificationEmail(issueObject.getReporterUser().getEmailAddress(), issueObject.getProjectObject()
                        .getLeadUser().getEmailAddress(), issueObject);
            }
        }

    }

    /**
     * Create and send a notification mail.
     * 
     * @param issueReporter
     *            The mail address where have to send the notification.
     * @param issue
     *            The notification subject.
     */
    private void sendNotificationEmail(final String issueReporter, final String projectLead, final MutableIssue issue) {
        Email email = new Email(issueReporter);
        email.setFrom(emailSender);
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
