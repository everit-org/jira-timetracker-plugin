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

/**
 * Representation of component to picker.
 */
public class PickerComponentDTO {

  /**
   * Alias names to projections bean.
   */
  public final class AliasNames {

    public static final String COMPONENT_NAME = "name";

    private AliasNames() {
    }
  }

  public static final String NO_COMPONENT = "No component";

  /**
   * Create no component {@link PickerComponentDTO}.
   */
  public static PickerComponentDTO createNoComponent() {
    PickerComponentDTO nocomponent = new PickerComponentDTO();
    nocomponent.setName(NO_COMPONENT);
    return nocomponent;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
