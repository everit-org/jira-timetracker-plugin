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

import javax.servlet.http.HttpServletRequest;

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

    boolean isPopup;

    boolean isActualDate;

    public JiraTimetrackerSettingsWebAction(final JiraTimetrackerPlugin jiraTimetrackerPlugin) {
        this.jiraTimetrackerPlugin = jiraTimetrackerPlugin;
    }

    @Override
    public String doDefault() throws ParseException {

        loadPluginSettingAndParseResult();

        return INPUT;
    }

    @Override
    public String doExecute() throws ParseException {
        // loadPluginSettingAndParseResult();
        if (request.getParameter("savesettings") != null) {
            parseSaveSettings(request);
            savePluginSettings();
            setReturnUrl("/secure/JiraTimetarckerWebAction!default.jspa");
            return getRedirect(INPUT);
        }

        return SUCCESS;
    }

    public boolean getIsActualDate() {
        return isActualDate;
    }

    public boolean getIsPopup() {
        return isPopup;
    }

    public void loadPluginSettingAndParseResult() {
        PluginSettingsValues pluginSettingsValues = jiraTimetrackerPlugin.loadPluginSettings();
        isPopup = pluginSettingsValues.isCalendarPopup();
        isActualDate = pluginSettingsValues.isActualDate();
    }

    public void parseSaveSettings(final HttpServletRequest request) {
        String calendarPopupCheckBoxValues = request.getParameter("calendarPopupCheckBox");
        if (calendarPopupCheckBoxValues == null) {
            isPopup = false;
        } else {
            isPopup = true;
        }
        String actualDateCheckBoxValues = request
                .getParameter("actualDateCheckBox");
        if (actualDateCheckBoxValues == null) {
            isActualDate = false;
        } else {
            isActualDate = true;
        }
    }

    public void savePluginSettings() {
        PluginSettingsValues pluginSettingValues = new PluginSettingsValues(isPopup, isActualDate);
        jiraTimetrackerPlugin.savePluginSettings(pluginSettingValues);

    }

    public void setIsActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setIsPopup(final boolean isPopup) {
        this.isPopup = isPopup;
    }
}
