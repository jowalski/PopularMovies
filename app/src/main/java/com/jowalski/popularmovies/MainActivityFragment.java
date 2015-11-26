package com.jowalski.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements FetchMoviesListener {
    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private static final long FETCH_MOVIES_MAX_WAIT = 3;

    private MovieAdapter movieAdapter;

    private ArrayList<Movie> movieList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(this);
            fetchMoviesTask.execute();

            Movie[] movies = null;
            try {
                movies = fetchMoviesTask.get(FETCH_MOVIES_MAX_WAIT, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                Log.e(TAG, "onCreate: FetchMoviesTask error", e);
            }

            if (movies == null) {
                movieList = new ArrayList<>();
            } else {
                movieList = new ArrayList<>(Arrays.asList(movies));
            }
        } else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_poster_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
        }
        return super.onOptionsItemSelected(item);
    }

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), movieList);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(movieAdapter);

        updateMovies();

        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(this);
        fetchMoviesTask.execute();
    }

    @Override
    public void onFetchMoviesComplete(Movie[] movies) {
        if (movies != null) {
            movieList = new ArrayList<>(Arrays.asList(movies));
            movieAdapter.clear();
            for (int i = 0; i < movies.length; i++) {
                movieAdapter.add(movieList.get(i));
            }
        }
    }
}
