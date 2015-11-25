package com.jowalski.popularmovies;

/**
 * Created by jowalski on 11/24/15.
 */
public interface FetchMoviesListener {
    void onFetchMoviesComplete(Movie[] movies);
}