package com.jowalski.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Parcelable class containing already parsed Json data
 * for a movie from theMovieDB.org website.
 */
public class Movie implements Parcelable {

    int movieId;
    String origTitle;
    String overview;
    String releaseDate;
    String posterPath;
    double popularity;
    String title;
    double vote_average;
    int vote_count;

    public Movie() {
    }

    private Movie(Parcel in) {
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

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
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
