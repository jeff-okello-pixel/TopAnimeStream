package com.topanimestream.interfaces;

import java.util.ArrayList;

import com.topanimestream.models.Episode;

public interface EpisodesLoadedEvent {
    void onEpisodesLoaded(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster);
}
