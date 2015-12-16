package com.jowalski.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An ArrayAdapter for the Movie class that uses the Picasso library to
 * download a movie poster, if it's not already cached, and create an ImageView.
 */
public class MovieAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public static class ViewHolder {
        @Bind(R.id.movie_poster_icon) ImageView iconView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.movie_poster_item, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);

        Picasso.with(context)
                .load(posterPath)
                .placeholder(R.drawable.the_arrival_of_a_train)
                .error(R.drawable.the_kid)
                .into(viewHolder.iconView);

        // TODO: 12/14/15 load accessibility text
    }
}
