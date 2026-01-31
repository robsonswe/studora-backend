package com.studora.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.studora.util.StringUtils;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class StringNormalizationDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return StringUtils.normalizeSpace(value);
    }
}
