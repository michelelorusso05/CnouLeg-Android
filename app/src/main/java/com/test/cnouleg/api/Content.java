package com.test.cnouleg.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Content implements Parcelable {
    /*
        image
        video
        document
    */
    private String type;
    private String path;

    public Content() {}
    protected Content(Parcel in) {
        type = in.readString();
        path = in.readString();
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(path);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
