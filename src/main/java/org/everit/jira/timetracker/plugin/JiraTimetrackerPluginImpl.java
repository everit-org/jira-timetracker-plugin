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

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.EveritWorklogComparator;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * The implementation of the {@link JiraTimetrackerPlugin}.
 */
public class JiraTimetrackerPluginImpl implements JiraTimetrackerPlugin, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public JiraTimetrackerPluginImpl() {
    }

    @Override
    public ActionResult createWorklog(final String issueId, final String comment, final String dateFormated,
            final String startTime, final String timeSpent) {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        MutableIssue issue = issueManager.getIssueObject(issueId);
        if (issue == null) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.invalide_issue", issueId);
        }
        PermissionManager permissionManager = ComponentManager.getInstance()
                .getPermissionManager();
        if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue, user)) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.nopermission_issue", issueId);
        }
        String dateAndTime = dateFormated + " " + startTime;
        Date date;
        try {
            date = DateTimeConverterUtil.stringToDateAndTime(dateAndTime);
        } catch (ParseException e) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.date_parse", dateAndTime);
        }

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl
                .issue(issue)
                .startDate(date)
                .timeSpent(timeSpent)
                .comment(comment)
                .buildNewEstimate();
        WorklogService worklogService = ComponentManager.getComponentInstanceOfType(WorklogService.class);
        WorklogResult worklogResult = worklogService.validateCreate(serviceContext, params);
        if (worklogResult == null) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.worklog.create.fail");
        }
        worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, false);
        return new ActionResult(ActionResultStatus.SUCCESS, "plugin.worklog.create.success");
    }

    @Override
    public ActionResult deleteWorklog(final Long id) {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
        WorklogService worklogService = ComponentManager.getComponentInstanceOfType(WorklogService.class);
        WorklogResult deleteWorklogResult = worklogService.validateDelete(serviceContext, id);
        if (deleteWorklogResult == null) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.worklog.delete.fail", id.toString());
        }
        worklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext, deleteWorklogResult, false);
        return new ActionResult(ActionResultStatus.SUCCESS, "plugin.worklog.delete.success", id.toString());
    }

    @Override
    public ActionResult editWorklog(final Long id, final String issueId, final String comment, final String time,
            final String timeSpent) {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        JiraServiceContext serviceContext = new JiraServiceContextImpl(user);

        WorklogManager worklogManager = ComponentManager.getInstance().getWorklogManager();
        Worklog worklog = worklogManager.getById(id);
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        MutableIssue issue = issueManager.getIssueObject(issueId);
        if (issue == null) {
            return new ActionResult(ActionResultStatus.FAIL, "plugin.invalide_issue", issueId);
        }
        if (!worklog.getIssue().getKey().equals(issueId)) {
            PermissionManager permissionManager = ComponentManager.getInstance()
                    .getPermissionManager();
            if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue, user)) {
                return new ActionResult(ActionResultStatus.FAIL, "plugin.nopermission_issue", issueId);
            }
            ActionResult deleteResult = deleteWorklog(id);
            if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
                return deleteResult;
            }
            String date = DateTimeConverterUtil.dateToString(worklog.getStartDate());
            ActionResult createResult = createWorklog(issueId, comment, date, time, timeSpent);
            if (createResult.getStatus() == ActionResultStatus.FAIL) {
                return createResult;
            }
        } else {
            String dateFormated = DateTimeConverterUtil.dateToString(worklog.getStartDate());
            String dateAndTime = dateFormated + " " + time;
            Date date;
            try {
                date = DateTimeConverterUtil.stringToDateAndTime(dateAndTime);
            } catch (ParseException e) {
                return new ActionResult(ActionResultStatus.FAIL, "plugin.date_parse" + dateAndTime);
            }
            WorklogInputParameters params = WorklogInputParametersImpl.issue(issue)
                    .startDate(date)
                    .timeSpent(timeSpent)
                    .comment(comment)
                    .worklogId(id).issue(issue)
                    .build();
            WorklogService worklogService = ComponentManager.getComponentInstanceOfType(WorklogService.class);
            WorklogResult worklogResult = worklogService.validateUpdate(serviceContext, params);
            if (worklogResult == null) {
                return new ActionResult(ActionResultStatus.FAIL, "plugin.worklog.update.fail");
            }

            worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, false);

        }
        return new ActionResult(ActionResultStatus.SUCCESS, "plugin.worklog.update.success");

    }

    @Override
    public List<Issue> getIssues() throws GenericEntityException {
        List<GenericValue> issuesGV = null;
        issuesGV = CoreFactory.getGenericDelegator().findAll("Issue");
        List<Issue> issues = new ArrayList<Issue>();
        for (GenericValue issueGV : issuesGV) {
            issues.add(IssueImpl.getIssueObject(issueGV));
        }
        return issues;

    }

    @Override
    public List<String> getProjectsId() throws GenericEntityException {
        List<String> projectsId = new ArrayList<String>();
        List<GenericValue> projectsGV = CoreFactory.getGenericDelegator().findAll("Project");
        for (GenericValue project : projectsGV) {
            projectsId.add(project.getString("id"));
        }
        return projectsId;
    }

    @Override
    public EveritWorklog getWorklog(final Long worklogId) throws ParseException {
        WorklogManager worklogManager = ComponentManager.getInstance().getWorklogManager();
        Worklog worklog = worklogManager.getById(worklogId);
        return new EveritWorklog(worklog);
    }

    @Override
    public List<EveritWorklog> getWorklogs(final Date date) throws GenericEntityException, ParseException {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();

        Date startDate = (Date) date.clone();
        startDate.setHours(0);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        Date endDate = (Date) date.clone();
        endDate.setHours(DateTimeConverterUtil.LAST_HOUR_OF_DAY);
        endDate.setMinutes(DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
        endDate.setSeconds(DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);

        EntityExpr startExpr =
                new EntityExpr("startdate", EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime()));
        EntityExpr endExpr =
                new EntityExpr("startdate", EntityOperator.LESS_THAN, new Timestamp(endDate.getTime()));
        EntityExpr userExpr =
                new EntityExpr("author", EntityOperator.EQUALS, user.getName());

        List<EntityExpr> exprList = new ArrayList<EntityExpr>();
        exprList.add(userExpr);
        if (startExpr != null) {
            exprList.add(startExpr);
        }
        if (endExpr != null) {
            exprList.add(endExpr);
        }
        List<GenericValue> worklogGVList = CoreFactory.getGenericDelegator().findByAnd("Worklog", exprList);
        List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
        for (GenericValue worklogGv : worklogGVList) {
            EveritWorklog worklog = new EveritWorklog(worklogGv);
            worklogs.add(worklog);
        }

        Collections.sort(worklogs, new EveritWorklogComparator());
        return worklogs;
    }

    @Override
    public String lastEndTime(final List<EveritWorklog> worklogs) throws ParseException {
        if ((worklogs == null) || (worklogs.size() == 0)) {
            return "08:00";
        }
        String endTime = worklogs.get(0).getEndTime();
        for (int i = 1; i < worklogs.size(); i++) {
            Date first = DateTimeConverterUtil.stringTimeToDateTime(worklogs.get(i - 1).getEndTime());
            Date second = DateTimeConverterUtil.stringTimeToDateTime(worklogs.get(i).getEndTime());
            if (first.compareTo(second) == 1) {
                endTime = worklogs.get(i - 1).getEndTime();
            } else {
                endTime = worklogs.get(i).getEndTime();
            }
        }
        return endTime;
    }

    @Override
    public String summary(final Date startSummary, final Date finishSummary) throws GenericEntityException {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        startSummary.setSeconds(0);
        finishSummary.setSeconds(0);
        EntityExpr startExpr =
                new EntityExpr("startdate",
                        EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(startSummary.getTime()));
        EntityExpr endExpr =
                new EntityExpr("startdate", EntityOperator.LESS_THAN, new Timestamp(finishSummary.getTime()));
        EntityExpr userExpr = null;
        if (user != null) {
            userExpr = new EntityExpr("author", EntityOperator.EQUALS, user.getName());
        }
        List<EntityExpr> exprs = new ArrayList<EntityExpr>();
        if (userExpr != null) {
            exprs.add(userExpr);
        }
        if (startExpr != null) {
            exprs.add(startExpr);
        }
        if (endExpr != null) {
            exprs.add(endExpr);
        }
        List<GenericValue> worklogs = CoreFactory.getGenericDelegator().findByAnd("Worklog", exprs);

        long timeSpent = 0;
        Iterator<GenericValue> worklogsIterator = worklogs.iterator();
        while (worklogsIterator.hasNext()) {
            GenericValue worklog = worklogsIterator.next();
            timeSpent = timeSpent + worklog.getLong("timeworked").longValue();
        }
        return DateTimeConverterUtil.secondConvertToString(timeSpent);
    }

}
