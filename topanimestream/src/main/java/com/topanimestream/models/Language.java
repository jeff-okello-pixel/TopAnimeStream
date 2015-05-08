package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Language implements Parcelable {
    private int LanguageId;
    private String Name;
    private String ISO639;

    public Language() {
        super();
    }

    public Language(int languageId, String name, String iSO639) {
        super();
        LanguageId = languageId;
        Name = name;
        ISO639 = iSO639;
    }
    public Language(Parcel in) {
        LanguageId = in.readInt();
        Name = in.readString();
        ISO639 = in.readString();
    }
    public int getLanguageId() {
        return LanguageId;
    }

    public void setLanguageId(int languageId) {
        LanguageId = languageId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getISO639() {
        return ISO639;
    }

    public void setISO639(String iSO639) {
        ISO639 = iSO639;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(LanguageId);
        dest.writeString(Name);
        dest.writeString(ISO639);
    }

    public static final Creator<Language> CREATOR = new Creator<Language>() {
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        public Language[] newArray(int size) {
            return new Language[size];
        }
    };
}
