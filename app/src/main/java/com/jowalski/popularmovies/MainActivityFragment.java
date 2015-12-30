package com.jowalski.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    private static final int ITEMS_PER_PAGE = 20;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
                updateMovies(getContext(), SORT_ORDER_BY_RATING, 1, true);
                item.setChecked(true);
            } else {
                // sort by popularity
                updateMovies(getContext(), SORT_ORDER_BY_POPULARITY, 1, true);
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
        AutofitRecyclerView rv = (AutofitRecyclerView) inflater.inflate(
                R.layout.fragment_main, container, false);
        mMovieAdapter = new MovieAdapter(getContext(), null);
        rv.setAdapter(mMovieAdapter);

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(
                (GridLayoutManager) rv.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                updateMovies(getContext(), SORT_ORDER_BY_POPULARITY, page, false);
            }
        });
        return rv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    static public void updateMovies(Context context, int movieSortOrder,
                              int pageNumber, boolean notifyResolver) {
        Intent movieIntent = new Intent(context, MovieService.class);
        movieIntent.putExtra(MovieService.SORT_ORDER_EXTRA, movieSortOrder);
        movieIntent.putExtra(MovieService.NOTIFY_RESOLVER_EXTRA, notifyResolver);
        movieIntent.putExtra(MovieService.PAGE_NUMBER_EXTRA, pageNumber);
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
        if (cursor.getCount() < ITEMS_PER_PAGE) {
            updateMovies(getContext(), SORT_ORDER_BY_POPULARITY, 1, true);
        }
        mMovieAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    /**
     * Automatically fetches items from the web database as the user scrolls down.
     * This code mostly comes from:
     *
     * https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
     */
    public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        // The minimum amount of items to have below your current scroll position
        // before loading more.
        private int visibleThreshold = 5;
        // The current offset index of data you have loaded
        private int currentPage = 0;
        // The total number of items in the dataset after the last load
        private int previousTotalItemCount = 0;
        // True if we are still waiting for the last set of data to load.
        private boolean loading = false;
        // Sets the starting page index
        private int startingPageIndex = 0;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
            this.mLinearLayoutManager = layoutManager;
        }

        // This happens many times a second during a scroll, so be wary of the code you place here.
        // We are given a few useful parameters to help us work out if we need to load some more data,
        // but first we check if we are waiting for the previous load to finish.
        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = view.getChildCount();
            int totalItemCount = mLinearLayoutManager.getItemCount();

            // If the total item count is zero and the previous isn't, assume the
            // list is invalidated and should be reset back to initial state
            if (totalItemCount < previousTotalItemCount) {
                this.currentPage = this.startingPageIndex;
                this.previousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.loading = true;
                }
            }
            // If it’s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (loading && (totalItemCount > previousTotalItemCount)) {
                currentPage = totalItemCount / ITEMS_PER_PAGE;
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            if (!loading && (totalItemCount - visibleItemCount) <=
                    (firstVisibleItem + visibleThreshold)) {
                onLoadMore(currentPage + 1, totalItemCount);
                loading = true;
            }
        }

        // Defines the process for actually loading more data based on page
        public abstract void onLoadMore(int page, int totalItemsCount);
    }
}