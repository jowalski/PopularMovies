package com.jowalski.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MoviePosterAdapter movieAdapter;

    private ArrayList<MoviePoster> moviePosterList;

    MoviePoster[] moviePosters = {
            new MoviePoster("Spectre", R.drawable.spectre),
            new MoviePoster("Ant-man", R.drawable.antman),
            new MoviePoster("Jurassic World", R.drawable.jurassicworld),
            new MoviePoster("Fantastic Four", R.drawable.fantasticfour),
            new MoviePoster("Minions", R.drawable.minions),
            new MoviePoster("Terminator Genisys", R.drawable.terminatorgenisys),
            new MoviePoster("The Hobbit: the Battle of the Five Armies",
                    R.drawable.thehobbitthebattleofthefivearmies)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            moviePosterList = new ArrayList<MoviePoster>(Arrays.asList(moviePosters));
        }
        else {
            moviePosterList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", moviePosterList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MoviePosterAdapter(getActivity(), Arrays.asList(moviePosters));

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(movieAdapter);

        return rootView;
    }
}
