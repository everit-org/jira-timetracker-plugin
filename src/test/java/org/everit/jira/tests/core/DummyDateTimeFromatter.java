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
package org.everit.jira.tests.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;

public class DummyDateTimeFromatter implements DateTimeFormatter {

  @Override
  public DateTimeFormatter forLoggedInUser() {
    return this;
  }

  @Override
  public String format(final Date arg0) {
    SimpleDateFormat sdf = new SimpleDateFormat(getFormatHint());
    return sdf.format(arg0);
  }

  @Override
  public DateTimeFormatter forUser(final User arg0) {
    return this;
  }

  @Override
  public String getFormatHint() {
    return "HH:mm";
  }

  @Override
  public Locale getLocale() {
    return Locale.ENGLISH;
  }

  @Override
  public DateTimeStyle getStyle() {
    return DateTimeStyle.TIME;
  }

  @Override
  public TimeZone getZone() {
    return TimeZone.getDefault();
  }

  @Override
  public Date parse(final String arg0)
      throws IllegalArgumentException, UnsupportedOperationException {
    SimpleDateFormat sdf = new SimpleDateFormat(getFormatHint());
    try {
      return sdf.parse(arg0);
    } catch (ParseException e) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public DateTimeFormatter withDefaultLocale() {
    return this;
  }

  @Override
  public DateTimeFormatter withDefaultZone() {
    return this;
  }

  @Override
  public DateTimeFormatter withLocale(final Locale arg0) {
    return this;
  }

  @Override
  public DateTimeFormatter withStyle(final DateTimeStyle arg0) {
    return this;
  }

  @Override
  public DateTimeFormatter withSystemZone() {
    return this;
  }

  @Override
  public DateTimeFormatter withZone(final TimeZone arg0) {
    return this;
  }

}
