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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jowalski.popularmovies.data.MovieContract;
import com.jowalski.popularmovies.service.MovieService;

/**
 *
 */
public class MainActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final String DET_ACT_MOVIE_OBJ_INTENT_KEY = "movie_obj";

    public static final int SORT_ORDER_BY_POPULARITY = 0;
    public static final int SORT_ORDER_BY_RATING = 1;
    public static final int SORT_ORDER_BY_FAVORITE = 2;

    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY_QUERY,
            MovieContract.MovieEntry.COLUMN_RATING_QUERY,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };

    static final int COL_MOVIE_TMDB_ID = 0;
    static final int COL_MOVIE_TITLE_ID = 1;
    static final int COL_POSTER_PATH = 2;
    static final int COL_POPULARITY = 3;

    private MovieAdapter mMovieAdapter;

    private static final int ITEMS_PER_PAGE = 20;

    private static final String PREF_SORT_ORDER_POPULARITY = "popularity.desc";
    private static final String PREF_SORT_ORDER_RATING = "vote_average.desc";
    private static final String PREF_SORT_ORDER_FAVORITE = "favorite";

    private static final String LOADER_ARGS_KEY = "movie_loader_arg";

    private int sortTypeState;

    private static final int SORT_TYPE_POPULARITY = 0;
    private static final int SORT_TYPE_RATING = 1;
    private static final int SORT_TYPE_FAVORITE = 2;

    private static final String SORT_TYPE_STATE = "sort_type";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            sortTypeState = savedInstanceState.getInt(SORT_TYPE_STATE);
        } else {
            sortTypeState = SORT_TYPE_POPULARITY;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_poster_fragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        int id;
        switch (sortTypeState) {
            case SORT_TYPE_POPULARITY:
                id = R.id.menu_sort_by_popularity;
                break;
            case SORT_TYPE_RATING:
                id = R.id.menu_sort_by_rating;
                break;
            case SORT_TYPE_FAVORITE:
                id = R.id.menu_sort_by_favorite;
                break;
            default:
                return;
        }
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String sortType;
        switch (id) {
            case R.id.menu_sort_by_popularity:
                sortType = PREF_SORT_ORDER_POPULARITY;
                sortTypeState = SORT_TYPE_POPULARITY;
                item.setChecked(true);
                break;
            case R.id.menu_sort_by_rating:
                sortType = PREF_SORT_ORDER_RATING;
                sortTypeState = SORT_TYPE_RATING;
                item.setChecked(true);
                break;
            case R.id.menu_sort_by_favorite:
                sortType = PREF_SORT_ORDER_FAVORITE;
                sortTypeState = SORT_TYPE_FAVORITE;
                item.setChecked(true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        restartMovieLoader(sortType);

        return true;
    }

    private void restartMovieLoader(String sortType) {
        Bundle b = new Bundle();
        b.putString(LOADER_ARGS_KEY, sortType);
        getLoaderManager().restartLoader(MOVIE_LOADER, b, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(SORT_TYPE_STATE, sortTypeState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AutofitRecyclerView rv = (AutofitRecyclerView)
                inflater.inflate(R.layout.fragment_main, container, false);
        mMovieAdapter = new MovieAdapter(getContext(), null);
        rv.setAdapter(mMovieAdapter);

        // add the endless scroll listener, which loads a new set of
        // movies from the API at a fixed scrolling interval
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(
                (GridLayoutManager) rv.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                updateMovies(getContext(), page);
            }
        });
        return rv;
    }

    private void updateMovies(Context context, int pageNumber) {
        int sortOrder;
        switch (sortTypeState) {
            case SORT_TYPE_POPULARITY:
                sortOrder = SORT_ORDER_BY_POPULARITY;
                break;
            case SORT_TYPE_RATING:
                sortOrder = SORT_ORDER_BY_RATING;
                break;
            case SORT_TYPE_FAVORITE:
                sortOrder = SORT_ORDER_BY_FAVORITE;
                break;
            default:
                throw new IllegalStateException("onLoadFinished");
        }

        Intent movieIntent = new Intent(context, MovieService.class);
        movieIntent.putExtra(MovieService.SORT_ORDER_EXTRA, sortOrder);
        movieIntent.putExtra(MovieService.PAGE_NUMBER_EXTRA, pageNumber);
        context.startService(movieIntent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle b = new Bundle();
        b.putString(LOADER_ARGS_KEY, PREF_SORT_ORDER_POPULARITY);
        getLoaderManager().initLoader(MOVIE_LOADER, b, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortType = bundle.getString(LOADER_ARGS_KEY);
        if (sortType == null) {
            sortType = PREF_SORT_ORDER_POPULARITY;
        }
        String selection, sortOrder;
        switch (sortType) {
            case PREF_SORT_ORDER_POPULARITY:
                selection = MovieContract.MovieEntry.COLUMN_POPULARITY_QUERY + " = 1";
                sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
                break;
            case PREF_SORT_ORDER_RATING:
                selection = MovieContract.MovieEntry.COLUMN_RATING_QUERY + " = 1";
                sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
                break;
            case PREF_SORT_ORDER_FAVORITE:
                selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1";
                sortOrder = null;
                break;
            default:
                throw new UnknownError();
        }

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() < ITEMS_PER_PAGE) {
            updateMovies(getContext(), 1);
        }
        mMovieAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}