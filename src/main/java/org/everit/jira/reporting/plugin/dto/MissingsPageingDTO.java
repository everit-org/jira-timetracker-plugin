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
 * Contains information to missings paging.
 */
public class MissingsPageingDTO {

  private Integer actPageNumber = 1;

  private Integer end = 0;

  private Integer maxPageNumber = null;

  private Integer resultSize = 0;

  private Integer start = 0;

  public MissingsPageingDTO actPageNumber(final Integer actPageNumber) {
    this.actPageNumber = actPageNumber;
    return this;
  }

  public MissingsPageingDTO end(final Integer end) {
    this.end = end;
    return this;
  }

  public Integer getActPageNumber() {
    return actPageNumber;
  }

  public Integer getEnd() {
    return end;
  }

  public Integer getMaxPageNumber() {
    return maxPageNumber;
  }

  public Integer getResultSize() {
    return resultSize;
  }

  public Integer getStart() {
    return start;
  }

  public MissingsPageingDTO maxPageNumber(final Integer maxPageNumber) {
    this.maxPageNumber = maxPageNumber;
    return this;
  }

  public MissingsPageingDTO resultSize(final Integer resultSize) {
    this.resultSize = resultSize;
    return this;
  }

  public MissingsPageingDTO start(final Integer start) {
    this.start = start;
    return this;
  }

}
