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

import java.util.List;
import java.util.regex.Pattern;

import org.everit.jira.timetracker.plugin.JiraTimetrackerPlugin;

/**
 * PluginSettingsValues class contains the result of the {@link JiraTimetrackerPlugin} loadPluginSetting method.
 */
public class PluginSettingsValues {

    /**
     * The plugin calendar is popup.
     */
    private int isCalendarPopup;

    /**
     * The plugin calendar show the actual date when start or the latest day what not contains worklog.
     */
    private Boolean isActualDate;
    /**
     * The non working issues list.
     */
    private List<Long> filteredSummaryIssues;
    /**
     * The collector issues pattern list;
     */
    private List<Pattern> collectorIssues;

    public PluginSettingsValues() {

    }

    public PluginSettingsValues(final int isCalendarPopup, final boolean isActualDate,
            final List<Long> filteredSummaryIssues, final List<Pattern> collectorIssues) {
        super();
        this.isCalendarPopup = isCalendarPopup;
        this.isActualDate = isActualDate;
        this.filteredSummaryIssues = filteredSummaryIssues;
        this.collectorIssues = collectorIssues;
    }

    public List<Pattern> getCollectorIssues() {
        return collectorIssues;
    }

    public List<Long> getFilteredSummaryIssues() {
        return filteredSummaryIssues;
    }

    public Boolean isActualDate() {
        return isActualDate;
    }

    public int isCalendarPopup() {
        return isCalendarPopup;
    }

    public void setActualDate(final boolean actualDateOrLastWorklogDate) {
        isActualDate = actualDateOrLastWorklogDate;
    }

    public void setCalendarPopup(final int isCalendarPopup) {
        this.isCalendarPopup = isCalendarPopup;
    }

    public void setCollectorIssues(final List<Pattern> collectorIssues) {
        this.collectorIssues = collectorIssues;
    }

    public void setFilteredSummaryIssues(final List<Long> filteredSummaryIssues) {
        this.filteredSummaryIssues = filteredSummaryIssues;
    }

}
