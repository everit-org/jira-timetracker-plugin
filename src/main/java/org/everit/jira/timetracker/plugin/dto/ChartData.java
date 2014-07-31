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

public class ChartData {

    private String projectId;

    private Long duration;

    public ChartData(final String projectId, final Long duration) {
        super();
        this.projectId = projectId;
        this.duration = duration;
    }

    public Long getDuration() {
        return duration;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setDuration(final Long duration) {
        this.duration = duration;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

}
