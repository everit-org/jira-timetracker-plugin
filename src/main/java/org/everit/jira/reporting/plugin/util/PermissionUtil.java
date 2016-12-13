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
package org.everit.jira.reporting.plugin.util;

import java.util.Collection;
import java.util.List;

import org.everit.jira.settings.TimeTrackerSettingsHelper;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Reporting permissions check handler Util.
 */
public final class PermissionUtil {

  /**
   * Check the user has permission set to browse other useres worklogs.
   *
   * @param user
   *          The checked user.
   * @return True if has permission otherwise false.
   */
  public static boolean hasBrowseUserPermission(final ApplicationUser user,
      final TimeTrackerSettingsHelper settingsHelper) {
    List<String> browseGroups = settingsHelper.loadReportingGlobalSettings().getBrowseGroups();
    if (browseGroups.isEmpty()) {
      return true;
    }
    Collection<String> groupNamesForUser =
        ComponentAccessor.getGroupManager().getGroupNamesForUser(user);
    for (String browseGroup : browseGroups) {
      if (groupNamesForUser.contains(browseGroup)) {
        return true;
      }
    }
    return false;
  }

  private PermissionUtil() {
  }
}
