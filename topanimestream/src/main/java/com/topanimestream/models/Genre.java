package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Genre implements Parcelable {

	private int GenreId;
	private String Name;
	
	public Genre() {
		super();
	}
    public Genre(Parcel in) {
		GenreId = in.readInt();
    	Name = in.readString();
    }
	public Genre(int genreId, String name) {
		super();
		GenreId = genreId;
		Name = name;
	}
	public Genre(JSONObject genreJson)
	{
		try
		{
			this.setGenreId(!genreJson.isNull("GenreId") ? genreJson.getInt("GenreId") : 0);
			this.setName(!genreJson.isNull("Name") ? genreJson.getString("Name") : null);
		}
		catch(JSONException e)
		{
			
		}
	}
	public int getGenreId() {
		return GenreId;
	}
	public void setGenreId(int genreId) {
		GenreId = genreId;
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
        dest.writeInt(GenreId);
        dest.writeString(Name);
		
	}
    public static final Creator<Genre> CREATOR = new Creator<Genre>()
    {
        public Genre createFromParcel(Parcel in)
        {
            return new Genre(in);
        }
        public Genre[] newArray(int size)
        {
            return new Genre[size];
        }
   };
	
}
