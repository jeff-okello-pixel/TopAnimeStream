package com.topanimestream.models;

public class Subtitle {
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
}
