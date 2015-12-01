package com.jowalski.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * An ArrayAdapter for the Movie class that uses the Picasso library to
 * download a movie poster, if it's not already cached, and create an ImageView.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View  then inflate the layout.
        // If not, we don't need to.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_poster_item, parent, false);
        }

        ImageView iconView = (ImageView) convertView
                .findViewById(R.id.movie_poster_icon);

        Picasso.with(getContext())
                .load(movie.posterPath)
                .into(iconView);

        Log.d(LOG_TAG, "getView: Loaded movie poster for " + movie.title +
                    " from " + movie.posterPath);

        return convertView;
    }
}
