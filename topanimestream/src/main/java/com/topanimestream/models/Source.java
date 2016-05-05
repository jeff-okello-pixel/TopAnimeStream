package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Source implements Parcelable{
    private int SourceId;
    private int LinkId;
    private String Url;
    private String Quality;
    private Link Link;

    public Source(){}

    public Source(Parcel in) {
        SourceId = in.readInt();
        LinkId = in.readInt();
        Url = in.readString();
        Quality = in.readString();
        Link = in.readParcelable(Link.class.getClassLoader());
    }

    public Link getLink() {
        return Link;
    }

    public void setLink(Link link) {
        Link = link;
    }

    public int getSourceId() {
        return SourceId;
    }

    public void setSourceId(int sourceId) {
        SourceId = sourceId;
    }

    public int getLinkId() {
        return LinkId;
    }

    public void setLinkId(int linkId) {
        LinkId = linkId;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getQuality() {
        return Quality + "p";
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    @Override
    public String toString() {
        return getQuality();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(SourceId);
        dest.writeInt(LinkId);
        dest.writeString(Url);
        dest.writeString(Quality);
        dest.writeParcelable(Link, flags);
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {
        public Source createFromParcel(Parcel in) {
            return new Source(in);
        }

        public Source[] newArray(int size) {
            return new Source[size];
        }
    };
}
