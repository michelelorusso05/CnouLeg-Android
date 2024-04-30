package com.test.cnouleg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

public class StaticData {
    private static ObjectMapper sharedMapper;
    public static ObjectMapper getMapper() {
        if (sharedMapper == null) {
            sharedMapper = new ObjectMapper();
            sharedMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return sharedMapper;
    }

    private static OkHttpClient sharedClient;
    public static OkHttpClient getClient() {
        if (sharedClient == null) {
            sharedClient = new OkHttpClient.Builder()
                    .build();
        }
        return sharedClient;
    }
}
