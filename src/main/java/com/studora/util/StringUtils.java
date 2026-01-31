package com.studora.util;

public class StringUtils {

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
        return input.trim().replaceAll(" +", " ");
    }
}
