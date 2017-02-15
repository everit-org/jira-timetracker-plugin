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

import java.util.ArrayList;
import java.util.List;

/**
 * The container for user picker to GUI. Contains the selected, or suggested user for the different
 * user pickers.
 */
public class UserPickerContainerDTO {

  private UserForPickerDTO currentUser;

  private List<UserForPickerDTO> issueAssignees = new ArrayList<>();

  private List<UserForPickerDTO> issueReporters = new ArrayList<>();

  private UserForPickerDTO noneUser;

  private List<UserForPickerDTO> suggestedUsers = new ArrayList<>();

  private UserForPickerDTO unassigedUser;

  private List<UserForPickerDTO> users = new ArrayList<>();

  public UserForPickerDTO getCurrentUser() {
    return currentUser;
  }

  public List<UserForPickerDTO> getIssueAssignees() {
    return issueAssignees;
  }

  public List<UserForPickerDTO> getIssueReporters() {
    return issueReporters;
  }

  public UserForPickerDTO getNoneUser() {
    return noneUser;
  }

  public List<UserForPickerDTO> getSuggestedUsers() {
    return suggestedUsers;
  }

  public UserForPickerDTO getUnassigedUser() {
    return unassigedUser;
  }

  public List<UserForPickerDTO> getUsers() {
    return users;
  }

  public void setCurrentUser(final UserForPickerDTO currentUser) {
    this.currentUser = currentUser;
  }

  public void setIssueAssignees(final List<UserForPickerDTO> issueAssignees) {
    this.issueAssignees = issueAssignees;
  }

  public void setIssueReporters(final List<UserForPickerDTO> issueReporters) {
    this.issueReporters = issueReporters;
  }

  public void setNoneUser(final UserForPickerDTO noneUser) {
    this.noneUser = noneUser;
  }

  public void setSuggestedUsers(final List<UserForPickerDTO> suggestedUsers) {
    this.suggestedUsers = suggestedUsers;
  }

  public void setUnassigedUser(final UserForPickerDTO unassigedUser) {
    this.unassigedUser = unassigedUser;
  }

  public void setUsers(final List<UserForPickerDTO> users) {
    this.users = users;
  }

}
