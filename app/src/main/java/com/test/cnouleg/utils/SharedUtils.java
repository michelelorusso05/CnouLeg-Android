package com.test.cnouleg.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;

import androidx.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedUtils {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    public static SimpleDateFormat altDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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

    final static char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
    public static String formatPowerOrder(int number) {
        int value = (int) Math.floor(Math.log10(number));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format((float) number / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(number);
        }
    }

    public static String FormatDateLocale(Context context, String apiFormatDate) {
        Date date = null;
        try {
            date = SharedUtils.dateFormat.parse(apiFormatDate);
        } catch (ParseException ex) {
            try {
                date = SharedUtils.altDateFormat.parse(apiFormatDate);
            } catch (ParseException ignored) {}
        }
        assert date != null;

        java.text.DateFormat format = DateFormat.getDateFormat(context);
        return format.format(date);
    }

    public static String FormatDateLocale(Context context, Long d) {
        Date date = new Date(d);
        java.text.DateFormat format = DateFormat.getDateFormat(context);
        return format.format(date);
    }

    public static String FormatDateApi(Long d) {
        return DateFormat.format("yyyy-MM-dd", d).toString();
    }

    public static String GetFilenameFromURI(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);

        returnCursor.close();

        return name;
    }
}
