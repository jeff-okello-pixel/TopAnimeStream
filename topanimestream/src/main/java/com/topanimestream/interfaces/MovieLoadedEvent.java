package com.topanimestream.interfaces;

import com.topanimestream.models.Mirror;

import java.util.ArrayList;

public interface MovieLoadedEvent {
	void onMovieLoaded(ArrayList<Mirror> mirrors);
}
