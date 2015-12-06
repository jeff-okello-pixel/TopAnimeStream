package com.topanimestream.models;

import java.util.Date;

public class Update {
    private int AnimeId;
    private int LanguageId;
    private int LastEpisodeId;
    private String FirstEpisodeNumber;
    private String LastEpisodeNumber;
    private int TotalEpisodes;
    private Date LastUpdatedDate;
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

    public int getLastEpisodeId() {
        return LastEpisodeId;
    }

    public void setLastEpisodeId(int lastEpisodeId) {
        LastEpisodeId = lastEpisodeId;
    }

    public Date getLastUpdatedDate() {
        return LastUpdatedDate;
    }

    public void setLastUpdateDate(Date lastUpdatedDate) {
        LastUpdatedDate = lastUpdatedDate;
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

    public String getFirstEpisodeNumber() {
        return FirstEpisodeNumber;
    }

    public void setFirstEpisodeNumber(String firstEpisodeNumber) {
        FirstEpisodeNumber = firstEpisodeNumber;
    }

    public String getLastEpisodeNumber() {
        return LastEpisodeNumber;
    }

    public void setLastEpisodeNumber(String lastEpisodeNumber) {
        LastEpisodeNumber = lastEpisodeNumber;
    }

    public int getTotalEpisodes() {
        return TotalEpisodes;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        TotalEpisodes = totalEpisodes;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        LastUpdatedDate = lastUpdatedDate;
    }
}
