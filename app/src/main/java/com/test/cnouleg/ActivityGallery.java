package com.test.cnouleg;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ActivityGallery extends FullscreenActivity {
    String[] contents;
    String noteID;
    ViewPager2 pager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        noteID = savedInstanceState.getString("noteID");
        contents = savedInstanceState.getStringArray("content");
        int page = savedInstanceState.getInt("page", 0);

        pager = findViewById(R.id.pager);
        pager.setSaveEnabled(false);

        pager.setAdapter(new GalleryPagesAdapter(this, noteID, contents, () -> onTouch.accept(null)));
        pager.setCurrentItem(page, false);
        pager.setOffscreenPageLimit(1);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_gallery;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteID", noteID);
        outState.putStringArray("content", contents);
        outState.putInt("page", pager.getCurrentItem());
    }
}