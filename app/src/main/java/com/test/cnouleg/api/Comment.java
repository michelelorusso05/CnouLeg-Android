package com.test.cnouleg.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment implements Parcelable {
    @JsonProperty("_id")
    private String _id;
    private String text;
    @JsonProperty("user_id")
    private String userID;
    @JsonProperty("post_id")
    private String postID;
    @JsonProperty("parent_id")
    private String parentID;
    private int likes;
    private String date;
    @JsonProperty("has_children")
    private boolean hasChildren;

    public Comment() {}

    protected Comment(Parcel in) {
        _id = in.readString();
        text = in.readString();
        userID = in.readString();
        postID = in.readString();
        parentID = in.readString();
        likes = in.readInt();
        date = in.readString();
        hasChildren = (in.readInt() > 0);
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(text);
        dest.writeString(userID);
        dest.writeString(postID);
        dest.writeString(parentID);
        dest.writeInt(likes);
        dest.writeString(date);
        dest.writeInt(hasChildren ? 1 : 0);
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
}
