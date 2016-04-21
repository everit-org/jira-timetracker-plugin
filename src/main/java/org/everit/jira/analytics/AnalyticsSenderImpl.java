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
package org.everit.jira.analytics;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.everit.jira.analytics.event.AnalyticsEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of {@link AnalyticsSender}.
 */
public class AnalyticsSenderImpl implements InitializingBean, DisposableBean, AnalyticsSender {

  /**
   * Sender command that send event to analytics application.
   */
  private static class Command implements Runnable {

    private final AnalyticsEvent analyticsEvent;

    Command(final AnalyticsEvent analyticsEvent) {
      this.analyticsEvent = analyticsEvent;
    }

    @Override
    public void run() {
      try {
        HttpClient httpClient = new HttpClient();
        String url = analyticsEvent.getUrl();
        GetMethod getMethod = new GetMethod(url);
        httpClient.executeMethod(getMethod);
      } catch (IOException e) {
        // do nothing
      }
    }
  }

  private static final int MAX_THREAD = 4;

  private ExecutorService executorService;

  @Override
  public void afterPropertiesSet() throws Exception {
    executorService = Executors.newFixedThreadPool(MAX_THREAD);
  }

  @Override
  public void destroy() throws Exception {
    executorService.shutdown();
  }

  @Override
  public void send(final AnalyticsEvent analyticsEvent) {
    executorService.execute(new Command(analyticsEvent));
  }
}
