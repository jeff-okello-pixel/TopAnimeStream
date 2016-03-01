package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class WatchType implements Parcelable {
    private int WatchTypeId;
    private String Name;

    public WatchType(){}

    public WatchType(Parcel in)
    {
        WatchTypeId = in.readInt();
        Name = in.readString();
    }

    public int getWatchTypeId() {
        return WatchTypeId;
    }

    public void setWatchTypeId(int watchTypeId) {
        WatchTypeId = watchTypeId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public static final Creator<WatchType> CREATOR = new Creator<WatchType>() {
        public WatchType createFromParcel(Parcel in) {
            return new WatchType(in);
        }

        public WatchType[] newArray(int size) {
            return new WatchType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(WatchTypeId);
        dest.writeString(Name);

    }
}
