package com.test.cnouleg;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.common.collect.Lists;
import com.test.cnouleg.utils.IconArrayAdapter;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.List;

public class FragmentSearchBar extends Fragment {
    public static final String TAG = FragmentSearchBar.class.getSimpleName();
    Context context;
    EditText searchBar;
    MaterialButton searchButton, filterButton;
    View progressBar;
    String text, school, subject;
    String tempSchool, tempSubject;
    String[] tags;
    int rating;

    public FragmentSearchBar() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_bar, container, false);

        searchBar = view.findViewById(R.id.searchBar);
        searchButton = view.findViewById(R.id.searchButton);
        filterButton = view.findViewById(R.id.filterButton);
        progressBar = view.findViewById(R.id.progressBar);

        filterButton.setOnClickListener((v) -> {
            View layout = LayoutInflater.from(context).inflate(R.layout.dialog_search_filters, null);

            Slider minStarsSlider = layout.findViewById(R.id.minRatingsSlider);
            minStarsSlider.setValue(rating);
            minStarsSlider.setLabelFormatter(new LabelFormatter() {
                @NonNull
                @Override
                public String getFormattedValue(float v) {
                    int stars = (int) v;
                    if (v == 0)
                        return getString(R.string.ratings_any);

                    return getResources().getQuantityString(R.plurals.stars, stars, stars);
                }
            });

            if (subject == null)
                subject = "any";
            if (school == null)
                school = "any";

            AutoCompleteTextView schoolEditText = layout.findViewById(R.id.school_edittext);
            schoolEditText.setAdapter(new ArrayAdapter<>(context, R.layout.dropdown_item,
                    Lists.asList(getString(R.string.ratings_any), getResources().getStringArray(R.array.class_level_strings))));
            schoolEditText.setOnFocusChangeListener((v1, hasFocus) -> {
                if (hasFocus) HideKeyboard(v);
            });

            String t = SharedUtils.GetMatchingString(context, R.array.class_level_values, R.array.class_level_strings, school);
            schoolEditText.setText(t.isEmpty() ? getString(R.string.ratings_any) : t, false);

            schoolEditText.setOnItemClickListener((parent, v1, position, id) ->
                    tempSchool = position == 0 ? "any" : getResources().getStringArray(R.array.class_level_values)[position - 1]);

            List<String> subjectIDs = Lists.asList("any", getResources().getStringArray(R.array.subject_values));
            List<String> subjectVals = Lists.asList(getString(R.string.ratings_any), getResources().getStringArray(R.array.subject_strings));

            TypedArray iconsArray = context.getResources().obtainTypedArray(R.array.subjects_icons);
            List<Drawable> iconsTemp = new ArrayList<>(subjectIDs.size());

            for (int i = 0; i < iconsArray.length(); i++) {
                iconsTemp.add(iconsArray.getDrawable(i));
            }
            iconsArray.recycle();

            iconsTemp.add(0, null);

            AutoCompleteTextView subjectEditText = layout.findViewById(R.id.subject_edittext);
            subjectEditText.setAdapter(new IconArrayAdapter(context, subjectIDs, subjectVals, iconsTemp));
            subjectEditText.setOnFocusChangeListener((v1, hasFocus) -> {
                if (hasFocus) HideKeyboard(v);
            });

            t = SharedUtils.GetMatchingString(context, R.array.subject_values, R.array.subject_strings, subject);
            subjectEditText.setText(t.isEmpty() ? getString(R.string.ratings_any) : t, false);

            subjectEditText.setOnItemClickListener((parent, v1, position, id) ->
                tempSubject = position == 0 ? "any" : getResources().getStringArray(R.array.subject_values)[position - 1]
            );

            EditText insertTags = layout.findViewById(R.id.tagEditText);
            ChipGroup tagsContainer = layout.findViewById(R.id.tagsContainer);

            if (tags != null) {
                for (String tag : tags) {
                    if (tagsContainer.getChildCount() >= 5) break;

                    Chip tagView = (Chip) getLayoutInflater().inflate(R.layout.alt_tag_chip, tagsContainer, false);
                    tagView.setText(tag);
                    tagView.setChipIcon(AppCompatResources.getDrawable(context, R.drawable.cancel_24px));
                    tagView.setOnClickListener((v1) -> tagsContainer.removeView(tagView));
                    tagsContainer.addView(tagView);
                }
            }

            insertTags.addTextChangedListener(new TextWatcher() {
                boolean tagsInserted = false;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.chars().anyMatch(value -> value == '\n' || value == '\t' || value == ',')) {
                        tagsInserted = true;
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (tagsInserted) {
                        tagsInserted = false;
                        String[] tagParts = s.toString().split("[\n\t,]+");
                        for (String tag : tagParts) {
                            if (tagsContainer.getChildCount() >= 5) break;

                            Chip tagView = (Chip) getLayoutInflater().inflate(R.layout.alt_tag_chip, tagsContainer, false);
                            tagView.setText(tag);
                            tagView.setChipIcon(AppCompatResources.getDrawable(context, R.drawable.cancel_24px));
                            tagView.setOnClickListener((v) -> tagsContainer.removeView(tagView));
                            tagsContainer.addView(tagView);
                        }
                        s.clear();
                    }
                }
            });

            new MaterialAlertDialogBuilder(context)
                    .setView(layout)
                    .setTitle(R.string.dialog_search_filters)
                    .setIcon(R.drawable.tune_24px)
                    .setPositiveButton(R.string.dialog_apply, (dialogInterface, which) -> {
                        rating = (int) minStarsSlider.getValue();
                        subject = tempSubject;
                        school = tempSchool;

                        tags = new String[tagsContainer.getChildCount()];

                        for (int i = 0; i < tags.length; i++) {
                            tags[i] = ((Chip) tagsContainer.getChildAt(i)).getText().toString();
                        }

                        searchButton.performClick();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.cancel())
                    .show();
        });

        searchButton.setOnClickListener((v) -> {
            Bundle bundle = new Bundle();
            bundle.putString("text", searchBar.getText().toString());
            bundle.putInt("rating", rating);
            bundle.putStringArray("tags", tags);
            bundle.putString("subject", subject);
            bundle.putString("classLevel", school);

            getParentFragmentManager().setFragmentResult("search", bundle);

            progressBar.setVisibility(View.VISIBLE);
            searchButton.setEnabled(false);
            searchBar.setEnabled(false);
        });

        getParentFragmentManager().setFragmentResultListener("searchEnd", this, (requestKey, result) -> {
            progressBar.setVisibility(View.GONE);
            searchButton.setEnabled(true);
            searchBar.setEnabled(true);
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick();
                HideKeyboard(v);
                return true;
            }
            return false;
        });

        return view;
    }

    private void HideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}