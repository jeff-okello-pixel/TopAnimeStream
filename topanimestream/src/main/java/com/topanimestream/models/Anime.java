package com.topanimestream.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.topanimestream.App;
import com.topanimestream.utilities.Utils;

public class Anime implements Parcelable, Comparator<Anime> {
    private int AnimeId;
    private String Description;
    private int StatusId;
    private String OriginalName;
    private Date AddedDate;
    private Date LastUpdatedDate;
    private boolean IsMovie;
    private String PosterPath;
    private int RunningTime;
    private Date ReleasedDate;
    private String BackdropPath;
    private boolean IsCartoon;
    private Double Rating;
    private String SourceUrl;
    private ArrayList<Episode> Episodes;
    private ArrayList<Genre> Genres;
    private ArrayList<AnimeInformation> AnimeInformations;
    private ArrayList<AnimeSource> AnimeSources;
    private ArrayList<Link> Links;
    private ArrayList<Theme> Themes;
    private int Order;
    private int VoteCount;
    private boolean IsFavorite;
    private Date LinkedDate;
    private boolean IsInMyList;
    private boolean IsAvailable;
    private int EpisodeCount;
    private int AgeRatingId;
    private int MyAnimeListId;

    public Anime() {
        super();
    }

    public Anime(Parcel in) {

        Parcelable[] parcelableEpisodeArray = in.readParcelableArray(Episode.class.getClassLoader());
        Episode[] resultEpisodeArray = null;
        if (parcelableEpisodeArray != null) {
            resultEpisodeArray = Arrays.copyOf(parcelableEpisodeArray, parcelableEpisodeArray.length, Episode[].class);
            Episodes = new ArrayList<Episode>(Arrays.asList(resultEpisodeArray));
        }

        Parcelable[] parcelableGenreArray = in.readParcelableArray(Genre.class.getClassLoader());
        Genre[] resultGenreArray = null;
        if (parcelableGenreArray != null) {
            resultGenreArray = Arrays.copyOf(parcelableGenreArray, parcelableGenreArray.length, Genre[].class);
            Genres = new ArrayList<Genre>(Arrays.asList(resultGenreArray));
        }

        Parcelable[] parcelableAnimeInformationArray = in.readParcelableArray(AnimeInformation.class.getClassLoader());
        AnimeInformation[] resultAnimeInformationArray = null;
        if (parcelableAnimeInformationArray != null) {
            resultAnimeInformationArray = Arrays.copyOf(parcelableAnimeInformationArray, parcelableAnimeInformationArray.length, AnimeInformation[].class);
            AnimeInformations = new ArrayList<AnimeInformation>(Arrays.asList(resultAnimeInformationArray));
        }

        Parcelable[] parcelableThemeArray = in.readParcelableArray(Theme.class.getClassLoader());
        Theme[] resultThemeArray = null;
        if (parcelableThemeArray != null) {
            resultThemeArray = Arrays.copyOf(parcelableThemeArray, parcelableThemeArray.length, Theme[].class);
            Themes = new ArrayList<Theme>(Arrays.asList(resultThemeArray));
        }

        Parcelable[] parcelableAnimeSourceArray = in.readParcelableArray(AnimeSource.class.getClassLoader());
        AnimeSource[] resultAnimeSourceArray = null;
        if (parcelableAnimeSourceArray != null) {
            resultAnimeSourceArray = Arrays.copyOf(parcelableAnimeSourceArray, parcelableAnimeSourceArray.length, AnimeSource[].class);
            AnimeSources = new ArrayList<AnimeSource>(Arrays.asList(resultAnimeSourceArray));
        }

        Parcelable[] parcelableLinkArray = in.readParcelableArray(Link.class.getClassLoader());
        Link[] resultLinkArray = null;
        if (parcelableLinkArray != null) {
            resultLinkArray = Arrays.copyOf(parcelableLinkArray, parcelableLinkArray.length, Link[].class);
            Links = new ArrayList<Link>(Arrays.asList(resultLinkArray));
        }

        AnimeId = in.readInt();
        Description = in.readString();
        StatusId = in.readInt();
        OriginalName = in.readString();
        long addedDateTime = in.readLong();
        if(addedDateTime != 0)
            AddedDate = new Date(addedDateTime);
        else
            AddedDate = null;
        long lastUpdatedDateTime = in.readLong();
        if(lastUpdatedDateTime != 0)
            LastUpdatedDate = new Date(lastUpdatedDateTime);
        else
            LastUpdatedDate = null;
        IsMovie = in.readByte() != 0;
        PosterPath = in.readString();
        RunningTime = in.readInt();
        long releasedDateTime = in.readLong();
        if(releasedDateTime != 0)
            ReleasedDate = new Date(releasedDateTime);
        else
            ReleasedDate = null;
        BackdropPath = in.readString();
        IsCartoon = in.readByte() != 0;
        Rating = in.readDouble();
        SourceUrl = in.readString();
        Order = in.readInt();
        VoteCount = in.readInt();
        IsFavorite = in.readByte() != 0;
        long linkedDateTime = in.readLong();
        if(linkedDateTime != 0)
            LinkedDate = new Date(linkedDateTime); //better performance than serializing it.
        else
            LinkedDate = null;
        IsInMyList = in.readByte() != 0;
        IsAvailable = in.readByte() != 0;
        EpisodeCount = in.readInt();
        AgeRatingId = in.readInt();
        MyAnimeListId = in.readInt();
    }

