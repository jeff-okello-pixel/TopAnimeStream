package com.aniblitz.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;

public class Anime implements Parcelable  {
	private int AnimeId;
	private String Description;
	private int StatusId;
	private String Name;
	private String AddedDate;
	private String LastUpdatedDate;
	private boolean IsMovie;
	private String PosterPath;
	private int RunningTime;
	private String ReleaseDate;
	private String BackdropPath;
	private boolean isCartoon;
	private Double Rating;
	private String SourceUrl;
	private ArrayList<Episode> episodes;
	private ArrayList<Genre> genres;
	private ArrayList <AnimeInformation> animeInformations;
    private ArrayList<AnimeSource> animeSources;
	private String imageHostPath = App.getContext().getResources().getString(R.string.image_host_path);
    private ArrayList<Theme> themes;
	public Anime() {
		super();
	}
    public Anime(Parcel in) {

		Parcelable[] parcelableEpisodeArray = in.readParcelableArray(Episode.class.getClassLoader());
		Episode[] resultEpisodeArray = null;
		if (parcelableEpisodeArray != null)
		{
			resultEpisodeArray = Arrays.copyOf(parcelableEpisodeArray, parcelableEpisodeArray.length, Episode[].class);
			episodes = new ArrayList<Episode>(Arrays.asList(resultEpisodeArray));
		}

		Parcelable[] parcelableGenreArray = in.readParcelableArray(Genre.class.getClassLoader());
		Genre[] resultGenreArray = null;
		if (parcelableGenreArray != null)
		{
			resultGenreArray = Arrays.copyOf(parcelableGenreArray, parcelableGenreArray.length, Genre[].class);
			genres = new ArrayList<Genre>(Arrays.asList(resultGenreArray));
		}

        Parcelable[] parcelableAnimeInformationArray = in.readParcelableArray(AnimeInformation.class.getClassLoader());
        AnimeInformation[] resultAnimeInformationArray = null;
        if (parcelableAnimeInformationArray != null)
        {
            resultAnimeInformationArray = Arrays.copyOf(parcelableAnimeInformationArray, parcelableAnimeInformationArray.length, AnimeInformation[].class);
            animeInformations = new ArrayList<AnimeInformation>(Arrays.asList(resultAnimeInformationArray));
        }

        Parcelable[] parcelableThemeArray = in.readParcelableArray(Theme.class.getClassLoader());
        Theme[] resultThemeArray = null;
        if (parcelableThemeArray != null)
        {
            resultThemeArray = Arrays.copyOf(parcelableThemeArray, parcelableThemeArray.length, Theme[].class);
            themes = new ArrayList<Theme>(Arrays.asList(resultThemeArray));
        }

        Parcelable[] parcelableAnimeSourceArray = in.readParcelableArray(AnimeSource.class.getClassLoader());
        AnimeSource[] resultAnimeSourceArray = null;
        if (parcelableAnimeSourceArray != null)
        {
            resultAnimeSourceArray = Arrays.copyOf(parcelableAnimeSourceArray, parcelableAnimeSourceArray.length, AnimeSource[].class);
            animeSources = new ArrayList<AnimeSource>(Arrays.asList(resultAnimeSourceArray));
        }

		AnimeId = in.readInt();
		Description = in.readString();
		StatusId = in.readInt();
		Name = in.readString();
    	AddedDate = in.readString();
    	LastUpdatedDate = in.readString();
    	IsMovie = in.readByte() != 0; 
    	PosterPath = in.readString();
    	RunningTime = in.readInt();
    	ReleaseDate = in.readString();
    	BackdropPath = in.readString();
    	isCartoon = in.readByte() != 0; 
    	Rating = in.readDouble();
    	SourceUrl = in.readString();
    	
    }
	public Anime(JSONObject animeJson, Context context)
	{
		try
		{
            animeSources = new ArrayList<AnimeSource>();
			episodes = new ArrayList<Episode>();
			genres = new ArrayList<Genre>();
            animeInformations = new ArrayList<AnimeInformation>();
            themes = new ArrayList<Theme>();
			this.setAnimeId(!animeJson.isNull("AnimeId") ? animeJson.getInt("AnimeId") : 0);
			this.setStatusId(!animeJson.isNull("StatusId") ? animeJson.getInt("StatusId") : 0);
			this.setAddedDate(!animeJson.isNull("AddedDate") ? animeJson.getString("AddedDate") : null);
			this.setLastUpdatedDate(!animeJson.isNull("LastUpdatedDate") ? animeJson.getString("LastUpdatedDate") : null);
			this.setName(!animeJson.isNull("OriginalName") ? animeJson.getString("OriginalName") : null);
			this.setPosterPath(!animeJson.isNull("PosterPath") ? animeJson.getString("PosterPath") : null);
			this.setRunningTime(!animeJson.isNull("RunningTime") ? animeJson.getInt("RunningTime") : 0);
			this.setReleaseDate(!animeJson.isNull("ReleaseDate") ? animeJson.getString("ReleaseDate") : null);
			this.setBackdropPath(!animeJson.isNull("BackdropPath") ? animeJson.getString("BackdropPath") : null);
			this.setRating(!animeJson.isNull("Rating") ? animeJson.getDouble("Rating") : null);
			this.setSourceUrl(!animeJson.isNull("SourceUrl") ? animeJson.getString("SourceUrl") : null);
			this.setIsMovie(!animeJson.isNull("IsMovie") ? animeJson.getBoolean("IsMovie") : false);
			this.setCartoon(!animeJson.isNull("IsCartoon") ? animeJson.getBoolean("IsCartoon") : false);

			if(!animeJson.isNull("AnimeInformations"))
			{
				JSONArray jsonAnimeInformations = animeJson.getJSONArray("AnimeInformations");
				for(int i = 0; i < jsonAnimeInformations.length(); i++)
				{
                    animeInformations.add(new AnimeInformation(jsonAnimeInformations.getJSONObject(i)));
				}
				
			}
			
			if(!animeJson.isNull("Genres"))
			{
				JSONArray jsonGenres = animeJson.getJSONArray("Genres");
				for(int i = 0; i < jsonGenres.length(); i++)
				{
					genres.add(new Genre(jsonGenres.getJSONObject(i)));
				}
			}
			if(!animeJson.isNull("Episodes"))
			{
				JSONArray jsonEpisodes = animeJson.getJSONArray("Episodes");
				for(int i = 0; i < jsonEpisodes.length(); i++)
				{
					episodes.add(new Episode(jsonEpisodes.getJSONObject(i), context));
				}
			}
            if(!animeJson.isNull("AnimeSources"))
            {
                JSONArray jsonAnimeSources = animeJson.getJSONArray("AnimeSources");
                for(int i = 0; i < jsonAnimeSources.length(); i++)
                {
                    animeSources.add(new AnimeSource(jsonAnimeSources.getJSONObject(i)));
                }
            }
            if(!animeJson.isNull("Themes"))
            {
                JSONArray jsonThemes = animeJson.getJSONArray("Themes");
                for(int i = 0; i < jsonThemes.length(); i++)
                {
                    themes.add(new Theme(jsonThemes.getJSONObject(i)));
                }
            }
		
		}
		catch(JSONException e)
		{
			
		}
	}

