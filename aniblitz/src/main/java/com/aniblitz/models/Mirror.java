package com.aniblitz.models;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Mirror implements Parcelable {
	private int MirrorId;
	private int EpisodeId;
	private int AnimeSourceId;
	private int PartNumber;
	private String AddedDate;
	private String source;
	private AnimeSource AnimeSource;
	private Provider provider;
	public Mirror() {
		super();
	}
	public Mirror(int mirrorId, int episodeId, int animeSourceId,
			String addedDate, String source) {
		super();
		MirrorId = mirrorId;
		EpisodeId = episodeId;
		AnimeSourceId = animeSourceId;
		AddedDate = addedDate;
		this.source = source;
	}
	public Mirror(JSONObject jsonMirror)
	{
		try
		{
            if(!jsonMirror.isNull("AnimeSource"))
			    this.AnimeSource = new AnimeSource(jsonMirror.getJSONObject("AnimeSource"));
            if(!jsonMirror.isNull("Provider"))
			    this.provider = new Provider(jsonMirror.getJSONObject("Provider"));
			this.MirrorId = !jsonMirror.isNull("MirrorId") ? jsonMirror.getInt("MirrorId") : 0;
			this.EpisodeId = !jsonMirror.isNull("EpisodeId") ? jsonMirror.getInt("EpisodeId") : 0;
			this.AnimeSourceId = !jsonMirror.isNull("AnimeSourceId") ? jsonMirror.getInt("AnimeSourceId") : 0;
			this.AddedDate = !jsonMirror.isNull("AddedDate") ? jsonMirror.getString("AddedDate") : null;
			this.source = !jsonMirror.isNull("Source") ? jsonMirror.getString("Source") : null;
		}catch(Exception e)
		{
			
		}
	}
    public Mirror(Parcel in) {
    	AnimeSource = (AnimeSource) in.readParcelable(AnimeSource.class.getClassLoader());
    	provider = (Provider) in.readParcelable(Provider.class.getClassLoader());
    	MirrorId = in.readInt();
    	EpisodeId = in.readInt();
    	AnimeSourceId = in.readInt();
    	PartNumber = in.readInt();
    	AddedDate = in.readString();
    	source = in.readString();
    }
	public Provider getProvider() {
		return provider;
	}
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	public AnimeSource getAnimeSource() {
		return AnimeSource;
	}
	public void setAnimeSource(AnimeSource animeSource) {
		AnimeSource = animeSource;
	}
	public int getMirrorId() {
		return MirrorId;
	}
	public void setMirrorId(int mirrorId) {
		MirrorId = mirrorId;
	}
	public int getEpisodeId() {
		return EpisodeId;
	}
	public void setEpisodeId(int episodeId) {
		EpisodeId = episodeId;
	}
	public int getAnimeSourceId() {
		return AnimeSourceId;
	}
	public void setAnimeSourceId(int animeSourceId) {
		AnimeSourceId = animeSourceId;
	}
	public String getAddedDate() {
		return AddedDate;
	}
	public void setAddedDate(String addedDate) {
		AddedDate = addedDate;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeParcelable(AnimeSource, flags);
    	dest.writeParcelable(provider, flags);
        dest.writeInt(MirrorId);
        dest.writeInt(EpisodeId);
        dest.writeInt(AnimeSourceId);
        dest.writeInt(PartNumber);
        dest.writeString(AddedDate);
        dest.writeString(source);
        

    }
    
    public static final Parcelable.Creator<Mirror> CREATOR = new Parcelable.Creator<Mirror>()
    {
	        public Mirror createFromParcel(Parcel in)
	        {
	            return new Mirror(in);
	        }
	        public Mirror[] newArray(int size)
	        {
	            return new Mirror[size];
	        }
    };
	
	
}
