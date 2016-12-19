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

  private double filteredNonWorkIndicatorPrecent;

  private double filteredPercent;

  private double filteredRealWorkIndicatorPrecent;

  private String filteredSummary;

  private String formattedExpectedWorkTime;

  private String formattedNonWorkTime;

  private double indicatorPrecent;

  private String summary;

  /**
   * Simple constructor.
   */
  public SummaryUnitDTO() {
  }

  public SummaryUnitDTO filteredNonWorkIndicatorPrecent(
      final double filteredNonWorkIndicatorPrecent) {
    this.filteredNonWorkIndicatorPrecent = filteredNonWorkIndicatorPrecent;
    return this;
  }

  public SummaryUnitDTO filteredPercent(final double filteredPercent) {
    this.filteredPercent = filteredPercent;
    return this;
  }

  public SummaryUnitDTO filteredRealWorkIndicatorPrecent(
      final double filteredRealWorkIndicatorPrecent) {
    this.filteredRealWorkIndicatorPrecent = filteredRealWorkIndicatorPrecent;
    return this;
  }

  public SummaryUnitDTO filteredSummary(final String filteredSummary) {
    this.filteredSummary = filteredSummary;
    return this;
  }

  public SummaryUnitDTO formattedExpectedWorkTime(final String formattedExpectedWorkTime) {
    this.formattedExpectedWorkTime = formattedExpectedWorkTime;
    return this;
  }

  public SummaryUnitDTO formattedNonWorkTime(final String formattedNonWorkTime) {
    this.formattedNonWorkTime = formattedNonWorkTime;
    return this;
  }

  public double getFilteredNonWorkIndicatorPrecent() {
    return filteredNonWorkIndicatorPrecent;
  }

  public double getFilteredPercent() {
    return filteredPercent;
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

  public SummaryUnitDTO indicatorPrecent(final double indicatorPrecent) {
    this.indicatorPrecent = indicatorPrecent;
    return this;
  }

  public SummaryUnitDTO summary(final String summary) {
    this.summary = summary;
    return this;
  }

}
