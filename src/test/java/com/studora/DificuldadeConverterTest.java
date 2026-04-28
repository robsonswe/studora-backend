package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.entity.Dificuldade;
import com.studora.entity.DificuldadeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DificuldadeConverterTest {

    private DificuldadeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DificuldadeConverter();
    }

    // ==================== convertToDatabaseColumn ====================

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_facil_returns1() {
        assertEquals(1, converter.convertToDatabaseColumn(Dificuldade.FACIL));
    }

    @Test
    void convertToDatabaseColumn_media_returns2() {
        assertEquals(2, converter.convertToDatabaseColumn(Dificuldade.MEDIA));
    }

    @Test
    void convertToDatabaseColumn_dificil_returns3() {
        assertEquals(3, converter.convertToDatabaseColumn(Dificuldade.DIFICIL));
    }

    @Test
    void convertToDatabaseColumn_chute_returns4() {
        assertEquals(4, converter.convertToDatabaseColumn(Dificuldade.CHUTE));
    }

    // ==================== convertToEntityAttribute ====================

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_1_returnsFacil() {
        assertEquals(Dificuldade.FACIL, converter.convertToEntityAttribute(1));
    }

    @Test
    void convertToEntityAttribute_2_returnsMedia() {
        assertEquals(Dificuldade.MEDIA, converter.convertToEntityAttribute(2));
    }

    @Test
    void convertToEntityAttribute_3_returnsDificil() {
        assertEquals(Dificuldade.DIFICIL, converter.convertToEntityAttribute(3));
    }

    @Test
    void convertToEntityAttribute_4_returnsChute() {
        assertEquals(Dificuldade.CHUTE, converter.convertToEntityAttribute(4));
    }

    @Test
    void convertToEntityAttribute_invalidId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(99));
    }

    @Test
    void convertToEntityAttribute_zeroId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(0));
    }

    // ==================== round-trip ====================

    @Test
    void roundTrip_allValues_preserveIdentity() {
        for (Dificuldade d : Dificuldade.values()) {
            Integer dbValue = converter.convertToDatabaseColumn(d);
            assertEquals(d, converter.convertToEntityAttribute(dbValue));
        }
    }
}
