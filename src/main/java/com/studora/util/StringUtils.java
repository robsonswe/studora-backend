package com.studora.util;

import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern MULTI_SPACE = Pattern.compile(" +");

    /**
     * Trims leading/trailing whitespace and replaces multiple consecutive 
     * space characters with a single space.
     * 
     * Example: " i   washed my  white      car " becomes "i washed my white car"
     */
    public static String normalizeSpace(String input) {
        if (input == null) {
            return null;
        }
        return MULTI_SPACE.matcher(input.trim()).replaceAll(" ");
    }
}
