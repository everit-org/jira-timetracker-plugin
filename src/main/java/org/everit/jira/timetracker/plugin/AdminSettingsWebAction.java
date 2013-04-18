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
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;

import com.atlassian.jira.web.action.JiraWebActionSupport;

public class AdminSettingsWebAction extends JiraWebActionSupport {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The {@link JiraTimetrackerPlugin}.
     */
    private JiraTimetrackerPlugin jiraTimetrackerPlugin;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AdminSettingsWebAction.class);

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
     * The calendar is popup, inLine or both.
     */
    private int isPopup;
    /**
     * The calenar show the actualDate or the last unfilled date.
     */
    private boolean isActualDate;
    /**
     * The issue key.
     */
    private String issueKey = "";
    /**
     * The collector issue key.
     */
    private String collectorIssueKey = "";
    /**
     * The filtered Issues id.
     */
    private List<Pattern> issuesPatterns;
    /**
     * The settings page message parameter.
     */
    private String messageExclude = "";
    /**
     * The paramater of the message.
     */
    private String messageParameterExclude = "";
    /**
     * The settings page message parameter.
     */
    private String messageInclude = "";
    /**
     * The paramater of the message.
     */
    private String messageParameterInclude = "";
    /**
     * The collector issue ids.
     */
    private List<Pattern> collectorIssuePatterns;

    public AdminSettingsWebAction(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    @Override
    public String doDefault() throws ParseException {
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
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
        loadPluginSettingAndParseResult();
        try {
            projectsId = jiraTimetrackerPlugin.getProjectsId();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }

        if (request.getParameter("savesettings") != null) {
            String parseReuslt = parseSaveSettings(request);
            if (parseReuslt != null) {
                return parseReuslt;
            }
            savePluginSettings();
            setReturnUrl("/secure/AdminSummary.jspa");
            return getRedirect(INPUT);
        }

        return SUCCESS;
    }

    public String getCollectorIssueKey() {
        return collectorIssueKey;
    }

    public String getExcludeDates() {
        return excludeDates;
    }

    public String getIncludeDates() {
        return includeDates;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getMessageExclude() {
        return messageExclude;
    }

    public String getMessageInclude() {
        return messageInclude;
    }

    public String getMessageParameterExclude() {
        return messageParameterExclude;
    }

    public String getMessageParameterInclude() {
        return messageParameterInclude;
    }

    public List<String> getProjectsId() {
        return projectsId;
    }

    /**
     * Load the plugin settings and set the variables.
     */
    public void loadPluginSettingAndParseResult() {
        PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin.loadPluginSettings();
        isPopup = pluginSettingsValues.isCalendarPopup();
        isActualDate = pluginSettingsValues.isActualDate();
        issuesPatterns = pluginSettingsValues.getFilteredSummaryIssues();
        for (Pattern issueId : issuesPatterns) {
            issueKey += issueId.toString() + " ";
        }
        collectorIssuePatterns = pluginSettingsValues.getCollectorIssues();
        for (Pattern issuePattern : collectorIssuePatterns) {
            collectorIssueKey += issuePattern.toString() + " ";
        }
        excludeDates = pluginSettingsValues.getExcludeDates();
        includeDates = pluginSettingsValues.getIncludeDates();
    }

    /**
     * Parse the reqest after the save button was clicked. Set the variables.
     * 
     * @param request
     *            The HttpServletRequest.
     */
    public String parseSaveSettings(final HttpServletRequest request) {
        String[] issueSelectValue = request.getParameterValues("issueSelect");
        String[] collectorIssueSelectValue = request.getParameterValues("issueSelect_collector");
        String[] excludeDatesValue = request.getParameterValues("excludedates");
        String[] includeDatesValue = request.getParameterValues("includedates");

        if (issueSelectValue == null) {
            issuesPatterns = new ArrayList<Pattern>();
        } else {
            issuesPatterns = new ArrayList<Pattern>();
            for (String filteredIssueKey : issueSelectValue) {
                issuesPatterns.add(Pattern.compile(filteredIssueKey));
            }
        }
        if (collectorIssueSelectValue == null) {
            collectorIssuePatterns = new ArrayList<Pattern>();
        } else {
            collectorIssuePatterns = new ArrayList<Pattern>();
            for (String filteredIssueKey : collectorIssueSelectValue) {
                collectorIssuePatterns.add(Pattern.compile(filteredIssueKey));
            }
        }
        boolean parseExcludeException = false;
        boolean parseIncludeException = false;
        // Handle exclude and include date in the parse method end.
        if (excludeDatesValue == null) {
            excludeDates = "";
        } else {
            String excludeDatesValueString = excludeDatesValue[0];
            if (!excludeDatesValueString.isEmpty()) {
                for (String dateString : excludeDatesValueString.split(",")) {
                    try {
                        DateTimeConverterUtil.stringToDate(dateString);
                    } catch (ParseException e) {
                        parseExcludeException = true;
                        messageExclude = "plugin.parse.exception.exclude";
                        if (messageParameterExclude.isEmpty()) {
                            messageParameterExclude += dateString;
                        } else {
                            messageParameterExclude += ", " + dateString;
                        }

                    }
                }
            }
            excludeDates = excludeDatesValueString;
        }
        if (includeDatesValue == null) {
            includeDates = "";
        } else {
            String includeDatesValueString = includeDatesValue[0];
            if (!includeDatesValueString.isEmpty()) {
                for (String dateString : includeDatesValueString.split(",")) {
                    try {
                        DateTimeConverterUtil.stringToDate(dateString);
                    } catch (ParseException e) {
                        parseIncludeException = true;
                        messageInclude = "plugin.parse.exception.include";
                        if (messageParameterInclude.isEmpty()) {
                            messageParameterInclude += dateString;
                        } else {
                            messageParameterInclude += ", " + dateString;
                        }
                    }
                }
            }
            includeDates = includeDatesValueString;
        }
        if (parseExcludeException || parseIncludeException) {
            return SUCCESS;
        }
        return null;
    }

    /**
     * Save the plugin settings.
     */
    public void savePluginSettings() {
        PluginSettingsValues pluginSettingValues = new PluginSettingsValues(isPopup, isActualDate, issuesPatterns,
                collectorIssuePatterns, excludeDates, includeDates);
        jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
    }

    public void setCollectorIssueKey(final String collectorIssueKey) {
        this.collectorIssueKey = collectorIssueKey;
    }

    public void setExcludeDates(final String excludeDates) {
        this.excludeDates = excludeDates;
    }

    public void setIncludeDates(final String includeDates) {
        this.includeDates = includeDates;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public void setMessageExclude(final String messageExclude) {
        this.messageExclude = messageExclude;
    }

    public void setMessageInclude(final String messageInclude) {
        this.messageInclude = messageInclude;
    }

    public void setMessageParameterExclude(final String messageParameterExclude) {
        this.messageParameterExclude = messageParameterExclude;
    }

    public void setMessageParameterInclude(final String messageParameterInclude) {
        this.messageParameterInclude = messageParameterInclude;
    }

    public void setProjectsId(final List<String> projectsId) {
        this.projectsId = projectsId;
    }
}
