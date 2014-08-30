package com.aniblitz.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class AnimeSource implements Parcelable {
	private int AnimeSourceId;
	private int AnimeId;
	private boolean IsSubbed;
	private int LanguageId;
	private String SourceUrl;
	private String AddedDate;
	private ArrayList<Mirror> mirrors;
	public AnimeSource() {
		super();
	}
	public AnimeSource(int animeSourceId, int animeId, boolean isDubbed,
			boolean isSubbed, int languageId, String sourceUrl, String addedDate) {
		super();
		AnimeSourceId = animeSourceId;
		AnimeId = animeId;
		IsSubbed = isSubbed;
		LanguageId = languageId;
		SourceUrl = sourceUrl;
		AddedDate = addedDate;
	}
	public AnimeSource(Parcel in) {
		Parcelable[] parcelableArray = in.readParcelableArray(Mirror.class.getClassLoader());
		Mirror[] resultArray = null;
		if (parcelableArray != null)
		{
			resultArray = Arrays.copyOf(parcelableArray, parcelableArray.length, Mirror[].class);
			mirrors = new ArrayList<Mirror>(Arrays.asList(resultArray));
		}
		    
		AnimeSourceId = in.readInt();
		AnimeId = in.readInt();
		IsSubbed = in.readByte() != 0; 
		LanguageId = in.readInt();
		SourceUrl = in.readString();
		AddedDate = in.readString();
		
	}
	public AnimeSource(JSONObject jsonAnimeSource)
	{
		try{
			mirrors = new ArrayList<Mirror>();

			this.AnimeSourceId = !jsonAnimeSource.isNull("AnimeSourceId") ? jsonAnimeSource.getInt("AnimeSourceId") : 0;
			this.AnimeId = !jsonAnimeSource.isNull("AnimeId") ? jsonAnimeSource.getInt("AnimeId") : 0;
			this.AddedDate = !jsonAnimeSource.isNull("AddedDate") ? jsonAnimeSource.getString("AddedDate") : null;
			this.IsSubbed = !jsonAnimeSource.isNull("IsSubbed") ? jsonAnimeSource.getBoolean("IsSubbed") : false;
			this.LanguageId = !jsonAnimeSource.isNull("LanguageId") ? jsonAnimeSource.getInt("LanguageId") : 0;
			this.SourceUrl = !jsonAnimeSource.isNull("SourceUrl") ? jsonAnimeSource.getString("SourceUrl") : null;
			if(!jsonAnimeSource.isNull("Mirrors"))
			{
				JSONArray jsonMirrors = jsonAnimeSource.getJSONArray("Mirrors");
				for(int i = 0; i < jsonMirrors.length(); i++)
				{
					mirrors.add(new Mirror(jsonMirrors.getJSONObject(i)));
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	public ArrayList<Mirror> getMirrors() {
		return mirrors;
	}
	public void setMirrors(ArrayList<Mirror> mirrors) {
		this.mirrors = mirrors;
	}
	public int getAnimeSourceId() {
		return AnimeSourceId;
	}
	public void setAnimeSourceId(int animeSourceId) {
		AnimeSourceId = animeSourceId;
	}
	public int getAnimeId() {
		return AnimeId;
	}
	public void setAnimeId(int animeId) {
		AnimeId = animeId;
	}

	public boolean isSubbed() {
		return IsSubbed;
	}
	public void setIsSubbed(boolean isSubbed) {
		IsSubbed = isSubbed;
	}
	public int getLanguageId() {
		return LanguageId;
	}
	public void setLanguageId(int languageId) {
		LanguageId = languageId;
	}
	public String getSourceUrl() {
		return SourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		SourceUrl = sourceUrl;
	}
	public String getAddedDate() {
		return AddedDate;
	}
	public void setAddedDate(String addedDate) {
		AddedDate = addedDate;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
    public void writeToParcel(Parcel dest, int flags) {
        Parcelable[] parcelableArray = new Parcelable[mirrors.size()];
        dest.writeParcelableArray(mirrors.toArray(parcelableArray),flags);
        dest.writeInt(AnimeSourceId);
        dest.writeInt(AnimeId);
        dest.writeByte((byte) (IsSubbed ? 1 : 0)); 
        dest.writeInt(LanguageId);
        dest.writeString(SourceUrl);
        dest.writeString(AddedDate);
    }
    
    public static final Parcelable.Creator<AnimeSource> CREATOR = new Parcelable.Creator<AnimeSource>()
    {
        public AnimeSource createFromParcel(Parcel in)
        {
            return new AnimeSource(in);
        }
        public AnimeSource[] newArray(int size)
        {
            return new AnimeSource[size];
        }
   };
}
