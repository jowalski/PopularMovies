package com.jowalski.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by jowalski on 11/20/15.
 */
public class MoviePosterAdapter extends ArrayAdapter<MoviePoster> {
    private static final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    public MoviePosterAdapter(Context context, List<MoviePoster> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoviePoster moviePoster = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView iconView = (ImageView) convertView.findViewById(R.id.grid_item_icon);
        iconView.setImageResource(moviePoster.image);

//        TextView versionNameView = (TextView) convertView.findViewById(R.id.grid_item_movie_name);
//        versionNameView.setText(moviePoster.movieName);

        return convertView;

    }
}
