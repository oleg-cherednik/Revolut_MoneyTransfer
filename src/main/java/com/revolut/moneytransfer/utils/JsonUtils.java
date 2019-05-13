package com.revolut.moneytransfer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 11.05.2019
 */
@UtilityClass
public final class JsonUtils {

    private final ObjectMapper MAPPER = new ObjectMapper();

    public <T> Map<String, T> readMap(String json) throws IOException {
        if (StringUtils.isBlank(json))
            return null;
        else {
            ObjectReader reader = MAPPER.readerFor(Map.class);
            MappingIterator<Map<String, T>> it = reader.readValues(json);

            if (it.hasNextValue()) {
                Map<String, T> res = it.next();
                return res.isEmpty() ? Collections.emptyMap() : res;
            } else
                return Collections.emptyMap();
        }
    }

    public static <T> T read(String json, Class<T> cls) {
        try {
            return json != null ? MAPPER.readerFor(cls).readValue(json) : null;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String write(T value) throws JsonProcessingException {
        return value != null ? MAPPER.writeValueAsString(value) : null;
    }

}
