package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class AnimeInformation implements Parcelable {
    private int AnimeId;
    private int LanguageId;
    private String Name;
    private String Description;
    private String SourceUrl;
    private String Overview;

    public AnimeInformation(int animeId, int languageId, String name,
                            String description, String sourceUrl, String overview) {
        super();
        AnimeId = animeId;
        LanguageId = languageId;
        Name = name;
        Description = description;
        SourceUrl = sourceUrl;
        Overview = overview;
    }

    public AnimeInformation(int LanguageId, String Overview) {
        this.setLanguageId(LanguageId);
        this.setOverview(Overview);
    }
    public AnimeInformation(String Overview) {
        this.setOverview(Overview);
    }


    public AnimeInformation(JSONObject animeInformationsJson) {
        try {
            AnimeId = !animeInformationsJson.isNull("AnimeId") ? animeInformationsJson.getInt("AnimeId") : 0;
            LanguageId = !animeInformationsJson.isNull("LanguageId") ? animeInformationsJson.getInt("LanguageId") : 0;
            Name = !animeInformationsJson.isNull("Name") ? animeInformationsJson.getString("Name") : null;
            Description = !animeInformationsJson.isNull("Description") ? animeInformationsJson.getString("Description") : null;
            SourceUrl = !animeInformationsJson.isNull("SourceUrl") ? animeInformationsJson.getString("SourceUrl") : null;
            Overview = !animeInformationsJson.isNull("Overview") ? animeInformationsJson.getString("Overview") : null;
        } catch (JSONException e) {

        }
    }

    public AnimeInformation(Parcel in) {
        AnimeId = in.readInt();
        LanguageId = in.readInt();
        Name = in.readString();
        Description = in.readString();
        SourceUrl = in.readString();
        Overview = in.readString();

    }

    public AnimeInformation() {
        super();
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
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

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getSourceUrl() {
        return SourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        SourceUrl = sourceUrl;
    }

    public String getOverview() {
        return Overview;
    }

    public void setOverview(String overview) {
        Overview = overview;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(AnimeId);
        dest.writeInt(LanguageId);
        dest.writeString(Name);
        dest.writeString(Description);
        dest.writeString(SourceUrl);
        dest.writeString(Overview);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static final Creator<AnimeInformation> CREATOR = new Creator<AnimeInformation>() {
        public AnimeInformation createFromParcel(Parcel in) {
            return new AnimeInformation(in);
        }

        public AnimeInformation[] newArray(int size) {
            return new AnimeInformation[size];
        }
    };
}
