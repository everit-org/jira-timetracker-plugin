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
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.CalendarSettingsValues;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;

import com.atlassian.jira.web.action.JiraWebActionSupport;

public class JiraTimetrackerSettingsWebAction extends JiraWebActionSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The {@link JiraTimetrackerPlugin}.
     */
    private JiraTimetrackerPlugin jiraTimetrackerPlugin;
    /**
     * The calendar is popup, inLine or both.
     */
    private int isPopup;
    /**
     * The calenar show the actualDate or the last unfilled date.
     */
    private boolean isActualDate;
    /**
     * The IDs of the projects.
     */
    private List<String> projectsId;
    /**
     * The exclude dates in String format.
     */
    private String excludeDates = "";
    /**
     * The include dates in String format.
     */
    private String includeDates = "";
    /**
     * The message.
     */
    private String message = "";
    /**
     * The message parameter.
     */
    private String messageParameter = "";
    /**
     * The startTime.
     */
    private String startTime;
    /**
     * The endTime.
     */
    private String endTime;
    /**
     * The calendar highlights coloring.
     */
    private boolean isColoring;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(JiraTimetrackerSettingsWebAction.class);

    /**
     * The filtered Issues id.
     */
    private List<Pattern> issuesPatterns;

    /**
     * The collector issue ids.
     */
    private List<Pattern> collectorIssuePatterns;

    /**
     * The first day of the week.
     */
    private int fdow;

    private String contextPath;

    public JiraTimetrackerSettingsWebAction(
            final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
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
        try {
            projectsId = jiraTimetrackerPlugin.getProjectsId();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }
        return INPUT;
    }

    @Override
    public String doExecute() throws ParseException {
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
        normalizeContextPath();
        loadPluginSettingAndParseResult();
        try {
            projectsId = jiraTimetrackerPlugin.getProjectsId();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }

        if (request.getParameter("savesettings") != null) {
            String parseResult = parseSaveSettings(request);
            if (parseResult != null) {
                return parseResult;
            }
            savePluginSettings();
            setReturnUrl("/secure/JiraTimetrackerWebAction!default.jspa");
            return getRedirect(INPUT);
        }

        return SUCCESS;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getFdow() {
        return fdow;
    }

    public boolean getIsActualDate() {
        return isActualDate;
    }

    public boolean getIsColoring() {
        return isColoring;
    }

    public int getIsPopup() {
        return isPopup;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageParameter() {
        return messageParameter;
    }

    public List<String> getProjectsId() {
        return projectsId;
    }

    public String getStartTime() {
        return startTime;
    }

    /**
     * Load the plugin settings and set the variables.
     */
    public void loadPluginSettingAndParseResult() {
        PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin
                .loadPluginSettings();
        isPopup = pluginSettingsValues.isCalendarPopup();
        isActualDate = pluginSettingsValues.isActualDate();
        issuesPatterns = pluginSettingsValues.getFilteredSummaryIssues();
        collectorIssuePatterns = pluginSettingsValues.getCollectorIssues();
        excludeDates = pluginSettingsValues.getExcludeDates();
        includeDates = pluginSettingsValues.getIncludeDates();
        startTime = Integer.toString(pluginSettingsValues.getStartTimeChange());
        endTime = Integer.toString(pluginSettingsValues.getEndTimeChange());
        isColoring = pluginSettingsValues.isColoring();
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
     * Parse the reqest after the save button was clicked. Set the variables.
     *
     * @param request
     *            The HttpServletRequest.
     */
    public String parseSaveSettings(final HttpServletRequest request) {
        String[] popupOrInlineValue = request
                .getParameterValues("popupOrInline");
        String[] startTimeValue = request.getParameterValues("startTime");
        String[] endTimeValue = request.getParameterValues("endTime");

        if (popupOrInlineValue[0].equals("popup")) {
            isPopup = JiraTimetrackerUtil.POPUP_CALENDAR_CODE;
        } else if (popupOrInlineValue[0].equals("inline")) {
            isPopup = JiraTimetrackerUtil.INLINE_CALENDAR_CODE;
        } else {
            isPopup = JiraTimetrackerUtil.BOTH_TYPE_CALENDAR_CODE;
        }
        String[] currentOrLastValue = request
                .getParameterValues("currentOrLast");
        if (currentOrLastValue[0].equals("current")) {
            isActualDate = true;
        } else {
            isActualDate = false;
        }
        String[] isColoringValue = request.getParameterValues("isColoring");
        if (isColoringValue != null) {
            isColoring = true;
        } else {
            isColoring = false;
        }
        try {
            if (jiraTimetrackerPlugin.validateTimeChange(startTimeValue[0])) {
                startTime = startTimeValue[0];
            } else {
                message = "plugin.setting.start.time.change.wrong";
            }
        } catch (NumberFormatException e) {
            message = "plugin.settings.time.format";
            messageParameter = startTimeValue[0];
        }
        try {
            if (jiraTimetrackerPlugin.validateTimeChange(endTimeValue[0])) {
                endTime = endTimeValue[0];
            } else {
                message = "plugin.setting.end.time.change.wrong";
            }
        } catch (NumberFormatException e) {
            message = "plugin.settings.time.format";
            messageParameter = endTimeValue[0];
        }
        if (!message.equals("")) {
            return SUCCESS;
        }
        return null;
    }

    /**
     * Save the plugin settings.
     */
    public void savePluginSettings() {
        PluginSettingsValues pluginSettingValues = new PluginSettingsValues(
                new CalendarSettingsValues(isPopup, isActualDate, excludeDates,
                        includeDates, isColoring, fdow), issuesPatterns,
                        collectorIssuePatterns, Integer.valueOf(startTime),
                        Integer.valueOf(endTime));
        jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
    }

    public void setColoring(final boolean isColoring) {
        this.isColoring = isColoring;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    public void setFdow(final int fdow) {
        this.fdow = fdow;
    }

    public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setIsPopup(final int isPopup) {
        this.isPopup = isPopup;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setMessageParameter(final String messageParameter) {
        this.messageParameter = messageParameter;
    }

    public void setProjectsId(final List<String> projectsId) {
        this.projectsId = projectsId;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

}
