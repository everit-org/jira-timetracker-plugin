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
package org.everit.jira.core;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl.Builder;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.issue.worklog.Worklog;

/**
 * Types of the remaining estimate for create and edit worklog.
 */
public enum RemainingEstimateType {

  AUTO {
    @Override
    public WorklogInputParameters build(final Builder builder,
        final String optinalValue) {
      return builder.build();
    }

    @Override
    public Worklog create(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult,
          true);
    }

    @Override
    public boolean delete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext, worklogResult,
          true);
    }

    @Override
    public Worklog update(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult,
          true);
    }

    @Override
    public WorklogResult validateCreate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateCreate(serviceContext, params);
    }

    @Override
    public WorklogResult validateDelete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final Long worklogId, final String optionalValue) {
      return worklogService.validateDelete(serviceContext, worklogId);
    }

    @Override
    public WorklogResult validateUpdate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateUpdate(serviceContext, params);
    }
  },

  LEAVE {
    @Override
    public WorklogInputParameters build(final Builder builder,
        final String optinalValue) {
      return builder.build();
    }

    @Override
    public Worklog create(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.createAndRetainRemainingEstimate(serviceContext, worklogResult, true);
    }

    @Override
    public boolean delete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.deleteAndRetainRemainingEstimate(serviceContext, worklogResult, true);
    }

    @Override
    public Worklog update(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.updateAndRetainRemainingEstimate(serviceContext, worklogResult, true);
    }

    @Override
    public WorklogResult validateCreate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateCreate(serviceContext, params);
    }

    @Override
    public WorklogResult validateDelete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final Long worklogId, final String optionalValue) {
      return worklogService.validateDelete(serviceContext, worklogId);
    }

    @Override
    public WorklogResult validateUpdate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateUpdate(serviceContext, params);
    }
  },

  MANUAL {
    @Override
    public WorklogInputParameters build(final Builder builder,
        final String adjustmentAmount) {
      return builder.adjustmentAmount(adjustmentAmount).buildAdjustmentAmount();
    }

    @Override
    public Worklog create(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.createWithManuallyAdjustedEstimate(serviceContext,
          (WorklogAdjustmentAmountResult) worklogResult, true);
    }

    @Override
    public boolean delete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.deleteWithManuallyAdjustedEstimate(serviceContext,
          (WorklogAdjustmentAmountResult) worklogResult, true);
    }

    @Override
    public Worklog update(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      throw new UnsupportedOperationException("Not supported operation.");
    }

    @Override
    public WorklogResult validateCreate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateCreateWithManuallyAdjustedEstimate(serviceContext,
          (WorklogAdjustmentAmountInputParameters) params);
    }

    @Override
    public WorklogResult validateDelete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final Long worklogId, final String adjustAmount) {
      return worklogService.validateDeleteWithManuallyAdjustedEstimate(serviceContext, worklogId,
          adjustAmount);
    }

    @Override
    public WorklogResult validateUpdate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      throw new UnsupportedOperationException("Not supported operation.");
    }
  },

  NEW {
    @Override
    public WorklogInputParameters build(final Builder builder,
        final String newEstimate) {
      return builder.newEstimate(newEstimate).buildNewEstimate();
    }

    @Override
    public Worklog create(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.createWithNewRemainingEstimate(serviceContext,
          (WorklogNewEstimateResult) worklogResult, true);
    }

    @Override
    public boolean delete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.deleteWithNewRemainingEstimate(serviceContext,
          (WorklogNewEstimateResult) worklogResult, true);
    }

    @Override
    public Worklog update(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogResult worklogResult) {
      return worklogService.updateWithNewRemainingEstimate(serviceContext,
          (WorklogNewEstimateResult) worklogResult, true);
    }

    @Override
    public WorklogResult validateCreate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateCreateWithNewEstimate(serviceContext,
          (WorklogNewEstimateInputParameters) params);
    }

    @Override
    public WorklogResult validateDelete(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final Long worklogId, final String newEstimate) {
      return worklogService.validateDeleteWithNewEstimate(serviceContext, worklogId, newEstimate);
    }

    @Override
    public WorklogResult validateUpdate(final WorklogService worklogService,
        final JiraServiceContext serviceContext, final WorklogInputParameters params) {
      return worklogService.validateUpdateWithNewEstimate(serviceContext,
          (WorklogNewEstimateInputParameters) params);
    }
  };

  public abstract WorklogInputParameters build(final WorklogInputParametersImpl.Builder builder,
      final String optinalValue);

  public abstract Worklog create(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final WorklogResult worklogResult);

  public abstract boolean delete(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final WorklogResult worklogResult);

  public abstract Worklog update(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final WorklogResult worklogResult);

  public abstract WorklogResult validateCreate(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final WorklogInputParameters params);

  public abstract WorklogResult validateDelete(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final Long worklogId, String optionalValue);

  public abstract WorklogResult validateUpdate(final WorklogService worklogService,
      final JiraServiceContext serviceContext, final WorklogInputParameters params);
}
