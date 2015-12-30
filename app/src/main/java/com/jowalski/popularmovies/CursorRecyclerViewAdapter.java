package com.jowalski.popularmovies;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;

/**
 * Created by jowalski on 12/24/15.
 */
public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Context mContext;
    private boolean mDataValid;
    private Cursor mCursor;
    private int mRowIdColumn;
//    private NotifyingContentObserver mContentObserver;
//    private NotifyingDataSetObserver mDataSetObserver;

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? cursor.getColumnIndexOrThrow("_id") : -1;

//        mDataSetObserver = new NotifyingDataSetObserver();
//        mContentObserver = new NotifyingContentObserver(null);
//        if (mCursor != null) {
//            mCursor.registerContentObserver(mContentObserver);
//            mCursor.registerDataSetObserver(mDataSetObserver);
//        }

        setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(viewHolder, mCursor);
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
//        if (oldCursor != null) {
//            if (mContentObserver != null) {
//                oldCursor.unregisterContentObserver(mContentObserver);
//            }
//            if (mDataSetObserver != null) {
//                oldCursor.unregisterDataSetObserver(mDataSetObserver);
//            }
//        }
        mCursor = newCursor;
        if (mCursor != null) {
//            if (mContentObserver != null) {
//                mCursor.registerContentObserver(mContentObserver);
//            }
//            if (mDataSetObserver != null) {
//                mCursor.registerDataSetObserver(mDataSetObserver);
//            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            int newSize = newCursor.getCount();
            int oldSize = (oldCursor != null) ? oldCursor.getCount() : 0;
            // notifyItemRangeInserted(oldSize, newSize - oldSize);
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    private class NotifyingContentObserver extends ContentObserver {

        public NotifyingContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
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
            notifyDataSetChanged();
        }
    }
}
