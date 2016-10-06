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
package org.everit.jira.timetracker.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.atlassian.plugin.util.ClassLoaderUtils;

/**
 * Utility class to load properties files.
 */
public final class PropertiesUtil {

  /**
   * The Issue Collector jttp_build.porperties key.
   */
  public static final String ISSUE_COLLECTOR_SRC = "ISSUE_COLLECTOR_SRC";

  private static final String JTTP_PROPERTIES = "jttp_build.properties";

  /**
   * Gets jttp_buil.properties.
   *
   */
  public static Properties getJttpBuildProperties() {
    InputStream inputStream = null;
    Properties properties = new Properties();
    try {

      inputStream = ClassLoaderUtils.getResourceAsStream(JTTP_PROPERTIES, PropertiesUtil.class);

      if (inputStream == null) {
        URL resource = ClassLoaderUtils.getResource(JTTP_PROPERTIES, PropertiesUtil.class);
        File propertiesFile = new File(resource.getFile());
        inputStream = new FileInputStream(propertiesFile);
      }

      properties.load(inputStream);

    } catch (IOException e) {
      return new Properties();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // do nothing
        }
      }
    }
    return properties;
  }

  private PropertiesUtil() {
  }
}
