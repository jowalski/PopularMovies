package com.jowalski.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jowalski.popularmovies.data.MovieContract;
import com.jowalski.popularmovies.service.MovieService;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final String DET_ACT_MOVIE_OBJ_INTENT_KEY = "movie_obj";

    public static final int SORT_ORDER_BY_POPULARITY = 0;
    public static final int SORT_ORDER_BY_RATING = 1;

    private static final String MOVIE_ID_EXTRA = "movie_id";

    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_POPULARITY
    };

    static final int COL_MOVIE_TMDB_ID = 0;
    static final int COL_MOVIE_TITLE_ID = 1;
    static final int COL_POSTER_PATH = 2;
    static final int COL_POPULARITY = 3;

    private MovieAdapter mMovieAdapter;

    private ArrayList<Movie> movieList;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        updateMovies(getContext(), SORT_ORDER_BY_POPULARITY, true);
        Log.d(LOG_TAG, "onCreate: updated Movie database");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_poster_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_by_rating_checkbox) {
            // TODO: 11/26/15 item.setChecked() should only be called
            // when GridView successfully updated
            if (!item.isChecked()) {
                // sort by rating
                updateMovies(getContext(), SORT_ORDER_BY_RATING, true);
                item.setChecked(true);
            } else {
                // sort by popularity
                updateMovies(getContext(), SORT_ORDER_BY_POPULARITY, true);
                item.setChecked(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                int movieId;
                if (cursor != null) {
                    movieId = cursor.getInt(COL_MOVIE_TMDB_ID);
                    Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                    intent.putExtra(MOVIE_ID_EXTRA, movieId);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovies(Context context, int movieSortOrder,
                              boolean notifyResolver) {
        Intent movieIntent = new Intent(getContext(), MovieService.class);
        movieIntent.putExtra(MovieService.SORT_ORDER_EXTRA, movieSortOrder);
        movieIntent.putExtra(MovieService.NOTIFY_RESOLVER_EXTRA, notifyResolver);
        context.startService(movieIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                null,
                null,
                MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMovieAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

}