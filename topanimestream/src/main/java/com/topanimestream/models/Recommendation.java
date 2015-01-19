package com.topanimestream.models;

public class Recommendation {
    private int RecommendationId;
    private int AccountId;
    private int AnimeId;
    private int RecommendedAnimeId;
    private String Value;
    private String AddedDate;

    public Recommendation() {
    }

    public Recommendation(int recommendationId, int accountId, int animeId, int recommendedAnimeId, String value, String addedDate) {
        RecommendationId = recommendationId;
        AccountId = accountId;
        AnimeId = animeId;
        RecommendedAnimeId = recommendedAnimeId;
        Value = value;
        AddedDate = addedDate;
    }

    public int getRecommendationId() {
        return RecommendationId;
    }

    public void setRecommendationId(int recommendationId) {
        RecommendationId = recommendationId;
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

    public int getRecommendedAnimeId() {
        return RecommendedAnimeId;
    }

    public void setRecommendedAnimeId(int recommendedAnimeId) {
        RecommendedAnimeId = recommendedAnimeId;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }
}
