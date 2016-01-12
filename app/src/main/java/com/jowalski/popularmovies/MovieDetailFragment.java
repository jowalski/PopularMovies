package com.jowalski.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jowalski.popularmovies.data.MovieContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;


public class MovieDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private static final SimpleDateFormat TMDB_SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static final int TMDB_VOTE_OUT_OF = 10;

    @Bind(R.id.plot_textview) TextView mPlotView;
    @Bind(R.id.backdrop) ImageView mIconView;
    @Bind(R.id.star_favorite) CheckBox mStar;
    @Bind(R.id.rel_date_textview) TextView mReleaseDateView;
    @Bind(R.id.rating_textview) TextView mRatingView;
    @Bind(R.id.rating_out_of_textview) TextView mRatingOutOf;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingTool;

    private int mMovieId;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY_QUERY,
            MovieContract.MovieEntry.COLUMN_RATING_QUERY,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW
    };

    public static final int COL_MOVIE_TMDB_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_POSTER_PATH = 2;
    public static final int COL_POPULARITY = 3;
    public static final int COL_VOTE_AVERAGE = 4;
    public static final int COL_POP_QUERY = 5;
    public static final int COL_RATING_QUERY = 6;
    public static final int COL_FAVORITE = 7;
    public static final int COL_RELEASE_DATE = 8;
    public static final int COL_OVERVIEW = 9;

    private static final int MOVIE_LOADER = 1;

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieId = arguments.getInt(MovieAdapter.MOVIE_ID_EXTRA);
        }

        View rootView = inflater.inflate(R.layout.coordinator_detail, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                MovieContract.MovieEntry.buildMovieUri(mMovieId),
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            String posterPath = cursor.getString(COL_POSTER_PATH);
            Glide.with(getContext())
                    .load(MovieAdapter.constructPosterPath(posterPath))
                    .dontAnimate()
                    .placeholder(R.drawable.the_arrival_of_a_train)
                    .error(R.drawable.the_kid)
                    .into(mIconView);

            mCollapsingTool.setTitle(cursor.getString(COL_MOVIE_TITLE));
            mPlotView.setText(cursor.getString(COL_OVERVIEW));
            mReleaseDateView.setText(parseAndFormatReleaseDate(cursor.getString(COL_RELEASE_DATE)));
            mRatingView.setText(String.format(getString(R.string.format_rating),
                    cursor.getDouble(COL_VOTE_AVERAGE)));
            mRatingOutOf.setText(String.format(getString(R.string.format_rating_out_of),
                    TMDB_VOTE_OUT_OF));
            mStar.setChecked(cursor.getInt(COL_FAVORITE) == 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private String parseAndFormatReleaseDate(String relDateStr) {
        Date relDate = null;

        // first parse the date string from theMovieDB
        try {
            relDate = TMDB_SDF.parse(relDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // next, format according to rule in strings.xml
        return Utility.formatReleaseDate(getContext(), relDate);
    }

    /**
     * Update the content provider whenever the star is favorited.
     */
    @OnCheckedChanged(R.id.star_favorite)
    void markFavorite () {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_FAVORITE, mStar.isChecked() ? 1 : 0);
        int success = getContext().getContentResolver()
                .update(MovieContract.MovieEntry.buildMovieUri(mMovieId),
                        cv, null, null);
        if (success != 1) {
            throw new Error("unable to update favorite value in DB");
        }
    }
}
