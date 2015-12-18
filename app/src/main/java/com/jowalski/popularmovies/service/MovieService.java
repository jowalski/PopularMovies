package com.jowalski.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.jowalski.popularmovies.MainActivityFragment;
import com.jowalski.popularmovies.data.MovieContract;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by jowalski on 12/14/15.
 */
public class MovieService extends IntentService {
    public static final String SORT_ORDER_EXTRA = "sort_order";
    public static final String NOTIFY_RESOLVER_EXTRA = "notify_resolver";

    private static final String LOG_TAG = MovieService.class.getSimpleName();

    public static final String SORT_BY_VALUE_POP = "popularity.desc";
    public static final String SORT_BY_VALUE_VOTE = "vote_average.desc";

    // fields for constructing the posterPath
    private static final String THE_MOVIE_DB_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";

    // these are all (?) possible image sizes
    // final String POSTER_SIZE_W92 = "w92";
    // final String POSTER_SIZE_W154 = "w154";
    private static final String POSTER_SIZE_W185 = "w185";
    // final String POSTER_SIZE_W342 = "w342";
    // final String POSTER_SIZE_W500 = "w500";
    // final String POSTER_SIZE_W780 = "w780";
    // final String POSTER_SIZE_ORIG = "original";

    public MovieService() {
        super("Movie");
    }

    private static TMDBService.TMDBApi mTmdbApi =
            TMDBService.createApi();

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Call<TMDBService.MoviePage> call =
                    mTmdbApi.discoverMovies(getSortByValue(intent), 1);
            Response<TMDBService.MoviePage> response = call.execute();

            if (!response.isSuccess()) {
                return;
            }

            List<TMDBService.Movie> movies = response.body().results;

            Vector<ContentValues> cVVector = new Vector<>(movies.size());
            for (int i = 0; i < cVVector.size(); i++) {
                cVVector.add(newCVFromTMDBMovie(movies.get(i)));
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }

            if (notifyResolver(intent)) {
                getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI, null);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error fetching from TMDB server", e);
        }
    }

    private boolean notifyResolver(Intent intent) {
        return intent.getBooleanExtra(NOTIFY_RESOLVER_EXTRA, true);
    }

    private String getSortByValue(Intent intent) {
        int sortOrder = intent.getIntExtra(SORT_ORDER_EXTRA,
                MainActivityFragment.SORT_ORDER_BY_POPULARITY);

        String sort_by_value = SORT_BY_VALUE_POP;
        switch (sortOrder) {
            case MainActivityFragment.SORT_ORDER_BY_POPULARITY:
                sort_by_value = SORT_BY_VALUE_POP;
                break;
            case MainActivityFragment.SORT_ORDER_BY_RATING:
                sort_by_value = SORT_BY_VALUE_VOTE;
                break;
        }
        return sort_by_value;
    }

    private ContentValues newCVFromTMDBMovie(TMDBService.Movie movie) {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry._ID, movie.id);
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIG_TITLE, movie.originalTitle);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.posterPath);
        movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.popularity);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.title);
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.voteAverage);
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, movie.voteCount);

        return movieValues;
    }

}
