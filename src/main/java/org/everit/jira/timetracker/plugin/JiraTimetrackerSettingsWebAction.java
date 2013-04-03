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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.everit.jira.timetracker.plugin.dto.PluginSettingsValues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
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
     * The calendar is popup or not.
     */
    private boolean isPopup;
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
     * The IDs of the projects.
     */
    private List<String> projectsId;

    /**
     * The user is admin or not.
     */
    private boolean isUserAdmin;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JiraTimetrackerSettingsWebAction.class);
    /**
     * The filtered Issues id.
     */
    private List<Long> issuesId;
    /**
     * The collector issue ids.
     */
    private List<Long> collectorIssueIds;

    public JiraTimetrackerSettingsWebAction(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    /**
     * Check the user have admin or sys admin permission.
     * 
     * @return True if had any admin permission.
     */
    private boolean checkTheUserAdminPermissions() {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance()
                .getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        // check the logged user admin or not
        boolean isUserSysAdmin = ComponentManager.getInstance().getUserUtil().getJiraSystemAdministrators()
                .contains(user);
        boolean isUserAdmin = ComponentManager.getInstance().getUserUtil().getJiraAdministrators().contains(user);
        return isUserSysAdmin || isUserAdmin;
    }

    @Override
    public String doDefault() throws ParseException {
        boolean isUserLogged = JiraTimetrackerUtil.isUserLogged();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
        isUserAdmin = checkTheUserAdminPermissions();
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
        isUserAdmin = checkTheUserAdminPermissions();
        loadPluginSettingAndParseResult();
        try {
            projectsId = jiraTimetrackerPlugin.getProjectsId();
        } catch (Exception e) {
            LOGGER.error("Error when try set the plugin variables.", e);
            return ERROR;
        }

        if (request.getParameter("savesettings") != null) {
            parseSaveSettings(request);
            savePluginSettings();
            setReturnUrl("/secure/JiraTimetarckerWebAction!default.jspa");
            return getRedirect(INPUT);
        }

        return SUCCESS;
    }

    public List<Long> getCollectorIssueIds() {
        return collectorIssueIds;
    }

    public String getCollectorIssueKey() {
        return collectorIssueKey;
    }

    public boolean getIsActualDate() {
        return isActualDate;
    }

    public boolean getIsPopup() {
        return isPopup;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public boolean getIsUserAdmin() {
        return isUserAdmin;
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
        issuesId = pluginSettingsValues.getFilteredSummaryIssues();
        for (Long issueId : issuesId) {
            IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
            String filteredIssueKey = issueManager.getIssueObject(issueId).getKey();
            issueKey += filteredIssueKey + " ";
        }
        collectorIssueIds = pluginSettingsValues.getCollectorIssues();
        for (Long issueId : collectorIssueIds) {
            IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
            String filteredIssueKey = issueManager.getIssueObject(issueId).getKey();
            collectorIssueKey += filteredIssueKey + " ";
        }
    }

    /**
     * Parse the reqest after the save button was clicked. Set the variables.
     * 
     * @param request
     *            The HttpServletRequest.
     */
    public void parseSaveSettings(final HttpServletRequest request) {
        String[] issueSelectValue = request.getParameterValues("issueSelect");
        String[] collectorIssueSelectValue = request.getParameterValues("issueSelect_collector");
        // if the user is admin and issueSelectValue is null means we don't want to filters
        // else if issueSelectValue not null then the user is admin and we save the new filters
        // else not have to implement because we use the loaded issuesId list
        if ((issueSelectValue == null) && isUserAdmin) {
            issuesId = new ArrayList<Long>();
        } else if (issueSelectValue != null) {
            issuesId = new ArrayList<Long>();
            for (String filteredIssueKey : issueSelectValue) {
                IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
                Long filteredIssueId = issueManager.getIssueObject(filteredIssueKey).getId();
                issuesId.add(filteredIssueId);
            }
        }
        if ((collectorIssueSelectValue == null) && isUserAdmin) {
            collectorIssueIds = new ArrayList<Long>();
        } else if (collectorIssueSelectValue != null) {
            collectorIssueIds = new ArrayList<Long>();
            for (String filteredIssueKey : collectorIssueSelectValue) {
                IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
                Long filteredIssueId = issueManager.getIssueObject(filteredIssueKey).getId();
                collectorIssueIds.add(filteredIssueId);
            }
        }
        String[] popupOrInlineValue = request.getParameterValues("popupOrInline");
        if (popupOrInlineValue[0].equals("popup")) {
            isPopup = true;
        } else {
            isPopup = false;
        }
        String[] currentOrLastValue = request.getParameterValues("currentOrLast");
        if (currentOrLastValue[0].equals("current")) {
            isActualDate = true;
        } else {
            isActualDate = false;
        }

    }

    /**
     * Save the plugin settings.
     */
    public void savePluginSettings() {
        PluginSettingsValues pluginSettingValues = new PluginSettingsValues(isPopup, isActualDate, issuesId,
                collectorIssueIds);
        jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);
    }

    public void setCollectorIssueIds(final List<Long> collectorIssueIds) {
        this.collectorIssueIds = collectorIssueIds;
    }

    public void setCollectorIssueKey(final String collectorIssueKey) {
        this.collectorIssueKey = collectorIssueKey;
    }

    public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setIsPopup(final boolean isPopup) {
        this.isPopup = isPopup;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public void setProjectsId(final List<String> projectsId) {
        this.projectsId = projectsId;
    }

    public void setUserAdmin(final boolean isUserAdmin) {
        this.isUserAdmin = isUserAdmin;
    }
}
