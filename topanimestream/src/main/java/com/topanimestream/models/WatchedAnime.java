package com.topanimestream.models;

import java.util.Date;

public class WatchedAnime {
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
}
