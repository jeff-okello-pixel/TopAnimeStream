package com.topanimestream.models;

public class Link {
    private int LinkId;
    private int AnimeId;
    private int EpisodeId;
    private int LanguageId;
    private String AddedDate;
    private String FileName;
    private String PicasaPhotoId;
    private String PicasaAlbumId;
    private String PicasaUserName;
    private String Thumbnail;
    private Anime Anime;
    private Episode Episode;

    public Link() {
    }

    public Anime getAnime() {
        return Anime;
    }

    public void setAnime(Anime anime) {
        Anime = anime;
    }

    public Episode getEpisode() {
        return Episode;
    }

    public void setEpisode(Episode episode) {
        Episode = episode;
    }

    public int getLinkId() {
        return LinkId;
    }

    public void setLinkId(int linkId) {
        LinkId = linkId;
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

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getPicasaPhotoId() {
        return PicasaPhotoId;
    }

    public void setPicasaPhotoId(String picasaPhotoId) {
        PicasaPhotoId = picasaPhotoId;
    }

    public String getPicasaAlbumId() {
        return PicasaAlbumId;
    }

    public void setPicasaAlbumId(String picasaAlbumId) {
        PicasaAlbumId = picasaAlbumId;
    }

    public String getPicasaUserName() {
        return PicasaUserName;
    }

    public void setPicasaUserName(String picasaUserName) {
        PicasaUserName = picasaUserName;
    }

    public String getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        Thumbnail = thumbnail;
    }
}
