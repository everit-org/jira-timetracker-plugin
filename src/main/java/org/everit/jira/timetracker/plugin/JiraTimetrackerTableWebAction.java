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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ChartData;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public class JiraTimetrackerTableWebAction extends JiraWebActionSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(JiraTimetrackerChartWebAction.class);
    /**
     * The {@link JiraTimetrackerPlugin}.
     */
    private JiraTimetrackerPlugin jiraTimetrackerPlugin;
    /**
     * The date.
     */
    private Date dateFrom = null;
    /**
     * The formated date.
     */
    private String dateFromFormated = "";
    /**
     * The date.
     */
    private Date dateTo = null;
    /**
     * The formated date.
     */
    private String dateToFormated = "";
    /**
     * The message.
     */
    private String message = "";

    private String contextPath;

    private List<ChartData> chartDataList;

    private List<User> allUsers;

    private String currentUser = "";

    private String avatarURL = "";

    private User userPickerObject;

    private List<EveritWorklog> worklogs;

    private HashMap<Integer, List<Object>> monthSum = new HashMap<Integer, List<Object>>();
    private HashMap<Integer, List<Object>> weekSum = new HashMap<Integer, List<Object>>();
    private HashMap<Integer, List<Object>> daySum = new HashMap<Integer, List<Object>>();
    private HashMap<Integer, List<Object>> realMonthSum = new HashMap<Integer, List<Object>>();
    private HashMap<Integer, List<Object>> realWeekSum = new HashMap<Integer, List<Object>>();
    private HashMap<Integer, List<Object>> realDaySum = new HashMap<Integer, List<Object>>();

    private List<Pattern> issuesRegex;

    /**
     * Jira Componentmanager instance.
     */
    private ComponentManager componentManager = ComponentManager.getInstance();

    /**
     * Simple constructor.
     *
     * @param jiraTimetrackerPlugin
     *            The {@link JiraTimetrackerPlugin}.
     */
    public JiraTimetrackerTableWebAction(
            final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Set dateFrom and dateFromFormated default value.
     */
    private void dateFromDefaultInit() {
        Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.add(Calendar.WEEK_OF_MONTH, -1);
        dateFrom = calendarFrom.getTime();
        dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
    }

    /**
     * Set dateTo and dateToFormated default value.
     */
    private void dateToDefaultInit() {
        Calendar calendarTo = Calendar.getInstance();
        dateTo = calendarTo.getTime();
        dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
    }

    @Override
    public String doDefault() throws ParseException {
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }

        normalizeContextPath();
        jiraTimetrackerPlugin.loadPluginSettings();

        if (dateFromFormated.equals("")) {
            dateFromDefaultInit();
        }
        if (dateToFormated.equals("")) {
            dateToDefaultInit();
        }
        chartDataList = null;

        allUsers = new ArrayList<User>(UserUtils.getAllUsers());
        Collections.sort(allUsers);

        JiraAuthenticationContext authenticationContext = componentManager.getJiraAuthenticationContext();
        currentUser = authenticationContext.getLoggedInUser().getName();
        setUserPickerObjectBasedOnSelectedUser();

        return INPUT;
    }

    @Override
    public String doExecute() throws ParseException, GenericEntityException {
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }

        normalizeContextPath();
        PluginSettingsValues pluginSettings = jiraTimetrackerPlugin.loadPluginSettings();
        setIssuesRegex(pluginSettings.getFilteredSummaryIssues());

        allUsers = new ArrayList<User>(UserUtils.getAllUsers());
        Collections.sort(allUsers);

        if (dateFromFormated.equals("")) {
            dateFromDefaultInit();
        }
        if (dateToFormated.equals("")) {
            dateToDefaultInit();
        }

        if (request.getParameterValues("userPicker") != null) {
            currentUser = request.getParameterValues("userPicker")[0];
        } else {
            message += "plugin.user.picker.label";
            return SUCCESS;
        }
        if ((currentUser == null) || currentUser.equals("")) {
            JiraAuthenticationContext authenticationContext = componentManager.getJiraAuthenticationContext();
            currentUser = authenticationContext.getLoggedInUser().getName();
        }
        setUserPickerObjectBasedOnSelectedUser();

        String dateFrom = request.getParameterValues("dateFrom")[0];
        if ((dateFrom != null) && !dateFrom.equals("")) {
            dateFromFormated = dateFrom;
        }
        else {
            message = "plugin.invalid_startTime";
            return SUCCESS;
        }
        Calendar startDate = Calendar.getInstance();
        try {
            startDate.setTime(DateTimeConverterUtil.stringToDate(dateFrom));
        } catch (ParseException e) {
            message = "plugin.invalid_startTime";
            return SUCCESS;
        }

        String dateTo = request.getParameterValues("dateTo")[0];
        if ((dateTo != null) && !dateTo.equals("")) {
            dateToFormated = dateTo;
        }
        else {
            message = "plugin.invalid_endTime";
            return SUCCESS;
        }
        Calendar lastDate = (Calendar) startDate.clone();
        try {
            lastDate.setTime(DateTimeConverterUtil.stringToDate(dateTo));
        } catch (ParseException e) {
            message = "plugin.invalid_endTime";
            return SUCCESS;
        }

        if (startDate.after(lastDate)) {
            message = "plugin.wrong.dates";
            return SUCCESS;
        }

        Calendar yearCheckCal = (Calendar) lastDate.clone();
        yearCheckCal.add(Calendar.YEAR, -1);
        if (startDate.before(yearCheckCal)) {
            message += "plugin.exceeded.year";
            return SUCCESS;
        }

        worklogs = new ArrayList<EveritWorklog>();
        try {
            worklogs.addAll(jiraTimetrackerPlugin.getWorklogs(currentUser, startDate.getTime(), lastDate.getTime()));
        } catch (DataAccessException e) {
            LOGGER.error("Error when trying to get worklogs.", e);
            return ERROR;
        } catch (SQLException e) {
            LOGGER.error("Error when trying to get worklogs.", e);
            return ERROR;
        }

        Collections.sort(worklogs, new Comparator<EveritWorklog>() {
            @Override
            public int compare(final EveritWorklog wl1, final EveritWorklog wl2) {
                return wl1.getDate().compareTo(wl2.getDate());
            }
        });

        for (EveritWorklog worklog : worklogs) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(worklog.getDate());

            boolean isRealWorklog = true;
            for (Pattern issuePattern : issuesRegex) {
                boolean issueMatches = issuePattern.matcher(worklog.getIssue()).matches();
                // if match not count in summary
                if (issueMatches) {
                    isRealWorklog = false;
                    break;
                }
            }

            int monthNo = worklog.getMonthNo();
            ArrayList<Object> list = new ArrayList<Object>();
            Long prevMonthSum = monthSum.get(monthNo) == null ? 0L : (Long) monthSum.get(monthNo).get(0);
            Long sumSec = prevMonthSum + (worklog.getMilliseconds() / 1000);
            list.add(sumSec);
            list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
            monthSum.put(monthNo, list);
            ArrayList<Object> realList = new ArrayList<Object>();
            Long prevRealMonthSum = realMonthSum.get(monthNo) == null ? 0L : (Long) realMonthSum.get(monthNo).get(0);
            Long realSumSec = prevRealMonthSum;
            if (isRealWorklog) {
                realSumSec += (worklog.getMilliseconds() / 1000);
            }
            realList.add(realSumSec);
            realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
            realMonthSum.put(monthNo, realList);

            int weekNo = worklog.getWeekNo();
            list = new ArrayList<Object>();
            Long prevWeekSum = weekSum.get(weekNo) == null ? 0L : (Long) weekSum.get(weekNo).get(0);
            sumSec = prevWeekSum + (worklog.getMilliseconds() / 1000);
            list.add(sumSec);
            list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
            weekSum.put(weekNo, list);
            Long prevRealWeekSum = realWeekSum.get(weekNo) == null ? 0L : (Long) realWeekSum.get(weekNo).get(0);
            realList = new ArrayList<Object>();
            realSumSec = prevRealWeekSum;
            if (isRealWorklog) {
                realSumSec += (worklog.getMilliseconds() / 1000);
            }
            realList.add(realSumSec);
            realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
            realWeekSum.put(weekNo, realList);

            int dayNo = worklog.getDayNo();
            list = new ArrayList<Object>();
            Long prevDaySum = daySum.get(dayNo) == null ? 0L : (Long) daySum.get(dayNo).get(0);
            sumSec = prevDaySum + (worklog.getMilliseconds() / 1000);
            list.add(sumSec);
            list.add(DateTimeConverterUtil.secondConvertToString(sumSec));
            daySum.put(dayNo, list);
            Long prevRealDaySum = realDaySum.get(dayNo) == null ? 0L : (Long) realDaySum.get(dayNo).get(0);
            realList = new ArrayList<Object>();
            realSumSec = prevRealDaySum;
            if (isRealWorklog) {
                realSumSec += (worklog.getMilliseconds() / 1000);
            }
            realList.add(realSumSec);
            realList.add(DateTimeConverterUtil.secondConvertToString(realSumSec));
            realDaySum.put(dayNo, realList);
        }

        return SUCCESS;
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public List<ChartData> getChartDataList() {
        return chartDataList;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getCurrentUserEmail() {
        return currentUser;
    }

    public String getDateFromFormated() {
        return dateFromFormated;
    }

    public String getDateToFormated() {
        return dateToFormated;
    }

    public HashMap<Integer, List<Object>> getDaySum() {
        return daySum;
    }

    public List<Pattern> getIssuesRegex() {
        return issuesRegex;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<Integer, List<Object>> getMonthSum() {
        return monthSum;
    }

    public HashMap<Integer, List<Object>> getRealDaySum() {
        return realDaySum;
    }

    public HashMap<Integer, List<Object>> getRealMonthSum() {
        return realMonthSum;
    }

    public HashMap<Integer, List<Object>> getRealWeekSum() {
        return realWeekSum;
    }

    public User getUserPickerObject() {
        return userPickerObject;
    }

    public HashMap<Integer, List<Object>> getWeekSum() {
        return weekSum;
    }

    public List<EveritWorklog> getWorklogs() {
        return worklogs;
    }

    private void normalizeContextPath() {
        String path = request.getContextPath();
        if ((path.length() > 0) && path.substring(path.length() - 1).equals("/")) {
            contextPath = path.substring(0, path.length() - 1);
        } else {
            contextPath = path;
        }
    }

    public void setAllUsers(final List<User> allUsers) {
        this.allUsers = allUsers;
    }

    public void setAvatarURL(final String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public void setChartDataList(final List<ChartData> chartDataList) {
        this.chartDataList = chartDataList;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public void setCurrentUser(final String currentUserEmail) {
        currentUser = currentUserEmail;
    }

    public void setDateFromFormated(final String dateFromFormated) {
        this.dateFromFormated = dateFromFormated;
    }

    public void setDateToFormated(final String dateToFormated) {
        this.dateToFormated = dateToFormated;
    }

    public void setDaySum(final HashMap<Integer, List<Object>> daySum) {
        this.daySum = daySum;
    }

    public void setIssuesRegex(final List<Pattern> issuesRegex) {
        this.issuesRegex = issuesRegex;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setMonthSum(final HashMap<Integer, List<Object>> monthSum) {
        this.monthSum = monthSum;
    }

    public void setUserPickerObject(final User userPickerObject) {
        this.userPickerObject = userPickerObject;
    }

    private void setUserPickerObjectBasedOnSelectedUser() {
        if ((currentUser != null) && !currentUser.equals("")) {
            userPickerObject = componentManager.getUserUtil().getUserObject(currentUser);
            AvatarService avatarService = ComponentManager
                    .getComponentInstanceOfType(AvatarService.class);
            setAvatarURL(avatarService.getAvatarURL(userPickerObject, currentUser, Avatar.Size.SMALL).toString());
        } else {
            userPickerObject = null;
        }
    }

    public void setWeekSum(final HashMap<Integer, List<Object>> weekSum) {
        this.weekSum = weekSum;
    }

    public void setWorklogs(final List<EveritWorklog> worklogs) {
        this.worklogs = worklogs;
    }
}
