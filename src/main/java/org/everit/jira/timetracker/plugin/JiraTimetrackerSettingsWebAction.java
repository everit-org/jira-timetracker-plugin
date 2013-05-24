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
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerSettingsWebAction.class);
    /**
     * The filtered Issues id.
     */
    private List<Pattern> issuesPatterns;
    /**
     * The collector issue ids.
     */
    private List<Pattern> collectorIssuePatterns;

    public JiraTimetrackerSettingsWebAction(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
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
            setReturnUrl("/secure/JiraTimetarckerWebAction!default.jspa");
            return getRedirect(INPUT);
        }

        return SUCCESS;
    }

    public boolean getIsActualDate() {
        return isActualDate;
    }

    public int getIsPopup() {
        return isPopup;
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
        collectorIssuePatterns = pluginSettingsValues.getCollectorIssues();
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
        String[] popupOrInlineValue = request.getParameterValues("popupOrInline");
        if (popupOrInlineValue[0].equals("popup")) {
            isPopup = JiraTimetrackerUtil.POPUP_CALENDAR_CODE;
        } else if (popupOrInlineValue[0].equals("inline")) {
            isPopup = JiraTimetrackerUtil.INLINE_CALENDAR_CODE;
        } else {
            isPopup = JiraTimetrackerUtil.BOTH_TYPE_CALENDAR_CODE;
        }
        String[] currentOrLastValue = request.getParameterValues("currentOrLast");
        if (currentOrLastValue[0].equals("current")) {
            isActualDate = true;
        } else {
            isActualDate = false;
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

    public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setIsPopup(final int isPopup) {
        this.isPopup = isPopup;
    }

    public void setProjectsId(final List<String> projectsId) {
        this.projectsId = projectsId;
    }

}
