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

public class CalendarSettingsValues {
	private int isCalendarPopup;
	private boolean isActualDate;
	private String excludeDates;
	private String includeDates;
	private boolean isColoring;

	public CalendarSettingsValues(int isCalendarPopup, boolean isActualDate,
			String excludeDates, String includeDates, boolean isColoring) {
		this.isCalendarPopup = isCalendarPopup;
		this.isActualDate = isActualDate;
		this.excludeDates = excludeDates;
		this.includeDates = includeDates;
		this.isColoring = isColoring;
	}

	public int getIsCalendarPopup() {
		return isCalendarPopup;
	}

	public boolean isActualDate() {
		return isActualDate;
	}

	public String getExcludeDates() {
		return excludeDates;
	}

	public String getIncludeDates() {
		return includeDates;
	}

	public boolean isColoring() {
		return isColoring;
	}
}