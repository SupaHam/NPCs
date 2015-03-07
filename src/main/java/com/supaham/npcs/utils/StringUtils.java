package com.supaham.npcs.utils;

import javax.annotation.Nullable;

public class StringUtils {

  /**
   * Normalizes a string. The following characters are converted to an underscore '_':
   * <ul>
   * <li><b>hyphen</b> '-'</li>
   * <li><b>space</b> ' '</li>
   * </ul>
   *
   * @param str String to normalize
   *
   * @return the normalized string
   */
  public static String normalizeString(@Nullable String str) {
    if (str == null) {
      return null;
    }
    return str.replaceAll("[- ]*", "_").toLowerCase();
  }
}
