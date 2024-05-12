package com.test.cnouleg.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;

import com.test.cnouleg.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IconArrayAdapter extends BaseAdapter implements Filterable {
    Context context;
    LayoutInflater inflater;
    List<String> ids;
    List<String> labels;
    List<Drawable> icons;
    public static IconArrayAdapter createFromRes(Context ctx, @ArrayRes int idsRes, @ArrayRes int labelsRes, @ArrayRes int iconsRes) {
        String[] ids = ctx.getResources().getStringArray(idsRes);
        String[] idVals = ctx.getResources().getStringArray(labelsRes);

        TypedArray iconsArray = ctx.getResources().obtainTypedArray(iconsRes);
        Drawable[] iconsTemp = new Drawable[ids.length];

        for (int i = 0; i < iconsTemp.length; i++) {
            iconsTemp[i] = iconsArray.getDrawable(i);
        }

        iconsArray.recycle();

        return new IconArrayAdapter(ctx, Arrays.asList(ids), Arrays.asList(idVals), Arrays.asList(iconsTemp));
    }
    public IconArrayAdapter(Context ctx, List<String> idsRes, List<String> labelsRes, List<Drawable> iconsRes) {
        context = ctx;
        ids = idsRes;
        labels = labelsRes;

        if (ids.size() != labels.size())
            throw new IllegalArgumentException("IDs array and labels array must be of the same length");

        icons = iconsRes;

        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public Object getItem(int position) {
        return labels.get(position);
    }

    public String getItemValue(int position) {
        return ids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ids.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.dropdown_item, parent, false);

        TextView textView = convertView.findViewById(android.R.id.text1);

        textView.setText(labels.get(position));
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.get(position), null, null, null);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }
}
