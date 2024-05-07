package com.test.cnouleg.api;

public class RatingUpdateResult {
    float rating;
    int numberOfRatings;

    public RatingUpdateResult() {}
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }
}
