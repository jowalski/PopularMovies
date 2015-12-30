package com.jowalski.popularmovies.service;

import com.google.gson.annotations.SerializedName;
import com.jowalski.popularmovies.BuildConfig;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jowalski on 12/16/15.
 */
public class TMDBService {

    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    public interface TMDBApi {
        // @Headers("Accept: application/json")
        @GET("discover/movie")
        Call<MoviePage> discoverMovies(@Query("sort_by") String sortBy,
                                       @Query("page") int page);

        @GET("movie/{id}/reviews")
        Call<List<Review>> getReviews(@Path("id") int movieId,
                                      @Query("page") int page);

        @GET("movie/{id}/videos")
        Call<List<Video>> getVideos(@Path("id") int movieId,
                                    @Query("page") int page);
    }

    public static TMDBApi createApi() {
        OkHttpClient okClient = new OkHttpClient();
        okClient.interceptors()
                .add(new AuthInterceptor(BuildConfig.THE_MOVIE_DB_API_KEY));

//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        okClient.interceptors()
//                .add(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okClient)
                .build();

        return retrofit.create(TMDBApi.class);
    }

    /**
     * This class registers an interceptor with OkHttp to add an
     * api_key to each api request, and is passed to Retrofit.Builder().
     *
     * This way it is done automatically for all GET requests and we
     * don't have to add an api_key parameter for each endpoint.
     *
     * This code mostly comes from a Stack Overflow item:
     * http://stackoverflow.com/a/33064807/4805349
     */
    private static class AuthInterceptor implements Interceptor {

        private String mApiKey;

        public AuthInterceptor(String apiKey) {
            mApiKey = apiKey;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            HttpUrl url = chain.request().httpUrl()
                    .newBuilder()
                    .addQueryParameter("api_key", mApiKey)
                    .build();
            Request request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build();
            return chain.proceed(request);
        }
    }

    public class ReviewPage {
        public int page;
        public List<Review> results = new ArrayList<Review>();
        @SerializedName("total_results")
        public int totalResults;
        @SerializedName("total_pages")
        public int totalPages;
    }

    public class Review {
        public String id;
        public String author;
        public String content;
        public String url;
    }

    public class VideoPage {
        public int page;
        public List<Video> results = new ArrayList<Video>();
        @SerializedName("total_results")
        public int totalResults;
        @SerializedName("total_pages")
        public int totalPages;
    }

    public class Video {
        public String id;
        public String iso6391;
        public String key;
        public String name;
        public String site;
        public long size;
        public String type;
    }

    public class MoviePage {
        public int page;
        public List<TMDBService.Movie> results = new ArrayList<TMDBService.Movie>();
        @SerializedName("total_results")
        public int totalResults;
        @SerializedName("total_pages")
        public int totalPages;
    }

    public class Movie {
        public boolean adult;
        @SerializedName("backdrop_path")
        public String backdropPath;
        public List<Long> genreIds = new ArrayList<Long>();
        public int id;
        @SerializedName("original_language")
        public String originalLanguage;
        @SerializedName("original_title")
        public String originalTitle;
        public String overview;
        @SerializedName("release_date")
        public String releaseDate;
        @SerializedName("poster_path")
        public String posterPath;
        public float popularity;
        public String title;
        public boolean video;
        @SerializedName("vote_average")
        public float voteAverage;
        @SerializedName("vote_count")
        public int voteCount;
    }
}