package com.studora.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DificuldadeConverter implements AttributeConverter<Dificuldade, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Dificuldade dificuldade) {
        if (dificuldade == null) {
            return null;
        }
        return dificuldade.getId();
    }

    @Override
    public Dificuldade convertToEntityAttribute(Integer id) {
        if (id == null) {
            return null;
        }
        return Dificuldade.fromId(id);
    }
}
