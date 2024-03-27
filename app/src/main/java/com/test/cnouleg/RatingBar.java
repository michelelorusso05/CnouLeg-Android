package com.test.cnouleg;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;

public class RatingBar {
    MaterialButton[] stars;
    private static final @DrawableRes int star_empty = R.drawable.star_empty_24px;
    private static final @DrawableRes int star = R.drawable.star_24px;
    private int currentRating;
    public RatingBar(ViewGroup parentView) {
        currentRating = -1;

        stars = new MaterialButton[parentView.getChildCount()];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = (MaterialButton) parentView.getChildAt(i);
            stars[i].setIconResource(star_empty);

            final int position = i;

            stars[i].setOnTouchListener((v, m) -> {
                if (m.getAction() == MotionEvent.ACTION_UP) {
                    currentRating = position;
                    v.performClick();
                }

                for (int j = 0; j < stars.length; j++) {
                    stars[j].setIconResource(j > (m.getAction() == MotionEvent.ACTION_CANCEL ? currentRating : position) ? star_empty : star);
                }

                return true;
            });

            stars[i].setOnHoverListener((v, m) -> {
                for (int j = 0; j < stars.length; j++) {
                    stars[j].setIconResource(j > (m.getAction() == MotionEvent.ACTION_HOVER_EXIT ? currentRating : position) ? star_empty : star);
                }

                return true;
            });

        }
    }
}
