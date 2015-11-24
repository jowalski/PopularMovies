package com.jowalski.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jowalski on 11/20/15.
 */
public class Movie implements Parcelable {
    String movieName;
    int image;

    public Movie(String movieName, int image) {
        this.movieName = movieName;
        this.image = image;
    }

    private Movie(Parcel in) {
        movieName = in.readString();
        image = in.readInt();
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
                '}';
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieName);
        parcel.writeInt(image);
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
