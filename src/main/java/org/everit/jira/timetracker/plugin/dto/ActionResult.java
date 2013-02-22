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

/**
 * The ActionResult class. When the web action use the
 * {@link org.everit.jira.timetracker.plugin.JiraTimetrackerPluginImpl} methods, the methods give back this result to
 * the web action-s.
 */
public class ActionResult {
    /**
     * The action result.
     */
    private final ActionResultStatus status;
    /**
     * The result message.
     */
    private final String message;
    /**
     * The result message parameter.
     */
    private String messageParameter;

    /**
     * Simple constructor.
     * 
     * @param status
     *            The status.
     * @param message
     *            The message.
     */
    public ActionResult(final ActionResultStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Simple constructor.
     * 
     * @param status
     *            The status.
     * @param message
     *            The message.
     * @param messageParameter
     *            The message parameter.
     */
    public ActionResult(final ActionResultStatus status, final String message, final String messageParameter) {
        this.status = status;
        this.message = message;
        this.messageParameter = messageParameter;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageParameter() {
        return messageParameter;
    }

    public ActionResultStatus getStatus() {
        return status;
    }

    public void setMessageParameter(final String messageParameter) {
        this.messageParameter = messageParameter;
    }
}
