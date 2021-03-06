package com.topanimestream.models;

public class Favorite {
    private int Order;
    private int FavoriteId;
    private int AnimeId;
    private Anime Anime;

    public Favorite() {
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public int getFavoriteId() {
        return FavoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        FavoriteId = favoriteId;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        Order = order;
    }

    public com.topanimestream.models.Anime getAnime() {
        return Anime;
    }

    public void setAnime(com.topanimestream.models.Anime anime) {
        Anime = anime;
    }
}
