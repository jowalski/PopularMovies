package com.jowalski.popularmovies.service;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by jowalski on 12/17/15.
 */
public class TestApi extends AndroidTestCase {


    private TMDBService.TMDBApi mTmdbApi;

    private static final int FANTASTIC_FOUR_MOVIE_ID = 166424;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTmdbApi = TMDBService.createApi();
    }

    public void testMovieEndpoint() {
        try {
            Call<TMDBService.MoviePage> call =
                    mTmdbApi.discoverMovies(MovieService.SORT_BY_VALUE_POP, 1);
            Response<TMDBService.MoviePage> response = call.execute();

            if (!response.isSuccess()) {
                assertTrue("API response error:\n" +
                        response.errorBody().string(),
                        false);
                return;
            }
            List<TMDBService.Movie> movies = response.body().results;
            assertEquals(20, movies.size());
        } catch (IOException e) {
            assertTrue("Error: Problem with movies API " + e, false);
        }
    }

}
