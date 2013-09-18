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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.CalendarSettingsValues;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.EveritWorklogComparator;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
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
import com.atlassian.jira.usercompatibility.UserCompatibilityHelper;
import com.atlassian.mail.MailException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * The implementation of the {@link JiraTimetrackerPlugin}.
 */
public class JiraTimetrackerPluginImpl implements JiraTimetrackerPlugin,
		Serializable, InitializingBean, DisposableBean {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(JiraTimetrackerPluginImpl.class);

	/**
	 * The plugin settings key prefix.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_KEY_PREFIX = "jttp";
	/**
	 * The plugin setting Summary Filters key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS = "SummaryFilters";
	/**
	 * The plugin setting Summary Filters key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES = "NonEstimated";
	/**
	 * The plugin setting Exclude dates key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES = "ExcludeDates";
	/**
	 * The plugin setting Include dates key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_INCLUDE_DATES = "IncludeDates";
	/**
	 * The plugin setting is calendar popup key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP = "isCalendarPopup";
	/**
	 * The plugin setting is calendar popup key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE = "startTimeChange";
	/**
	 * The plugin setting is calendar popup key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE = "endTimechange";
	/**
	 * The plugin setting is actual date key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE = "isActualDate";
	/**
	 * The plugin setting is actual date key.
	 */
	private static final String JTTP_PLUGIN_SETTINGS_IS_COLORIG = "isColoring";
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
	private Set<String> excludeDatesSet = new HashSet<String>();
	/**
	 * The parsed include dates.
	 */
	private Set<String> includeDatesSet = new HashSet<String>();
	/**
	 * The summary filter issues ids.
	 */
	private List<Pattern> summaryFilteredIssuePatterns;
	/**
	 * The collector issues ids.
	 */
	private List<Pattern> collectorIssuePatterns;
	/**
	 * The summary filter issues ids.
	 */
	private List<Pattern> defaultNonWorkingIssueIds = new ArrayList<Pattern>();
	/**
	 * The collector issues ids.
	 */
	private List<Pattern> defaultNonEstimedIssuePatterns = new ArrayList<Pattern>();
	/**
	 * The plugin Scheduled Executor Service.
	 */
	private final ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(1);
	/**
	 * The issues Estimated Time Checker Future.
	 */
	private ScheduledFuture<?> issueEstimatedTimeCheckerFuture;
	/**
	 * The JiraTimetarckerPluginImpl logger.
	 */
	private Logger log = Logger.getLogger(JiraTimetrackerPluginImpl.class);

	/**
	 * Default constructor.
	 */
	public JiraTimetrackerPluginImpl(final PluginSettingsFactory settingFactory) {
		settingsFactory = settingFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		setDefaultVairablesValue();

		final Runnable issueEstimatedTimeChecker = new IssueEstimatedTimeChecker(
				this);

		// //TEST SETTINGS
		// Calendar now = Calendar.getInstance();
		// Long nowPlusTWOMin = (long) ((now.get(Calendar.HOUR_OF_DAY) * 60) +
		// now.get(Calendar.MINUTE) + 1);
		// issueEstimatedTimeCheckerFuture =
		// scheduledExecutorService.scheduleAtFixedRate(issueEstimatedTimeChecker,
		// calculateInitialDelay(nowPlusTWOMin), // FIXME fix the time
		// // calculateInitialDelay(issueCheckTimeInMinutes),
		// 5, TimeUnit.MINUTES);

		issueEstimatedTimeCheckerFuture = scheduledExecutorService
				.scheduleAtFixedRate(issueEstimatedTimeChecker,
						calculateInitialDelay(issueCheckTimeInMinutes),
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
	public ActionResult createWorklog(final String issueId,
			final String comment, final String dateFormated,
			final String startTime, final String timeSpent) {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		log.warn("JTTP createWorklog: user: " + user.getDisplayName() + " "
				+ user.getName() + " " + user.getEmailAddress());
		JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
		log.warn("JTTP createWorklog: serviceContext User: "
				+ serviceContext.getLoggedInUser().getName() + " "
				+ serviceContext.getLoggedInUser().getEmailAddress());
		IssueManager issueManager = ComponentManager.getInstance()
				.getIssueManager();
		MutableIssue issue = issueManager.getIssueObject(issueId);
		if (issue == null) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.invalid_issue", issueId);
		}
		PermissionManager permissionManager = ComponentManager.getInstance()
				.getPermissionManager();
		if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue,
				user)) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.nopermission_issue", issueId);
		}
		String dateAndTime = dateFormated + " " + startTime;
		Date date;
		try {
			date = DateTimeConverterUtil.stringToDateAndTime(dateAndTime);
		} catch (ParseException e) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.date_parse", dateAndTime);
		}

		WorklogNewEstimateInputParameters params = WorklogInputParametersImpl
				.issue(issue).startDate(date).timeSpent(timeSpent)
				.comment(comment).buildNewEstimate();
		WorklogService worklogService = ComponentManager
				.getComponentInstanceOfType(WorklogService.class);
		WorklogResult worklogResult = worklogService.validateCreate(
				serviceContext, params);
		if (worklogResult == null) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.worklog.create.fail");
		}
		worklogService.createAndAutoAdjustRemainingEstimate(serviceContext,
				worklogResult, true);

		return new ActionResult(ActionResultStatus.SUCCESS,
				"plugin.worklog.create.success");
	}

	@Override
	public ActionResult deleteWorklog(final Long id) {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		JiraServiceContext serviceContext = new JiraServiceContextImpl(user);
		WorklogService worklogService = ComponentManager
				.getComponentInstanceOfType(WorklogService.class);
		WorklogResult deleteWorklogResult = worklogService.validateDelete(
				serviceContext, id);
		if (deleteWorklogResult == null) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.worklog.delete.fail", id.toString());
		}
		worklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext,
				deleteWorklogResult, false);
		return new ActionResult(ActionResultStatus.SUCCESS,
				"plugin.worklog.delete.success", id.toString());
	}

	@Override
	public void destroy() throws Exception {
		scheduledExecutorService.shutdown();
		issueEstimatedTimeCheckerFuture.cancel(true);
		LOGGER.info("JiraTimetrackerPluginImpl destroyed");
	}

	@Override
	public ActionResult editWorklog(final Long id, final String issueId,
			final String comment, final String dateFormated, final String time,
			final String timeSpent) {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		JiraServiceContext serviceContext = new JiraServiceContextImpl(user);

		WorklogManager worklogManager = ComponentManager.getInstance()
				.getWorklogManager();
		Worklog worklog = worklogManager.getById(id);
		IssueManager issueManager = ComponentManager.getInstance()
				.getIssueManager();
		MutableIssue issue = issueManager.getIssueObject(issueId);
		if (issue == null) {
			return new ActionResult(ActionResultStatus.FAIL,
					"plugin.invalide_issue", issueId);
		}
		if (!worklog.getIssue().getKey().equals(issueId)) {
			PermissionManager permissionManager = ComponentManager
					.getInstance().getPermissionManager();
			if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issue,
					user)) {
				return new ActionResult(ActionResultStatus.FAIL,
						"plugin.nopermission_issue", issueId);
			}
			ActionResult deleteResult = deleteWorklog(id);
			if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
				return deleteResult;
			}
			// String dateCreate =
			// DateTimeConverterUtil.dateToString(worklog.getStartDate());
			// String dateCreate = date;
			ActionResult createResult = createWorklog(issueId, comment,
					dateFormated, time, timeSpent);
			if (createResult.getStatus() == ActionResultStatus.FAIL) {
				return createResult;
			}
		} else {
			// String dateFormated =
			// DateTimeConverterUtil.dateToString(worklog.getStartDate());
			// String dateFormated = date;
			String dateAndTime = dateFormated + " " + time;
			Date dateCreate;
			try {
				dateCreate = DateTimeConverterUtil
						.stringToDateAndTime(dateAndTime);
			} catch (ParseException e) {
				return new ActionResult(ActionResultStatus.FAIL,
						"plugin.date_parse" + dateAndTime);
			}
			WorklogInputParameters params = WorklogInputParametersImpl
					.issue(issue).startDate(dateCreate).timeSpent(timeSpent)
					.comment(comment).worklogId(id).issue(issue).build();
			WorklogService worklogService = ComponentManager
					.getComponentInstanceOfType(WorklogService.class);
			WorklogResult worklogResult = worklogService.validateUpdate(
					serviceContext, params);
			if (worklogResult == null) {
				return new ActionResult(ActionResultStatus.FAIL,
						"plugin.worklog.update.fail");
			}

			worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext,
					worklogResult, true);

		}
		return new ActionResult(ActionResultStatus.SUCCESS,
				"plugin.worklog.update.success");

	}

	@Override
	public Date firstMissingWorklogsDate() throws GenericEntityException {
		Calendar scannedDate = Calendar.getInstance();
		// one week
		scannedDate.set(Calendar.DAY_OF_YEAR,
				scannedDate.get(Calendar.DAY_OF_YEAR)
						- DateTimeConverterUtil.DAYS_PER_WEEK);
		for (int i = 0; i < DateTimeConverterUtil.DAYS_PER_WEEK; i++) {
			// convert date to String
			Date scanedDateDate = scannedDate.getTime();
			String scanedDateString = DateTimeConverterUtil
					.dateToString(scanedDateDate);
			// check excludse - pass
			if (excludeDatesSet.contains(scanedDateString)) {
				scannedDate.set(Calendar.DAY_OF_YEAR,
						scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
				continue;
			}
			// check includes - not check weekend
			if (!includeDatesSet.contains(scanedDateString)) {
				// check weekend - pass
				if ((scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
						|| (scannedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
					scannedDate.set(Calendar.DAY_OF_YEAR,
							scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
					continue;
				}
			}
			// check worklog. if no worklog set result else ++ scanedDate
			boolean isDateContainsWorklog = isContainsWorklog(scanedDateDate);
			if (!isDateContainsWorklog) {
				return scanedDateDate;
			} else {
				scannedDate.set(Calendar.DAY_OF_YEAR,
						scannedDate.get(Calendar.DAY_OF_YEAR) + 1);
			}
		}
		// if we find everything all right then return with the current date
		return scannedDate.getTime();
	}

	@Override
	public List<Pattern> getCollectorIssuePatterns() {
		if (collectorIssuePatterns == null) {
			collectorIssuePatterns = defaultNonEstimedIssuePatterns;
		}
		return collectorIssuePatterns;
	}

	@Override
	public List<Date> getDates(final Date from, final Date to,
			final boolean workingHour, final boolean checkNonWorking)
			throws GenericEntityException {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		List<Date> datesWhereNoWorklog = new ArrayList<Date>();
		while (!from.equals(to)) {
			String scanedDateString = DateTimeConverterUtil.dateToString(to);
			if (excludeDatesSet.contains(scanedDateString)) {
				to.setDate(to.getDate() - 1);
				continue;
			}
			// check includes - not check weekend
			if (!includeDatesSet.contains(scanedDateString)) {
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
			boolean isDateContainsWorklog;
			if (workingHour) {
				isDateContainsWorklog = isContainsEnoughWorklog(to,
						checkNonWorking);
			} else {
				isDateContainsWorklog = isContainsWorklog(to);
			}
			if (!isDateContainsWorklog) {
				datesWhereNoWorklog.add((Date) to.clone());
			}
			to.setDate(to.getDate() - 1);

		}
		return datesWhereNoWorklog;
	}

	@Override
	public List<String> getExluceDaysOfTheMonth(final String date) {
		List<String> resultexcludeDays = new ArrayList<String>();
		for (String exludeDate : excludeDatesSet) {
			// TODO this if not handle the 2013-4-04 date..... this is wrong or
			// not? .... think about it.
			if (exludeDate.startsWith(date.substring(0, 7))) {
				resultexcludeDays
						.add(exludeDate.substring(exludeDate.length() - 2));
			}
		}

		return resultexcludeDays;
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
	public List<String> getLoggedDaysOfTheMonth(final Date date)
			throws GenericEntityException {
		List<String> resultDays = new ArrayList<String>();
		int dayOfMonth = 1;
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.set(Calendar.YEAR, date.getYear()
				+ DateTimeConverterUtil.BEGIN_OF_YEAR);
		startCalendar.set(Calendar.MONTH, date.getMonth());
		startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		Date start = startCalendar.getTime();

		while (dayOfMonth != DateTimeConverterUtil.LAST_DAY_OF_MONTH) {
			if (isContainsWorklog(start)) {
				resultDays.add(Integer.toString(dayOfMonth));
			}
			startCalendar.set(Calendar.DAY_OF_MONTH, ++dayOfMonth);
			start = startCalendar.getTime();
		}

		return resultDays;
	}

	@Override
	public List<String> getProjectsId() throws GenericEntityException {
		List<String> projectsId = new ArrayList<String>();
		List<GenericValue> projectsGV = CoreFactory.getGenericDelegator()
				.findAll("Project");
		for (GenericValue project : projectsGV) {
			projectsId.add(project.getString("id"));
		}
		return projectsId;
	}

	@Override
	public EveritWorklog getWorklog(final Long worklogId) throws ParseException {
		WorklogManager worklogManager = ComponentManager.getInstance()
				.getWorklogManager();
		Worklog worklog = worklogManager.getById(worklogId);
		return new EveritWorklog(worklog);
	}

	@Override
	public List<EveritWorklog> getWorklogs(final Date date)
			throws GenericEntityException, ParseException {

		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		log.warn("JTTP LOG: getWorklogs user display: " + user.getDisplayName()
				+ " user name: " + user.getName());
		Date startDate = (Date) date.clone();
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);
		Date endDate = (Date) date.clone();
		endDate.setHours(DateTimeConverterUtil.LAST_HOUR_OF_DAY);
		endDate.setMinutes(DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
		endDate.setSeconds(DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);

		String userKey = UserCompatibilityHelper.getKeyForUser(user);

		EntityExpr startExpr = new EntityExpr("startdate",
				EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
						startDate.getTime()));
		EntityExpr endExpr = new EntityExpr("startdate",
				EntityOperator.LESS_THAN, new Timestamp(endDate.getTime()));
		EntityExpr userExpr = new EntityExpr("author", EntityOperator.EQUALS,
				userKey);
		log.warn("JTTP LOG: getWorklogs start date: " + startDate.toString()
				+ " end date:" + endDate.toString());

		List<EntityExpr> exprList = new ArrayList<EntityExpr>();
		exprList.add(userExpr);
		if (startExpr != null) {
			exprList.add(startExpr);
		}
		if (endExpr != null) {
			exprList.add(endExpr);
		}
		log.warn("JTTP LOG: getWorklogs expr list size: " + exprList.size());
		List<GenericValue> worklogGVList = CoreFactory.getGenericDelegator()
				.findByAnd("Worklog", exprList);
		log.warn("JTTP LOG: getWorklogs worklog GV list size: "
				+ worklogGVList.size());

		List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
		for (GenericValue worklogGv : worklogGVList) {
			EveritWorklog worklog = new EveritWorklog(worklogGv,
					collectorIssuePatterns);
			worklogs.add(worklog);
		}

		Collections.sort(worklogs, new EveritWorklogComparator());
		log.warn("JTTP LOG: getWorklogs worklog GV list size: "
				+ worklogs.size());
		return worklogs;
	}

	/**
	 * Check the given date is containt enough worklog. The worklog spent time
	 * have to be equlase or greater then 8 hours.
	 * 
	 * @param date
	 *            The date what have to check.
	 * @param checkNonWorking
	 *            Exclude or not the non-working issues.
	 * @return True if the day contains enough worklog or weeked or exclude
	 *         date.
	 * @throws GenericEntityException
	 *             If GenericEntity Exception.
	 */
	private boolean isContainsEnoughWorklog(final Date date,
			final boolean checkNonWorking) throws GenericEntityException {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();

		Date startDate = (Date) date.clone();
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);
		Date endDate = (Date) date.clone();
		endDate.setHours(DateTimeConverterUtil.LAST_HOUR_OF_DAY);
		endDate.setMinutes(DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
		endDate.setSeconds(DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);

		String userKey = UserCompatibilityHelper.getKeyForUser(user);

		EntityExpr startExpr = new EntityExpr("startdate",
				EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
						startDate.getTime()));
		EntityExpr endExpr = new EntityExpr("startdate",
				EntityOperator.LESS_THAN, new Timestamp(endDate.getTime()));
		EntityExpr userExpr = new EntityExpr("author", EntityOperator.EQUALS,
				userKey);

		List<EntityExpr> exprList = new ArrayList<EntityExpr>();
		exprList.add(userExpr);
		if (startExpr != null) {
			exprList.add(startExpr);
		}
		if (endExpr != null) {
			exprList.add(endExpr);
		}
		List<GenericValue> worklogGVList = CoreFactory.getGenericDelegator()
				.findByAnd("Worklog", exprList);
		if ((worklogGVList == null) || worklogGVList.isEmpty()) {
			return false;
		} else {
			if (checkNonWorking) {
				List<GenericValue> worklogsCopy = new ArrayList<GenericValue>();
				worklogsCopy.addAll(worklogGVList);
				// if we have non-estimated issues

				// TODO FIXME summaryFilteredIssuePatterns rename nonworking
				// pattern
				if ((summaryFilteredIssuePatterns != null)
						&& !summaryFilteredIssuePatterns.isEmpty()) {
					for (GenericValue worklog : worklogsCopy) {
						IssueManager issueManager = ComponentManager
								.getInstance().getIssueManager();
						Long issueId = worklog.getLong("issue");
						MutableIssue issue = issueManager
								.getIssueObject(issueId);
						for (Pattern issuePattern : summaryFilteredIssuePatterns) {
							boolean issueMatches = issuePattern.matcher(
									issue.getKey()).matches();
							// if match not count in summary
							if (issueMatches) {
								worklogGVList.remove(worklog);
								break;
							}
						}
					}
				}
			}
			long timeSpent = 0;
			for (GenericValue worklog : worklogGVList) {
				timeSpent += worklog.getLong("timeworked").longValue();
			}
			if (timeSpent < DateTimeConverterUtil.EIGHT_HOUR_IN_SECONDS) {
				return false;
			}
		}

		return true;
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
	private boolean isContainsWorklog(final Date date)
			throws GenericEntityException {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();

		Date startDate = (Date) date.clone();
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);
		Date endDate = (Date) date.clone();
		endDate.setHours(DateTimeConverterUtil.LAST_HOUR_OF_DAY);
		endDate.setMinutes(DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
		endDate.setSeconds(DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);

		String userKey = UserCompatibilityHelper.getKeyForUser(user);

		EntityExpr startExpr = new EntityExpr("startdate",
				EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
						startDate.getTime()));
		EntityExpr endExpr = new EntityExpr("startdate",
				EntityOperator.LESS_THAN, new Timestamp(endDate.getTime()));
		EntityExpr userExpr = new EntityExpr("author", EntityOperator.EQUALS,
				userKey);

		List<EntityExpr> exprList = new ArrayList<EntityExpr>();
		exprList.add(userExpr);
		if (startExpr != null) {
			exprList.add(startExpr);
		}
		if (endExpr != null) {
			exprList.add(endExpr);
		}
		List<GenericValue> worklogGVList = CoreFactory.getGenericDelegator()
				.findByAnd("Worklog", exprList);
		if ((worklogGVList == null) || worklogGVList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String lastEndTime(final List<EveritWorklog> worklogs)
			throws ParseException {
		if ((worklogs == null) || (worklogs.size() == 0)) {
			return "08:00";
		}
		String endTime = worklogs.get(0).getEndTime();
		for (int i = 1; i < worklogs.size(); i++) {
			Date first = DateTimeConverterUtil.stringTimeToDateTime(worklogs
					.get(i - 1).getEndTime());
			Date second = DateTimeConverterUtil.stringTimeToDateTime(worklogs
					.get(i).getEndTime());
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
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();

		globalSettings = settingsFactory.createGlobalSettings();
		List<String> tempIssuePatternList = (List<String>) globalSettings
				.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS);
		if (tempIssuePatternList != null) {
			// add non working issues
			summaryFilteredIssuePatterns = new ArrayList<Pattern>();
			for (String tempIssuePattern : tempIssuePatternList) {
				summaryFilteredIssuePatterns.add(Pattern
						.compile(tempIssuePattern));
			}
		} else {
			// default! from properties load default issues!!
			summaryFilteredIssuePatterns = defaultNonWorkingIssueIds;

		}
		tempIssuePatternList = (List<String>) globalSettings
				.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES);
		if (tempIssuePatternList != null) {
			// add collector issues
			collectorIssuePatterns = new ArrayList<Pattern>();
			for (String tempIssuePattern : tempIssuePatternList) {
				collectorIssuePatterns.add(Pattern.compile(tempIssuePattern));
			}
		} else {
			collectorIssuePatterns = defaultNonEstimedIssuePatterns;
		}
		String tempSpecialDates = (String) globalSettings
				.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES);
		if (tempSpecialDates != null) {
			excludeDatesString = tempSpecialDates;
			excludeDatesSet = new HashSet<String>();
			for (String excludeDate : excludeDatesString.split(",")) {
				excludeDatesSet.add(excludeDate);
			}
		} else {
			// Default Empty
			excludeDatesSet = new HashSet<String>();
			excludeDatesString = "";
		}
		tempSpecialDates = (String) globalSettings
				.get(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ JTTP_PLUGIN_SETTINGS_INCLUDE_DATES);
		if (tempSpecialDates != null) {
			includeDatesString = tempSpecialDates;
			includeDatesSet = new HashSet<String>();
			for (String includeDate : includeDatesString.split(",")) {
				includeDatesSet.add(includeDate);
			}
		} else {
			// Default Empty
			includeDatesSet = new HashSet<String>();
			includeDatesString = "";
		}

		pluginSettings = settingsFactory
				.createSettingsForKey(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ user.getName());
		Integer isPopup = null;
		if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP) != null) {
			try {
				isPopup = Integer.valueOf(pluginSettings.get(
						JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP).toString());
			} catch (NumberFormatException e) {
				// the default is the popup calendar
				LOGGER.error(
						"Wrong formated calender type. Set the default value (popup).",
						e);
				isPopup = JiraTimetrackerUtil.POPUP_CALENDAR_CODE;
			}
		} else {
			// the default is the popup calendar
			isPopup = JiraTimetrackerUtil.POPUP_CALENDAR_CODE;
		}
		Boolean isActualDate = null;
		if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE) != null) {
			if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE).equals(
					"true")) {
				isActualDate = true;
			} else if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE)
					.equals("false")) {
				isActualDate = false;
			}
		} else {
			// the default is the Actual Date
			isActualDate = true;
		}
		Boolean isColoring = null;
		if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_COLORIG) != null) {
			if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_COLORIG).equals(
					"true")) {
				isColoring = true;
			} else if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_IS_COLORIG)
					.equals("false")) {
				isColoring = false;
			}

		} else {
			// the default coloring is TRUE
			isColoring = true;
		}

		// SET startTime Change the default value is 5
		int startTimeChange = 5;

		if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE) != null) {
			try {
				startTimeChange = Integer.valueOf(pluginSettings.get(
						JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE).toString());
				if (!validateTimeChange(Integer.toString(startTimeChange))) {
					startTimeChange = 5;
				}
			} catch (NumberFormatException e) {
				LOGGER.error(
						"Wrong formated startTime change value. Set the default value (1).",
						e);
			}
		}
		// SET endtTime Change the defaulte value is 5
		int endTimeChange = 5;

		if (pluginSettings.get(JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE) != null) {
			try {
				endTimeChange = Integer.valueOf(pluginSettings.get(
						JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE).toString());
				if (!validateTimeChange(Integer.toString(endTimeChange))) {
					endTimeChange = 5;
				}
			} catch (NumberFormatException e) {
				LOGGER.error(
						"Wrong formated startTime change value. Set the default value (1).",
						e);
			}
		}
		// Here set the other values
		pluginSettingsValues = new PluginSettingsValues(
				new CalendarSettingsValues(isPopup, isActualDate,
						excludeDatesString, includeDatesString, isColoring),
				summaryFilteredIssuePatterns, collectorIssuePatterns,
				startTimeChange, endTimeChange);
		return pluginSettingsValues;
	}

	@Override
	public void savePluginSettings(
			final PluginSettingsValues pluginSettingsParameters) {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		pluginSettings = settingsFactory
				.createSettingsForKey(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
						+ user.getName());
		pluginSettings.put(JTTP_PLUGIN_SETTINGS_IS_CALENDAR_POPUP,
				Integer.toString(pluginSettingsParameters.isCalendarPopup()));
		pluginSettings.put(JTTP_PLUGIN_SETTINGS_IS_ACTUAL_DATE,
				pluginSettingsParameters.isActualDate().toString());
		pluginSettings.put(JTTP_PLUGIN_SETTINGS_IS_COLORIG,
				pluginSettingsParameters.isColoring().toString());
		pluginSettings
				.put(JTTP_PLUGIN_SETTINGS_START_TIME_CHANGE,
						Integer.toString(pluginSettingsParameters
								.getStartTimeChange()));
		pluginSettings.put(JTTP_PLUGIN_SETTINGS_END_TIME_CHANGE,
				Integer.toString(pluginSettingsParameters.getEndTimeChange()));

		globalSettings = settingsFactory.createGlobalSettings();
		globalSettings.put(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
				+ JTTP_PLUGIN_SETTINGS_SUMMARY_FILTERS,
				pluginSettingsParameters.getFilteredSummaryIssues());
		globalSettings.put(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
				+ JTTP_PLUGIN_SETTINGS_NON_ESTIMATED_ISSUES,
				pluginSettingsParameters.getCollectorIssues());
		globalSettings.put(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
				+ JTTP_PLUGIN_SETTINGS_EXCLUDE_DATES,
				pluginSettingsParameters.getExcludeDates());
		globalSettings.put(JTTP_PLUGIN_SETTINGS_KEY_PREFIX
				+ JTTP_PLUGIN_SETTINGS_INCLUDE_DATES,
				pluginSettingsParameters.getIncludeDates());
	}

	/**
	 * Set the default values of the important variables.
	 * 
	 * @throws MailException
	 */
	private void setDefaultVairablesValue() throws MailException {
		// DEFAULT 20:00
		issueCheckTimeInMinutes = 1200;
		// Default exclude and include dates set are empty. No DATA!!
		// Default: no non working issue. we simple use the empty list
		// defaultNonWorkingIssueIds = new ArrayList<Long>();
		// The default non estimted issues regex. All issue non estimeted.
		defaultNonEstimedIssuePatterns = new ArrayList<Pattern>();
		defaultNonEstimedIssuePatterns.add(Pattern.compile(".*"));
	}

	@Override
	public String summary(final Date startSummary, final Date finishSummary,
			final List<Pattern> issuePatterns) throws GenericEntityException {
		JiraAuthenticationContext authenticationContext = ComponentManager
				.getInstance().getJiraAuthenticationContext();
		User user = authenticationContext.getLoggedInUser();
		startSummary.setSeconds(0);
		finishSummary.setSeconds(0);
		EntityExpr startExpr = new EntityExpr("startdate",
				EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(
						startSummary.getTime()));
		EntityExpr endExpr = new EntityExpr("startdate",
				EntityOperator.LESS_THAN,
				new Timestamp(finishSummary.getTime()));
		EntityExpr userExpr = null;
		if (user != null) {
			String userKey = UserCompatibilityHelper.getKeyForUser(user);
			userExpr = new EntityExpr("author", EntityOperator.EQUALS, userKey);
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
		// worklog query
		worklogs = CoreFactory.getGenericDelegator()
				.findByAnd("Worklog", exprs);
		List<GenericValue> worklogsCopy = new ArrayList<GenericValue>();
		worklogsCopy.addAll(worklogs);
		// if we have non-estimated issues
		if ((issuePatterns != null) && !issuePatterns.isEmpty()) {
			for (GenericValue worklog : worklogsCopy) {
				IssueManager issueManager = ComponentManager.getInstance()
						.getIssueManager();
				Long issueId = worklog.getLong("issue");
				MutableIssue issue = issueManager.getIssueObject(issueId);
				for (Pattern issuePattern : issuePatterns) {
					boolean issueMatches = issuePattern.matcher(issue.getKey())
							.matches();
					// if match not count in summary
					if (issueMatches) {
						worklogs.remove(worklog);
						break;
					}
				}
			}
		}
		long timeSpent = 0;
		// Iterator<GenericValue> worklogsIterator = worklogs.iterator();
		// while (worklogsIterator.hasNext()) {
		// GenericValue worklog = worklogsIterator.next();
		// timeSpent = timeSpent + worklog.getLong("timeworked").longValue();
		// }
		for (GenericValue worklog : worklogs) {
			timeSpent += worklog.getLong("timeworked").longValue();
		}
		return DateTimeConverterUtil.secondConvertToString(timeSpent);
	}

	@Override
	public boolean validateTimeChange(final String changeValue)
			throws NumberFormatException {
		int changeValueInt = Integer.valueOf(changeValue);

		switch (changeValueInt) {
		case 5:
			return true;
		case 10:
			return true;
		case 15:
			return true;
		case 20:
			return true;
		case 30:
			return true;
		default:
			return false;
		}

	}
}
