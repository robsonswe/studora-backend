package com.studora.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    // ==================== normalizeSpace ====================

    @Test
    void normalizeSpace_null_returnsNull() {
        assertNull(StringUtils.normalizeSpace(null));
    }

    @Test
    void normalizeSpace_emptyString_returnsEmpty() {
        assertEquals("", StringUtils.normalizeSpace(""));
    }

    @Test
    void normalizeSpace_onlySpaces_returnsEmpty() {
        assertEquals("", StringUtils.normalizeSpace("   "));
    }

    @Test
    void normalizeSpace_leadingTrailingSpaces_trimmed() {
        assertEquals("hello", StringUtils.normalizeSpace("  hello  "));
    }

    @Test
    void normalizeSpace_multipleInternalSpaces_collapsedToOne() {
        assertEquals("a b c", StringUtils.normalizeSpace("a   b   c"));
    }

    @Test
    void normalizeSpace_alreadyNormalized_unchanged() {
        assertEquals("hello world", StringUtils.normalizeSpace("hello world"));
    }

    @Test
    void normalizeSpace_mixedLeadingInternalTrailing() {
        assertEquals("i washed my white car", StringUtils.normalizeSpace(" i   washed my  white      car "));
    }

    @Test
    void normalizeSpace_tabsNotCollapsed() {
        // Only space characters are handled; tab is not a ' '
        String input = "a\t\tb";
        // trim() only removes leading/trailing whitespace, MULTI_SPACE matches " +"
        // Tab characters are NOT matched by " +" (space), so they're preserved
        assertEquals("a\t\tb", StringUtils.normalizeSpace(input));
    }

    // ==================== normalizeForSearch ====================

    @Test
    void normalizeForSearch_null_returnsNull() {
        assertNull(StringUtils.normalizeForSearch(null));
    }

    @Test
    void normalizeForSearch_emptyString_returnsEmpty() {
        assertEquals("", StringUtils.normalizeForSearch(""));
    }

    @Test
    void normalizeForSearch_uppercaseConverted() {
        assertEquals("direito constitucional", StringUtils.normalizeForSearch("DIREITO CONSTITUCIONAL"));
    }

    @Test
    void normalizeForSearch_accentRemoved() {
        assertEquals("joao", StringUtils.normalizeForSearch("João"));
    }

    @Test
    void normalizeForSearch_multipleAccents() {
        assertEquals("administracao publica", StringUtils.normalizeForSearch("Administração Pública"));
    }

    @Test
    void normalizeForSearch_cedilhaRemoved() {
        assertEquals("licao", StringUtils.normalizeForSearch("lição"));
    }

    @Test
    void normalizeForSearch_spacesNormalizedAndAccentsRemoved() {
        assertEquals("multiplos espacos", StringUtils.normalizeForSearch("  Múltiplos   Espaços  "));
    }

    @Test
    void normalizeForSearch_noAccentsOrSpaces_lowercaseOnly() {
        assertEquals("abc", StringUtils.normalizeForSearch("ABC"));
    }

    @Test
    void normalizeForSearch_allAccentTypes() {
        assertEquals("aeiouu", StringUtils.normalizeForSearch("áéíõúü"));
    }
}
