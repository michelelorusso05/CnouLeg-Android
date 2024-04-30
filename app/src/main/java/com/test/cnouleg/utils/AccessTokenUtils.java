package com.test.cnouleg.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.cnouleg.StaticData;
import com.test.cnouleg.api.LoginResult;
import com.test.cnouleg.api.Token;

public class AccessTokenUtils {
    public static void SaveToken(Context context, String token) {
        SharedPreferences.Editor editor = context.getSharedPreferences("com.cocolorussococo.cnouleg", Context.MODE_PRIVATE).edit();

        editor.putString("accessToken", token);

        editor.apply();
    }
    public static String GetAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.cocolorussococo.cnouleg", Context.MODE_PRIVATE);

        return prefs.getString("accessToken", null);
    }

    public static String GetMongoDBIDFromToken(String token) {
        String[] chunks = token.split("\\.");

        String header = new String(Base64.decode(chunks[0], Base64.DEFAULT));
        String payload = new String(Base64.decode(chunks[1], Base64.DEFAULT));

        try {
            Token r = StaticData.getMapper().readValue(payload, Token.class);
            return r.getId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
