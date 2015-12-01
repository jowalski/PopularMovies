package com.jowalski.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An AsyncTask for fetching movie info from theMovieDb. On completion
 * it returns parsed Json info as an array of Movie objects (Movie[])
 * through the onFetchMoviesComplete() method.
 */
public class FetchMoviesTask extends AsyncTask<MainActivityFragment.MovieSortOrder, Void, Movie[]> {

    private static final String TAG = FetchMoviesTask.class.getSimpleName();
    private final FetchMoviesListener listener;

    final String THE_MOVIE_DB_BASE_API_URL = "https://api.themoviedb.org/3/";
    final String DISCOVER_MOVIE_ENDPOINT = "discover/movie";

    // themoviedb api uri fields
    final String SORT_BY_PARAM = "sort_by";
    final String SORT_BY_VALUE_POP = "popularity.desc";
    final String SORT_BY_VALUE_VOTE = "vote_average.desc";
    final String API_KEY_PARAM = "api_key";

    // movie info element fields
    static final String TMDB_ID = "id";
    static final String TMDB_ORIG_TITLE = "original_title";
    static final String TMDB_OVERVIEW = "overview";
    static final String TMDB_RELEASE_DATE = "release_date";
    static final String TMDB_POSTER_PATH = "poster_path";
    static final String TMDB_POPULARITY = "popularity";
    static final String TMDB_TITLE = "title";
    // TODO: 11/24/15 decide whether to use genre_ids
    // final String TMDB_GENRE_IDS = "genre_ids";
    static final String TMDB_VOTE_AVG = "vote_average";
    static final String TMDB_VOTE_CNT = "vote_count";

    static final int TMDB_VOTE_OUT_OF = 10;

    // static final String TMDB_RELEASE_DATE_FMT = "MM/dd/yyyy";
    static final String TMDB_DATE_FORMAT = "yyyy-MM-dd";
    // static final SimpleDateFormat dateFormat =
    //        new SimpleDateFormat(TMDB_RELEASE_DATE_FMT, Locale.US);

    // fields for constructing the posterPath
    static final String THE_MOVIE_DB_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";

    // these are all (?) possible image sizes
    // final String POSTER_SIZE_W92 = "w92";
    // final String POSTER_SIZE_W154 = "w154";
    static final String POSTER_SIZE_W185 = "w185";
    // final String POSTER_SIZE_W342 = "w342";
    // final String POSTER_SIZE_W500 = "w500";
    // final String POSTER_SIZE_W780 = "w780";
    // final String POSTER_SIZE_ORIG = "original";

    public FetchMoviesTask(FetchMoviesListener listener) {
        this.listener = listener;
    }

    @Override
    protected Movie[] doInBackground(MainActivityFragment.MovieSortOrder... movieSortOrders) {
        // need to be declared outside try/catch
        // so they can be closed in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        Movie[] moviesArray = null;

        try {
            String base_url = THE_MOVIE_DB_BASE_API_URL +
                    DISCOVER_MOVIE_ENDPOINT + "?";

            String sort_by_value = SORT_BY_VALUE_POP;
            switch (movieSortOrders[0]) {
                case BY_POPULARITY:  sort_by_value = SORT_BY_VALUE_POP;
                    break;
                case BY_RATING:  sort_by_value = SORT_BY_VALUE_VOTE;
                    break;
            }

            Uri builtUri = Uri.parse(base_url).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, sort_by_value)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the connection to themoviedb.org
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // adding a newline to make debugging easier
                buffer.append(line)
                        .append("\n");
            }

            if (buffer.length() == 0) {
                // empty stream so no point in parsing
                return null;
            }

            String moviesJsonStr = buffer.toString();
            moviesArray = getMoviesInfoFromJson(moviesJsonStr);
        } catch (IOException e) {
            Log.e(TAG, "Could not fetch the movie data", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return moviesArray;
    }

    private Movie[] getMoviesInfoFromJson(String moviesJsonStr)  {
        // each Json result consists of these top-level elements,
        // the 'results' field contains the movie info elements
        final String TMDB_RESULTS = "results";
        // NOTE: not using these now, but may need them for content provider?
        // final String TMDB_PAGE = "page";
        // final String TMDB_TOT_PAGES = "total_pages";
        // final String TMDB_TOT_RESULTS = "total_results";

        Movie[] moviesArray;
        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesJSONArray = moviesJson.getJSONArray(TMDB_RESULTS);
            // int tmdb_page = moviesJson.getInt(TMDB_PAGE);
            // int tmdb_totPages = moviesJson.getInt(TMDB_TOT_PAGES);
            // int tmdb_totResults = moviesJson.getInt(TMDB_TOT_RESULTS);

            moviesArray = new Movie[moviesJSONArray.length()];
            for (int i = 0; i < moviesJSONArray.length(); i++) {
                JSONObject movieJson = moviesJSONArray.getJSONObject(i);
                moviesArray[i] = newMovieFromJson(movieJson);
            }
            Log.d(TAG, "FetchMoviesTask Complete. ");
            return moviesArray;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private Movie newMovieFromJson(JSONObject movieJson) throws JSONException {
        Movie movie = new Movie();

        movie.movieId = movieJson.getInt(TMDB_ID);
        movie.origTitle = movieJson.getString(TMDB_ORIG_TITLE);
        movie.overview = movieJson.getString(TMDB_OVERVIEW);
        movie.releaseDate = movieJson.getString(TMDB_RELEASE_DATE);
        movie.popularity = movieJson.getDouble(TMDB_POPULARITY);
        movie.title = movieJson.getString(TMDB_TITLE);
        movie.vote_average = movieJson.getDouble(TMDB_VOTE_AVG);
        movie.vote_count = movieJson.getInt(TMDB_VOTE_CNT);

        movie.posterPath = THE_MOVIE_DB_BASE_IMAGE_URL + POSTER_SIZE_W185 +
                movieJson.getString(TMDB_POSTER_PATH);

        return movie;
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        super.onPostExecute(movies);

        listener.onFetchMoviesComplete(movies);
    }
}
