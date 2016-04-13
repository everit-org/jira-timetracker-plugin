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

import java.util.List;

/**
 * Representation the converted search params.
 */
public class ConvertedSearchParam {

  public List<String> notBrowsableProjectKeys;

  public ReportSearchParam reportSearchParam;

  public ConvertedSearchParam notBrowsableProjectKeys(final List<String> notBrowsableProjectKeys) {
    this.notBrowsableProjectKeys = notBrowsableProjectKeys;
    return this;
  }

  public ConvertedSearchParam reportSearchParam(final ReportSearchParam reportSearchParam) {
    this.reportSearchParam = reportSearchParam;
    return this;
  }

}