    public ArrayList<Link> getLinks() {
        return Links;
    }

    public void setLinks(ArrayList<Link> links) {
        Links = links;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        Order = order;
    }

    public ArrayList<AnimeSource> getAnimeSources() {
        return AnimeSources;
    }

    public void setAnimeSources(ArrayList<AnimeSource> animeSources) {
        this.AnimeSources = animeSources;
    }

    public ArrayList<AnimeInformation> getAnimeInformations() {
        return this.AnimeInformations;
    }

    public AnimeInformation getAnimeInformation(Context context) {
            for (AnimeInformation info : this.AnimeInformations) {
                if (String.valueOf(info.getLanguageId()).equals(App.currentLanguageId))
                    return info;
        }

        return null;

    }

    public int getVoteCount() {
        return VoteCount;
    }

    public void setVoteCount(int voteCount) {
        VoteCount = voteCount;
    }

    public void setAnimeInformations(ArrayList<AnimeInformation> animeInformations) {
        this.AnimeInformations = animeInformations;
    }

    public ArrayList<Genre> getGenres() {
        return Genres;
    }

    public ArrayList<Theme> getThemes() {
        return this.Themes;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.Genres = genres;
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
        return IsCartoon;
    }


    public void setCartoon(boolean isCartoon) {
        this.IsCartoon = isCartoon;
    }


    public ArrayList<Episode> getEpisodes() {
        return Episodes;
    }

