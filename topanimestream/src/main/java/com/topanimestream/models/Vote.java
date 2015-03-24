package com.topanimestream.models;

public class Vote {
    private int AccountId;
    private int AnimeId;
    private int Value;
    private String AddedDate;
    private Anime Anime;
    public Vote() {
    }

    public Vote(int accountId, int animeId, int value, String addedDate) {
        AccountId = accountId;
        AnimeId = animeId;
        Value = value;
        AddedDate = addedDate;
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

    public int getValue() {
        return Value;
    }

    public void setValue(int value) {
        Value = value;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public Anime getAnime() {
        return Anime;
    }

    public void setAnime(Anime anime) {
        this.Anime = anime;
    }
}
