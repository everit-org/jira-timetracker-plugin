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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The hash class. It hashes a string. Include a method converts byte array to hex string and an
 * encrypt method.
 */
public final class HashUtil {

  private static final int HEX_0X0F = 0x0F;

  private static final int SHIFT_4 = 4;

  private static final int HEX_0X_FF = 0xFF;

  /**
   * Converts byte array to hex String.
   *
   * @param bytes
   *          The byte array.
   * @return The hex value in string.
   */
  private static String bytesToHex(final byte[] bytes) {
    char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for (int j = 0; j < bytes.length; j++) {
      v = bytes[j] & HEX_0X_FF;
      hexChars[j * 2] = hexArray[v >>> SHIFT_4];
      hexChars[(j * 2) + 1] = hexArray[v & HEX_0X0F];
    }
    return new String(hexChars);
  }

  /**
   * The encrypt method. Encode a string to a hex string with the SHA-1 cryptographic hash function.
   *
   * @param string
   *          The string you would like to encode.
   * @return The encrypted hex string.
   * @throws NoSuchAlgorithmException
   *           If no Provider supports a MessageDigestSpi implementation for the specified
   *           algorithm.
   * @throws UnsupportedEncodingException
   *           If the named charset is not supported.
   */
  public static String encryptString(final String string)
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
    crypt.reset();
    crypt.update(string.getBytes("UTF-8"));
    String sha1 = bytesToHex(crypt.digest());
    return sha1;
  }

  private HashUtil() {
  }
}
