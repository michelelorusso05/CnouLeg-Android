package com.test.cnouleg.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Profile implements Parcelable {
    @JsonProperty("_id")
    private String id;
    private String username;
    private String birthdate;
    private String gender;
    private String role;
    private String school;
    private String subject;
    private String bio;
    @JsonProperty("profile_pic_url")
    private String profilePicURL;

    public Profile() {}
    protected Profile(Parcel in) {
        id = in.readString();
        username = in.readString();
        birthdate = in.readString();
        gender = in.readString();
        role = in.readString();
        school = in.readString();
        subject = in.readString();
        bio = in.readString();
        profilePicURL = in.readString();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(birthdate);
        dest.writeString(gender);
        dest.writeString(role);
        dest.writeString(school);
        dest.writeString(subject);
        dest.writeString(bio);
        dest.writeString(profilePicURL);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }
}
