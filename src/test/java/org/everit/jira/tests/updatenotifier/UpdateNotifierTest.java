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
package org.everit.jira.tests.updatenotifier;

import org.everit.jira.updatenotifier.TimetrackerVersionUpdater;
import org.everit.jira.updatenotifier.UpdateNotifier;
import org.everit.jira.updatenotifier.exception.UpdateException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.BuildUtilsInfo;

public class UpdateNotifierTest {

  private static final long TWO_MINUTE_IN_MILISEC = 120000;

  private void initMockComponentWorker(final Integer buildNumber) {
    MockComponentWorker mockComponentWorker = new MockComponentWorker();

    BuildUtilsInfo buildUtilsInfo = Mockito.mock(BuildUtilsInfo.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(buildUtilsInfo.getApplicationBuildNumber())
        .thenReturn(buildNumber);

    mockComponentWorker.addMock(BuildUtilsInfo.class, buildUtilsInfo)
        .init();
  }

  @Test
  public void testNotUpdateVersion() {
    initMockComponentWorker(725);
    UpdateNotifier mock = Mockito.mock(UpdateNotifier.class);
    TimetrackerVersionUpdater jttpVersionUpdater = new TimetrackerVersionUpdater(mock);
    Mockito.when(mock.getLastUpdateTime())
        .thenReturn(System.currentTimeMillis() - TWO_MINUTE_IN_MILISEC);
    jttpVersionUpdater.updateLatestVersion();
    Mockito.verify(mock).getLastUpdateTime();
  }

  @Test
  public void testUpdateFail() {
    initMockComponentWorker(711);
    UpdateNotifier mock = Mockito.mock(UpdateNotifier.class);
    TimetrackerVersionUpdater jttpVersionUpdater = new TimetrackerVersionUpdater(mock);
    Mockito.when(mock.getLastUpdateTime()).thenReturn(2L);
    Mockito.doNothing().when(mock).putLastUpdateTime(org.easymock.EasyMock.anyLong());
    Mockito.doNothing().when(mock).putLatestVersion(Matchers.anyString());
    try {
      jttpVersionUpdater.updateLatestVersion();
      Assert.fail();
    } catch (UpdateException e) {
    }
    Mockito.verify(mock, Mockito.times(2)).getLastUpdateTime();
    Mockito.verify(mock).putLastUpdateTime(Matchers.anyLong());
  }

  @Test
  public void testUpdateVersion() {
    initMockComponentWorker(725);
    UpdateNotifier mock = Mockito.mock(UpdateNotifier.class);
    TimetrackerVersionUpdater jttpVersionUpdater = new TimetrackerVersionUpdater(mock);
    Mockito.when(mock.getLastUpdateTime()).thenReturn(2L);
    Mockito.doNothing().when(mock).putLastUpdateTime(org.easymock.EasyMock.anyLong());
    Mockito.doNothing().when(mock).putLatestVersion(Matchers.anyString());
    jttpVersionUpdater.updateLatestVersion();
    Mockito.verify(mock, Mockito.times(2)).getLastUpdateTime();
    Mockito.verify(mock).putLastUpdateTime(Matchers.anyLong());
    Mockito.verify(mock).putLatestVersion(Matchers.eq("1.4.3"));
  }

}
