package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Subtitle implements Parcelable {
    private int SubtitleId;
    private String FileName;
    private int AnimeId;
    private int EpisodeId;
    private int LanguageId;
    private String RelativeUrl;
    private String RelativePath;
    private int Size;
    private String FormattedSize;
    private String Specification;
    private String AddedDate;

    public Subtitle() {
    }
    public Subtitle(Parcel in) {
        SubtitleId = in.readInt();
        FileName = in.readString();
        AnimeId = in.readInt();
        EpisodeId = in.readInt();
        LanguageId = in.readInt();
        RelativeUrl = in.readString();
        RelativePath = in.readString();
        Size = in.readInt();
        FormattedSize = in.readString();
        Specification = in.readString();
        AddedDate = in.readString();
    }
    public int getSubtitleId() {
        return SubtitleId;
    }

    public void setSubtitleId(int subtitleId) {
        SubtitleId = subtitleId;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int episodeId) {
        EpisodeId = episodeId;
    }

    public int getLanguageId() {
        return LanguageId;
    }

    public void setLanguageId(int languageId) {
        LanguageId = languageId;
    }

    public String getRelativeUrl() {
        return RelativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        RelativeUrl = relativeUrl;
    }

    public String getRelativePath() {
        return RelativePath;
    }

    public void setRelativePath(String relativePath) {
        RelativePath = relativePath;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getFormattedSize() {
        return FormattedSize;
    }

    public void setFormattedSize(String formattedSize) {
        FormattedSize = formattedSize;
    }

    public String getSpecification() {
        return Specification;
    }

    public void setSpecification(String specification) {
        Specification = specification;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(SubtitleId);
        dest.writeString(FileName);
        dest.writeInt(AnimeId);
        dest.writeInt(EpisodeId);
        dest.writeInt(LanguageId);
        dest.writeString(RelativeUrl);
        dest.writeString(RelativePath);
        dest.writeInt(Size);
        dest.writeString(FormattedSize);
        dest.writeString(Specification);
        dest.writeString(AddedDate);
    }
}
