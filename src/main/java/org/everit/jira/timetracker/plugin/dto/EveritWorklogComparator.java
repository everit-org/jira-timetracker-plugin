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

import java.io.Serializable;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import org.everit.jira.timetracker.plugin.DateTimeConverterUtil;

/**
 * The comparator of the {@link EveritWorklog}.
 */
public class EveritWorklogComparator implements Comparator<EveritWorklog>, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4563970131275373338L;

    @Override
    public int compare(final EveritWorklog o1, final EveritWorklog o2) {
        Date o1StartDate = null;
        Date o2StartDate = null;
        try {
            o1StartDate = DateTimeConverterUtil.stringTimeToDateTime(o1.getStartTime());
            o2StartDate = DateTimeConverterUtil.stringTimeToDateTime(o2.getStartTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Faild to convert startDate to Date", e);
        }
        int result = o1StartDate.compareTo(o2StartDate);
        if (result != 0) {
            return result;
        }
        Date o1EndDate = null;
        Date o2EndDate = null;
        try {
            o1EndDate = DateTimeConverterUtil.stringTimeToDateTime(o1.getEndTime());
            o2EndDate = DateTimeConverterUtil.stringTimeToDateTime(o2.getEndTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Faild to convert endDate to Date", e);
        }
        return o1EndDate.compareTo(o2EndDate);
    }

}
