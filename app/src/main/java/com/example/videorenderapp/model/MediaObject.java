package com.example.videorenderapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaObject implements Parcelable {

    private String thumbnail;
    private String title;
    private String description;
    private String mediaURL;
    private boolean isDownloaded;
    private boolean isSelected;
//    fileName = sUrl[0].substring(sUrl[0].lastIndexOf("/") + 1);


    public String getMediaFileName() {
        return mediaURL.substring(mediaURL.lastIndexOf("/") + 1);
    }

    public MediaObject() {
    }

    public MediaObject(String thumbnail, String title, String description, String mediaURL) {
        this.thumbnail = thumbnail;
        this.title = title;
        this.description = description;

        this.mediaURL = mediaURL;
    }

    protected MediaObject(Parcel in) {
        thumbnail = in.readString();
        title = in.readString();
        description = in.readString();
        mediaURL = in.readString();
    }

    public static final Creator<MediaObject> CREATOR = new Creator<MediaObject>() {
        @Override
        public MediaObject createFromParcel(Parcel in) {
            return new MediaObject(in);
        }

        @Override
        public MediaObject[] newArray(int size) {
            return new MediaObject[size];
        }
    };

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnail);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(mediaURL);
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
