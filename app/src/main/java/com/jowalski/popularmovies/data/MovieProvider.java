package com.jowalski.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private MovieDBHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int MOVIE = 100;
    private static final int MOVIE_WITH_ID = 101;
    private static final int MOVIE_WITH_REVIEWS = 200;
    private static final int MOVIE_WITH_REVIEWS_WITH_ID = 201;
    private static final int REVIEW = 300;
    private static final int REVIEW_WITH_ID = 301;
    private static final int REVIEW_WITH_MOVIE_ID = 401;

    public static final String MOVIE_WREV_URI_ABR = "wrev";
    public static final String MOVIE_ID_URI_ABR = "mid";

    private static final SQLiteQueryBuilder sMoviewReviewsQueryBuilder;

    static {
        sMoviewReviewsQueryBuilder = new SQLiteQueryBuilder();

        // movies INNER JOIN reviews ON movies._id = reviews.movie_id
        sMoviewReviewsQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_MOVIES + " INNER JOIN " +
                        MovieContract.ReviewEntry.TABLE_REVIEWS +
                        " ON " + MovieContract.MovieEntry.TABLE_MOVIES +
                        "." + MovieContract.MovieEntry._ID +
                        " = " + MovieContract.ReviewEntry.TABLE_REVIEWS +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID);
    }

    private static UriMatcher buildUriMatcher() {
        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // add a code for each type of URI you want
        matcher.addURI(authority, MovieContract.MovieEntry.TABLE_MOVIES, MOVIE);
        matcher.addURI(authority, MovieContract.MovieEntry.TABLE_MOVIES + "/#",
                MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.MovieEntry.TABLE_MOVIES +
                "/" + MOVIE_WREV_URI_ABR, MOVIE_WITH_REVIEWS);
        matcher.addURI(authority, MovieContract.MovieEntry.TABLE_MOVIES +
                "/" + MOVIE_WREV_URI_ABR + "/#", MOVIE_WITH_REVIEWS_WITH_ID);
        matcher.addURI(authority, MovieContract.ReviewEntry.TABLE_REVIEWS, REVIEW);
        matcher.addURI(authority, MovieContract.ReviewEntry.TABLE_REVIEWS + "/#",
                REVIEW_WITH_ID);
        matcher.addURI(authority, MovieContract.ReviewEntry.TABLE_REVIEWS +
                "/" + MOVIE_ID_URI_ABR + "/#", REVIEW_WITH_MOVIE_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            // All Movies selected
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // Individual movie based on Id selected
            case MOVIE_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE_WITH_REVIEWS: {
                retCursor = sMoviewReviewsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE_WITH_REVIEWS_WITH_ID: {
                retCursor = sMoviewReviewsQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW: {
                // All reviews selected
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW_WITH_ID: {
                // single review with matching _id
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        MovieContract.ReviewEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case REVIEW_WITH_MOVIE_ID: {
                // All reviews matching the specified MOVIE_ID
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_REVIEWS,
                        projection,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            throw new Error("unable to set cursor notification URI");
        }
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_DIR_TYPE;
            case REVIEW_WITH_MOVIE_ID:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_MOVIES, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_REVIEWS, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        notifyResolverChange(getContext(), uri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match) {
            case MOVIE:
                numDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_MOVIES, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MovieContract.MovieEntry.TABLE_MOVIES + "'");
                break;
            case MOVIE_WITH_ID:
                numDeleted = db.delete(MovieContract.MovieEntry.TABLE_MOVIES,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MovieContract.MovieEntry.TABLE_MOVIES + "'");
                break;
            case REVIEW:
                numDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_REVIEWS, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MovieContract.ReviewEntry.TABLE_REVIEWS + "'");
                break;
            case REVIEW_WITH_ID:
                numDeleted = db.delete(MovieContract.ReviewEntry.TABLE_REVIEWS,
                        MovieContract.ReviewEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '");
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numDeleted > 0) {
            notifyResolverChange(getContext(), uri);
        }
        return numDeleted;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;
        String labelColumn;
        switch (match) {
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_MOVIES;
                labelColumn = MovieContract.MovieEntry.COLUMN_TITLE;
                break;
            case REVIEW:
                tableName = MovieContract.ReviewEntry.TABLE_REVIEWS;
                labelColumn = MovieContract.ReviewEntry.COLUMN_AUTHOR;
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        // allows for multiple transactions
        db.beginTransaction();

        // keep track of successful inserts
        int numInserted = 0;
        try {
            for (ContentValues value : values) {
                if (value == null){
                    throw new IllegalArgumentException("Cannot have null content values");
                }
                long _id = -1;
                try {
                    _id = db.insertOrThrow(tableName, null, value);
                } catch(SQLiteConstraintException e) {
                    Log.w(LOG_TAG, "Attempting to insert " +
                            value.getAsString(labelColumn) +
                            " but received SQLite error: " + e);
                }
                if (_id != -1){
                    numInserted++;
                }
            }
            if (numInserted > 0){
                // If no errors, declare a successful transaction.
                // database will not populate if this is not called
                db.setTransactionSuccessful();
            }
        } finally {
            // all transactions occur at once
            db.endTransaction();
        }
        if (numInserted > 0){
            // if there was successful insertion, notify the content resolver that there
            // was a change
            notifyResolverChange(getContext(), uri);
        }
        return numInserted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated;

        if (contentValues == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case MOVIE: {
                numUpdated = db.update(MovieContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        s,
                        strings);
                break;
            }
            case MOVIE_WITH_ID: {
                numUpdated = db.update(MovieContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            case REVIEW: {
                numUpdated = db.update(MovieContract.ReviewEntry.TABLE_REVIEWS,
                        contentValues,
                        s,
                        strings);
                break;
            }
            case REVIEW_WITH_ID: {
                numUpdated = db.update(MovieContract.ReviewEntry.TABLE_REVIEWS,
                        contentValues,
                        MovieContract.ReviewEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (numUpdated > 0) {
            notifyResolverChange(getContext(), uri);
        }

        return numUpdated;
    }

    static private void notifyResolverChange(Context context, Uri uri) {
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        } else throw new NullPointerException("No context.");
    }
}
