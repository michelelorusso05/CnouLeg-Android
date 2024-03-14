package com.test.cnouleg.api;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Note implements Parcelable {
    @JsonProperty("_id")
    private String _id;
    private String title;
    @JsonProperty("author_id")
    private int authorID;
    /*
        Possible values:
        italian_literature
        math
        ict
        english_grammar
        english_literature
        history
        chemistry
        physics
        science
        biology
        economy
        law
    */
    private String subject;
    /*
        Possible values:
        elementary_school
        middle_school
        high_school
        university
    */
    @JsonProperty("class")
    private String classLevel;
    private String[] tags;
    private String description;
    @JsonProperty("data_upload")
    private String uploadDate;
    @JsonProperty("data_last_modified")
    private String modifiedDate;
    @JsonProperty("avg_rating")
    private float averageRating;
    @JsonProperty("no_of_ratings")
    private int numberOfRatings;
    private String markdown;
    private Content[] contents;

    public Note() {}
    protected Note(Parcel in) {
        _id = in.readString();
        title = in.readString();
        authorID = in.readInt();
        subject = in.readString();
        classLevel = in.readString();
        tags = in.createStringArray();
        description = in.readString();
        uploadDate = in.readString();
        modifiedDate = in.readString();
        averageRating = in.readFloat();
        numberOfRatings = in.readInt();
        markdown = in.readString();
        contents = in.createTypedArray(Content.CREATOR);
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(title);
        dest.writeInt(authorID);
        dest.writeString(subject);
        dest.writeString(classLevel);
        dest.writeStringArray(tags);
        dest.writeString(description);
        dest.writeString(uploadDate);
        dest.writeString(modifiedDate);
        dest.writeFloat(averageRating);
        dest.writeInt(numberOfRatings);
        dest.writeString(markdown);
        dest.writeTypedArray(contents, 0);
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public Content[] getContents() {
        return contents;
    }

    public void setContents(Content[] contents) {
        this.contents = contents;
    }
}
