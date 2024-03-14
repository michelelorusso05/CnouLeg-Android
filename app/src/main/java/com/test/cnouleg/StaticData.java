package com.test.cnouleg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StaticData {
    private static ObjectMapper sharedMapper;
    public static ObjectMapper getMapper() {
        if (sharedMapper == null) {
            sharedMapper = new ObjectMapper();
        }
        return sharedMapper;
    }
}
