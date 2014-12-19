package org.topanimestream.interfaces;

import java.util.ArrayList;

import org.topanimestream.models.Episode;

public interface EpisodesLoadedEvent {
	void onEpisodesLoaded(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster);
}
