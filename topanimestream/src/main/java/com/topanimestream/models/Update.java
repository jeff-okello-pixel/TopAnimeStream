package com.topanimestream.models;

import java.util.Date;

public class Update {
    private int AnimeId;
    private int LanguageId;
    private int TotalUpdates;
    private int LastLinkId;
    private int LastEpisodeId;
    private int LastUploadId;
    private int LastLinkedBy;
    private Date LastUpdateDate;
    private Anime Anime;
    private Episode Episode;
    private Language Language;

    public Update() {
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

    public int getTotalUpdates() {
        return TotalUpdates;
    }

    public void setTotalUpdates(int totalUpdates) {
        TotalUpdates = totalUpdates;
    }

    public int getLastLinkId() {
        return LastLinkId;
    }

    public void setLastLinkId(int lastLinkId) {
        LastLinkId = lastLinkId;
    }

    public int getLastEpisodeId() {
        return LastEpisodeId;
    }

    public void setLastEpisodeId(int lastEpisodeId) {
        LastEpisodeId = lastEpisodeId;
    }

    public int getLastUploadId() {
        return LastUploadId;
    }

    public void setLastUploadId(int lastUploadId) {
        LastUploadId = lastUploadId;
    }

    public int getLastLinkedBy() {
        return LastLinkedBy;
    }

    public void setLastLinkedBy(int lastLinkedBy) {
        LastLinkedBy = lastLinkedBy;
    }

    public Date getLastUpdateDate() {
        return LastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        LastUpdateDate = lastUpdateDate;
    }

    public com.topanimestream.models.Anime getAnime() {
        return Anime;
    }

    public void setAnime(com.topanimestream.models.Anime anime) {
        Anime = anime;
    }

    public com.topanimestream.models.Episode getEpisode() {
        return Episode;
    }

    public void setEpisode(com.topanimestream.models.Episode episode) {
        Episode = episode;
    }

    public com.topanimestream.models.Language getLanguage() {
        return Language;
    }

    public void setLanguage(com.topanimestream.models.Language language) {
        Language = language;
    }
}
