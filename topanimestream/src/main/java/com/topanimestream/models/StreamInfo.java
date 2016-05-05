package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class StreamInfo implements Parcelable {
    private String Title;
    private String ImageUrl;
    private Source Source;
    private String SubtitleLanguage;
    private String SubtitleLocation;

    public StreamInfo(String title, String imageUrl, com.topanimestream.models.Source source, String subtitleLanguage, String subtitleLocation) {
        Title = title;
        ImageUrl = imageUrl;
        Source = source;
        SubtitleLanguage = subtitleLanguage;
        SubtitleLocation = subtitleLocation;
    }

    public StreamInfo(Parcel in) {
        Title = in.readString();
        ImageUrl = in.readString();
        Source = in.readParcelable(Source.class.getClassLoader());
        SubtitleLanguage = in.readString();
        SubtitleLocation = in.readString();
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getSubtitleLanguage() {
        return SubtitleLanguage;
    }

    public void setSubtitleLanguage(String subtitleLanguage) {
        SubtitleLanguage = subtitleLanguage;
    }

    public com.topanimestream.models.Source getSource() {
        return Source;
    }

    public void setSource(com.topanimestream.models.Source source) {
        Source = source;
    }

    public String getSubtitleLocation() {
        return SubtitleLocation;
    }

    public void setSubtitleLocation(String subtitleLocation) {
        SubtitleLocation = subtitleLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Title);
        dest.writeString(ImageUrl);
        dest.writeParcelable(Source, flags);
        dest.writeString(SubtitleLanguage);
        dest.writeString(SubtitleLocation);
    }

    public static final Creator<StreamInfo> CREATOR = new Creator<StreamInfo>() {
        public StreamInfo createFromParcel(Parcel in) {
            return new StreamInfo(in);
        }

        public StreamInfo[] newArray(int size) {
            return new StreamInfo[size];
        }
    };
}
