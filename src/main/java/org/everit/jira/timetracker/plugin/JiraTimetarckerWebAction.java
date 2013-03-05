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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ActionResult;
import org.everit.jira.timetracker.plugin.dto.ActionResultStatus;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * The timetracker web action support class.
 */
public class JiraTimetarckerWebAction extends JiraWebActionSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The increment of the next day or month.
     */
    private static final int NEXT = 1;

    /**
     * The increment of the previous day or month.
     */
    private static final int PREV = -1;

    /**
     * The increment of the next week.
     */
    private static final int NEXT_WEEK = 7;

    /**
     * The increment of the previous week.
     */
    private static final int PREV_WEEK = -7;

    /**
     * The default worklog ID.
     */
    private static final Long DEFAULT_WORKLOG_ID = Long.valueOf(0);

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JiraTimetarckerWebAction.class);
    /**
     * The {@link JiraTimetrackerPlugin}.
     */
    private JiraTimetrackerPlugin jiraTimetrackerPlugin;
    /**
     * The issues.
     */
    private transient List<Issue> issues = new ArrayList<Issue>();
    /**
     * The worklogs.
     */
    private List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
    /**
     * The deleted worklog id.
     */
    private Long deletedWorklogId = DEFAULT_WORKLOG_ID;
    /**
     * The date.
     */
    private Date date = null;
    /**
     * The formated date.
     */
    private String dateFormated = "";
    /**
     * The summary of month.
     */
    private String monthSummary = "";
    /**
     * The summary of week.
     */
    private String weekSummary = "";
    /**
     * The summary of day.
     */
    private String daySummary = "";
    /**
     * The edited worklog id.
     */
    private Long editedWorklogId = DEFAULT_WORKLOG_ID;
    /**
     * The WebAction is edit a worklog or not.
     */
    private boolean isEdit = false;
    /**
     * The issue key.
     */
    private String issueKey = "";

    /**
     * The worklog start time.
     */
    private String startTime = "";

    /**
     * The worklog end time.
     */
    private String endTime = "";
    /**
     * The worklog duration.
     */
    private String durationTime = "";
    /**
     * The worklog comment.
     */
    private String comment = "Default worklog description!";
    /**
     * The spent time in Jira time format (1h 20m).
     */
    private String timeSpent = "";
    /**
     * The IDs of the projects.
     */
    private List<String> projectsId;
    /**
     * The message.
     */
    private String message = "";
    /**
     * The message parameter.
     */
    private String messageParameter = "";

    Date datePCalendar = new Date();

    /**
     * Simple constructor.
     * 
     * @param jiraTimetrackerPlugin
     *            The {@link JiraTimetrackerPlugin}.
     */
    public JiraTimetarckerWebAction(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Handle the date change.
     * 
     * @throws ParseException
     *             When can't parse date.
     */
    public void dateSwitcherAction() throws ParseException {
        String[] dayBackValue = request.getParameterValues("dayBack");
        String[] dayNextValue = request.getParameterValues("dayNext");
        String[] weekBackValue = request.getParameterValues("weekBack");
        String[] weekNextValue = request.getParameterValues("weekNext");
        String[] monthBackValue = request.getParameterValues("monthBack");
        String[] monthNextVaule = request.getParameterValues("monthNext");

        if (dayNextValue != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setDate(date.getDate() + NEXT);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else if (dayBackValue != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setDate(date.getDate() + PREV);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else if (monthNextVaule != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setMonth(date.getMonth() + NEXT);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else if (monthBackValue != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setMonth(date.getMonth() + PREV);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else if (weekNextValue != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setDate(date.getDate() + NEXT_WEEK);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else if (weekBackValue != null) {
            date = DateTimeConverterUtil.stringToDate(dateFormated);
            date.setDate(date.getDate() + PREV_WEEK);
            dateFormated = DateTimeConverterUtil.dateToString(date);
        } else {
            String[] requestDateArray = request.getParameterValues("date");
            if (requestDateArray != null) {
                String requestDate = request.getParameterValues("date")[0];
                if (!requestDate.equals("")) {
                    dateFormated = requestDate;
                }
                date = DateTimeConverterUtil.stringToDate(dateFormated);
            } else {
                date = new Date();
                dateFormated = DateTimeConverterUtil.dateToString(date);
            }
        }
    }

    @Override
    public String doDefault() throws ParseException {
        if (dateFormated.equals("")) {
            date = Calendar.getInstance().getTime();
            dateFormated = DateTimeConverterUtil.dateToString(date);
        }
        date = DateTimeConverterUtil.stringToDate(dateFormated);
        if ((deletedWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(deletedWorklogId)) {
            ActionResult deleteResult = jiraTimetrackerPlugin.deleteWorklog(deletedWorklogId);
            if (deleteResult.getStatus() == ActionResultStatus.FAIL) {
                return ERROR;
            }
        }
        try {
            projectsId = jiraTimetrackerPlugin.getProjectsId();
            makeSummary();
            worklogs = jiraTimetrackerPlugin.getWorklogs(date);
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }
        startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
        endTime = DateTimeConverterUtil.dateTimeToString(new Date());

        if ((editedWorklogId != null) && !DEFAULT_WORKLOG_ID.equals(editedWorklogId)) {
            isEdit = true;
            EveritWorklog editWorklog;
            try {
                editWorklog = jiraTimetrackerPlugin.getWorklog(editedWorklogId);
            } catch (ParseException e) {
                LOGGER.error("Error when try parse the worklog.", e);
                return ERROR;
            }
            issueKey = editWorklog.getIssue();
            comment = editWorklog.getBody();
            startTime = editWorklog.getStartTime();
            endTime = editWorklog.getEndTime();
            durationTime = editWorklog.getDuration();
        }

        return INPUT;
    }

    @Override
    public String doExecute() throws ParseException {

        message = "";
        messageParameter = "";

        String[] issueSelectValue = request.getParameterValues("issueSelect");
        String[] commentsValue = request.getParameterValues("comments");
        String[] startTimeValue = request.getParameterValues("startTime");

        dateSwitcherAction();

        try {
            makeSummary();
            worklogs = jiraTimetrackerPlugin.getWorklogs(date);
            startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
            projectsId = jiraTimetrackerPlugin.getProjectsId();
            endTime = DateTimeConverterUtil.dateTimeToString(new Date());
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }

        // if not edit and not submit than just a simple date change
        if ((request.getParameter("edit") == null) && (request.getParameter("submit") == null)) {
            return SUCCESS;
        }

        if (issueSelectValue == null) {
            message = "plugin.missing_issue";
            return SUCCESS;
        }

        issueKey = issueSelectValue[0];

        String validateInputFieldsResult = validateInputFields();
        if (validateInputFieldsResult.equals(ERROR)) {
            return SUCCESS;
        }

        comment = commentsValue[0];

        if (request.getParameter("edit") != null) {
            return editAction();
        }

        ActionResult createResult = jiraTimetrackerPlugin.createWorklog(issueKey, comment, dateFormated,
                startTimeValue[0], timeSpent);
        if (createResult.getStatus() == ActionResultStatus.FAIL) {
            message = createResult.getMessage();
            messageParameter = createResult.getMessageParameter();
        }
        endTime = DateTimeConverterUtil.dateTimeToString(new Date());
        try {
            worklogs = jiraTimetrackerPlugin.getWorklogs(date);
            startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
            makeSummary();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }
        return SUCCESS;
    }

    /**
     * Edit the worklog and handle the problems.
     * 
     * @return String witch will be passed to the WebAction.
     */
    public String editAction() {
        String[] startTimeValue = request.getParameterValues("startTime");
        ActionResult updateResult = jiraTimetrackerPlugin.editWorklog(editedWorklogId, issueKey,
                comment, startTimeValue[0], timeSpent);
        if (updateResult.getStatus() == ActionResultStatus.FAIL) {
            message = updateResult.getMessage();
            messageParameter = updateResult.getMessageParameter();
            return SUCCESS;
        }
        try {
            worklogs = jiraTimetrackerPlugin.getWorklogs(date);
            startTime = jiraTimetrackerPlugin.lastEndTime(worklogs);
            endTime = DateTimeConverterUtil.dateTimeToString(new Date());
            makeSummary();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }
        editedWorklogId = DEFAULT_WORKLOG_ID;
        return SUCCESS;
    }

    public String getComment() {
        return comment;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public String getDateFormated() {
        return dateFormated;
    }

    public Date getDatePCalendar() {
        return datePCalendar;
    }

    public String getDaySummary() {
        return daySummary;
    }

    public Long getDeletedWorklogId() {
        return deletedWorklogId;
    }

    public String getDurationTime() {
        return durationTime;
    }

    public Long getEditedWorklogId() {
        return editedWorklogId;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean getIsEdit() {
        return isEdit;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageParameter() {
        return messageParameter;
    }

    public String getMonthSummary() {
        return monthSummary;
    }

    public List<String> getProjectsId() {
        return projectsId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getWeekSummary() {
        return weekSummary;
    }

    public List<EveritWorklog> getWorklogs() {
        return worklogs;
    }

    /**
     * Make summary today, this week and this month.
     * 
     * @throws GenericEntityException
     *             GenericEntityException.
     */
    public void makeSummary() throws GenericEntityException {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, date.getYear() + DateTimeConverterUtil.BEGIN_OF_YEAR);
        startCalendar.set(Calendar.MONTH, date.getMonth());
        startCalendar.set(Calendar.DAY_OF_MONTH, date.getDate());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        Calendar originalStartcalendar = (Calendar) startCalendar.clone();
        Date start = startCalendar.getTime();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.YEAR, date.getYear() + DateTimeConverterUtil.BEGIN_OF_YEAR);
        endCalendar.set(Calendar.MONTH, date.getMonth());
        endCalendar.set(Calendar.DAY_OF_MONTH, date.getDate());
        endCalendar.set(Calendar.HOUR_OF_DAY, DateTimeConverterUtil.LAST_HOUR_OF_DAY);
        endCalendar.set(Calendar.MINUTE, DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
        endCalendar.set(Calendar.SECOND, DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);
        startCalendar.set(Calendar.MILLISECOND, DateTimeConverterUtil.LAST_MILLISECOND_OF_SECOND);
        Calendar originalEndCcalendar = (Calendar) endCalendar.clone();
        Date end = endCalendar.getTime();

        daySummary = jiraTimetrackerPlugin.summary(start, end);

        startCalendar = (Calendar) originalStartcalendar.clone();
        startCalendar.set(Calendar.DAY_OF_MONTH, (date.getDate() - (date.getDay() == 0 ? 6 : date.getDay() - 1)));
        start = startCalendar.getTime();

        endCalendar = (Calendar) originalEndCcalendar.clone();
        endCalendar
                .set(Calendar.DAY_OF_MONTH,
                        (date.getDate() + (DateTimeConverterUtil.DAYS_PER_WEEK - (date.getDay() == 0 ? 7 : date
                                .getDay()))));
        end = endCalendar.getTime();

        weekSummary = jiraTimetrackerPlugin.summary(start, end);

        startCalendar = (Calendar) originalStartcalendar.clone();
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        start = startCalendar.getTime();

        endCalendar = (Calendar) originalEndCcalendar.clone();
        endCalendar.set(Calendar.DAY_OF_MONTH, DateTimeConverterUtil.LAST_DAY_OF_MONTH);
        end = endCalendar.getTime();
        monthSummary = jiraTimetrackerPlugin.summary(start, end);
    }

    /**
     * The readObject method for the transient variable.
     * 
     * @param in
     *            The ObjectInputStream.
     * @throws IOException
     *             IOException.
     * @throws ClassNotFoundException
     *             ClassNotFoundException.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        issues = new ArrayList<Issue>();
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setDate(final Date date) {
        this.date = (Date) date.clone();
    }

    public void setDateFormated(final String dateFormated) {
        this.dateFormated = dateFormated;
    }

    public void setDatePCalendar(final Date datePCalendar) {
        this.datePCalendar = datePCalendar;
    }

    public void setDaySummary(final String daySummary) {
        this.daySummary = daySummary;
    }

    public void setDeletedWorklogId(final Long deletedWorklogId) {
        this.deletedWorklogId = deletedWorklogId;
    }

    public void setDurationTime(final String durationTime) {
        this.durationTime = durationTime;
    }

    public void setEdit(final boolean edit) {
        isEdit = edit;
    }

    public void setEditedWorklogId(final Long editedWorklogId) {
        this.editedWorklogId = editedWorklogId;
    }

    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public void setIssues(final List<Issue> issues) {
        this.issues = issues;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setMessageParameter(final String messageParameter) {
        this.messageParameter = messageParameter;
    }

    public void setMonthSummary(final String monthSummary) {
        this.monthSummary = monthSummary;
    }

    public void setProjectsId(final List<String> projectsId) {
        this.projectsId = projectsId;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    public void setWeekSummary(final String weekSummary) {
        this.weekSummary = weekSummary;
    }

    public void setWorklogs(final List<EveritWorklog> worklogs) {
        this.worklogs = worklogs;
    }

    /**
     * Check the startTime, endTime or durationTime fields values.
     * 
     * @return If the values valid the return SUCCESS else return ERROR.
     */
    public String validateInputFields() {
        String[] startTimeValue = request.getParameterValues("startTime");
        String[] endOrDurationValue = request.getParameterValues("endOrDuration");
        String[] commentsValue = request.getParameterValues("comments");

        if (!DateTimeConverterUtil.isValidTime(startTimeValue[0])) {
            message = "plugin.invalid_startTime";
            return ERROR;
        }
        if (commentsValue[0] == null) {
            return ERROR;
        }
        if (endOrDurationValue[0].equals("duration")) {
            String[] durationTimeValue = request.getParameterValues("durationTime");
            if (!DateTimeConverterUtil.isValidTime(durationTimeValue[0])) {
                message = "plugin.invalid_durationTime";
                return ERROR;
            }
            Date durationDateTime;
            try {
                durationDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(durationTimeValue[0]);
            } catch (ParseException e) {
                message = "plugin.invalid_durationTime";
                return ERROR;
            }

            long seconds = durationDateTime.getTime() / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
            timeSpent = DateTimeConverterUtil.secondConvertToString(seconds);
        } else {
            String[] endTimeValue = request.getParameterValues("endTime");
            if (!DateTimeConverterUtil.isValidTime(endTimeValue[0])) {
                message = "plugin.invalid_endTime";
                return ERROR;
            }
            Date startDateTime;
            Date endDateTime;
            try {
                startDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(startTimeValue[0]);
                endDateTime = DateTimeConverterUtil.stringTimeToDateTimeGMT(endTimeValue[0]);
            } catch (ParseException e) {
                message = "plugin.invalid_endTime";
                return ERROR;
            }

            long seconds = (endDateTime.getTime() - startDateTime.getTime())
                    / DateTimeConverterUtil.MILLISECONDS_PER_SECOND;
            if (seconds > 0) {
                timeSpent = DateTimeConverterUtil.secondConvertToString(seconds);
            } else {
                message = "plugin.invalid_timeInterval";
                return ERROR;
            }
        }
        return SUCCESS;
    }
}
