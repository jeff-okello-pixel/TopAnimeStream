package com.aniblitz.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class EpisodeInformations implements Parcelable {
	private int EpisodeId;
	private int LanguageId;
	private String EpisodeName;
	private String Description;
	
	public EpisodeInformations() {
		super();
	}
    public EpisodeInformations(Parcel in) {
    	EpisodeId = in.readInt();
    	LanguageId = in.readInt();
    	EpisodeName = in.readString();
    	Description = in.readString();
    }
	public EpisodeInformations(int episodeId, int languageId,
			String episodeName, String description) {
		super();
		EpisodeId = episodeId;
		LanguageId = languageId;
		EpisodeName = episodeName;
		Description = description;
	}
	public EpisodeInformations(JSONObject episodeInformationsJson) {
		try {
			this.setEpisodeId(!episodeInformationsJson.isNull("EpisodeId") ? episodeInformationsJson.getInt("EpisodeId") : 0);
			this.setLanguageId(!episodeInformationsJson.isNull("LanguageId") ? episodeInformationsJson.getInt("LanguageId") : 0);
			this.setEpisodeName(!episodeInformationsJson.isNull("EpisodeName") ? episodeInformationsJson.getString("EpisodeName") : null);
			this.setDescription(!episodeInformationsJson.isNull("Description") ? episodeInformationsJson.getString("Description") : null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public int getEpisodeId() {
		return EpisodeId;
	}
	public void setEpisodeId(int episodeId) {
		EpisodeId = episodeId;
	}
	public int getLanguageId() {
		return LanguageId;
	}
	public void setLanguageId(int languageId) {
		LanguageId = languageId;
	}
	public String getEpisodeName() {
		return EpisodeName;
	}
	public void setEpisodeName(String episodeName) {
		EpisodeName = episodeName;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(EpisodeId);
        dest.writeInt(LanguageId);
        dest.writeString(EpisodeName);
        dest.writeString(Description);
		
	}
    public static final Parcelable.Creator<EpisodeInformations> CREATOR = new Parcelable.Creator<EpisodeInformations>()
    {
        public EpisodeInformations createFromParcel(Parcel in)
        {
            return new EpisodeInformations(in);
        }
        public EpisodeInformations[] newArray(int size)
        {
            return new EpisodeInformations[size];
        }
   };
	
}
