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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.ChartData;
import org.everit.jira.timetracker.plugin.dto.EveritWorklog;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public class JiraTimetrackerChartWebAction extends JiraWebActionSupport {

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

    private List<String> allDatesWhereNoWorklog;

    private List<String> showDatesWhereNoWorklog;
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
    /**
     * The message parameter.
     */
    private String messageParameter = "";
    /**
     * The message parameter.
     */
    private String statisticsMessageParameter = "0";
    /**
     * The number of pages.
     */
    private int numberOfPages;

    /**
     * The actual page.
     */
    private int actualPage;

    /**
     * The interval of the search.
     */
    private int interval;

    private String contextPath;

    private int fdow;

    private List<ChartData> chartDataList;

    /**
     * The report check the worklogs time spent is equal or greater than 8 hours.
     */
    public boolean checkHours = false;

    /**
     * If check the worklogs spent time, then exclude the non working issues, or not.
     */
    public boolean checkNonWorkingIssues = false;

    private Collection<User> allUsers;

    private String currentUserEmail;

    /**
     * Simple constructor.
     *
     * @param jiraTimetrackerPlugin
     *            The {@link JiraTimetrackerPlugin}.
     */
    public JiraTimetrackerChartWebAction(
            final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Set dateFrom and dateFromFormated default value.
     */
    private void dateFromDefaultInit() {
        Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.set(Calendar.WEEK_OF_MONTH, calendarFrom.get(Calendar.WEEK_OF_MONTH) - 1);
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
        loadPluginSettingAndParseResult();

        if (dateFromFormated.equals("")) {
            dateFromDefaultInit();
        }
        if (dateToFormated.equals("")) {
            dateToDefaultInit();
        }
        chartDataList = null;

        allUsers = UserUtils.getAllUsers();
        JiraAuthenticationContext authenticationContext = ComponentManager
                .getInstance().getJiraAuthenticationContext();
        currentUserEmail = authenticationContext.getLoggedInUser().getEmailAddress();

        return INPUT;
    }

    @Override
    public String doExecute() throws ParseException {
        // set variables default value back
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }

        normalizeContextPath();
        loadPluginSettingAndParseResult();

        String[] searchValue = request.getParameterValues("search");
        if (searchValue != null) {

            String dateFrom = request.getParameterValues("dateFrom")[0];
            if (dateFrom != null) {
                dateFromFormated = dateFrom;
            }
            else {
                return ERROR;
            }
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(DateTimeConverterUtil.stringToDate(dateFrom));

            String dateTo = request.getParameterValues("dateTo")[0];
            if (dateTo != null) {
                dateToFormated = dateTo;
            }
            else {
                return ERROR;
            }

            if (dateFrom.compareTo(dateTo) >= 0) {
                message = "plugin.wrong.dates";
                return SUCCESS;
            }

            Calendar lastDate = (Calendar) startDate.clone();
            lastDate.setTime(DateTimeConverterUtil.stringToDate(dateTo));

            lastDate.set(Calendar.HOUR_OF_DAY,
                    DateTimeConverterUtil.LAST_HOUR_OF_DAY);
            lastDate.set(Calendar.MINUTE,
                    DateTimeConverterUtil.LAST_MINUTE_OF_HOUR);
            lastDate.set(Calendar.SECOND,
                    DateTimeConverterUtil.LAST_SECOND_OF_MINUTE);
            lastDate.set(Calendar.MILLISECOND,
                    DateTimeConverterUtil.LAST_MILLISECOND_OF_SECOND);

            String userEmail = request.getParameterValues("userPicker")[0];
            currentUserEmail = userEmail;

            List<EveritWorklog> worklogs = new ArrayList<EveritWorklog>();
            while (startDate.before(lastDate)) {
                try {
                    worklogs.addAll(jiraTimetrackerPlugin.getWorklogs(startDate.getTime(), userEmail));
                } catch (GenericEntityException e) {
                    LOGGER.error("Error when trying to get worklogs.", e);
                    return ERROR;
                }
                startDate.add(Calendar.DATE, 1);
            }

            Map<String, Long> map = new HashMap<String, Long>();
            for (EveritWorklog worklog : worklogs) {
                String projectName = worklog.getIssue().split("-")[0];
                Long newValue = worklog.getMilliseconds();
                Long oldValue = map.get(projectName);
                if (oldValue == null) {
                    map.put(projectName, newValue);
                } else {
                    map.put(projectName, oldValue + newValue);
                }
            }

            chartDataList = new ArrayList<ChartData>();
            for (String key : map.keySet()) {
                chartDataList.add(new ChartData(key, map.get(key)));
            }

        }

        allUsers = UserUtils.getAllUsers();

        return SUCCESS;

    }

    public int getActualPage() {
        return actualPage;
    }

    public Collection<User> getAllUsers() {
        return allUsers;
    }

    public List<ChartData> getChartDataList() {
        return chartDataList;
    }

    public boolean getCheckHours() {
        return checkHours;
    }

    public boolean getCheckNonWorkingIssues() {
        return checkNonWorkingIssues;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public String getDateFromFormated() {
        return dateFromFormated;
    }

    public List<String> getDateswhereNoWorklog() {
        return allDatesWhereNoWorklog;
    }

    public String getDateToFormated() {
        return dateToFormated;
    }

    public int getFdow() {
        return fdow;
    }

    public int getInterval() {
        return interval;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageParameter() {
        return messageParameter;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public List<String> getShowDatesWhereNoWorklog() {
        return showDatesWhereNoWorklog;
    }

    public String getStatisticsMessageParameter() {
        return statisticsMessageParameter;
    }

    private void loadPluginSettingAndParseResult() {
        PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
                .loadPluginSettings();
        fdow = pluginSettingsValues.getFdow();
    }

    private void normalizeContextPath() {
        String path = request.getContextPath();
        if ((path.length() > 0) && path.substring(path.length() - 1).equals("/")) {
            contextPath = path.substring(0, path.length() - 1);
        } else {
            contextPath = path;
        }
    }

    /**
     * Handle the page changer action.
     */
    public void pageChangeAction() {
        String[] dayBackValue = request.getParameterValues("pageBack");
        String[] dayNextValue = request.getParameterValues("pageNext");
        if ((dayBackValue != null) && (actualPage > 1)) {
            actualPage--;
        }
        if ((dayNextValue != null) && (actualPage < numberOfPages)) {
            actualPage++;
        }

    }

    public void setActualPage(final int actualPage) {
        this.actualPage = actualPage;
    }

    public void setAllUsers(final Collection<User> allUsers) {
        this.allUsers = allUsers;
    }

    public void setChartDataList(final List<ChartData> chartDataList) {
        this.chartDataList = chartDataList;
    }

    public void setCheckHours(final boolean checkHours) {
        this.checkHours = checkHours;
    }

    public void setCheckNonWorkingIssues(final boolean checkNonWorkingIssues) {
        this.checkNonWorkingIssues = checkNonWorkingIssues;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public void setCurrentUser(final String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }

    public void setDateFromFormated(final String dateFromFormated) {
        this.dateFromFormated = dateFromFormated;
    }

    public void setDateswhereNoWorklog(final List<String> dateswhereNoWorklog) {
        allDatesWhereNoWorklog = dateswhereNoWorklog;
    }

    public void setDateToFormated(final String dateToFormated) {
        this.dateToFormated = dateToFormated;
    }

    public void setFdow(final int fdow) {
        this.fdow = fdow;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setMessageParameter(final String messageParameter) {
        this.messageParameter = messageParameter;
    }

    public void setNumberOfPages(final int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public void setShowDatesWhereNoWorklog(
            final List<String> showDatesWhereNoWorklog) {
        this.showDatesWhereNoWorklog = showDatesWhereNoWorklog;
    }

    public void setStatisticsMessageParameter(
            final String statisticsMessageParameter) {
        this.statisticsMessageParameter = statisticsMessageParameter;
    }

}
