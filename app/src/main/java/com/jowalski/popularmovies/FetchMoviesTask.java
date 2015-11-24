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
 * Created by jowalski on 11/23/15.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, Movie[]> {

    private static final String TAG = FetchMoviesTask.class.getSimpleName();

    @Override
    protected Movie[] doInBackground(Void... voids) {

        // need to be declared outside try/catch
        // so they can be closed in finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Sort by popularity (0) or vote (1)
        // TODO: 11/23/15 turn this into an enum
        Integer sort_by = 0;

        Movie[] moviesArray = null;

        try {
            final String THE_MOVIE_DB_BASE_API_URL = "https://api.themoviedb.org/3/";
            final String DISCOVER_MOVIE_ENDPOINT = "discover/movie";

            final String SORT_BY_PARAM = "sort_by";
            final String SORT_BY_VALUE_POP = "popularity.desc";
            final String SORT_BY_VALUE_VOTE = "vote_average.desc";
            final String API_KEY_PARAM = "api_key";

            final String THE_MOVIE_DB_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";

            final String POSTER_SIZE_W92 = "w92";
            final String POSTER_SIZE_W154 = "w154";
            final String POSTER_SIZE_W185 = "w185";
            final String POSTER_SIZE_W342 = "w342";
            final String POSTER_SIZE_W500 = "w500";
            final String POSTER_SIZE_W780 = "w780";
            final String POSTER_SIZE_ORIG = "original";

            String base_url = THE_MOVIE_DB_BASE_API_URL +
                    DISCOVER_MOVIE_ENDPOINT + "/?";

            String sort_by_value = SORT_BY_VALUE_POP;
            switch (sort_by) {
                case 0:  sort_by_value = SORT_BY_VALUE_POP;
                    break;
                case 1:  sort_by_value = SORT_BY_VALUE_VOTE;
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
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // adding a newline to make debugging easier
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // empty stream so no point in parsing
                return null;
            }

            String moviesJsonStr = buffer.toString();
            moviesArray = getMoviesInfoFromJson(moviesJsonStr);
        } catch (IOException e) {
            Log.e(TAG, "Could not fetch the movie data", e);
        } catch (JSONException e) {

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

    private Movie[] getMoviesInfoFromJson(String moviesJsonStr) throws JSONException {
        // each Json result consists of these top-level elements,
        // the 'results' field contains the movie info elements
        final String TMDB_RESULTS = "results";
        // NOTE: not using these now, but may need them for content provider?
        // final String TMDB_PAGE = "page";
        // final String TMDB_TOT_PAGES = "total_pages";
        // final String TMDB_TOT_RESULTS = "total_results";

        Movie[] moviesArray = null;
        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesJSONArray = moviesJson.getJSONArray(TMDB_RESULTS);
            // int tmdb_page = moviesJson.getInt(TMDB_PAGE);
            // int tmdb_totPages = moviesJson.getInt(TMDB_TOT_PAGES);
            // int tmdb_totResults = moviesJson.getInt(TMDB_TOT_RESULTS);

            moviesArray = new Movie[moviesJSONArray.length()];
            for (int i = 0; i < moviesJSONArray.length(); i++) {
                JSONObject movieJson = moviesJSONArray.getJSONObject(i);
                moviesArray[i] = new Movie(movieJson);
            }
            Log.d(TAG, "FetchMoviesTask Complete. ");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return moviesArray;
    }
}
