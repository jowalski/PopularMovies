package com.jowalski.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
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
 *
 * Much of this code is derived from:
 *
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 *
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;
    private boolean mDataValid;
    private Cursor mCursor;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    private static final String MOVIE_ID_EXTRA = "movie_id";

    public MovieAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? cursor.getColumnIndexOrThrow("_id") : -1;

        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @Bind(R.id.movie_poster_icon) public ImageView mIconView;
        public int mMovieId;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mMovieId = -1;
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            Context context = view.getContext();
            Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra(MOVIE_ID_EXTRA, mMovieId);
            context.startActivity(intent);
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException(
                    "this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException(
                    "couldn't move cursor to position " + position);
        }

        // tell the viewholder what the movieId is, so the click handler knows
        holder.mMovieId = mCursor.getInt(MainActivityFragment.COL_MOVIE_TMDB_ID);
        String posterPath = mCursor.getString(MainActivityFragment.COL_POSTER_PATH);

        Picasso.with(mContext)
                .load(posterPath)
                .placeholder(R.drawable.the_arrival_of_a_train)
                .error(R.drawable.the_kid)
                .into(holder.mIconView);

        // TODO: 12/14/15 load accessibility text
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());
        }
    }
}

