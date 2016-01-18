package com.topanimestream.models;

public class Favorite {
    private int Order;
    private Anime Anime;

    public Favorite() {
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
