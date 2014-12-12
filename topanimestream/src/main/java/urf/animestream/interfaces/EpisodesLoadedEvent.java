package urf.animestream.interfaces;

import java.util.ArrayList;

import urf.animestream.models.Episode;

public interface EpisodesLoadedEvent {
	void onEpisodesLoaded(ArrayList<Episode> episodes, String animeName, String animeDescription, String animePoster);
}
