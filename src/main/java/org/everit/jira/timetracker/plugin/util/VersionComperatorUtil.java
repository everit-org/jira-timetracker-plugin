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

/**
 * The Jira Timetracker plugin Version comperator utils class.
 */
public final class VersionComperatorUtil {

  /**
   * Remove the not numerical part of the vesion. Expects the following version format
   * (3.0.1-SNAPSHOT). The method cut down the -SNAPSHOT part.
   *
   * @param version
   *          The oroginal version.
   * @return Only the numerical part of the original version.
   */
  private static String removeNotNumericVersionParts(final String version) {
    int numericEnds = version.indexOf("-");
    if (numericEnds != -1) {
      return version.substring(0, numericEnds);
    }
    return version;
  }

  /**
   * Compare the two String version. Ignor string part version like -SNAPSHOT.
   *
   * @param str1
   *          a string of ordinal numbers separated by decimal points.
   * @param str2
   *          a string of ordinal numbers separated by decimal points.
   * @return The result is a negative integer if str1 is _numerically_ less than str2. The result is
   *         a positive integer if str1 is _numerically_ greater than str2. The result is zero if
   *         the strings are _numerically_ equal.
   */
  public static int versionCompare(final String str1, final String str2) {
    // Check empty Strign and null
    if ((str1 == null) || str1.isEmpty()) {
      return -1;
    }
    if ((str2 == null) || str2.isEmpty()) {
      return 1;
    }
    // remove not numeric version parts. we dont works with them
    String version1 = removeNotNumericVersionParts(str1);
    String version2 = removeNotNumericVersionParts(str2);

    String[] vals1 = version1.split("\\.");
    String[] vals2 = version2.split("\\.");
    int i = 0;
    // set index to first non-equal ordinal or length of shortest version string
    while ((i < vals1.length) && (i < vals2.length) && vals1[i].equals(vals2[i])) {
      i++;
    }
    // compare first non-equal ordinal number
    if ((i < vals1.length) && (i < vals2.length)) {
      int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
      return Integer.signum(diff);
    }
    // the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
    return Integer.signum(vals1.length - vals2.length);
  }

  private VersionComperatorUtil() {
  }

}
