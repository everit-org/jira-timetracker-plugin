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
 * Information of unit summary.
 */
public class SummaryUnitDTO {

  private final double filteredNonWorkIndicatorPrecent;

  private final double filteredRealWorkIndicatorPrecent;

  private final String filteredSummary;

  private final String formattedExpectedWorkTime;

  private final String formattedNonWorkTime;

  private final double indicatorPrecent;

  private final String summary;

  /**
   * Simple constructor.
   *
   * @param filteredRealWorkIndicatorPrecent
   *          the percent of the filtered real work indicator.
   * @param filteredNonWorkIndicatorPrecent
   *          the percent of the filtered non work indicator.
   * @param indicatorPrecent
   *          the percent of indicator percent.
   * @param filteredSummary
   *          the formatted filtered summary.
   * @param summary
   *          the formatted summary.
   * @param formattedExpectedWorkTime
   *          the formatted expected work time.
   * @param formattedNonWorkTime
   *          the formatted non work time.
   */
  public SummaryUnitDTO(final double filteredRealWorkIndicatorPrecent,
      final double filteredNonWorkIndicatorPrecent, final double indicatorPrecent,
      final String filteredSummary, final String summary,
      final String formattedExpectedWorkTime,
      final String formattedNonWorkTime) {
    this.filteredNonWorkIndicatorPrecent = filteredNonWorkIndicatorPrecent;
    this.filteredRealWorkIndicatorPrecent = filteredRealWorkIndicatorPrecent;
    this.filteredSummary = filteredSummary;
    this.formattedExpectedWorkTime = formattedExpectedWorkTime;
    this.formattedNonWorkTime = formattedNonWorkTime;
    this.indicatorPrecent = indicatorPrecent;
    this.summary = summary;
  }

  public double getFilteredNonWorkIndicatorPrecent() {
    return filteredNonWorkIndicatorPrecent;
  }

  public double getFilteredRealWorkIndicatorPrecent() {
    return filteredRealWorkIndicatorPrecent;
  }

  public String getFilteredSummary() {
    return filteredSummary;
  }

  public String getFormattedExpectedWorkTime() {
    return formattedExpectedWorkTime;
  }

  public String getFormattedNonWorkTime() {
    return formattedNonWorkTime;
  }

  public double getIndicatorPrecent() {
    return indicatorPrecent;
  }

  public String getSummary() {
    return summary;
  }

}
