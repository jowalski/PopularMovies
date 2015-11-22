package com.jowalski.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jowalski on 11/20/15.
 */
public class MoviePoster implements Parcelable {
    String movieName;
    int image;

    public MoviePoster(String movieName, int image) {
        this.movieName = movieName;
        this.image = image;
    }

    private MoviePoster(Parcel in) {
        movieName = in.readString();
        image = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "MoviePoster{" +
                "movieName='" + movieName + '\'' +
                ", image=" + image +
                '}';
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieName);
        parcel.writeInt(image);
    }

    public final Parcelable.Creator<MoviePoster> CREATOR = new Parcelable.Creator<MoviePoster>() {
        @Override
        public MoviePoster createFromParcel(Parcel parcel) {
            return new MoviePoster(parcel);
        }

        @Override
        public MoviePoster[] newArray(int i) {
            return new MoviePoster[i];
        }
    };
}
