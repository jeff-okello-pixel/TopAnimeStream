package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Link implements Parcelable {
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
    private Language Language;

    public Link() {
    }
    public Link(Parcel in) {

        LinkId = in.readInt();
        AnimeId = in.readInt();
        EpisodeId = in.readInt();
        LanguageId = in.readInt();
        AddedDate = in.readString();
        FileName = in.readString();
        PicasaPhotoId = in.readString();
        PicasaAlbumId = in.readString();
        PicasaUserName = in.readString();
        Thumbnail = in.readString();
        Anime = in.readParcelable(Anime.class.getClassLoader());
        Episode = in.readParcelable(Episode.class.getClassLoader());
        Language = in.readParcelable(Language.class.getClassLoader());
    }

    public Language getLanguage() {
        return Language;
    }

    public void setLanguage(Language language) {
        Language = language;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(LinkId);
        dest.writeInt(AnimeId);
        dest.writeInt(EpisodeId);
        dest.writeInt(LanguageId);
        dest.writeString(AddedDate);
        dest.writeString(FileName);
        dest.writeString(PicasaPhotoId);
        dest.writeString(PicasaAlbumId);
        dest.writeString(PicasaUserName);
        dest.writeString(Thumbnail);
        dest.writeParcelable(Anime, flags);
        dest.writeParcelable(Episode, flags);
        dest.writeParcelable(Language, flags);
    }

    public static final Creator<Link> CREATOR = new Creator<Link>() {
        public Link createFromParcel(Parcel in) {
            return new Link(in);
        }

        public Link[] newArray(int size) {
            return new Link[size];
        }
    };
}
