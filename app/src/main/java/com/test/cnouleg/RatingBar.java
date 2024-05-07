package com.test.cnouleg;

import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.util.function.Consumer;

public class RatingBar {
    MaterialButton[] stars;
    private static final @DrawableRes int star_empty = R.drawable.star_empty_24px;
    private static final @DrawableRes int star = R.drawable.star_24px;
    private int currentRating;
    private Consumer<Integer> onRateChanged;
    public RatingBar(@NonNull ViewGroup parentView) {
        currentRating = -1;

        stars = new MaterialButton[parentView.getChildCount()];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = (MaterialButton) parentView.getChildAt(i);
            stars[i].setIconResource(star_empty);

            final int position = i;

            stars[i].setOnTouchListener((v, m) -> {
                int p = position;
                if (m.getAction() == MotionEvent.ACTION_UP) {
                    if (currentRating == position) {
                        p = -1;
                        currentRating = -1;
                    }
                    else
                        currentRating = position;

                    if (onRateChanged != null)
                        onRateChanged.accept(currentRating + 1);

                    v.performClick();
                }

                for (int j = 0; j < stars.length; j++) {
                    stars[j].setIconResource(j > (m.getAction() == MotionEvent.ACTION_CANCEL ? currentRating : p) ? star_empty : star);
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
    public void setOnRateChangedListener(Consumer<Integer> c) {
        onRateChanged = c;
    }
    public void setEnabled(boolean enabled) {
        for (MaterialButton star : stars) {
            star.setEnabled(enabled);
        }
    }

    public void setRateAmount(int v) {
        currentRating = v - 1;
        for (int j = 0; j < stars.length; j++) {
            stars[j].setIconResource(j > currentRating ? star_empty : star);
        }
    }
}
