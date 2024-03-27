package com.test.cnouleg.utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.preference.PreferenceManager;

import com.test.cnouleg.api.Author;
import com.test.cnouleg.api.Note;

import java.io.Serializable;

public class SharedUtils {
    public static String GetServer(Context context) {
        String server = PreferenceManager.getDefaultSharedPreferences(context).getString("server", "https://cochome.ddns.net");
        if (server.isEmpty())
            throw new IllegalArgumentException("Server is not set");

        if (!server.contains("://"))
            server = "http://" + server;

        if (server.endsWith("/"))
            server = server.substring(0, server.length() - 1);

        return server;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Parcelable> T GetParcelable(Bundle bundle, String name, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return bundle.getParcelable(name, clazz);
        else {
            Object obj = bundle.getParcelable(name);
            return (T) obj;
        }
    }
}