    public ArrayList<AnimeSource> getAnimeSources() {
        return animeSources;
    }

    public void setAnimeSources(ArrayList<AnimeSource> animeSources) {
        this.animeSources = animeSources;
    }

    public ArrayList<AnimeInformation> getAnimeInformations()
    {
        return this.animeInformations;
    }
    public AnimeInformation getAnimeInformation(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for(AnimeInformation info:this.animeInformations)
        {
            if(String.valueOf(info.getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                return info;
        }

        return null;

    }

    public void setAnimeInformations(ArrayList<AnimeInformation> animeInformations) {
        this.animeInformations = animeInformations;
    }

    public ArrayList<Genre> getGenres() {
		return genres;
	}
    public ArrayList<Theme> getThemes()
    {
        return this.themes;
    }
	public void setGenres(ArrayList<Genre> genres) {
		this.genres = genres;
	}

	public String getSourceUrl() {
		return SourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		SourceUrl = sourceUrl;
	}

	public Double getRating() {
		return Rating;
	}

	public void setRating(Double rating) {
		Rating = rating;
	}

	public boolean isCartoon() {
		return isCartoon;
	}


	public void setCartoon(boolean isCartoon) {
		this.isCartoon = isCartoon;
	}


	public ArrayList<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(ArrayList<Episode> episodes) {
		this.episodes = episodes;
	}

	public int getAnimeId() {
		return AnimeId;
	}
	public void setAnimeId(int animeId) {
		AnimeId = animeId;
	}
	public String getDescription(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = prefs.getString("prefLanguage", "1");
        for(AnimeInformation animeInfo : this.getAnimeInformations())
        {
            if(App.isGooglePlayVersion)
            {
                if(String.valueOf(animeInfo.getLanguageId()).equals(App.phoneLanguage))
                {
                    if(animeInfo.getOverview() != null && !animeInfo.getOverview().equals(""))
                        return animeInfo.getOverview().trim();
                    else if(animeInfo.getDescription() != null && !animeInfo.getDescription().equals(""))
                        return animeInfo.getDescription().trim();
                    else
                        return "";
                }
            }
            else
            {
                if (String.valueOf(animeInfo.getLanguageId()).equals(language)) {
                    if (animeInfo.getOverview() != null && !animeInfo.getOverview().equals(""))
                        return animeInfo.getOverview().trim();
                    else if (animeInfo.getDescription() != null && !animeInfo.getDescription().equals(""))
                        return animeInfo.getDescription().trim();
                    else
                        return "";
                }
            }
        }
		return "";
	}
	public void setDescription(String description) {
		Description = description;
	}
	public int getStatusId() {
		return StatusId;
	}
	public void setStatusId(int statusId) {
		StatusId = statusId;
	}
	public String getGenresFormatted()
	{
        Context context = App.getContext();
        ArrayList<String> translatedGenres = new ArrayList<String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = prefs.getString("prefLanguage", "1");

        for(Genre genre:this.genres)
        {
            if(!language.equals("1"))
            {
                if(App.isGooglePlayVersion && App.phoneLanguage.equals("1"))
                {
                    translatedGenres.add(genre.getName());
                }
                else
                {
                    String genreName = genre.getName().toLowerCase().replace(" ", "");
                    translatedGenres.add(Utils.getStringResourceByName(genreName));
                }
            }
            else
                translatedGenres.add(genre.getName());
        }

		String genresFormatted = "";
		if(this.genres != null)
		{
			for(String genre: translatedGenres)
			{
				genresFormatted += genre + ", ";
			}
		}
		if(!genresFormatted.equals(""))
			return genresFormatted.substring(0,genresFormatted.length() - 2);
		else
			return "";
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getAddedDate() {
		return AddedDate;
	}
	public void setAddedDate(String addedDate) {
		AddedDate = addedDate;
	}
	public String getLastUpdatedDate() {
		return LastUpdatedDate;
	}
	public void setLastUpdatedDate(String lastUpdatedDate) {
		LastUpdatedDate = lastUpdatedDate;
	}
	public boolean isMovie() {
		return IsMovie;
	}
	public void setIsMovie(boolean isMovie) {
		IsMovie = isMovie;
	}
    public String getRelativePosterPath(String size)
    {
        if(PosterPath == null)
            return null;

        if(size == null || size.equals(""))
            return PosterPath;

        String imageName = PosterPath.substring(PosterPath.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullPosterPath = PosterPath.substring(0, PosterPath.lastIndexOf("/") + 1) + imageName;
        return fullPosterPath;
    }
	public String getPosterPath(String size) {
		if(PosterPath == null)
			return null;
		
		if(size == null || size.equals(""))
			return imageHostPath + PosterPath;
		
		String imageName = PosterPath.substring(PosterPath.lastIndexOf("/") + 1);
		imageName = "w" + size + "_" + imageName;
		String fullPosterPath = imageHostPath + PosterPath.substring(0, PosterPath.lastIndexOf("/") + 1) + imageName;
		return fullPosterPath;
	}
	public void setPosterPath(String posterPath) {
		PosterPath = posterPath;
	}
	public int getRunningTime() {
		return RunningTime;
	}
	public void setRunningTime(int runningTime) {
		RunningTime = runningTime;
	}
	public String getReleaseDate() {
		return ReleaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		ReleaseDate = releaseDate;
	}
    public String getRelativeBackdropPath(String size) {
        if(BackdropPath == null)
            return null;

        if(size == null || size.equals(""))
            return BackdropPath;

        String imageName = BackdropPath.substring(BackdropPath.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullPosterPath = BackdropPath.substring(0, BackdropPath.lastIndexOf("/") + 1) + imageName;
        return fullPosterPath;
    }
	public String getBackdropPath(String size) {
		if(BackdropPath == null)
			return null;
		
		if(size == null || size.equals(""))
			return imageHostPath + BackdropPath;
		
		String imageName = BackdropPath.substring(BackdropPath.lastIndexOf("/") + 1);
		imageName = "w" + size + "_" + imageName;
		String fullPosterPath = imageHostPath + BackdropPath.substring(0, BackdropPath.lastIndexOf("/") + 1) + imageName;
		return fullPosterPath;
	}
	public void setBackdropPath(String backdropPath) {
		BackdropPath = backdropPath;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
    public void writeToParcel(Parcel dest, int flags) {
		if(episodes == null)
			episodes = new ArrayList<Episode>();
		if(genres == null)
			genres = new ArrayList<Genre>();
        if(animeInformations == null)
            animeInformations = new ArrayList<AnimeInformation>();
        if(themes == null)
            themes = new ArrayList<Theme>();
        if(animeSources == null)
            animeSources = new ArrayList<AnimeSource>();

	    Parcelable[] parcelableEpisodeArray = new Parcelable[episodes.size()];
	    dest.writeParcelableArray(episodes.toArray(parcelableEpisodeArray), flags);
	    
	    Parcelable[] parcelableGenreArray = new Parcelable[genres.size()];
	    dest.writeParcelableArray(genres.toArray(parcelableGenreArray), flags);

        Parcelable[] parcelableAnimeInformationArray = new Parcelable[animeInformations.size()];
	    dest.writeParcelableArray(animeInformations.toArray(parcelableAnimeInformationArray), flags);

        Parcelable[] parcelableThemeArray = new Parcelable[themes.size()];
        dest.writeParcelableArray(themes.toArray(parcelableThemeArray), flags);

        Parcelable[] parcelableAnimeSourceArray = new Parcelable[animeSources.size()];
        dest.writeParcelableArray(animeSources.toArray(parcelableAnimeSourceArray), flags);

        dest.writeInt(AnimeId);
        dest.writeString(Description);
        dest.writeInt(StatusId);
        dest.writeString(Name);
        dest.writeString(AddedDate);
        dest.writeString(LastUpdatedDate);
        dest.writeByte((byte) (IsMovie ? 1 : 0)); 
        dest.writeString(PosterPath);
        dest.writeInt(RunningTime);
        dest.writeString(ReleaseDate);
        dest.writeString(BackdropPath);
        dest.writeByte((byte) (isCartoon ? 1 : 0)); 
        dest.writeDouble(Rating != null ? Rating : 0);
        dest.writeString(SourceUrl);
        dest.writeString(LastUpdatedDate);
        

    }
    public static final Parcelable.Creator<Anime> CREATOR = new Parcelable.Creator<Anime>()
    {
        public Anime createFromParcel(Parcel in)
        {
            return new Anime(in);
        }
        public Anime[] newArray(int size)
        {
            return new Anime[size];
        }
   };
}
