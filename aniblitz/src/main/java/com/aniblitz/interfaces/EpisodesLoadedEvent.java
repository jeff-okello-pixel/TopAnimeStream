package com.aniblitz.interfaces;

import java.util.ArrayList;

import com.aniblitz.models.Episode;

public interface EpisodesLoadedEvent {
	void onEpisodesLoaded(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster);
}
