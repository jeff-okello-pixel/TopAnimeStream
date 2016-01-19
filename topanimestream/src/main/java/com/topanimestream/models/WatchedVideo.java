package com.topanimestream.models;

import java.util.Date;

public class WatchedVideo {
    private int WatchedVideoId;
    private long DurationInSeconds;
    private long TimeInSeconds;
    private String DisplayTime;
    private String DisplayDuration;
    private Date AddedDate;
    private String Time;
    private String Duration;
    private Boolean IsComplete;
    private Date LastWatchedDate;
    private Episode Episode;
    private Anime Anime;

    public WatchedVideo() {
    }

    public int getWatchedVideoId() {
        return WatchedVideoId;
    }

    public void setWatchedVideoId(int watchedVideoId) {
        WatchedVideoId = watchedVideoId;
    }

    public long getDurationInSeconds() {
        return DurationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        DurationInSeconds = durationInSeconds;
    }

    public long getTimeInSeconds() {
        return TimeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        TimeInSeconds = timeInSeconds;
    }

    public String getDisplayTime() {
        return DisplayTime;
    }

    public void setDisplayTime(String displayTime) {
        DisplayTime = displayTime;
    }

    public String getDisplayDuration() {
        return DisplayDuration;
    }

    public void setDisplayDuration(String displayDuration) {
        DisplayDuration = displayDuration;
    }

    public Date getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(Date addedDate) {
        AddedDate = addedDate;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public Boolean getIsComplete() {
        return IsComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        IsComplete = isComplete;
    }

    public Date getLastWatchedDate() {
        return LastWatchedDate;
    }

    public void setLastWatchedDate(Date lastWatchedDate) {
        LastWatchedDate = lastWatchedDate;
    }

    public com.topanimestream.models.Episode getEpisode() {
        return Episode;
    }

    public void setEpisode(com.topanimestream.models.Episode episode) {
        Episode = episode;
    }

    public com.topanimestream.models.Anime getAnime() {
        return Anime;
    }

    public void setAnime(com.topanimestream.models.Anime anime) {
        Anime = anime;
    }
}
