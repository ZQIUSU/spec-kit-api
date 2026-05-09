package com.creamlogin.app.validation;

/** Validates 18-digit Chinese mainland resident ID numbers (GB 11643 checksum). */
public final class IdCardValidator {

  private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
  private static final String CHECK_CHARS = "10X98765432";

  private IdCardValidator() {}

  public static boolean isValid(String raw) {
    if (raw == null) {
      return false;
    }
    String id = raw.trim().toUpperCase();
    if (id.length() != 18) {
      return false;
    }
    for (int i = 0; i < 17; i++) {
      if (!Character.isDigit(id.charAt(i))) {
        return false;
      }
    }
    char last = id.charAt(17);
    if (!Character.isDigit(last) && last != 'X') {
      return false;
    }
    int sum = 0;
    for (int i = 0; i < 17; i++) {
      sum += (id.charAt(i) - '0') * WEIGHTS[i];
    }
    char expected = CHECK_CHARS.charAt(sum % 11);
    return last == expected;
  }

  /** Normalizes to uppercase X, no internal spaces. */
  public static String normalize(String raw) {
    return raw == null ? "" : raw.trim().toUpperCase();
  }
}
