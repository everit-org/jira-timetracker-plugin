/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.timetracker.plugin.dto;

/**
 * The ActionResult class. When the web action use the
 * {@link org.everit.jira.timetracker.plugin.JiraTimetrackerPluginImpl} methods, the methods give
 * back this result to the web action-s.
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
   *          The status.
   * @param message
   *          The message.
   */
  public ActionResult(final ActionResultStatus status, final String message) {
    this.status = status;
    this.message = message;
    messageParameter = "";
  }

  /**
   * Simple constructor.
   *
   * @param status
   *          The status.
   * @param message
   *          The message.
   * @param messageParameter
   *          The message parameter.
   */
  public ActionResult(final ActionResultStatus status, final String message,
      final String messageParameter) {
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
