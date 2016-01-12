package com.jowalski.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jowalski.popularmovies.service.MovieService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An ArrayAdapter for the Movie class that uses the Picasso library to
 * download a movie poster, if it's not already cached, and create an ImageView.
 *
 * Much of this code is derived from:
 *
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 *
 */
public class MovieAdapter extends CursorRecyclerViewAdapter<MovieAdapter.ViewHolder> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    public static final String MOVIE_ID_EXTRA = "movie_id";

    Context mContext;

    public MovieAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.movie_poster_icon) public ImageView mIconView;
        public int mMovieId;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mMovieId = -1;
            // mIconView.setOnClickListener(this);
        }

        @OnClick(R.id.movie_poster_icon)
        public void startDetail(View view) {
            Context context = view.getContext();
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(MOVIE_ID_EXTRA, mMovieId);
            context.startActivity(intent);
        }

//        @Override
//        public void onClick(View view) {
//            int position = getLayoutPosition();
//            Context context = view.getContext();
//            Intent intent = new Intent(context, MovieDetailActivity.class);
//                intent.putExtra(MOVIE_ID_EXTRA, mMovieId);
//            context.startActivity(intent);
//        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_poster_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        // tell the viewholder what the movieId is, so the click handler knows
        holder.mMovieId = cursor.getInt(MainActivityFragment.COL_MOVIE_TMDB_ID);
        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);

        Glide.with(mContext)
                .load(constructPosterPath(posterPath))
                .fitCenter()
                .dontAnimate()
                .placeholder(R.drawable.the_arrival_of_a_train)
                .error(R.drawable.the_kid)
                .into(holder.mIconView);

//        Picasso.with(mContext)
//                .load(constructPosterPath(posterPath))
//                .placeholder(R.drawable.the_arrival_of_a_train)
//                .error(R.drawable.the_kid)
//                .into(holder.mIconView);

        // TODO: 12/14/15 load accessibility text
    }

    static public String constructPosterPath(String queryPath) {
        return MovieService.BASE_URL_PATH + MovieService.POSTER_SIZE_W185 + queryPath;
    }

}

