package com.test.cnouleg.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;

import com.test.cnouleg.R;

import java.util.HashMap;

public class ValuesTranslator {
    static HashMap<String, Integer> classLevelIdToStringResourceIndex = new HashMap<>();
    static HashMap<String, Integer> subjectIdToStringResourceIndex = new HashMap<>();

    private static void initLUP(Context context, @ArrayRes int array, HashMap<String, Integer> dest) {
        String[] arr = context.getResources().getStringArray(array);

        for (int i = 0; i < arr.length; i++)
            dest.put(arr[i], i);
    }
    private static Integer getIndex(Context context, @ArrayRes int tag_array, HashMap<String, Integer> hashmap, String id) {
        if (hashmap.isEmpty())
            initLUP(context, tag_array, hashmap);

        return hashmap.get(id);
    }
    private static String getTranslatedName(Context context, @ArrayRes int tag_array, @ArrayRes int values_array, HashMap<String, Integer> hashmap, String id) {
        Integer index = getIndex(context, tag_array, hashmap, id);

        if (index == null)
            return id;

        return context.getResources().getStringArray(values_array)[index];
    }

    public static String getTranslatedSubject(Context context, String id) {
        return getTranslatedName(context, R.array.subject_values, R.array.subject_strings, subjectIdToStringResourceIndex, id);
    }
    public static String getTranslatedClassLevel(Context context, String id) {
        return getTranslatedName(context, R.array.class_level_values, R.array.class_level_strings, classLevelIdToStringResourceIndex, id);
    }

    public static Drawable getDrawableForSubject(Context context, String id) {
        TypedArray icons = context.getResources().obtainTypedArray(R.array.subjects_icons);
        Drawable drawable = icons.getDrawable(getIndex(context, R.array.subject_values, subjectIdToStringResourceIndex, id));
        icons.recycle();
        return drawable;
    }
}
