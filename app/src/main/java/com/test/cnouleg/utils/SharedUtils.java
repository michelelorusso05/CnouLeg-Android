package com.test.cnouleg.utils;

import android.content.Context;

import androidx.preference.PreferenceManager;

public class SharedUtils {
    public static String GetServer(Context context) {
        String server = PreferenceManager.getDefaultSharedPreferences(context).getString("server", "");
        if (server.isEmpty())
            throw new IllegalArgumentException("Server is not set");

        if (!server.contains("://"))
            server = "http://" + server;

        if (server.endsWith("/"))
            server = server.substring(0, server.length() - 1);

        return server;
    }
}
