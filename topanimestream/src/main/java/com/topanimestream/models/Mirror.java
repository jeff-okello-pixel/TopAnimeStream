package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Mirror implements Parcelable {
    private int MirrorId;
    private int EpisodeId;
    private int AnimeSourceId;
    private int PartNumber;
    private String AddedDate;
    private String Source;
    private int ReportCount;
    private boolean IsVisible;
    private com.topanimestream.models.AnimeSource AnimeSource;
    private Provider Provider;

    public Mirror() {
        super();
    }

    public Mirror(int mirrorId, int episodeId, int animeSourceId, int partNumber, String addedDate, String source, int reportCount, boolean isVisible, com.topanimestream.models.AnimeSource animeSource, Provider provider) {
        MirrorId = mirrorId;
        EpisodeId = episodeId;
        AnimeSourceId = animeSourceId;
        PartNumber = partNumber;
        AddedDate = addedDate;
        this.Source = source;
        ReportCount = reportCount;
        IsVisible = isVisible;
        AnimeSource = animeSource;
        this.Provider = provider;
    }

    public Mirror(Vk vk) {
        this.EpisodeId = vk.getEpisodeId();
        this.AnimeSourceId = vk.getAnimeSourceId();
        this.AddedDate = vk.getAddedDate();
        this.Source = vk.getSource();
        this.AnimeSource = vk.getAnimeSource();
        this.Provider = new Provider(70, "vk");
    }

    public Mirror(JSONObject jsonMirror) {
        try {
            if (!jsonMirror.isNull("AnimeSource"))
                this.AnimeSource = new com.topanimestream.models.AnimeSource(jsonMirror.getJSONObject("AnimeSource"));
            if (!jsonMirror.isNull("Provider"))
                this.Provider = new Provider(jsonMirror.getJSONObject("Provider"));
            this.MirrorId = !jsonMirror.isNull("MirrorId") ? jsonMirror.getInt("MirrorId") : 0;
            this.EpisodeId = !jsonMirror.isNull("EpisodeId") ? jsonMirror.getInt("EpisodeId") : 0;
            this.AnimeSourceId = !jsonMirror.isNull("AnimeSourceId") ? jsonMirror.getInt("AnimeSourceId") : 0;
            this.AddedDate = !jsonMirror.isNull("AddedDate") ? jsonMirror.getString("AddedDate") : null;
            this.Source = !jsonMirror.isNull("Source") ? jsonMirror.getString("Source") : null;
            this.ReportCount = !jsonMirror.isNull("ReportCount") ? jsonMirror.getInt("ReportCount") : null;
            this.IsVisible = !jsonMirror.isNull("IsVisible") ? jsonMirror.getBoolean("IsVisible") : null;
        } catch (Exception e) {

        }
    }

    public Mirror(Parcel in) {
        AnimeSource = (com.topanimestream.models.AnimeSource) in.readParcelable(com.topanimestream.models.AnimeSource.class.getClassLoader());
        Provider = (Provider) in.readParcelable(Provider.class.getClassLoader());
        MirrorId = in.readInt();
        EpisodeId = in.readInt();
        AnimeSourceId = in.readInt();
        PartNumber = in.readInt();
        AddedDate = in.readString();
        Source = in.readString();
        ReportCount = in.readInt();
        IsVisible = in.readByte() != 0;
    }

    public int getReportCount() {
        return ReportCount;
    }

    public void setReportCount(int reportCount) {
        ReportCount = reportCount;
    }

    public boolean isVisible() {
        return IsVisible;
    }

    public void setVisible(boolean isVisible) {
        IsVisible = isVisible;
    }

    public Provider getProvider() {
        return Provider;
    }

    public void setProvider(Provider provider) {
        this.Provider = provider;
    }

    public com.topanimestream.models.AnimeSource getAnimeSource() {
        return AnimeSource;
    }

    public void setAnimeSource(com.topanimestream.models.AnimeSource animeSource) {
        AnimeSource = animeSource;
    }

    public int getMirrorId() {
        return MirrorId;
    }

    public void setMirrorId(int mirrorId) {
        MirrorId = mirrorId;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int episodeId) {
        EpisodeId = episodeId;
    }

    public int getAnimeSourceId() {
        return AnimeSourceId;
    }

    public void setAnimeSourceId(int animeSourceId) {
        AnimeSourceId = animeSourceId;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        this.Source = source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(AnimeSource, flags);
        dest.writeParcelable(Provider, flags);
        dest.writeInt(MirrorId);
        dest.writeInt(EpisodeId);
        dest.writeInt(AnimeSourceId);
        dest.writeInt(PartNumber);
        dest.writeString(AddedDate);
        dest.writeString(Source);
        dest.writeInt(ReportCount);
        dest.writeByte((byte) (IsVisible ? 1 : 0));
    }

    public static final Creator<Mirror> CREATOR = new Creator<Mirror>() {
        public Mirror createFromParcel(Parcel in) {
            return new Mirror(in);
        }

        public Mirror[] newArray(int size) {
            return new Mirror[size];
        }
    };


}
