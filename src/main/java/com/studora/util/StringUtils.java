package com.studora.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern MULTI_SPACE = Pattern.compile(" +");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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

    /**
     * Normalizes a string for searching by removing accents, 
     * converting to lowercase and normalizing spaces.
     */
    public static String normalizeForSearch(String input) {
        if (input == null) {
            return null;
        }
        String normalized = normalizeSpace(input).toLowerCase();
        String nfdNormalizedString = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return DIACRITICS.matcher(nfdNormalizedString).replaceAll("");
    }
}
