package com.jowalski.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements FetchMoviesListener {

    private MovieAdapter movieAdapter;

    private ArrayList<Movie> movieList;

    Movie[] movies = {
            new Movie("Spectre", R.drawable.spectre),
            new Movie("Ant-man", R.drawable.antman),
            new Movie("Jurassic World", R.drawable.jurassicworld),
            new Movie("Fantastic Four", R.drawable.fantasticfour),
            new Movie("Minions", R.drawable.minions),
            new Movie("Terminator Genisys", R.drawable.terminatorgenisys),
            new Movie("The Hobbit: the Battle of the Five Armies",
                    R.drawable.thehobbitthebattleofthefivearmies)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movieList = new ArrayList<Movie>(Arrays.asList(movies));
        }
        else {
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

        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(this);
        fetchMoviesTask.execute();
    }

    @Override
    public void onFetchMoviesComplete(Movie[] movies) {
        movieList = new ArrayList<Movie>(Arrays.asList(movies));
        movieAdapter.clear();
        for (int i = 0; i < movies.length; i++) {
            movieAdapter.add(movies[i]);
        }
    }
}
