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
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.textview.MaterialTextView;
import com.test.cnouleg.R;

import org.w3c.dom.Text;

public class IconArrayAdapter extends BaseAdapter implements Filterable {
    Context context;
    LayoutInflater inflater;
    String[] ids;
    String[] labels;
    Drawable[] icons;
    public IconArrayAdapter(Context ctx, @ArrayRes int idsRes, @ArrayRes int labelsRes, @ArrayRes int iconsRes) {
        context = ctx;
        ids = context.getResources().getStringArray(idsRes);
        labels = context.getResources().getStringArray(labelsRes);

        if (ids.length != labels.length)
            throw new IllegalArgumentException("IDs array and labels array must be of the same length");

        icons = new Drawable[ids.length];

        TypedArray imgs = context.getResources().obtainTypedArray(iconsRes);

        for (int i = 0; i < icons.length; i++) {
            icons[i] = imgs.getDrawable(i);
        }

        imgs.recycle();

        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return ids.length;
    }

    @Override
    public Object getItem(int position) {
        return labels[position];
    }

    public String getItemValue(int position) {
        return ids[position];
    }

    @Override
    public long getItemId(int position) {
        return ids[position].hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.dropdown_item, parent, false);

        TextView textView = convertView.findViewById(android.R.id.text1);

        textView.setText(labels[position]);
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icons[position], null, null, null);

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