    public void setEpisodes(ArrayList<Episode> episodes) {
        this.Episodes = episodes;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public String getDescription(Context context) {
        for (AnimeInformation animeInfo : this.getAnimeInformations()) {
            if (String.valueOf(animeInfo.getLanguageId()).equals(App.currentLanguageId)) {
                if (animeInfo.getOverview() != null && !animeInfo.getOverview().equals(""))
                    return animeInfo.getOverview().trim();
                else if (animeInfo.getDescription() != null && !animeInfo.getDescription().equals(""))
                    return animeInfo.getDescription().trim();
                else
                    return "";
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

    public String getGenresFormatted() {
        ArrayList<String> translatedGenres = new ArrayList<String>();

        for (Genre genre : this.Genres) {
            if (!App.currentLanguageId.equals("1")) {
                String genreName = genre.getName().toLowerCase().replace(" ", "");
                translatedGenres.add(Utils.getStringResourceByName(genreName));
            } else
                translatedGenres.add(genre.getName());
        }

        String genresFormatted = "";
        if (this.Genres != null) {
            int counter = 0;
            for (String genre : translatedGenres) {
                if (counter == 4)
                    break;

                genresFormatted += genre + ", ";
                counter++;
            }
        }
        if (!genresFormatted.equals(""))
            return genresFormatted.substring(0, genresFormatted.length() - 2);
        else
            return "";
    }

    public String getName() {
        return OriginalName;
    }

    public void setName(String name) {
        OriginalName = name;
    }

    public Date getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(Date addedDate) {
        AddedDate = addedDate;
    }

    public Date getLastUpdatedDate() {
        return LastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        LastUpdatedDate = lastUpdatedDate;
    }

    public boolean isMovie() {
        return IsMovie;
    }

    public void setIsMovie(boolean isMovie) {
        IsMovie = isMovie;
    }

    public String getRelativePosterPath(String size) {
        if (PosterPath == null)
            return null;

        if (size == null || size.equals(""))
            return PosterPath;

        String imageName = PosterPath.substring(PosterPath.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullPosterPath = PosterPath.substring(0, PosterPath.lastIndexOf("/") + 1) + imageName;
        return fullPosterPath;
    }

    public String getPosterPath() {
        return PosterPath;
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

    public Date getReleaseDate() {
        return ReleasedDate;
    }

    public void setReleaseDate(Date releaseDate) {
        ReleasedDate = releaseDate;
    }

    public String getRelativeBackdropPath(String size) {
        if (BackdropPath == null)
            return null;

        if (size == null || size.equals(""))
            return BackdropPath;

        String imageName = BackdropPath.substring(BackdropPath.lastIndexOf("/") + 1);
        imageName = "w" + size + "_" + imageName;
        String fullPosterPath = BackdropPath.substring(0, BackdropPath.lastIndexOf("/") + 1) + imageName;
        return fullPosterPath;
    }

    public String getBackdropPath() {
        return BackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        BackdropPath = backdropPath;
    }

    public int getMyAnimeListId() {
        return MyAnimeListId;
    }

    public void setMyAnimeListId(int myAnimeListId) {
        MyAnimeListId = myAnimeListId;
    }

    public boolean isFavorite() {
        return IsFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        IsFavorite = isFavorite;
    }

    public Date getLinkedDate() {
        return LinkedDate;
    }

    public void setLinkedDate(Date linkedDate) {
        LinkedDate = linkedDate;
    }

    public boolean isInMyList() {
        return IsInMyList;
    }

    public void setIsInMyList(boolean isInMyList) {
        IsInMyList = isInMyList;
    }

    public boolean isAvailable() {
        return IsAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        IsAvailable = isAvailable;
    }

    public int getEpisodeCount() {
        return EpisodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        EpisodeCount = episodeCount;
    }

    public int getAgeRatingId() {
        return AgeRatingId;
    }

    public void setAgeRatingId(int ageRatingId) {
        AgeRatingId = ageRatingId;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Episodes == null)
            Episodes = new ArrayList<Episode>();
        if (Genres == null)
            Genres = new ArrayList<Genre>();
        if (AnimeInformations == null)
            AnimeInformations = new ArrayList<AnimeInformation>();
        if (Themes == null)
            Themes = new ArrayList<Theme>();
        if (AnimeSources == null)
            AnimeSources = new ArrayList<AnimeSource>();
        if(Links == null)
            Links = new ArrayList<Link>();

        Parcelable[] parcelableEpisodeArray = new Parcelable[Episodes.size()];
        dest.writeParcelableArray(Episodes.toArray(parcelableEpisodeArray), flags);

        Parcelable[] parcelableGenreArray = new Parcelable[Genres.size()];
        dest.writeParcelableArray(Genres.toArray(parcelableGenreArray), flags);

        Parcelable[] parcelableAnimeInformationArray = new Parcelable[AnimeInformations.size()];
        dest.writeParcelableArray(AnimeInformations.toArray(parcelableAnimeInformationArray), flags);

        Parcelable[] parcelableThemeArray = new Parcelable[Themes.size()];
        dest.writeParcelableArray(Themes.toArray(parcelableThemeArray), flags);

        Parcelable[] parcelableAnimeSourceArray = new Parcelable[AnimeSources.size()];
        dest.writeParcelableArray(AnimeSources.toArray(parcelableAnimeSourceArray), flags);

        Parcelable[] parcelableLinkArray = new Parcelable[Links.size()];
        dest.writeParcelableArray(Links.toArray(parcelableLinkArray), flags);


        dest.writeInt(AnimeId);
        dest.writeString(Description);
        dest.writeInt(StatusId);
        dest.writeString(OriginalName);
        dest.writeLong(AddedDate != null ? AddedDate.getTime() : 0);
        dest.writeLong(LastUpdatedDate != null ? LastUpdatedDate.getTime() : 0);
        dest.writeByte((byte) (IsMovie ? 1 : 0));
        dest.writeString(PosterPath);
        dest.writeInt(RunningTime);
        dest.writeLong(ReleasedDate != null ? ReleasedDate.getTime() : 0);
        dest.writeString(BackdropPath);
        dest.writeByte((byte) (IsCartoon ? 1 : 0));
        dest.writeDouble(Rating != null ? Rating : 0);
        dest.writeString(SourceUrl);
        dest.writeInt(Order);
        dest.writeInt(VoteCount);
        dest.writeByte((byte) (IsFavorite ? 1 : 0));
        dest.writeLong(LinkedDate != null ? LinkedDate.getTime() : 0);
        dest.writeByte((byte) (IsInMyList ? 1 : 0));
        dest.writeByte((byte) (IsAvailable ? 1 : 0));
        dest.writeInt(EpisodeCount);
        dest.writeInt(AgeRatingId);
        dest.writeInt(MyAnimeListId);
    }

    public static final Creator<Anime> CREATOR = new Creator<Anime>() {
        public Anime createFromParcel(Parcel in) {
            return new Anime(in);
        }

        public Anime[] newArray(int size) {
            return new Anime[size];
        }
    };

    @Override
    public int compare(Anime anime, Anime anime2) {
        int val = 0;

        if (anime.getOrder() < anime2.getOrder()) {
            val = -1;
        } else if (anime.getOrder() > anime2.getOrder()) {
            val = 1;
        } else if (anime.getOrder() == anime2.getOrder()) {
            val = 0;
        }
        return val;
    }
}
