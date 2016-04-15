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
 * Contains information to paging.
 */
public class PagingDTO {

  private Integer actPageNumber = 1;

  private Long end = 0L;

  private Integer maxPageNumber = null;

  private Long start = 0L;

  public PagingDTO actPageNumber(final Integer actPageNumber) {
    this.actPageNumber = actPageNumber;
    return this;
  }

  public PagingDTO end(final Long end) {
    this.end = end;
    return this;
  }

  public Integer getActPageNumber() {
    return actPageNumber;
  }

  public Long getEnd() {
    return end;
  }

  public Integer getMaxPageNumber() {
    return maxPageNumber;
  }

  public Long getStart() {
    return start;
  }

  public PagingDTO maxPageNumber(final Integer maxPageNumber) {
    this.maxPageNumber = maxPageNumber;
    return this;
  }

  public PagingDTO start(final Long start) {
    this.start = start;
    return this;
  }

}
