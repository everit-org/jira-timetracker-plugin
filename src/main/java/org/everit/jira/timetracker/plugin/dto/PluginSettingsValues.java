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
    private List<Pattern> filteredSummaryIssues;
    /**
     * The collector issues pattern list;
     */
    private List<Pattern> collectorIssues;

    // TODO javadoc !!! WHY MISSING!!!

    private int startTimeChange;

    private int endTimeChange;

    private String excludeDates;

    private String includeDates;

    private Boolean isColoring;

    private int fdow;

    public PluginSettingsValues() {

    }

    public PluginSettingsValues(final CalendarSettingsValues calendarSettingValues,
            final List<Pattern> filteredSummaryIssues,
            final List<Pattern> collectorIssues,
            final int startTimeChange, final int endTimeChange) {
        super();
        isCalendarPopup = calendarSettingValues.getIsCalendarPopup();
        isActualDate = calendarSettingValues.isActualDate();
        this.filteredSummaryIssues = filteredSummaryIssues;
        this.collectorIssues = collectorIssues;
        excludeDates = calendarSettingValues.getExcludeDates();
        includeDates = calendarSettingValues.getIncludeDates();
        this.startTimeChange = startTimeChange;
        this.endTimeChange = endTimeChange;
        isColoring = calendarSettingValues.isColoring();
        fdow = calendarSettingValues.getFdow();
    }

    public List<Pattern> getCollectorIssues() {
        return collectorIssues;
    }

    public int getEndTimeChange() {
        return endTimeChange;
    }

    public String getExcludeDates() {
        return excludeDates;
    }

    public int getFdow() {
        return fdow;
    }

    public List<Pattern> getFilteredSummaryIssues() {
        return filteredSummaryIssues;
    }

    public String getIncludeDates() {
        return includeDates;
    }

    public int getIsCalendarPopup() {
        return isCalendarPopup;
    }

    public int getStartTimeChange() {
        return startTimeChange;
    }

    public Boolean isActualDate() {
        return isActualDate;
    }

    public int isCalendarPopup() {
        return isCalendarPopup;
    }

    public Boolean isColoring() {
        return isColoring;
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

    public void setColoring(final boolean isColoring) {
        this.isColoring = isColoring;
    }

    public void setEndTimeChange(final int endTimeChange) {
        this.endTimeChange = endTimeChange;
    }

    public void setExcludeDates(final String excludeDates) {
        this.excludeDates = excludeDates;
    }

    public void setFdow(final int fdow) {
        this.fdow = fdow;
    }

    public void setFilteredSummaryIssues(
            final List<Pattern> filteredSummaryIssues) {
        this.filteredSummaryIssues = filteredSummaryIssues;
    }

    public void setIncludeDates(final String includeDates) {
        this.includeDates = includeDates;
    }

    public void setIsCalendarPopup(final int isCalendarPopup) {
        this.isCalendarPopup = isCalendarPopup;
    }

    public void setStartTimeChange(final int startTimeChange) {
        this.startTimeChange = startTimeChange;
    }

}
