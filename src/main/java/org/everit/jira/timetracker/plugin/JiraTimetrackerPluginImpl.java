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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.EveritWorklogComparator;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

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
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The implementation of the {@link JiraTimetrackerPlugin}.
 */
public class JiraTimetrackerPluginImpl implements JiraTimetrackerPlugin, Serializable, InitializingBean, DisposableBean {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerPluginImpl.class);

    /**
     * The name of the plugin properties files.
     */
    private static final String PROPERTIES = "jiraTimetracker.properties";
    /**
     * The exclude dates key in the properties file..
     */
    private static final String EMAIL_SENDER = "EMAIL_SENDER";
    /**
     * The exclude dates key in the properties file..
     */
    private static final String ISSUE_CHECK_TIME = "ISSUE_CHECK_TIME";
    /**
     * The exclude dates key in the properties file..
     */
    private static final String EXCLUDE_DATES = "EXCLUDE_DATES";
    /**
     * The include dates key in the properties file..
     */
    private static final String INCLUDE_DATES = "INCLUDE_DATES";
    /**
     * The plugin settings key prefix.
     */
    private static final String JTTP_PLUGIN_SETTINGS_KEY_PREFIX = "jttp";
    /**
     * The plugin setting Summary Filters key.
     */
    private static final String JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS = "SummaryFilters";
    /**
     * The plugin setting is calendar popup key.
     */
    private static final String JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP = "isCalendarPopup";
    /**
     * The plugin setting is actual date key.
     */
    private static final String JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE = "isActualDate";
    /**
     * A day in minutes.
     */
    private static final int ONE_DAY_IN_MINUTES = 1440;

    /**
     * The PluginSettingsFactory.
     */
    private final PluginSettingsFactory settingsFactory;
    /**
     * The plugin setting form the settingsFactory.
     */
    private PluginSettings pluginSettings;
    /**
     * The plugin global setting form the settingsFactory.
     */
    private PluginSettings globalSettings;
    /**
     * The plugin setting values.
     */
    private PluginSettingsValues pluginSettingsValues;
    /**
     * The email Sender from the properties file.
     */
    private String emailSender;
    /**
     * The issue check time in minutes.
     */
    private long issueCheckTimeInMinutes;
    /**
     * The exclude dates from the properties file.
     */
    private String excludeDatesString;
    /**
     * The include dates from the properties file.
     */
    private String includeDatesString;
    /**
     * The parsed exclude dates.
     */
    private final Set<String> excludeDates = new HashSet<String>();
    /**
     * The parsed include dates.
     */
    private final Set<String> includeDates = new HashSet<String>();
    /**
     * The summary filter issues ids.
     */
    private List<Long> summaryFilteredIssuesId;
    /**
     * The plugin Scheduled Executor Service.
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    /**
     * The issues Estimated Time Checker Future.
     */
    private ScheduledFuture<?> issueEstimatedTimeCheckerFuture;

    /**
     * Default constructor.
     */
    public JiraTimetrackerPluginImpl(final PluginSettingsFactory settingFactory) {
        settingsFactory = settingFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        importProperties();

        final Runnable issueEstimatedTimeChecker = new IssueEstimatedTimeChecker(emailSender);

        // FIXME this is TEST initTIME
        Calendar now = Calendar.getInstance();
        Long nowPlusTWOMin = (long) ((now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE) + 1);

        issueEstimatedTimeCheckerFuture = scheduledExecutorService.scheduleAtFixedRate(issueEstimatedTimeChecker,
                calculateInitialDelay(nowPlusTWOMin), // FIXME fix the time
                                                      // calculateInitialDelay(issueCheckTimeInMinutes),
                ONE_DAY_IN_MINUTES, TimeUnit.MINUTES);
    }

    private long calculateInitialDelay(final long time) {
        Calendar now = Calendar.getInstance();
        long hours = now.get(Calendar.HOUR_OF_DAY);
        long minutes = now.get(Calendar.MINUTE);
        long initialDelay = time - ((hours * 60) + minutes);
        if (initialDelay < 0) {
            initialDelay = initialDelay + ONE_DAY_IN_MINUTES;
        }
        return initialDelay;
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
        worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);

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
    public void destroy() throws Exception {
        scheduledExecutorService.shutdown();
        issueEstimatedTimeCheckerFuture.cancel(true);
        LOGGER.info("JiraTimetrackerPluginImpl destroyed");
    }

    @Override
    public ActionResult editWorklog(final Long id, final String issueId, final String comment,
            final String dateFormated,
            final String time,
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
            // String dateCreate = DateTimeConverterUtil.dateToString(worklog.getStartDate());
            // String dateCreate = date;
            ActionResult createResult = createWorklog(issueId, comment, dateFormated, time, timeSpent);
            if (createResult.getStatus() == ActionResultStatus.FAIL) {
                return createResult;
            }
        } else {
            // String dateFormated = DateTimeConverterUtil.dateToString(worklog.getStartDate());
            // String dateFormated = date;
            String dateAndTime = dateFormated + " " + time;
            Date dateCreate;
            try {
                dateCreate = DateTimeConverterUtil.stringToDateAndTime(dateAndTime);
            } catch (ParseException e) {
                return new ActionResult(ActionResultStatus.FAIL, "plugin.date_parse" + dateAndTime);
            }
            WorklogInputParameters params = WorklogInputParametersImpl.issue(issue)
                    .startDate(dateCreate)
                    .timeSpent(timeSpent)
                    .comment(comment)
                    .worklogId(id).issue(issue)
                    .build();
            WorklogService worklogService = ComponentManager.getComponentInstanceOfType(WorklogService.class);
            WorklogResult worklogResult = worklogService.validateUpdate(serviceContext, params);
            if (worklogResult == null) {
                return new ActionResult(ActionResultStatus.FAIL, "plugin.worklog.update.fail");
            }

            worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);

        }
        return new ActionResult(ActionResultStatus.SUCCESS, "plugin.worklog.update.success");

    }

    @Override
    public Date firstMissingWorklogsDate() throws GenericEntityException {
        Calendar scanedDate = Calendar.getInstance();
        // one week
        scanedDate
                .set(Calendar.DAY_OF_YEAR, scanedDate.get(Calendar.DAY_OF_YEAR) - DateTimeConverterUtil.DAYS_PER_WEEK);
        for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
            // convert date to String
            Date scanedDateDate = scanedDate.getTime();
            String scanedDateString = DateTimeConverterUtil.dateToString(scanedDateDate);
            // check excludse - pass
            if (excludeDates.contains(scanedDateString)) {
                scanedDate
                        .set(Calendar.DAY_OF_YEAR, scanedDate.get(Calendar.DAY_OF_YEAR) + 1);
                continue;
            }
            // check includes - not check weekend
            if (!includeDates.contains(scanedDateString)) {
                // check weekend - pass
                if ((scanedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                        || (scanedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                    scanedDate
                            .set(Calendar.DAY_OF_YEAR, scanedDate.get(Calendar.DAY_OF_YEAR) + 1);
                    continue;
                }
            }
            // check worklog. if no worklog set result else ++ scanedDate
            boolean isDateContainsWorklog = isContainsWorklog(scanedDateDate);
            if (!isDateContainsWorklog) {
                return scanedDateDate;
            } else {
                scanedDate
                        .set(Calendar.DAY_OF_YEAR, scanedDate.get(Calendar.DAY_OF_YEAR) + 1);
            }
        }
        // if we find everything all right then return with the current date
        return scanedDate.getTime();
    }

    @Override
    public List<Date> getDates(final Date from, final Date to) throws GenericEntityException {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        List<Date> datesWhereNoWorklog = new ArrayList<Date>();
        while (!from.equals(to)) {
            String scanedDateString = DateTimeConverterUtil.dateToString(to);
            if (excludeDates.contains(scanedDateString)) {
                to.setDate(to.getDate() - 1);
                continue;
            }
            // check includes - not check weekend
            if (!includeDates.contains(scanedDateString)) {
                Calendar toDate = Calendar.getInstance();
                toDate.setTime(to);
                // check weekend - pass
                if ((toDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                        || (toDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                    to.setDate(to.getDate() - 1);
                    continue;
                }
            }
            // check worklog. if no worklog set result else ++ scanedDate
            boolean isDateContainsWorklog = isContainsWorklog(to);
            if (!isDateContainsWorklog) {
                datesWhereNoWorklog.add((Date) to.clone());
            }
            to.setDate(to.getDate() - 1);

        }
        return datesWhereNoWorklog;
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

    /**
     * Import the properties file values.
     * 
     * @throws IOException
     *             If IO Exception.
     */
    private void importProperties() throws IOException {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = ClassLoaderUtils.getResourceAsStream(PROPERTIES, JiraTimetrackerPluginImpl.class);
            if (inputStream == null) {
                URL resource = ClassLoaderUtils.getResource(PROPERTIES, JiraTimetrackerPluginImpl.class);
                File propertiesFile = new File(resource.getFile());
                inputStream = new FileInputStream(propertiesFile);
            }
            properties.load(inputStream);
            emailSender = properties.getProperty(EMAIL_SENDER);
            issueCheckTimeInMinutes = Long.valueOf(properties.getProperty(ISSUE_CHECK_TIME)).longValue();
            excludeDatesString = properties.getProperty(EXCLUDE_DATES);
            includeDatesString = properties.getProperty(INCLUDE_DATES);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        for (String dateString : excludeDatesString.split(",")) {
            try {
                DateTimeConverterUtil.stringToDate(dateString);
                excludeDates.add(dateString);
            } catch (ParseException e) {
                LOGGER.error("Faild parse exculde date [" + dateString + "] to ISO-8601 format (2012-12-15)", e);
            }
        }
        for (String dateString : includeDatesString.split(",")) {
            try {
                DateTimeConverterUtil.stringToDate(dateString);
                includeDates.add(dateString);
            } catch (ParseException e) {
                LOGGER.error("Faild parse include date [" + dateString + "] to ISO-8601 format (2012-12-15)", e);
            }
        }

    }

    /**
     * Check the given date, the user have worklogs or not.
     * 
     * @param date
     *            The date what have to check.
     * @return If The user have worklogs the given date then true, esle false.
     * @throws GenericEntityException
     *             GenericEntity Exception.
     */
    private boolean isContainsWorklog(final Date date) throws GenericEntityException {
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
        if ((worklogGVList == null) || worklogGVList.isEmpty()) {
            return false;
        } else {
            return true;
        }
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
    public PluginSettingsValues loadPluginSettings() {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance()
                .getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();

        globalSettings = settingsFactory.createGlobalSettings();
        if (globalSettings.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX + JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS) != null) {
            summaryFilteredIssuesId = new ArrayList<Long>();
            List<String> tempIssueList = (List<String>) globalSettings.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
                    + JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS);
            for (String tempIssueId : tempIssueList) {
                summaryFilteredIssuesId.add(Long.valueOf(tempIssueId));
            }
        } else {
            // default empty list
            summaryFilteredIssuesId = new ArrayList<Long>();
        }

        pluginSettings = settingsFactory.createSettingsForKey(JTTP_PLUGIN_SETTINGS_KEY_PREFIX + user.getName());
        Boolean isPopup = null;
        if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP) != null) {
            if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP).equals("true")) {
                isPopup = true;
            } else if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP).equals("false")) {
                isPopup = false;
            }
        } else {
            // the default is the popup calendar
            isPopup = true;
        }
        Boolean isActualDate = null;
        if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE) != null) {
            if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE).equals("true")) {
                isActualDate = true;
            } else if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE).equals("false")) {
                isActualDate = false;
            }
        } else {
            // the default is the Actual Date
            isActualDate = true;
        }
        // Here set the other values
        pluginSettingsValues = new PluginSettingsValues(isPopup, isActualDate, summaryFilteredIssuesId);
        return pluginSettingsValues;
    }

    @Override
    public ActionResult savePluginSettings(final PluginSettingsValues pluginSettingsParameters) {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance()
                .getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        pluginSettings = settingsFactory.createSettingsForKey(JTTP_PLUGIN_SETTINGS_KEY_PREFIX + user.getName());
        pluginSettings.put(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP, pluginSettingsParameters.isCalendarPopup()
                .toString());
        pluginSettings.put(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE, pluginSettingsParameters.isActualDate().toString());

        globalSettings = settingsFactory.createGlobalSettings();
        globalSettings.put(JTTP_PLUGIN_SETTINGS_KEY_PREFIX + JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS,
                pluginSettingsParameters.getFilteredSummaryIssues());
        // TODO better message
        return new ActionResult(ActionResultStatus.SUCCESS, "");
    }

    @Override
    public String summary(final Date startSummary, final Date finishSummary, final List<Long> issuesId)
            throws GenericEntityException {
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
        List<GenericValue> worklogs;
        // if issuesId not null we make a filtered summary
        if ((issuesId != null) && !issuesId.isEmpty()) {
            List<EntityExpr> issuesConditions = new ArrayList<EntityExpr>();
            for (Long issueId : issuesId) {
                issuesConditions.add(new EntityExpr("issue", EntityOperator.NOT_EQUAL, issueId));
            }
            EntityCondition issueCondition = new EntityConditionList(issuesConditions, EntityOperator.AND);
            EntityCondition otherCondition = new EntityConditionList(exprs, EntityOperator.AND);
            EntityExpr allConditionInOne = new EntityExpr(issueCondition, EntityOperator.AND, otherCondition);
            List<EntityExpr> allExps = new ArrayList<EntityExpr>();
            allExps.add(allConditionInOne);
            worklogs = CoreFactory.getGenericDelegator().findByAnd("Worklog", allExps);
        } else {
            worklogs = CoreFactory.getGenericDelegator().findByAnd("Worklog", exprs);
        }

        long timeSpent = 0;
        Iterator<GenericValue> worklogsIterator = worklogs.iterator();
        while (worklogsIterator.hasNext()) {
            GenericValue worklog = worklogsIterator.next();
            timeSpent = timeSpent + worklog.getLong("timeworked").longValue();
        }
        return DateTimeConverterUtil.secondConvertToString(timeSpent);
    }
}
