package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Provider implements Parcelable {
    private int ProviderId;
    private String Name;

    public Provider() {
        super();
    }

    public Provider(JSONObject jsonProvider) {
        try {
            this.ProviderId = !jsonProvider.isNull("ProviderId") ? jsonProvider.getInt("ProviderId") : 0;
            this.Name = !jsonProvider.isNull("Name") ? jsonProvider.getString("Name") : null;
        } catch (Exception e) {
        }
    }

    public Provider(int providerId, String name) {
        super();
        this.ProviderId = providerId;
        this.Name = name;
    }

    public Provider(Parcel in) {
        ProviderId = in.readInt();
        Name = in.readString();
    }

    public int getProviderId() {
        return ProviderId;
    }

    public void setProviderId(int providerId) {
        this.ProviderId = providerId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ProviderId);
        dest.writeString(Name);
    }

    public static final Creator<Provider> CREATOR = new Creator<Provider>() {
        public Provider createFromParcel(Parcel in) {
            return new Provider(in);
        }

        public Provider[] newArray(int size) {
            return new Provider[size];
        }
    };
}
