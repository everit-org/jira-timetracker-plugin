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

import com.atlassian.jira.issue.MutableIssue;

/**
 * The Jira Timetracker plugin utils class.
 */
public final class JiraTimetrackerUtil {

    /**
     * Check the issue original estimated time. If null then the original estimated time wasn't specified, else compare
     * the spent time whit the original estimated time.
     * 
     * @param issue
     *            The issue.
     * @return True if not specified, bigger or equals whit spent time else false.
     */
    public static boolean checkIssueEstimatedTime(final MutableIssue issue) {
        Long originalEstimated = issue.getOriginalEstimate();
        Long timeSpent = issue.getTimeSpent();
        if (originalEstimated != null) {
            if (timeSpent > originalEstimated) {
                return false;
            }
        }
        return true;
    }

}
