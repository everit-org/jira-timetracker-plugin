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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.web.action.JiraWebActionSupport;

public class JiraTimetrackerWorklogsWebAction extends JiraWebActionSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(JiraTimetrackerWorklogsWebAction.class);
    /**
     * The {@link JiraTimetrackerPlugin}.
     */
    private JiraTimetrackerPlugin jiraTimetrackerPlugin;

    /**
     * The number of rows in the dates table.
     */
    private static final int ROW_COUNT = 20;

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

    private String contextPath;

    /**
     * The report check the worklogs time spent is equal or greater than 8 hours.
     */
    public boolean checkHours = false;

    /**
     * If check the worklogs spent time, then exclude the non working issues, or not.
     */
    public boolean checkNonWorkingIssues = false;

    /**
     * Simple constructor.
     *
     * @param jiraTimetrackerPlugin
     *            The {@link JiraTimetrackerPlugin}.
     */
    public JiraTimetrackerWorklogsWebAction(
            final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Count how much page need to show the dates.
     *
     * @return Number of pages.
     */
    private int countNumberOfPages() {
        int numberOfPages = 0;
        numberOfPages = allDatesWhereNoWorklog.size() / ROW_COUNT;
        if ((allDatesWhereNoWorklog.size() % ROW_COUNT) != 0) {
            numberOfPages++;
        }
        return numberOfPages;
    }

    /**
     * Set dateFrom and dateFromFormated default value.
     */
    private void dateFromDefaultInit() {
        Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.set(Calendar.MONTH, calendarFrom.get(Calendar.MONTH) - 1);
        dateFrom = calendarFrom.getTime();
        dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
    }

    /**
     * Handle the date change.
     *
     * @throws ParseException
     *             When can't parse the date.
     */
    public void dateSwitcherAction() throws ParseException {

        String[] requestDateFromArray = request.getParameterValues("dateFrom");
        if (requestDateFromArray != null) {
            String requestDate = request.getParameterValues("dateFrom")[0];
            if (!requestDate.equals("")) {
                dateFromFormated = requestDate;
            }
            dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
        } else if ((dateFromFormated == null) && dateFromFormated.equals("")) {
            dateFromDefaultInit();
        } else {
            dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
        }

        String[] requestDateToArray = request.getParameterValues("dateTo");
        if (requestDateToArray != null) {
            String requestDate = request.getParameterValues("dateTo")[0];
            if (!requestDate.equals("")) {
                dateToFormated = requestDate;
            }
            dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
        } else if ((dateToFormated == null) && dateToFormated.equals("")) {
            dateToDefaultInit();
        } else {
            dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
        }

        dateFromFormated = DateTimeConverterUtil.dateToString(dateFrom);
        dateToFormated = DateTimeConverterUtil.dateToString(dateTo);
    }

    /**
     * Set dateTo and dateToFormated default values.
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

        if (dateToFormated.equals("")) {
            dateToDefaultInit();
        }
        dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
        if (dateFromFormated.equals("")) {
            dateFromDefaultInit();
        }
        dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
        try {
            // Default check box parameter false, false
            List<Date> dateswhereNoWorklogDate = jiraTimetrackerPlugin
                    .getDates(dateFrom, dateTo, checkHours,
                            checkNonWorkingIssues);
            allDatesWhereNoWorklog = new ArrayList<String>();
            for (Date date : dateswhereNoWorklogDate) {
                allDatesWhereNoWorklog.add(DateTimeConverterUtil
                        .dateToString(date));
            }
            statisticsMessageParameter = Integer
                    .toString(allDatesWhereNoWorklog.size());
        } catch (GenericEntityException e) {
            LOGGER.error("Error when try to run the query.", e);
            return ERROR;
        }
        numberOfPages = countNumberOfPages();
        actualPage = 1;
        setShowDatesListByActualPage(actualPage);
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

        message = "";
        messageParameter = "";
        statisticsMessageParameter = "0";
        allDatesWhereNoWorklog = new ArrayList<String>();
        showDatesWhereNoWorklog = new ArrayList<String>();
        String[] searchValue = request.getParameterValues("search");
        // if not null then we have to change the dates and make a new query
        if (searchValue != null) {
            // set actual page default! we start the new query with the first
            // page
            dateSwitcherAction();
            actualPage = 1;
            if (dateFrom.compareTo(dateTo) >= 0) {
                message = "plugin.wrong.dates";
                return SUCCESS;
            }
            String[] hourValue = request.getParameterValues("hour");
            String[] nonworkingValue = request.getParameterValues("nonworking");
            if (hourValue != null) {
                checkHours = true;
            }
            if (nonworkingValue != null) {
                checkNonWorkingIssues = true;
            }
        } else {
            dateFrom = DateTimeConverterUtil.stringToDate(dateFromFormated);
            dateTo = DateTimeConverterUtil.stringToDate(dateToFormated);
        }
        try {
            List<Date> dateswhereNoWorklogDate = jiraTimetrackerPlugin
                    .getDates(dateFrom, dateTo, checkHours,
                            checkNonWorkingIssues);
            for (Date date : dateswhereNoWorklogDate) {
                allDatesWhereNoWorklog.add(DateTimeConverterUtil
                        .dateToString(date));
            }
            statisticsMessageParameter = Integer
                    .toString(allDatesWhereNoWorklog.size());
        } catch (GenericEntityException e) {
            LOGGER.error("Error when try to run the query.", e);
            return ERROR;
        }
        // check the page changer buttons
        numberOfPages = countNumberOfPages();
        pageChangeAction();
        setShowDatesListByActualPage(actualPage);
        return SUCCESS;
    }

    public int getActualPage() {
        return actualPage;
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

    public String getDateFromFormated() {
        return dateFromFormated;
    }

    public List<String> getDateswhereNoWorklog() {
        return allDatesWhereNoWorklog;
    }

    public String getDateToFormated() {
        return dateToFormated;
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

    public void setCheckHours(final boolean checkHours) {
        this.checkHours = checkHours;
    }

    public void setCheckNonWorkingIssues(final boolean checkNonWorkingIssues) {
        this.checkNonWorkingIssues = checkNonWorkingIssues;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
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

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setMessageParameter(final String messageParameter) {
        this.messageParameter = messageParameter;
    }

    public void setNumberOfPages(final int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    /**
     * Set the showDatesWhereNoWorklog by the actual page.
     *
     * @param actualPage
     *            The sub list of allDatesWhereNoWorklog.
     */
    private void setShowDatesListByActualPage(final int actualPage) {
        int from = (actualPage - 1) * ROW_COUNT;
        int to = actualPage * ROW_COUNT;
        if ((actualPage == 1) && (allDatesWhereNoWorklog.size() < ROW_COUNT)) {
            to = allDatesWhereNoWorklog.size();
        }
        if ((actualPage == numberOfPages)
                && ((allDatesWhereNoWorklog.size() % ROW_COUNT) != 0)) {
            to = from + (allDatesWhereNoWorklog.size() % ROW_COUNT);
        }
        showDatesWhereNoWorklog = allDatesWhereNoWorklog.subList(from, to);
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
