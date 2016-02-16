package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class WatchedVideo implements Parcelable {
    private int WatchedVideoId;
    private double DurationInSeconds;
    private double TimeInSeconds;
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
    public WatchedVideo(Parcel in) {

        WatchedVideoId = in.readInt();
        DurationInSeconds = in.readDouble();
        TimeInSeconds = in.readDouble();
        DisplayTime = in.readString();
        DisplayDuration = in.readString();
        long addedDateTime = in.readLong();
        if(addedDateTime != 0)
            AddedDate = new Date(addedDateTime);
        else
            AddedDate = null;
        Time = in.readString();
        Duration = in.readString();
        IsComplete = in.readByte() != 0;
        long lastWatchedDateTime = in.readLong();
        if(lastWatchedDateTime != 0)
            LastWatchedDate = new Date(lastWatchedDateTime);
        else
            LastWatchedDate = null;
        Episode = in.readParcelable(com.topanimestream.models.Episode.class.getClassLoader());
        Anime = in.readParcelable(com.topanimestream.models.Anime.class.getClassLoader());
    }

    public static final Creator<WatchedVideo> CREATOR = new Creator<WatchedVideo>() {
        @Override
        public WatchedVideo createFromParcel(Parcel in) {
            return new WatchedVideo(in);
        }

        @Override
        public WatchedVideo[] newArray(int size) {
            return new WatchedVideo[size];
        }
    };

    public int getWatchedVideoId() {
        return WatchedVideoId;
    }

    public void setWatchedVideoId(int watchedVideoId) {
        WatchedVideoId = watchedVideoId;
    }

    public double getDurationInSeconds() {
        return DurationInSeconds;
    }

    public void setDurationInSeconds(double durationInSeconds) {
        DurationInSeconds = durationInSeconds;
    }

    public double getTimeInSeconds() {
        return TimeInSeconds;
    }

    public void setTimeInSeconds(double timeInSeconds) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(WatchedVideoId);
        dest.writeDouble(DurationInSeconds);
        dest.writeDouble(TimeInSeconds);
        dest.writeString(DisplayTime);
        dest.writeString(DisplayDuration);
        dest.writeLong(AddedDate != null ? AddedDate.getTime() : 0);
        dest.writeString(Time);
        dest.writeString(Duration);
        dest.writeByte((byte) (IsComplete != null && IsComplete ? 1 : 0));
        dest.writeLong(LastWatchedDate != null ? LastWatchedDate.getTime() : 0);
        dest.writeParcelable(Episode, flags);
        dest.writeParcelable(Anime, flags);

    }
}
