package com.test.cnouleg.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rating {
    @JsonProperty("_id")
    String id;
    @JsonProperty("note_id")
    String noteID;
    @JsonProperty("user_id")
    String userID;
    float rating;

    public Rating() {}

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getNoteID() {
        return noteID;
    }

    public void setNoteID(String noteID) {
        this.noteID = noteID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = this.userID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
