package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class WatchedAnime implements Parcelable {
    private int WatchedAnimeId;
    private int AccountId;
    private int AnimeId;
    private Date AddedDate;
    private int WatchTypeId;
    private int TotalWatchedEpisodes;
    private boolean IsPrivate;
    private Date LastWatchedDate;
    private Anime Anime;
    private WatchType WatchType;

    public WatchedAnime(){}

    public WatchedAnime(Parcel in)
    {
        WatchedAnimeId = in.readInt();
        AccountId = in.readInt();
        AnimeId = in.readInt();
        long addedDateTime = in.readLong();
        if(addedDateTime != 0)
            AddedDate = new Date(addedDateTime);
        else
            AddedDate = null;
        WatchTypeId = in.readInt();
        TotalWatchedEpisodes = in.readInt();
        Anime = in.readParcelable(com.topanimestream.models.Anime.class.getClassLoader());
        WatchType = in.readParcelable(com.topanimestream.models.WatchType.class.getClassLoader());
    }

    public int getWatchedAnimeId() {
        return WatchedAnimeId;
    }

    public void setWatchedAnimeId(int watchedAnimeId) {
        WatchedAnimeId = watchedAnimeId;
    }

    public int getAccountId() {
        return AccountId;
    }

    public void setAccountId(int accountId) {
        AccountId = accountId;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public Date getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(Date addedDate) {
        AddedDate = addedDate;
    }

    public int getWatchTypeId() {
        return WatchTypeId;
    }

    public void setWatchTypeId(int watchTypeId) {
        WatchTypeId = watchTypeId;
    }

    public int getTotalWatchedEpisodes() {
        return TotalWatchedEpisodes;
    }

    public void setTotalWatchedEpisodes(int totalWatchedEpisodes) {
        TotalWatchedEpisodes = totalWatchedEpisodes;
    }

    public boolean isPrivate() {
        return IsPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        IsPrivate = isPrivate;
    }

    public Date getLastWatchedDate() {
        return LastWatchedDate;
    }

    public void setLastWatchedDate(Date lastWatchedDate) {
        LastWatchedDate = lastWatchedDate;
    }

    public com.topanimestream.models.Anime getAnime() {
        return Anime;
    }

    public void setAnime(com.topanimestream.models.Anime anime) {
        Anime = anime;
    }

    public com.topanimestream.models.WatchType getWatchType() {
        return WatchType;
    }

    public void setWatchType(com.topanimestream.models.WatchType watchType) {
        WatchType = watchType;
    }

    public static final Creator<WatchedAnime> CREATOR = new Creator<WatchedAnime>() {
        public WatchedAnime createFromParcel(Parcel in) {
            return new WatchedAnime(in);
        }

        public WatchedAnime[] newArray(int size) {
            return new WatchedAnime[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(WatchedAnimeId);
        dest.writeInt(AccountId);
        dest.writeInt(AnimeId);
        dest.writeLong(AddedDate != null ? AddedDate.getTime() : 0);
        dest.writeInt(WatchTypeId);
        dest.writeInt(TotalWatchedEpisodes);
        dest.writeByte((byte) (IsPrivate ? 1 : 0));
        dest.writeLong(LastWatchedDate != null ? LastWatchedDate.getTime() : 0);
        dest.writeParcelable(Anime, flag);
        dest.writeParcelable(WatchType, flag);
    }
}
