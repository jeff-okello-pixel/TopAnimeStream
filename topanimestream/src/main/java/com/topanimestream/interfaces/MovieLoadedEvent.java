package com.topanimestream.interfaces;

import java.util.ArrayList;

import com.topanimestream.models.Mirror;

public interface MovieLoadedEvent {
    void onMovieLoaded(ArrayList<Mirror> mirrors);
}
