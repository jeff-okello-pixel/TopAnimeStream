package org.topanimestream.interfaces;

import java.util.ArrayList;

import org.topanimestream.models.Mirror;

public interface MovieLoadedEvent {
	void onMovieLoaded(ArrayList<Mirror> mirrors);
}
