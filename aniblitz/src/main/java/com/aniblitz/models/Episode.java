package com.aniblitz.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.aniblitz.App;

public class Episode implements Parcelable {
	private int AnimeId;
	private int EpisodeId;
	private String EpisodeNumber;
	private String EpisodeName;
	private String AiredDate;
	private ArrayList<Mirror> Mirrors;
	private EpisodeInformations EpisodeInformations;
	private String Screenshot;
	public Episode() {
		super();
	}
    public Episode(Parcel in) {
		Parcelable[] parcelableArray = in.readParcelableArray(Mirror.class.getClassLoader());
		Mirror[] resultArray = null;
		if (parcelableArray != null)
		{
			resultArray = Arrays.copyOf(parcelableArray, parcelableArray.length, Mirror[].class);
			Mirrors = new ArrayList<Mirror>(Arrays.asList(resultArray));
		}
		EpisodeInformations = (EpisodeInformations) in.readParcelable(EpisodeInformations.class.getClassLoader());
    	AnimeId = in.readInt();
    	EpisodeId = in.readInt();
    	EpisodeNumber = in.readString();
    	EpisodeName = in.readString();
    	AiredDate = in.readString();
    	Screenshot = in.readString();
    }
	public Episode(JSONObject jsonEpisode)
	{
		JSONArray episodeInfoArray = new JSONArray();
		JSONArray episodeMirrors = new JSONArray();
		try
		{
			this.setEpisodeNumber(!jsonEpisode.isNull("EpisodeNumber") ? jsonEpisode.getString("EpisodeNumber") : "0");
			this.setEpisodeId(!jsonEpisode.isNull("EpisodeId") ? jsonEpisode.getInt("EpisodeId") : 0);
			this.setAnimeId(!jsonEpisode.isNull("AnimeId") ? jsonEpisode.getInt("AnimeId") : 0);
			this.setAiredDate(!jsonEpisode.isNull("AiredDate") ? jsonEpisode.getString("AiredDate") : null);
			this.setScreenshot(!jsonEpisode.isNull("Screenshot") ? jsonEpisode.getString("Screenshot") : null);
			episodeMirrors = !jsonEpisode.isNull("Mirrors") ? jsonEpisode.getJSONArray("Mirrors") : null;
			this.Mirrors = new ArrayList<Mirror>();
			if(episodeMirrors != null)
			{
				for(int i =0; i<episodeMirrors.length(); i++)
				{
					this.Mirrors.add(new Mirror(episodeMirrors.getJSONObject(i)));
				}
			}
			episodeInfoArray = !jsonEpisode.isNull("EpisodeInformations") ? jsonEpisode.getJSONArray("EpisodeInformations") : null;
			if(episodeInfoArray != null)
			{
				this.EpisodeInformations = new EpisodeInformations(episodeInfoArray.getJSONObject(0));
			}

			
		}catch(Exception e){}
	}
	public Episode(int animeId, int episodeId, String episodeNumber,
			String episodeName, String airedDate) {
		super();
		AnimeId = animeId;
		EpisodeId = episodeId;
		EpisodeNumber = episodeNumber;
		EpisodeName = episodeName;
		AiredDate = airedDate;
	}
	
	
	public String getScreenshot() {
		return Screenshot;
	}
	public void setScreenshot(String screenshot) {
		Screenshot = screenshot;
	}
	public EpisodeInformations getEpisodeInformations() {
		return EpisodeInformations;
	}
	public void setEpisodeInformations(EpisodeInformations episodeInformations) {
		EpisodeInformations = episodeInformations;
	}
	public ArrayList<Mirror> getMirrors() {
		return Mirrors;
	}
	public void setMirrors(ArrayList<Mirror> mirrors) {
		Mirrors = mirrors;
	}
	public int getAnimeId() {
		return AnimeId;
	}
	public void setAnimeId(int animeId) {
		AnimeId = animeId;
	}
	public int getEpisodeId() {
		return EpisodeId;
	}
	public void setEpisodeId(int episodeId) {
		EpisodeId = episodeId;
	}
	public String getEpisodeNumber() {
		return EpisodeNumber;
	}
	public void setEpisodeNumber(String episodeNumber) {
		EpisodeNumber = episodeNumber;
	}
	public String getEpisodeName() {
		return EpisodeName;
	}
	public void setEpisodeName(String episodeName) {
		EpisodeName = episodeName;
	}
	public String getAiredDate() {
		return AiredDate;
	}
	public void setAiredDate(String airedDate) {
		AiredDate = airedDate;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(Mirrors == null)
			Mirrors = new ArrayList<Mirror>();
        Parcelable[] parcelableArray = new Parcelable[Mirrors.size()];
        dest.writeParcelableArray(Mirrors.toArray(parcelableArray),flags);
        dest.writeParcelable(EpisodeInformations, flags);
        dest.writeInt(AnimeId);
        dest.writeInt(EpisodeId);
        dest.writeString(EpisodeNumber);
        dest.writeString(EpisodeName);
        dest.writeString(AiredDate);
        dest.writeString(Screenshot);

		
	}
    public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>()
    {
        public Episode createFromParcel(Parcel in)
        {
            return new Episode(in);
        }
        public Episode[] newArray(int size)
        {
            return new Episode[size];
        }
   };
	
}
