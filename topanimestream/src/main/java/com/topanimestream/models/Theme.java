package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Theme implements Parcelable {
	private int ThemeId;
	private String Name;
	public Theme(int themeId, String name) {
		super();
		ThemeId = themeId;
		Name = name;
	}
    public Theme(Parcel in) {
        ThemeId = in.readInt();
        Name = in.readString();
    }
    public Theme(JSONObject jsonTheme)
    {
        try {
            this.setThemeId(!jsonTheme.isNull("ThemeId") ? jsonTheme.getInt("ThemeId") : 0);
            this.setName(!jsonTheme.isNull("Name") ? jsonTheme.getString("Name") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
	public int getThemeId() {
		return ThemeId;
	}
	public void setThemeId(int themeId) {
		ThemeId = themeId;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ThemeId);
        dest.writeString(Name);

    }
    public static final Parcelable.Creator<Theme> CREATOR = new Parcelable.Creator<Theme>()
    {
        public Theme createFromParcel(Parcel in)
        {
            return new Theme(in);
        }
        public Theme[] newArray(int size)
        {
            return new Theme[size];
        }
    };
	
}
