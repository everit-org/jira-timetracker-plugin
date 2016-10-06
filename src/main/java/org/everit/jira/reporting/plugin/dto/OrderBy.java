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
 * Represents the ordering of the a search query.
 */
public class OrderBy {

  public static final OrderBy DEFAULT;

  static {
    DEFAULT = new OrderBy()
        .columnName("jtrp_col_issueKey")
        .order("ASC")
        .asc(true);
  }

  public boolean asc;

  public String columnName;

  public String order;

  public OrderBy asc(final boolean asc) {
    this.asc = asc;
    return this;
  }

  public OrderBy columnName(final String columnName) {
    this.columnName = columnName;
    return this;
  }

  public OrderBy order(final String order) {
    this.order = order;
    return this;
  }
}
