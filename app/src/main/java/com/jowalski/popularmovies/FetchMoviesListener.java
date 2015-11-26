package com.jowalski.popularmovies;

/**
 * A listener interface that waits for the FetchMoviesTask
 * to complete and then calls its associated object's
 * onFetchMoviesComplete() method.
 */
interface FetchMoviesListener {
    void onFetchMoviesComplete(Movie[] movies);
}