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
package org.everit.jira.reporting.plugin.dto;

public class UserSummaryDTO {

  public final class AliasNames {

    public static final String USER_DISPLAY_NAME = "userDisplayName";

    public static final String WORKLOGGED_TIME_SUM = "workloggedTimeSum";

    private AliasNames() {
    }
  }

  private String userDisplayName;

  private long workloggedTimeSum;

  public String getUserDisplayName() {
    return userDisplayName;
  }

  public long getWorkloggedTimeSum() {
    return workloggedTimeSum;
  }

  public void setUserDisplayName(final String userDisplayName) {
    this.userDisplayName = userDisplayName;
  }

  public void setWorkloggedTimeSum(final long workloggedTimeSum) {
    this.workloggedTimeSum = workloggedTimeSum;
  }
}
