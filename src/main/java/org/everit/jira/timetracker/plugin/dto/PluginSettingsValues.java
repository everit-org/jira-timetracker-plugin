package org.everit.jira.timetracker.plugin.dto;

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

import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;

/**
 * PluginSettingsValues class contains the result of the {@link JiraTimetrackerPlugin} loadPluginSetting method.
 */
public class PluginSettingsValues {

    /**
     * The plugin calendar is popup.
     */
    private Boolean isCalendarPopup;

    /**
     * The plugin calendar show the actual date when start or the latest day what not contains worklog.
     */
    private Boolean isActualDate;

    public PluginSettingsValues() {

    }

    public PluginSettingsValues(final boolean isCalendarPopup, final boolean isActualDate) {
        super();
        this.isCalendarPopup = isCalendarPopup;
        this.isActualDate = isActualDate;
    }

    public Boolean isActualDate() {
        return isActualDate;
    }

    public Boolean isCalendarPopup() {
        return isCalendarPopup;
    }

    public void setActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setCalendarPopup(final boolean isCalendarPopup) {
        this.isCalendarPopup = isCalendarPopup;
    }

}
