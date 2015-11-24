package com.jowalski.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jowalski on 11/20/15.
 */
public class Movie implements Parcelable {
    String movieName;
    int image;

    int movieId;
    String origTitle;
    String overview;
    String releaseDate;
    String posterPath;
    double popularity;
    String title;
    double vote_average;
    int vote_count;

    public Movie(JSONObject movieJson) throws JSONException {
        // movie info element fields
        final String TMDB_ID = "id";
        final String TMDB_ORIG_TITLE = "original_title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_TITLE = "title";
        // TODO: 11/24/15 decide whether to use genre_ids
        // final String TMDB_GENRE_IDS = "genre_ids";
        final String TMDB_VOTE_AVG = "vote_average";
        final String TMDB_VOTE_CNT = "vote_count";

        movieId = movieJson.getInt(TMDB_ID);
        origTitle = movieJson.getString(TMDB_ORIG_TITLE);
        overview = movieJson.getString(TMDB_OVERVIEW);
        releaseDate = movieJson.getString(TMDB_RELEASE_DATE);
        posterPath = movieJson.getString(TMDB_POSTER_PATH);
        popularity = movieJson.getDouble(TMDB_POPULARITY);
        title = movieJson.getString(TMDB_TITLE);
        vote_average = movieJson.getDouble(TMDB_VOTE_AVG);
        vote_count = movieJson.getInt(TMDB_VOTE_CNT);

        movieName = title;
        image = movieId;
    }

    public Movie(String movieName, int image) {
        this.movieName = movieName;
        this.image = image;
    }

    private Movie(Parcel in) {
        movieName = in.readString();
        image = in.readInt();

        movieId = in.readInt();
        origTitle = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        popularity = in.readDouble();
        title = in.readString();
        vote_average = in.readDouble();
        vote_count = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieName='" + movieName + '\'' +
                ", image=" + image +
                ", movieId=" + movieId +
                ", origTitle='" + origTitle + '\'' +
                ", overview='" + overview + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", popularity=" + popularity +
                ", title='" + title + '\'' +
                ", vote_average=" + vote_average +
                ", vote_count=" + vote_count +
                '}';
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieName);
        parcel.writeInt(image);

        parcel.writeInt(movieId);
        parcel.writeString(origTitle);
        parcel.writeString(overview);
        parcel.writeString(releaseDate);
        parcel.writeString(posterPath);
        parcel.writeDouble(popularity);
        parcel.writeString(title);
        parcel.writeDouble(vote_average);
        parcel.writeInt(vote_count);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}
