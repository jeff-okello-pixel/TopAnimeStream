package com.aniblitz.interfaces;

import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;

import java.util.ArrayList;

public interface MovieLoadedEvent {
	void onMovieLoaded(ArrayList<Mirror> mirrors);
}
