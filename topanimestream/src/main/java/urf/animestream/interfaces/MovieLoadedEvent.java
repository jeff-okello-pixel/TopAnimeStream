package urf.animestream.interfaces;

import urf.animestream.models.Mirror;

import java.util.ArrayList;

public interface MovieLoadedEvent {
	void onMovieLoaded(ArrayList<Mirror> mirrors);
}
